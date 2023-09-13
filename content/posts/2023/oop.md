---
author: "Manuel Teodoro Tenango"
title: "Object Oriented Programming"
image: "/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png"
draft: true
date: 2023-09-13
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

This post is about **Object Oriented Programming**.

I hope you all enjoy it. Feel free to leave any kind of comment and/or question at the end.

![R Maps](/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png)

# Object oriented programming and R

# Implementation of OOP for the creation of the maps

An S3 object in R is basically a structured list with a class name. It can be easily created by placing the list within the function `structure` and defining the `class`, `structure(list(...), class = c("class_name"))`. Let's see an example.

## A simple S3 object for the colors

We start by defining a function that initializes the object using the function `structure`. We can use this function to check that our object contains the values that we need and throw some errors when there are mistakes.


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
my_colors <- define_map_colors(dots_orgs = "#493252",
                               target_country = "#8caeb4",
                               empty_countries = "#f3f3f3",
                               border_countries = "#9c9c9c",
                               oceans = "#4e91d2",
                               text_cities = "#a0a0a0",
                               text_legend = "#493252",
                               background_legend = "#ffffff",
                               text_copyright = "#f3f3f3")

class(my_colors)
```

```
> [1] "map_colors"
```

This is the same list of colors used in our [previous post](/posts/2023/programming_with_ggplot2/) by the [function that creates the maps](/posts/2023/programming_with_ggplot2/#background-and-preliminaries). Since our new object is also a list, we could use it indistinctly to create the maps. Thus, we need a method that verifies that our object is actually of the class `map_colors`. The method `is` already does that for other classes (i.e., `is.character()`) therefore, we can add our object to tell it how to handle it.


```r
is.map_colors <- function(x) inherits(x, "map_colors")

is.map_colors(my_colors)
```

```
> [1] TRUE
```

The function is very simple, we just need to check if the object passed inherits the class. Now we can use `is.map_colors()` in all the functions that create maps to pass the list of colors with our new class rather than a simple list.

## Defining our own methods
