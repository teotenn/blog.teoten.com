---
author: "Manuel Teodoro Tenango"
title: "My first Golem app"
image: "post/2023/my_first_golem_app/Golemof-Prague-Rabbi-Loew.jpg"
draft: false
date: 2023-09-29
description: "Description"
tags: ["R shiny", "exploring"]
categories: ["R", "web-dev"]
archives: ["2023"]
---

# About
A few days ago I finished reading the book [Engineering Production-Grade Shiny Apps](https://engineering-shiny.org/) by Colin Fay, Sebastien Rochette, Vincent Guyader and Cervan Girard. It was an easy read so, I decided to move forward and create my first `shiny` app with `golem`. But before doing that I wanted to read some comments or opinions about it and I realized that there is not so much about it outside of the `golem` team. Thus, I decided to record my exploration of the package and share it in my blog.

Here you will find my opinion about the book and the package, the steps I took and a live example of the app. The post has a lot of lines but it is mostly because I include some of the snippets that come by default with a `golem`'s project, as well as my own scripts for the app.

I hope that this post can help to some R `shiny` users to have an opinion about `golem` and save time deciding if this is the framework that can help with their needs. I need to mention that `golem` is just a proposition of how a `shiny` app should be organized and managed, similar to [leprechaun](https://leprechaun.opifex.org) and [rhino](https://appsilon.github.io/rhino/). There is always the option of following your own, with only `shiny` and you selected tools.


# What is shiny golem?
`golem` is an opinionated framework to facilitate the creation of robust `shiny` apps. In the words of the authors:

>> An opinionated framework for building a production-ready 'Shiny' application. This package contains a series of tools for building a robust 'Shiny' application from start to finish.
>
> https://cran.r-project.org/web/packages/golem/index.html

The packages contains a series of functions that initialize a `shiny` app, creating the files and folders necessary according to the `golem` proposition of how it should be structured. The initialization includes default scripts to be modified, scripts to guide you through the usage of the framework, and functions that can be used within the app.

# Engineering Production-Grade Shiny Apps
First of all I have to say that book is not only about `golem`. It is rather an excellent book about software engineering for the average R user. In other words, if you have been using R without any background in software development, and you are working (or starting) with `shiny`, this book is definitely a must read. The book is organized in a systematic way that starts with the planning, moves forward to the design, prototyping and building and ends up with optimization. It contains a lot of interesting and useful tips for the beginner and some even for the experienced. 

The topics covered are wide but the authors try to explained them as detailed as possible, while keeping it simple and practical. They offer code snippets and simple example to keep up with what is explained. Something that I particularly like is that the book is not fully focused on the use of `golem` but it also shares different ways of achieving similar results. One example of all this is the chapter 17 "Using JavaScript": the authors provide a general overview of the basics of JavaScript, they touch HTML topics necessary to understand JavaScript interaction, and share options to pass info between JavaScript and R, explaining the `golem` way and the traditional `shiny` way.

Another interesting aspect is that most of the chapters offer further sources to learn more about the topic, either books, web pages and even GitHub repos with examples. Although the book claims that it is oriented to people who are already somehow familiar with shiny, it is my opinion that it can as well be followed easily by someone who just started learning shiny. Somehow I thought how useful it could have been to myself a few years ago when I was already experienced with R and familiar with Shiny, but completely ignorant to HTML, CSS, JavaScript and Software Engineering. I built my knowledge up by collecting info scatter over different sources until I could have a solid base. The authors collect it all in one e-book an keep there useful references and links which surely are now in my personal bookmarks. 

Finally, the book is an easy read. Probably for someone with less experience in some of the topics it can take some effort, but it is not definitely the kind of headache-producer book that we offer encounter when learning a new programming language with highly technical but almost not practical guides out there. The authors have a good style for explaining and sharing examples and I believe that anyone with a good base knowledge of R or web development can follow.

# My first golem app: personal_finances

So, I decided to create a toy app following the book. Since it is written in chronological order for the creation of a Shiny app, I went one more time through it and created a very simple app for calculating savings. The app calculates either the amount of money necessary each month to reach a goal, or the amount of time.

You can find the code in my github repo [PersonalFinances](https://github.com/teotenn/PersonalFinances) and the live app under [this link](https://dev.teoten.com/shiny/PersonalFinances/). Notice that at the moment of writing this post, the app is functional but still in a "toy" state without too much aesthetics and a few minor bugs. Yet, it showed me how quickly and easily one can build a strong base app using Golem.

## Setup
We start a golem project by calling

```r
create_golem("path_to_project")
```

At the end of the execution it suggests you to install some dependencies using the built in function `install_dev_deps()` (in case they are missing). These are basically utilities that are useful for particular cases depending what you want to achieve, for example `dockerfiler`. This can result in getting installed a bunch of packages that you might never use. I'd recommend to apply the function if you want to be prepared for everything and you don't have concerns about storage memory. Alternatively, I assume that is possible to try and continue without this step and only install particular packages when it is really necessary.

Next, we can see that the directory tree has been created with the default golem files. By default, the DESCRIPTION file already contains 3 dependency packages:

```
Imports:
    config (>= 0.3.2),
    golem (>= 0.4.1),
    shiny (>= 1.7.4.1)
```

I was nicely surprised with the fact that every R script has useful comments, and the scripts under `/dev` show clear instructions on how to be used. Here is the example of `01_start.R`.

```r
# Building a Prod-Ready, Robust Shiny Application.
#
# README: each step of the dev files is optional, and you don't have to
# fill every dev scripts before getting started.
# 01_start.R should be filled at start.
# 02_dev.R should be used to keep track of your development during the project.
# 03_deploy.R should be used once you need to deploy your app.
#
#
########################################
#### CURRENT FILE: ON START SCRIPT #####
########################################

## Fill the DESCRIPTION ----
## Add meta data about your application
##
## /!\ Note: if you want to change the name of your app during development,
## either re-run this function, call golem::set_golem_name(), or don't forget
## to change the name in the app_sys() function in app_config.R /!\
##
golem::fill_desc(
  pkg_name = "PersonalFinances", # The Name of the package containing the App
  pkg_title = "PKG_TITLE", # The Title of the package containing the App
  pkg_description = "PKG_DESC.", # The Description of the package containing the App
  author_first_name = "AUTHOR_FIRST", # Your First Name
  author_last_name = "AUTHOR_LAST", # Your Last Name
  author_email = "AUTHOR@MAIL.COM", # Your Email
  repo_url = NULL, # The URL of the GitHub Repo (optional),
  pkg_version = "0.0.0.9000" # The Version of the package containing the App
)

## Set {golem} options ----
golem::set_golem_options()

## Install the required dev dependencies ----
golem::install_dev_deps()

## Create Common Files ----
## See ?usethis for more information
usethis::use_mit_license("Golem User") # You can set another license here
usethis::use_readme_rmd(open = FALSE)
devtools::build_readme()
# Note that `contact` is required since usethis version 2.1.5
# If your {usethis} version is older, you can remove that param
usethis::use_code_of_conduct(contact = "Golem User")
usethis::use_lifecycle_badge("Experimental")
usethis::use_news_md(open = FALSE)

## Use git ----
usethis::use_git()

## Init Testing Infrastructure ----
## Create a template for tests
golem::use_recommended_tests()

## Favicon ----
# If you want to change the favicon (default is golem's one)
golem::use_favicon() # path = "path/to/ico". Can be an online file.
# golem::remove_favicon() # Uncomment to remove the default favicon

## Add helper functions ----
golem::use_utils_ui(with_test = TRUE)
golem::use_utils_server(with_test = TRUE)

# You're now set! ----

# go to dev/02_dev.R
rstudioapi::navigateToFile("dev/02_dev.R")
```

And here we start with the package dependency and black magic solutions. The `golem::fill_desc` part is clear what it does: to modify the DESCRIPTION file accordingly; but `golem::set_golem_options()` it is not really clear what it does. According to the book:

> ... (the function) will add information to the `golem-config.yml` file, and set the `here` (Müller 2017) package root sentinel. `here` is an R package designed to handle directory management in R. When used in combination with golem, `here` helps ensure that everything you do in your console is performed relatively to the root directory of your project: the one containing the `DESCRIPTION` of your application. That way, even if you change the working directory of your R session to a subfolder, you will still be able to create modules and CSS files in the correct folder.

However, it was not as obscure as I thought. At execution, the function prints what it is doing (or what has been done).

```r
── Setting {golem} options in `golem-config.yml` ──────────────────────────────────────
✔ Setting `golem_name` to PersonalFinances
✔ Setting `golem_wd` to golem::pkg_path()
You can change golem working directory with set_golem_wd('path/to/wd')
✔ Setting `golem_version` to 0.0.1
✔ Setting `app_prod` to FALSE
── Setting {usethis} project as `golem_wd` ────────────────────────────────────────────
```

The section "Create Common Files" allows you to use `usethis` as you would during package creation (which I usually don't use). My section looks like this:

```r
## Create Common Files ----
## See ?usethis for more information
usethis::use_gpl_license()
## usethis::use_readme_rmd(open = FALSE)
## devtools::build_readme()
# Note that `contact` is required since usethis version 2.1.5
# If your {usethis} version is older, you can remove that param
usethis::use_code_of_conduct(contact = "Golem User")
## usethis::use_lifecycle_badge("Experimental")
## usethis::use_news_md(open = FALSE)
## Use git ----
usethis::use_git()
```

I have changed from MIT license to GPL 3. I am keeping the code of conduct and git as well. Since this is a toy project, I have commented everything else.

Moving forward, I allowed `golem::use_recommended_tests()` which basically initializes the basic structure for `testthat`. It suggested to me to install the package `spelling` which I have never used so, I chose not to. Then, I replaced the default `favicon.ico` for my own file and execute the function `golem::use_favicon()`.

The final 2 functions were as follows:

```r
> golem::use_utils_ui(with_test = TRUE)
✔ File created at ../PersonalFinances/R/golem_utils_ui.R
✔ Utils UI added
✔ File created at ../PersonalFinances/tests/testthat/test-golem_utils_ui.R
✔ Tests on utils_server added
> golem::use_utils_server(with_test = TRUE)
✔ File created at ../PersonalFinances/R/golem_utils_server.R
✔ Utils server added
✔ File created at ../PersonalFinances/tests/testthat/test-golem_utils_server.R
✔ Tests on utils_server added
```

Since I'm not using R Studio, I simply commented the last line `rstudioapi::navigateToFile("dev/02_dev.R")`. Finally I saved the changed to the script in case I need to go back to them later.

Before moving further forward, I created a Github repository and did my first commit. There were 24 untracked files according to Magit.

![The first files committed](/post/2023/my_first_golem_app/golem_init_files_git.png)

## 02_dev - Prototyping

>> When designing a first prototype, the challenge is in making it “as simple as possible”. There’s a tension between getting the basics working quickly and planning for the future of the app. Either extreme can be bad: if you design too narrowly, you’ll spend a lot of time later on reworking your app; if you design too rigorously, you’ll spend a bunch of time writing code that later ends up on the cutting floor. To help get the balance right, I often do a few pencil-and-paper sketches to rapidly explore the UI and reactive graph before committing to code.
>
> Hadely Wickham [Mastering Shiny](https://mastering-shiny.org/basic-case-study.html)

The next script, `02_dev.R` proposes functions to start with the development of the app itself. Following the advice from the book, I started by prototyping with an empty UI and a prototype for the functions.

### The empty UI

My first step was to create a module, as recommended by the script. Since I plan to start with one page only, I execute the first line that creates modules and commented the second.

```r
## Add modules ----
## Create a module infrastructure in R/
golem::add_module(name = "savings", with_test = TRUE) # Name of the module
## golem::add_module(name = "name_of_module2", with_test = TRUE) # Name of the module
```

The line creates `mod_savings.R` ot the folder `R`. The package is supposed to cope well with RStudio, but I was surprised at how well it does on Emacs as well: it opens a new buffer with the new file and allows you to edit it or exit and continue on your own later. Kudos to the developers of [ESS](https://ess.r-project.org/).

Now I can start prototyping the user interface.

```r
#' savings UI Function
#'
#' @description A shiny Module.
#'
#' @param id,input,output,session Internal parameters for {shiny}.
#'
#' @noRd
#'
#' @importFrom shiny NS tagList
mod_savings_ui <- function(id){
  ns <- NS(id)
  tagList(
    fluidRow(
      column(6 ,
             h2("Input parameters"),
             fluidRow(
               column(6, textInput("initial_ammount", "Initial Ammount", value = "0", placeholder = "$"))
             ),
             fluidRow(
               column(6, textInput("int_rate", "Interest Rate", value = "3", placeholder = "%"))
             ),
             fluidRow(
               column(6, selectInput("int_return", "Interest Return",
                                     choices = list("Annual" = 1, "6 Months" = 2,
                                                    "3 Months" = 3, "Monthly" = 4),
                                     selected = 1))
             ),
             fluidRow(
               column(6, textInput("goal", "Savings Goal", value = "0", placeholder = "$"))
             ),
             fluidRow(
               column(4,
                      h3("Time to reach"),
                      textInput("reach_years", "Years", value = "0"),
                      textInput("reach_months", "Months", value = "0")
                      ),
               column(4,
                      h3("Time to reach"),
                      textInput("add_monthly", "Ammount", value = "0")
                      )
             ),
             fluidRow(
               column(4, actionButton(NS(id, "calc_ammount"), "Calculate Monthly Needed")),
               column(4, actionButton(NS(id, "calc_time"), "Calculate Time"))
             )
             ),
      column(6 ,
             h3("Results"),
             fluidRow(
               textOutput(NS(id, "results")),
               plotOutput(NS(id, "pSavings"))
             )
             )
    )
  )
}
```

Such UI can already render the page. Thus, I executed `golem::document_and_reload()` which basically loads our app (as package) and then simply `run_app()`. Now, the structure of my app can already be seen at `127.0.0.1:3721`.

### Prototyping the server

As recommended in the section "Building an ipsum-app", I added random generated numbers to the results.

```r
#' savings Server Functions
#'
#' @noRd
mod_savings_server <- function(id){
  moduleServer( id, function(input, output, session){
    ns <- session$ns

    observeEvent(input$calc_ammount, {
      save_per_month <- round(runif(1, min = 100, max = 5000), 2)
      y <- seq(from = 1, to = save_per_month)
      x <- seq(from = 0, to = 10, length.out = length(y))
      output$results = renderText(glue::glue("You need to save ${save_per_month} per month."))
      output$pSavings = renderPlot(plot(x, y, xlab = "Years", ylab = "$"))
    })

    observeEvent(input$calc_time, {
      years_saving <- round(runif(1, min = 1, max = 50), 1)
      x <- seq(from = 0, to = years_saving)
      y <- seq(from = 1, to = 5000, length.out = length(x))
      output$results = renderText(glue::glue("You will reach your goal in {years_saving} years."))
      output$pSavings = renderPlot(plot(x, y, xlab = "Years", ylab = "$"))
    })

  })
}
```

I used `glue` in purpose to see how the adding of packages works. I added and executed the following line to the `02_dev.R` script.

```r
usethis::use_package("glue", min_version = TRUE)

✔ Adding 'glue' to Imports field in DESCRIPTION
✔ Increasing 'glue' version to '>= 1.6.2' in DESCRIPTION
• Refer to functions with `glue::fun()`
```

As part of prototyping, it is also recommended to create `.Rmd` notebooks with the workflow and/or functions that will be used in the app. So I did and created `savings_example.Rmd` inside **dev/** folder. I basically made a few notes, created and tested some functions and wrap them all in a main function that will be called from shiny to do the work.

## Build 

### Utils and functions

Following `02_dev.R`, I executed the following code to create new scripts with my functions.

```r
## Add helper functions ----
## Creates fct_* and utils_*
golem::add_fct("savings", with_test = TRUE)
golem::add_utils("helpers", with_test = TRUE)
```

This created `fct_savings.R` and `utils_helpers.R` in the **R/** folder, with some prepared snippet for the documentation. It also asks if you want to include unit test, and it creates the proper script inside **tests/testthat/** if you accept. 

My `utils_helpers.R` script contains only one simple function to help calculate the increase of a initial value based on the interest. `fct_savings.R` contains the main functions that will be used by shiny. These are `estimate_monthly` to calculate the monthly income needed to achieve the goal, and `estimate_time` that instead estimates the time needed to reach a goal. Each of them returns a list with the `text` to be displayed instead of the random number and a `data` frame to create the plot.

I could have placed all the functions inside the `fct` script but I want to try as many features as possible as described in the book. Since the goal is to test out `golem`, I will not go into the details of my functions, but you can check them out on the [github repo](https://github.com/teotenn/PersonalFinances) if you are interested.

### New server

Once that my supportive functions are available, I can simply call them from the module as any any other shiny app and get them to work. Here are the results of that.

```r
mod_savings_server <- function(id){
  moduleServer( id, function(input, output, session){
    ns <- session$ns

    init_amount <- reactive(as.numeric(input$initial_ammount))
    add_monthly <- reactive(as.numeric(input$add_monthly))
    goal <- reactive(as.numeric(input$goal))
    input_rate <- reactive(as.numeric(input$int_rate))
    t_years <- reactive(as.numeric(input$reach_years))
    t_months <- reactive(as.numeric(input$reach_months))

    int_return <- reactive({
      switch(as.numeric(input$int_return),
             12, 6, 3, 1)
    })

    observeEvent(input$calc_ammount, {
      results <- estimate_monthly(
        init_amount(),
        goal(),
        t_years(),
        t_months(),
        input_rate(),
        int_return()
      )
      output$results = renderText(results$text)
      output$pSavings = renderPlot(
        plot(results$data$Month, results$data$Amount, xlab = "Months", ylab = "$"))
    })

    observeEvent(input$calc_time, {
      results <- estimate_time(
        init_amount(),
        goal(),
        add_monthly(),
        input_rate(),
        int_return()
      )
      output$results = renderText(results$text)
      output$pSavings = renderPlot(
        plot(results$data$Month, results$data$Amount, xlab = "Months", ylab = "$"))
    })

  })
}
```

![Calculating the monthly amount](/post/2023/my_first_golem_app/Screenshot_calculate_amount.png)

![Calculating the time](/post/2023/my_first_golem_app/Screenshot_calculate_time.png)

Although there is still a lot to improve, it seems that I got already a functional app.

## Default unit tests

As I mentioned earlier, using the `golem` functions to create the scripts gives us the option of adding tests files in the style of `thestthat`. I think that the unit test is critical for any kind of software because it gives confidence when doing modifications. Thus, before moving any further with improvements, I decided to have a functional unit test ready.

For this step I went myself through the tests automatically created by `golem` first. In this aspect, `golem` does not offer anything special, but the book mentions good practices, practical advises and useful examples to for testing shiny apps. Therefore, I am not going in detail for this section. I basically used `testthat` as usual and implement some tests within `testServer`.

Something that I can do recommend for a person who is not so familiar with Shiny is to go through the section [Mastering Reactivity](https://mastering-shiny.org/reactivity-intro.html) of [Mastering Shiny](https://mastering-shiny.org/index.html) which explains reactivity in detail and helps a great deal at designing a proper unit test for shiny apps.

# Conclusions

My conclusions are kind of mixed feelings about `golem`. I find the book a fantastic read to improve my knowledge and skills with Shiny and I would recommend it to anyone using Shiny. 

When it comes to the package and the framework, I would recommend it to someone who wants a simple and quick way to create production ready apps in Shiny. It will require some learning in the framework but the e-book is a great source for that and following it can be almost enough. Golem is great to automatize certain processes in shiny, generate snippets and obtain all kind of tools directly from the box.

I am more a person who likes coding it myself before automating it. I like to know what is going on and why and after that I can either accept some automating tool or create my own. Therefore, `golem` is not for me. I don't like the way how it organize things and I definitely don't like the excess of code and dependencies that it generates for my shiny apps, specially if these are simple ones. On the other hand, I like how it organizes the modules and I learned some useful and practical things from creating a `golem` app which I am ready to implement in my next app. It is always good to learn something new and get a different perspective on something we are used to do in our own way.


