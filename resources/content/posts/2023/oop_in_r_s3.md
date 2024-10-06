---
author: "Manuel Teodoro Tenango"
title: "Object Oriented Programming in R: S3"
image: "/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png"
draft: false
date: "2023-09-13"
description: "Part of making maps of any region in the world with R using ggplot2 and maps packages: Object Oriented Programming"
tags: ["R maps", "OOP"]
series: ["maps-app"]
categories: ["R"]
archives: ["2023"]
---



This post is part of the series [maps-app](/series/maps-app/ "maps-app").

You can also find the current state of the project under [my GitHub](https://github.com/teotenn) repo [mapic](https://github.com/teotenn/mapic).

# Scope of this post
We are creating maps of data showing changes over a span of time for different countries and pointing at all kinds of cities. That basically means that we need to **map any region of the world with R**. Today there are all kinds of packages and techniques to do that. I will share the strategy I used with [ggplot2](https://cran.r-project.org/web/packages/ggplot2/index.html) and [maps](https://cran.r-project.org/web/packages/maps/index.html) packages, using support of [Open Street Map](https://www.openstreetmap.org/) to obtain the coordinates of cities and finally making it interactive with [shiny](https://shiny.rstudio.com/). 

This series of posts share my path towards the creation of the Shiny app. It is a live project and I decided to share my path and experiences along the creation process. The posts are not only about the Shiny app, but the package I created behind it, including topics of functions crafting, creation of the maps, classes of objects, etc., as well as any interesting issue that appear on the way. It is my way to contribute to the R community and at the same time keeping the project documented for myself.

This post is about **Object Oriented Programming in R using S3 objects**.

I hope you all enjoy it. Feel free to leave any kind of comment and/or question at the end.

![R Maps](/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png)

# Object oriented programming and R

R is a programming language that benefits greatly from the paradigm of functional programming. This is actually how most of R users utilize it and how it is recommended. However, it also offers the possibility of applying object oriented programming (OOP) paradigm which is the creation and use of objects with defined characteristics and methods. If you have never heard of this concept I recommend you to do a little research about it before getting deep into this post. I would recommend to start with the introduction to OOP of [Advanced R](https://adv-r.hadley.nz/oo.html). It is not my intention to explain OOP but rather to use it, in combination with functional programming, to support sharing information between functions.

If you followed the [previous post](/posts/2023/programming_with_ggplot2/) you might have noticed that at the [conclusions](/posts/2023/programming_with_ggplot2/#conclusions) section I mentioned the use of OOP to extend our ggplot2 functions. The idea is to pass information between the functions to make the calculations more accurate rather than forcing the end user to repeat the information in each function. I mentioned the possibility of using environments, or the ggplot2 internal class `ggproto`. The use of environments has a different function than what I am trying to achieve here, while the `ggproto` objects are excellent to pass information between graphics created with `ggplot2` but they become complicated if we want to include any more information like data frames or lists of values. Therefore I decided to keep it simple with the use of S3 objects. 

S3 objects are the most commonly used in R packages, the recommended ones and apparently, the only ones used in base-r and stats packages. As [Hadley Wickham](https://adv-r.hadley.nz/s3.html) says it:

> S3 is R’s first and simplest OO system. S3 is informal and ad hoc, but there is a certain elegance in its minimalism: you can’t take away any part of it and still have a useful OO system. For these reasons, you should use it, unless you have a compelling reason to do otherwise. S3 is the only OO system used in the base and stats packages, and it’s the most commonly used system in CRAN packages.
>
> S3 is very flexible, which means it allows you to do things that are quite ill-advised. If you’re coming from a strict environment like Java this will seem pretty frightening, but it gives R programmers a tremendous amount of freedom. It may be very difficult to prevent people from doing something you don’t want them to do, but your users will never be held back because there is something you haven’t implemented yet. Since S3 has few built-in constraints, the key to its successful use is applying the constraints yourself.

# Implementation of S3 class objects for the creation of the maps

An S3 object in R is basically a structured list with a class name. It can be easily created by placing the list within the function `structure` and defining the `class`, `structure(list(...), class = c("class_name"))`, or by creating the list first and then setting the class of that list. Then we create generics using the function `UseMethod()` and add methods for each class by appending the class name to the generic, followed by a dot (i.e., `my_generic.MyClass`, `my_generic.vector`, `my_generic.matrix`, etc.).

Let's start with a simple example.

## A simple S3 object for the colors

We start by defining a function that initializes the object, our **constructor**. We should use this function to also check that our object contains the values that we need and throw some errors when there are mistakes.


```r
define_map_colors <- function(dots_orgs,
                              target_country,
                              empty_countries,
                              border_countries,
                              oceans,
                              text_cities,
                              text_legend,
                              background_legend,
                              text_copyright) {
  require(stringr)

  ## Error handling
  all_arguments <- c(as.list(environment()))
  for (arggs in all_arguments) {
    stopifnot("All arguments must be character" = is.character(arggs))
    if (nchar(arggs) != 7) {
      stop("Colors should be in hex notation")
    }
    if (!str_detect(arggs, "^#")) {
      stop("Colors should be in hex notation")
    }
  }

  ## S3 object definition
  structure(
    list(
      dots_orgs = dots_orgs,
      target_country = target_country,
      empty_countries = empty_countries,
      border_countries = border_countries,
      oceans = oceans,
      text_cities = text_cities,
      text_legend = text_legend,
      background_legend = background_legend,
      text_copyright = text_copyright),
    class = c("map_colors"))
}
```

The function takes 9 arguments, each of them should be a color in hex notation, meaning that it must start with `#` and it must contain 6 alphanumeric characters (i.e., `#f0f0f0`). Thus, our error handling basically verifies that the parameters passed are of class `character` starting with `#` and containing exactly 7 symbols. Then, each of the 9 arguments is passed to a list within `structure` and set them to the class `map_colors`. And our object is created.


```r
default_map_colors <- define_map_colors(dots_orgs = "#493252",
                               target_country = "#8caeb4",
                               empty_countries = "#f3f3f3",
                               border_countries = "#9c9c9c",
                               oceans = "#4e91d2",
                               text_cities = "#a0a0a0",
                               text_legend = "#493252",
                               background_legend = "#ffffff",
                               text_copyright = "#f3f3f3")
```

This is the same list of colors used in our [previous post](/posts/2023/programming_with_ggplot2/) by the [function that creates the maps](/posts/2023/programming_with_ggplot2/#background-and-preliminaries). If you check the class of our new list of colors, `class(default_map_colors)` it should be `map_colors`. Since our new object is also a list, we could use it indistinctly to create the maps. Thus, we need a method that verifies that our object is actually of the class `map_colors`. This is the **validator**. The method `is` already does that for other classes (i.e., `is.character()`) therefore, we can add our object to tell it how to handle it.


```r
is.map_colors <- function(x) inherits(x, "map_colors")

is.map_colors(default_map_colors)
```
> [1] TRUE

The function is very simple, we just need to check if the object passed inherits the class. Now we can use `is.map_colors()` in all the functions that create maps in order to ensure that our new class is used rather than a simple list.

## Creating a new object within a function

Once again, I have made improvements to the function presented before, [my_country_prev](/posts/2023/programming_with_ggplot2/#background-and-preliminaries). This time the change is only one, almost by the end of the body: the addition of an S3 object that holds information which is used later by other functions that add layers to the map. It is not only about the colors, but we are also adding the values of the limits, so that other functions, such as labels creation, know about it. Since this version is more stable, I have also renamed it to a more formal name.


```r
base_map <- function(country,
                     x_limits = NULL,
                     y_limits = NULL,
                     show_coords = FALSE,
                     return_mapic_obj = TRUE,
                     map_colors = default_map_colors) {
  require(maps)
  require(ggplot2)

  ## Verifying the arguments passed to the function
  if (length(country) != 1) stop("Function supports only one country per map")
  stopifnot(is.logical(show_coords))
  stopifnot("Name of the country should be character" = is.character(country))

  if (!country %in% map_data("world")$region) {
    stop(paste("Country name not recognized",
               "To see a list of recognized countries run",
               "<unique(maps::map_data('world')$region)>", sep = "\n"))
  }

  ## If coords limits missing, print worldwide map with coordinates system to allow
  ## User observe coords for reference
  if (missing(x_limits) || missing(y_limits)) {
    warning("X and/or Y limits not provided.\nPrinting worldwide map.")
    map_country_theme <- theme(panel.background = element_rect(fill = map_colors$oceans))
  } else if (show_coords) {
    map_country_theme <- theme(panel.background = element_rect(fill = map_colors$oceans))
  } else {
    if (length(x_limits) != 2 || length(y_limits) != 2 ||
         !all(grepl("^-?[0-9.]+$", c(x_limits, y_limits)))) {
      stop("Limits for X and Y coords should be provided as vectors with two numeric values")
    } else {

      ## Custom theme for the final map
      map_country_theme <- theme_bw() +
        theme(panel.background = element_rect(fill = map_colors$oceans),
              legend.position = "none",
              panel.grid.major = element_blank(),
              panel.grid.minor = element_blank(),
              axis.line = element_line(colour = "black"),
              axis.title.x = element_blank(),
              axis.text.x = element_blank(),
              axis.ticks.x = element_blank(),
              axis.title.y = element_blank(),
              axis.text.y = element_blank(),
              axis.ticks.y = element_blank())
    }
  }

  ## Get the target cpuntry data
  map_data_country <- map_data("world")[map_data("world")$region == country, ]

  ## The map
  mapic <- ggplot() +
    ## First layer: worldwide map
    geom_polygon(data = map_data("world"),
                 aes(x = long, y = lat, group = group),
                 color = map_colors$border_countries,
                 fill = map_colors$empty_countries) +
    ## Second layer: Country map
    geom_polygon(data = map_data_country,
                 aes(x = long, y = lat, group = group),
                 color = map_colors$border_countries,
                 fill = map_colors$target_country) +
    coord_map() +
    coord_fixed(1.3,
                xlim = x_limits,
                ylim = y_limits) +
    map_country_theme

  if (return_mapic_obj) {
    map_pointer <- structure(
      list(
        mapic = mapic,
        base_map = mapic,
        x_limits = x_limits,
        y_limits = y_limits,
        colors = map_colors
      ),
      class = "mapicHolder")
    return(map_pointer)
  } else {
    return(mapic)
  }
}
```

The new `base_map` does the same as the previous `my_country_prev`: it creates the base map of a country. However, we have added the option to return a mapic object or not. When the option, `return_mapic_obj` is set to `FALSE`, the function behaves as before and it returns the map only. But when it is set as `TRUE`, it generates a new object of class `mapicHolder` that holds the information that will be piped to other functions as we mentioned above. Other changes are  minimal regarding style, the function still does the same.

As you can see, the object does not necessarily has to be available to the end user for manipulation or modification. Here we can create 2 different objects to move forward with the creation of the maps and the end user does not need to know the structure or even the existence of the object. Thus, `spain <- base_map("Spain")` will create an object with the base map for Spain, and `france <- base_map("France")` creates a similar object for the base map of France. Each of them in an object of class `mapicHolder` that can be called later. The idea is to be able to use the information of each of them easily by the rest of the functions that add layers to our maps.

## Defining our own methods

Now that we have our S3 object and we know how to modify methods, let's create a new method specially for it. We are going to replace our previous function [make_dots](/posts/2023/programming_with_ggplot2/#a-map-with-growing-dots-per-city) for a method that works differently depending on the class of object that is passed. The initialization is pretty simple, it can be accomplished in one line:


```r
make_dots <- function(x, ...) UseMethod("make_dots")
```

After this `make_dots` exists as a method which behavior we can modified based on the class of the object passed. Notice the ellipsis (the three dots `...`) in the function definition, they are necessary to ensure that all the arguments defined after our object (`x`) are taken into account as well. 

With this we can re-write the definition of our function [make_dots](/posts/2023/programming_with_ggplot2/#a-map-with-growing-dots-per-city), or in other words, its name, to have it as the default behavior. The body of the function remains the same.


```r
make_dots.default <- function(.df,
                      year,
                      map_colors,
                      column_names = list(
                        lat = "lat",
                        lon = "lon",
                        cities = "city",
                        start_year = "year",
                        end_year = NULL),
                      dot_size = 1) {
  ...
}
```

The function should be working as if no changes were done. The advantage is that now, we can create a second one, with the same name, which will behave differently when our object `mapicHolder` is used.


```r
make_dots.mapicHolder <- function(.mapic_holder,
                                        .df,
                                        year,
                                        column_names = list(
                                          lat = "lat",
                                          lon = "lon",
                                          cities = "city",
                                          start_year = "year",
                                          end_year = NULL),
                                        dot_size = 1) {
  require(dplyr)
  require(tidyr)
  require(stringr)

  column_names <- column_names[lengths(column_names) != 0]
  year__ <- year

  ## Check required fields
  mandatory_cols <- c("lat", "lon", "cities", "start_year")
  if (!all(mandatory_cols %in% names(column_names))) {
    stop("Column names missing!")
  } else {
    if (!"end_year" %in% names(column_names)) {
      .df$final_year <- NA_real_
      column_names[["end_year"]] <- "final_year"
    }
  }

  ## Make map using default method
  mapic_dots <- make_dots(.df = .df,
                          year = year__,
                          map_colors = .mapic_holder$colors,
                          column_names = column_names,
                          dot_size = dot_size)

  ## Papere the data
  data_for_map <- .df  %>%
    mutate_at(vars(column_names$end_year), ~replace_na(., year__ + 1)) %>%
    mutate(year_final = !!sym(column_names$end_year),
           city_name = str_to_sentence(!!sym(column_names$cities))) %>%
    filter(year_final > year__ & !!sym(column_names$start_year) <= year__) %>%
    group_by(city_name) %>%
    summarise(x = median(!!sym(column_names$lon), na.rm = TRUE),
              y = median(!!sym(column_names$lat), na.rm = TRUE),
              n = n())

  ## Empty theme for labels
  empty_theme <- theme_bw() +
    theme(legend.position = "none",
          panel.grid.major = element_blank(),
          panel.grid.minor = element_blank(),
          axis.line = element_line(colour = "white"),
          axis.title.x = element_blank(),
          axis.text.x = element_blank(),
          axis.ticks.x = element_blank(),
          axis.title.y = element_blank(),
          axis.text.y = element_blank(),
          axis.ticks.y = element_blank(),
          plot.margin = unit(c(-0, -0, -0, -0), "cm"))

  .mapic_holder[["theme_labels"]] <- empty_theme
  .mapic_holder[["mapic_dots"]] <- mapic_dots
  .mapic_holder[["year"]] <- year__
  .mapic_holder[["data"]] <- list(base = .df, map = data_for_map)
  .mapic_holder[["mapic"]] <- .mapic_holder[["mapic"]] + mapic_dots
  return(.mapic_holder)
}
```

The new function is basically applying the default function to the data, but the parameters required are different: we now request the object of class `mapicHolder` but we don't need to request the `map_colors` because they come within the mentioned object. Additionally, we are adding additional data to our `mapicHolder` to be passed and used for more functions.

Now we have two options to create the maps. One specifying every parameter like in the [previous post](/posts/2023/programming_with_ggplot2) using the `.default` method:


```r
base_map("Mexico",
                map_colors,
                 x_limits = c(-118, -86),
                 y_limits = c(14, 34),
                 show_coords = T) +
  make_dots(datmx,
            year = 2022,
            map_colors, column_names = col_names)
```

Or the new version where the `mapicHolder` can be piped from function to function:


```r
base_map("Mexico",
         map_colors,
         x_limits = c(-118, -86),
         y_limits = c(14, 34),
         show_coords = T) |>
  make_dots(datmx,
            year = 2022,
            column_names = col_names)
```

It has been a good exercise to learn the basis of S3 object but so far the differences between one and the other are minimal. Other than avoid repeating the list of colors, there is not much gain. However, all the information that we have gathered in our `mapicHolder` object has high value to create the labels accurately and to place them in a proper position.

## Passing information to the labels

To have our map complete, we are going to pass our `mapicHolder` to the functions that print the years and the totals. As we did above, we first create our method and define its default behaviour.



```r
my_print_totals <- function(x, ...) UseMethod("my_print_totals")

my_print_totals.default <- function(totals, map_colors, x_limits, y_limits, totals_label = "Totals") {
  ...
}
```

You can find the [code of the original functions](/posts/2023/programming_with_ggplot2/#adding-labels-for-the-map) in my previous post. Now for our new function we can basically remove all the parameters and add only a `mapicHolder` object, which already contains the rest of the information.


```r
my_print_totals.mapicHolder <- function(.mapic_holder,
                                        totals_label = "Totals") {
  data_totals <- sum(.mapic_holder$data$map$n)
  mapic_totals <- my_print_totals(totals = data_totals,
                                  x_limits = .mapic_holder$x_limits,
                                  y_limits = .mapic_holder$y_limits,
                                  totals_label = totals_label,
                                  map_colors = .mapic_holder$colors)

  .mapic_holder[["mapic_totals"]] <- mapic_totals
  .mapic_holder[["totals"]] <- data_totals
  .mapic_holder[["mapic"]] <- .mapic_holder[["mapic"]] + mapic_totals
  return(.mapic_holder)
}
```

Basically the body of `my_print_totals.mapicHolder` consists of passing the right parameters to `my_print_totals.default`, and adding the new layer to the `mapicHolder`. We can do exactly the same for the years and we will have the complete map ready to be shown.

# Creating the map

Before we are able to pipe and show the map, we need a couple preparations more. In order to show the map when we call our object, rather than a bunch of information, we need to add our object class to `print`.


```r
print.mapicHolder <- function(p) plot(p$mapic)
```

It will be useful to do the same for `plot`.


```r
plot.mapicHolder <- function(p) plot(p$mapic)
```

Now we can simply pipe one function after the other and reduce the amount of arguments passed to each function.



```r
my_country_prev("Mexico",
                map_colors,
                x_coords,
                y_coords,
                show_coords = T) |>
  make_dots(rbind(datmx, datmx),
            year = 2020,
            col_names) |>
  my_print_years(year_label = "Año") |>
  my_print_totals(totals_label = "Totales")
```

![plot of chunk unnamed-chunk-8](/post/2023/map_any_region_with_ggplot2_part_III/unnamed-chunk-8-1.png)

# Final Remarks

Now we have not only a functional workflow for the creation of the maps, but also a more user friendly one. If it is true that it does not follow the standards of `ggplot2` of adding layers using `+`, it uses the R pipe introduced in version 4.0 (we can also use dplyr's pipe `%>%`) which makes more sense, since it is the direction that R as a whole is taking.

In the next post we will step back to the coordinates manipulation to implement a new system of objects to be able to use different types of databases. For now, our functions are able to use only `SQLite` and `data.frame` to store the information, which is fine for prototyping or for small projects, but very limited for production usage.

