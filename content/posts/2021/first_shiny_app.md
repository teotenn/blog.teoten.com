---
author: "M. Teodoro Tenango"
title: "My first shiny app"
image: "/post/2021/first_shiny_app/02-Mexico.png"
draft: false
date: 2021-12-20
description: "Shiny app to make dynamic maps"
tags: ["R shiny"]
categories: ["R"]
archives: ["2021"]
---

I am happy and excited as I have just deployed my first shiny app on the web. You can find it running at [shiny.rwhitedwarf.com](http://shiny.rwhitedwarf.com) (NOTE: I don't have ssl certificate so, your browser might tell you that is not secure, but you can trust me that there's no risk). I have created a few shiny apps in the past but I never deployed one, especially in an owned domain.

The app can create a map of all cities listed in a table for a given country. The idea is to simulate a table with a list of organizations, franchises, shops, etc. However, columns for name of organization or ID are missing in order to ensure data protection. In this way the user only needs to provide to the table name of the city, country, region (optional) and year of opening. The app creates the map, placing bigger dots in cities with more organizations. The year is interactive.

\
![Map of Italy](/post/2021/first_shiny_app/03-italy.png)
![Log](/post/2021/first_shiny_app/04-log.png)

\
The app uses the package [RJSONIO](https://github.com/duncantl/RJSONIO) to do a simple web scrapping on [Open Street Maps](https://www.openstreetmap.org/) using its API [Nominatim](https://nominatim.org/) to obtain the coordinates (latitude and longitude), in this way any city that can be found in open street maps can be pointed in the map. The data is then sorted and cleaned with some basic R functions and some [Tidyverse](https://www.tidyverse.org/) and finally the map is made with [ggplot2](https://ggplot2.tidyverse.org/) and [maps](https://cran.r-project.org/web/packages/maps/index.html). The front end is created of course with [shiny](https://shiny.rstudio.com/) and [css](https://www.w3schools.com/css/) but I have to mention also the use of [rhandsontable](https://jrowen.github.io/rhandsontable/), a wonderful package that allows the user to interact with the tables and therefore, the data. The app was very easily deployed in [heroku](https://www.heroku.com/) thanks to the wonderful work of Chris Stefano who did all the hard work of creating a [buildpack](https://github.com/virtualstaticvoid/heroku-buildpack-r) for applications written in R. The buildpack recognizes shiny and plumber apps. For shiny, it builds based on the `run.R` file and installs all the packages listed in `init.R`, making the deployment in heroku extremely easy.

The app is still in a raw state but already functional. The plan is to improve both, the functionality and the view in the following days. If you are interested in the source code to get inspiration or create your own, you can find it on github under the MIT license: [teotenn/dynamic_maps_shiny](https://github.com/teotenn/dynamic_maps_shiny).

If you want to learn how to make a similar shiny app, stay connected for a 3-4 part tutorial to get full details step by step.

\
![Empty app](/post/2021/first_shiny_app/01-empty-app.png)
