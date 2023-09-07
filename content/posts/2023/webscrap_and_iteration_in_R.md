---
author: "Manuel Teodoro Tenango"
title: "Webscrap and iteration in R"
image: ""
draft: false
date: 2023-03-24
description: "Part of how to make maps of any region in the world using ggplot2 and maps libraries"
tags: ["maps-app", "R maps", "R functions", "web-scrap", "database", "recursion"]
categories: ["R"]
archives: ["2023"]
---

## About this post

We are creating maps of data showing changes over a span of time for different countries and pointing at all kinds of cities. That basically means that we need to **map any region of the world with R**. Today there are all kinds of packages and techniques to do that. I will share the strategy I used with [ggplot2](https://cran.r-project.org/web/packages/ggplot2/index.html) and [maps](https://cran.r-project.org/web/packages/maps/index.html) packages, using support of [Open Street Map](https://www.openstreetmap.org/) to obtain the coordinates of cities and finally making it interactive with [shiny](https://shiny.rstudio.com/). 

This series of posts share my path towards the creation of the Shiny app. It is a live project and I decided to share my path and experiences along the creation process. The posts are not only about the Shiny app, but the package I created behind it, including topics of functions crafting, creation of the maps, classes of objects, etc., as well as any interesting issue that appear on the way. It is my way to contribute to the R community and at the same time keeping the project documented for myself.

You can find all the posts on this series under the tag [maps-app](https://blog.rwhitedwarf.com/tags/maps-app/ "#maps-app") (including the Spanish versions).

You can also find the current state of the project under [my GitHub](https://github.com/teotenn) repo [mapic](https://github.com/teotenn/mapic).

This post is originally written in Spanish, from the Amsterdam airport, on the way to Mexico. I hope you enjoy. Feel free to leave any type of comment and/or question at the end.

![R Maps](/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png)

## Motivation

As I mentioned in the previous posts in the series, I've been working lately on the code for creating the maps and I've made changes that increase the efficiency of the functions, the readability of the code, and make it easier to use. At the same it allows me to extend the functions beyond their original design.

I have mentioned on a few occasions that the code evolved slowly from scripts created to generate the specific map of some country. So, the first few functions are more of a collection of the steps used to generate the map, wrapped in the form of functions to automate the process.

For this reason, I wanted to make changes to adapt the functions to paradigms more suitable for functional programming, which is R's strong point. However, back then the priority was to generate the maps, and thus, most of my time was devoted to creating the maps and the debugging of the code when it was necessary. I need to add that this project is part of a voluntary work for an NGO, of which I became the director of the research division, which generated even more responsibilities and work for me. And all as a side job, separated from my main source of income (which is also based on R).

However, for better or worse, 2022 was a year full of changes and challenges for me and my family, which forced me to put the project aside for a while, resign my position as responsible of the division, and focus solely on to my career, my health and my family. The result was that when I managed to regaining stability in my life, I found myself with more free time and fewer obligations to rethink the code and work on it. Additionally, my main job had a turn going from statistics to more programming oriented in R, which has given me more tools and experience to improve the code, and has motivated me to take up old lessons about functional programming and, above all, iteration.

This allowed me to improve the two main functions: the one in charge of the webscrapping and the one that sends the data to SQLite. You can find the original functions in the previous post, [Map any region in the world with R - Part I: The basic map](https://blog.rwhitedwarf.com/post/map_any_region_with_ggplot2_part_i/) and compare it with the new, improved functions in this.

## Webscrapp to SQLite
The `webscrap_to_sqlite` function is responsible for sending the coordinates found by Open Street Map to our database. The original function is inefficient, as it does each operation line by line. It is also very rigid in the way it directs the values of the regions, both its request to the API and the placement of the values in the database, which makes any extension or modification very complicated.

For these reasons, it is the function that received the most changes, it was practically rewritten from scratch, making the search more efficient, also allowing internal search of the data already stored; more flexible, dealing with region parameters more clearly; and more understandable, improving the style of the code.


```r
webscrap_to_sqlite <- function(db.name,
                               dat,
                               city = "City",
                               country = "Country",
                               region = NULL,
                               state = NULL,
                               county = NULL,
                               db_backup_after = 10) {
  ## Loading libraries
  require(RSQLite)
  require(dplyr)

  ## 1. DB connection
  con <- dbConnect(drv = SQLite(), dbname = db.name)
  dbExecute(conn = con,
            "CREATE TABLE IF NOT EXISTS orgs
                    (ID INTEGER UNIQUE,
                     City TEXT,
                     Country TEXT, 
                     Region TEXT,
                     State TEXT,
                     County TEXT,
                     osm_name TEXT,
                     lon REAL,
                     lat REAL)")
  db <- as_tibble(dbReadTable(con, "orgs"))

  ## 2. Data filtering
  new_coords <- data.frame()
  dat_local <- compare_db_data(db.name, dat)
  df_len <- nrow(dat_local)

  ## 3. While there are rows in DF:
  if (df_len != 0) {
    ## 3.1 Define subsample size
    dat_local <- dat_local[c(1:db_backup_after), ]
    dat_local <- filter(dat_local, rowSums(is.na(dat_local)) != ncol(dat_local))

    ## 3.2 for loop for the webscrapping
    for (i in 1:nrow(dat_local)) {
      print(paste0("Searching entry ", dat_local[["ID"]][i]))
      
      ## 3.3 Info abstraction
      rg <- ifelse(is.null(region), "", dat_local[[region]][i])
      st <- ifelse(is.null(state), "", dat_local[[state]][i])
      ct <- ifelse(is.null(county), "", dat_local[[county]][i])
      rcity <- dat_local[[city]][i]
      rcountry <- dat_local[[country]][i]

      ## 3.4 Getting the coords
      ## 3.4.1. First, check if they are already in the DB
      search_query <- filter(db, City == rcity, Country == rcountry,
                             Region == rg, State == st, County == ct)
      if (nrow(search_query != 0)) {
        coords <- search_query[1, ]
        coords$ID <- dat_local[["ID"]][i]
        print("Found from memory")
        
        ## 3.4.2 If they are not, search with OSM API
      } else {
        coords <- coords_from_city(rcity, rcountry,
                                   Region = rg, State = st, County = ct)
        coords <- cbind(ID = dat_local[["ID"]][i],
                        City = rcity,
                        Country = rcountry,
                        Region = rg,
                        State = st,
                        County = ct,
                        coords)
      }
      new_coords <- rbind(new_coords, coords)
    }

    ## Send only new results to DB
    dbWriteTable(con, "orgs", new_coords, append = TRUE)
    dbDisconnect(con)

    ## 3.4.3 Repeat
    webscrap_to_sqlite(db.name = db.name,
                       dat = dat,
                       city = city,
                       country = country,
                       region = region,
                       state = state,
                       county = county,
                       db_backup_after = db_backup_after)

    ## 4. Exit iteration
  } else {
    db_final <- import_db_as_df(db.name)
    size <- nrow(db_final)
    not_found <- nrow(db_final[is.na(db_final$lat), ])
    message(paste("Search finished.\n",
                  size, "entries searched.\n",
                  not_found, "ENTRIES NOT FOUND"))
  }
}        
```

The function starts by calling the necessary libraries and (1) connecting to the database, creating it if necessary. (2) Then it generates two data frames, an empty one that will store new coordinates and a relative one that contains only the data that does not yet exist in the database. (3) So, as long as there is data in this last data frame, the function will continue to loop.

(3.1) We then define the subsample, which is a subset of `dat_local` the size of `db_backup_after` and focus solely on this subsample. (3.2) On this subsample we make the iterations using `for` to obtain the coordinates. First (3.3) we prepare the data as strings and then (3.4) we look up the coordinates. (3.4.1) If they already exist in the database we take it from there, and if not (3.4.2) they are searched using `coords_from_city`. Finally, (3.4.3) we iterate all over again, allowing the function to call itself.

Since step 2 filters the data that is not yet in the database and step 3 places the results of new searches in an empty data frame, the function calls itself and applies only for each subset of data. When `compare_db_data` finally returns 0 values because all the data that was fed into the function is already contained in the database, we can exit the function. In this case I decided to import the data again from SQLite to get details of the search, and end the iteration by sending a message to the user about the total number of entries and the number of which were not found.

If we compare this function with the one proposed in my previous post, the function is completely different but the end result is the same. The arguments used by the function are also the same and take the same values, which avoids conflicts for the user. The only new parameter is `db_backup_after` which allows us to control how many rows the iteration is done. A smaller value means more iterations, which results in higher local memory usage, but also faster in finding data that already exists in the DB. On the other hand, a higher value reduces the number of iterations but increases the number of API connections. For this reason I have given it a default value of 10. This, in addition to being a balanced value, also reduces confusion for the user who might not be familiar with the changes.

## Remove missing values from the database
In the previous proposal, only found coordinates were sent to the database, and those not found were ignored. In the present proposal, all entries are sent to the DB. Therefore, it is important to have some option to remove the missing entries.

For this I generated the function `remove_na_from_db`, a very simple function which gives the user the possibility of removing `NA`s automatically.


```r
remove_na_from_db <- function(db.file) {
  require(RSQLite)
  con <- dbConnect(drv = RSQLite::SQLite(), dbname = db.file)
  dbExecute(conn = con,
            "DELETE FROM orgs WHERE lon IS NULL OR trim(lon) = '';")
  dbDisconnect(con)
}
```

The function is just a connection to the database that issues the command to remove rows where the `lon` field is empty, in SQLite syntax. This is the safest, most direct and fastest way to do it. We could also import the data back into R, filter it, and send it back to SQLite, but this would require more local memory usage, more code, and more risk as it would require rewriting the database to SQLite entirely. . The power of the `RSQLite` library (or any other library that connects R to SQL) lies precisely in the ability to pass commands written and executed directly in SQL.

## Obtaining the coordinates
The `coords_from_city` function also received significant changes in code readability and flexibility, and a bit less in functionality and efficiency.


```r
coords_from_city <- function(city = NULL,
                             country_code,
                             region = NULL,
                             state = NULL,
                             county = NULL) {
  require("RJSONIO")

  ## 1. Abstract regions for OSM
  CityCoded <- gsub(" ", "%20", City) 
  CountryCoded <- paste("&countrycodes=", CountryTwoLetter, sep = "")
  extras <- c(city = City, state = State, region = Region, county = County)
  extrasCoded <- ""
  if (!is.null(extras)) {
    for (i in 1:length(extras)) {
      if (extras[i] != "" && !is.na(extras[i]) && !grepl("^\\s*$", extras[i])) {
        valCoded <- gsub(" ", "%20", extras[i])
        extrasCoded <- paste0(extrasCoded, "&", names(extras)[i], "=", valCoded)
      }
    }
  }

  ## 2. Response
  link <- paste(
    "http://nominatim.openstreetmap.org/search?city="
  , extrasCoded
  , CountryCoded
  , "&format=json"
  , sep = ""
  )

  response <- try({fromJSON(link)},
                  silent = TRUE)

  if (class(response) == "try-error") {
    stop(response[1])
  } else if (class(response) == "response") {
    response_status <- http_status(response)
    if (response_status$category != "Success") {
      stop(response_status$message)
    }
  } else if (is.list(response)) {

    ## 3. Organize results
    if (length(response) == 0) {
      message(paste("No results found for", extrasCoded))
      coords <- data.frame("lon" = NA, "lat" = NA, "osm_name" = as.character(NA))
      
    } else if (length(response) == 1) {
      message(paste("Found", response[[1]]$display_name))
      coords <- data.frame(
        lon = response[[1]]$lon,
        lat = response[[1]]$lat,
        osm_name = response[[1]]$display_name
      )
      
    } else {
      message(paste("Several entries found for", city, country_code))
      coords <- data.frame(
        lon = response[[1]]$lon,
        lat = response[[1]]$lat,
        osm_name = response[[1]]$display_name
      )
    }
  }
    
  ## 4. Exit as data frame
  return(coords)
}
```

The main change is in section 1, instead of passing each of the regions as its own string and formatting them one by one, I have abstracted them all into a single vector. This reduces the amount of code, memory usage, and allows us to include the city in the list, making it an optional value as well. The reason I had prepared them separately in the previous post is simply because the feature grew slowly: at first we only needed city, but then we had to use some additional fields depending on the country we were working in. To make things easier for me, I simply added each region field as needed. Now that I have time to work on the code, this was the first function I modified.

Step 2 now prints messages that help us identify the error when it comes to the connection, while also stopping the process. Whether it is a local connection error, or problems on the API side, we will get a message and the process will stop, which should avoid long waiting times when there is no connection and several locations are being searched.

Step 3 changes the organization of the results a bit, always returning a data frame with the same columns when the results were not found, but now with empty fields in such case. This helps the functions presented above to populate the database. Additionally, when many results were found, this information is printed on the screen; for now this is for information purposes only. The idea is to keep this space to make changes in the future that allow us to select the option interactively. This is something I still need to think about and plan properly because on one hand I want to use it in a Shiny app, and on the other we want to keep the ability for web scrapping to happen automatically with as little intervention as possible.

As I mentioned before, these new features also allow us to perform searches with the empty city value. This was a requested requirement in the last version, as some users started making maps by region, while others, not finding very small cities, decided to group the data by region. Thanks to the changes made to `coords_from_city`, the `webscrap_to_sqlite` function can now return results when the value for city is `NA`, assuming that the coordinates for the region or state are found. Here it is important to mention that it is recommended to use the `state` argument for region search, for some reason this works better in the OSM API. As an example, the search `coords_from_city(state = "Castilla La Mancha", country_code = "ES")` returns the expected results, despite of the fact that Spain has no states; however if we do `coords_from_city(region = "Castilla La Mancha", country_code = "ES")` nominatim does not find the results.

## Conclusions

These changes have been very important in speeding up the coordinate search process and automating map creation. On the other hand, it allowed me to style the code more and improve its efficiency. Since my main project for now is turning it into a Shiny app, it was important for me to improve the code and the efficiency before dealing with the details of the server. Since this is recent work that I have been doing in the last few months, I decided to share it right away now that I have fresh information on the changes. I hope it can help more than one to make more abstract code and practice recursion.
