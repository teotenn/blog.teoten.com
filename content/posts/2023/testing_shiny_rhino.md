---
author: "Manuel Teodoro Tenango"
title: "Testing Shiny Rhino"
image: ""
draft: true
date: 2023-10-10
description: "Description"
tags: ["R shiny", "exploring"]
categories: ["R", "web-dev"]
archives: ["2023"]
---

# About rhino

# Creating a shiny app with Rhino

## Setup
A rhino app is initialized using the function `init`. I moved the working directory to the parent directory where I want the app and initialized it.

```r
rhino::init("spectrograms")
```

```r
✔ Rproj file created.
- Linking packages into the project library ... Done!
The following package(s) will be updated in the lockfile:

# CRAN -----------------------------------------------------------------------
  - backports      [* -> 1.4.1]
... ## <- list of all my installed packages here
The version of R recorded in the lockfile will be updated:
- R              [* -> 4.3.1]

- Lockfile written to '~/spectrograms/renv.lock'.
- Project '~/spectrograms' loaded. [renv 1.0.0]
✔ Initialized renv.
✔ Application structure created.
✔ Unit tests structure created.
✔ E2E tests structure created.
✔ Github Actions CI added.
```

By the end of the execution it created a working directory tree similar to what is shown in the tutorials. I did the first git commit which tracked 24 files!.

```git
Initial commit

24 files changed, 2370 insertions(+)
.Rprofile                             |    9 +
.github/workflows/rhino-test.yml      |   86 +++
.lintr                                |    5 +
.renvignore                           |    3 +
.rscignore                            |    7 +
app.R                                 |    2 +
app/js/index.js                       |    0 
app/logic/__init__.R                  |    2 +
app/main.R                            |   25 +
app/static/favicon.ico                |  Bin 0 -> 9662 bytes 
app/styles/main.scss                  |    0 
app/view/__init__.R                   |    2 +
config.yml                            |    3 +
dependencies.R                        |    2 +
renv.lock                             |  972 +++++++++++++++++++++++++++
renv/.gitignore                       |    7 +
renv/activate.R                       | 1181 +++++++++++++++++++++++++++++++++
renv/settings.json                    |   19 +
rhino.yml                             |    1 +
spectrograms.Rproj                    |   17 +
tests/cypress.json                    |    5 +
tests/cypress/.gitignore              |    2 +
tests/cypress/integration/app.spec.js |    7 +
tests/testthat/test-main.R            |   13 +
```

Something that surprised me was the huge directory tree created under `.../renv/library/R-4.3/x86_64-pc-linux-gnu/`, it is basically a list of all the R packages that I have installed. A directory with about 82l inks to the path where the packages are installed in my computer.

## First steps prototyping
I moved forward as suggested and created a first semi-empty script with a basic UI. I basically called it `spectra` because it will be the main module receiving the data and plotting the spectra. However, I keep thinking that it should received some more specialized name in the style of `golem` and call it `mod_spectra.R` or alike. However, I wanted to follow the tutorial closely so, I went with the same conventions they suggest.

```r
## app/view/spectra
box::use(
  shiny[NS, h2, h4, tagList, fluidRow, column, moduleServer, verbatimTextOutput,
        p, hr, checkboxGroupInput, actionButton, plotOutput, observeEvent, renderPrint],
  rhandsontable[rHandsontableOutput, renderRHandsontable, rhandsontable]
)

#' @export
ui <- function(id) {
  ns <- NS(id)

  tagList(
    h2("SPECTRA"),
    column(
      4,
      fluidRow(h4("Data")),
      fluidRow(
        rHandsontableOutput(NS(id, "counts"))
      )
    ),
    column(
      8,
      fluidRow(
        column(3,
               p("Selected elements"),
               hr(),
               verbatimTextOutput(NS(id, "selected_elements"))
               ),
        column(9,
               checkboxGroupInput(NS(id, "choose_element"), "Select elements",
                           choices = list("Pb", "Fe", "Zn", "Hg", "Au"))
               )
      ),
      fluidRow(
        actionButton(NS(id, "plotBut"), "Plot"),
        plotOutput(NS(id, "plotGraph"))
      )
    )
  )
}


#' @export
server <- function(id) {
  moduleServer(id, function(input, output, session) {
    ## initialize empty table
    df <- data.frame(Energy = double(50), Counts = double(50))
    output$counts <- renderRHandsontable(rhandsontable(df, readOnly = F))

    ## cast elements
    output$selected_elements <- renderPrint({input$choose_element})
  })
}
```
As you can see, they suggest the use of `box` package so, you need to call each function that is used within each script. This has its pros and cons. 

On the bright side, it gives complete control over the exact packages and functions that are used. It pairs well with `renv` which is the recommended method to install a package. In this case, I did `renv::install("rhandsontable")`. Then, it is recommended to list all the packages used in the `dependencies.R` file, located at the root directory, and calling `renv::snapshot`. I will leave the last one for later, when I will have more dependencies listed. This system helps to keep track of the packages, versions and function used within the app, making it easily reproducible. 

`box` also facilitates the calls to the different scripts that constitute the app. Since we are not building a package, the traditional method of calling a script would be `source("path/script.R")`. However, such method can bring a lot of headaches as the project grows bigger and the clear map of which scripts calls which becomes blur. With `box::use()` we have clarity per each script, giving clear info of its dependencies. Thus, to call the modules from the `main.R` script that contains `ui` and `server`, we get the following:

```r
box::use(
  app/view/spectra
)

box::use(
  shiny[bootstrapPage, div, moduleServer, NS, renderUI, tags],
)

#' @export
ui <- function(id) {
  ns <- NS(id)
  
  bootstrapPage(
    spectra$ui(ns("spectra"))
  )
}

#' @export
server <- function(id) {
  moduleServer(id, function(input, output, session) {
    spectra$server("spectra")
  })
}
```

And with these few extra lines of code, we have a first prototype of how the UI will look.

## Supportive functions
