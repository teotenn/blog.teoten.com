---
author: "Manuel Teodoro Tenango"
title: "Getting started with Leprechaun for Shiny"
image: ""
draft: true
date: 2023-10-10
description: "Description"
tags: ["R shiny", "exploring"]
categories: ["R", "web-dev"]
archives: ["2023"]
---

# About Leprechaun

# Creating a shiny app with Leprechaun

## Setup
Unlike `golem` and `rhino`, `leprechaun` does not create the folder structure for the project. Instead, it is recommended to start with the basic structure for an R package and call `leprechaun::scafold` from the root directory. I went with the proposition of the quick start and used the `usethis` package. Here is how it looks:

```r
usethis::create_package("Spectra")

leprechaun::scaffold()
```

```r
Installing package into ‘/home/teoten/R/x86_64-pc-linux-gnu-library/4.3’
(as ‘lib’ is unspecified)
trying URL 'https://cloud.r-project.org/src/contrib/leprechaun_1.0.0.tar.gz'
Content type 'application/x-gzip' length 21402 bytes (20 KB)
==================================================
downloaded 20 KB

* installing *source* package ‘leprechaun’ ...
** package ‘leprechaun’ successfully unpacked and MD5 sums checked
** using staged installation
** R
** inst
** byte-compile and prepare package for lazy loading
** help
*** installing help indices
** building package indices
** testing if installed package can be loaded from temporary location
** testing if installed package can be loaded from final location
** testing if installed package keeps a record of temporary installation path
* DONE (leprechaun)

The downloaded source packages are in
	‘/tmp/RtmpTZaiTu/downloaded_packages’
> leprechaun::scaffold()

── Scaffolding leprechaun app ──────────────────────────────────────────────────

── Creating lock file ──

✔ Creating .leprechaun

── Adding dependencies ──

✔ Adding 'shiny' to Imports in DESCRIPTION
✔ Adding 'bslib' to Imports in DESCRIPTION
✔ Adding 'htmltools' to Imports in DESCRIPTION
✔ Adding 'pkgload' to Suggests in DESCRIPTION


── Generating code ──

✔ Creating R/ui.R
✔ Creating R/assets.R
✔ Creating R/run.R
✔ Creating R/server.R
✔ Creating R/leprechaun-utils.R
✔ Creating R/_disable_autoload.R
✔ Creating R/zzz.R
✔ Creating R/input-handlers.R

✔ Creating inst/dev
✔ Creating inst/assets
✔ Creating inst/img
✔ Creating inst/run/app.R

── Ignoring files ──

✔ Adding '^\\.leprechaun$' to '.Rbuildignore'
```

The first commit added 16 new files.
**ADD IMAGE, SCREENSHOT ALREADY EXISTS**

Then we document and load. This by default creates the R documentation and updates the `NAMESPACE` file.

## Prototyping

```r
leprechaun::add_module("spectrograms")
```

It creates the file `R/module_spectrograms.R` in a very similar fashion as `golem` does. It got my attention was how everything is organized in new lines, in a different fashion than traditional `shiny`, especially the server function.

```r
spectrograms_server <- function(id){
	moduleServer(
		id,
		function(
			input, 
			output, 
			session
			){
				
				ns <- session$ns
				send_message <- make_send_message(session)

				# your code here
		}
	)
}
```

We can then use this basic structure to write our modules. It requires as much documentation as any normal package in order to have the `NAMESPACE` rendered correctly. One thing that in my opinion is missing is the tag `@noRd` since I don't need to have documentation for every single module. However, some people might like it or needed and `leprechaun` gives total freedom here.

Before documenting I added a package for my module with the help of `usethis`.

```r
usethis::use_package('rhandsontable')
```

Then I need to place the module functions in the main `ui` and `server`. These are easily found under `R/ui.R` and `R/server.R` respectively. The default scripts generated by `leprechaun` are filled in with a default body. I have removed it and place a very simple `fluidPage`.

```r
# R/module_spectrogram.R
#' spectrograms UI
#' 
#' @param id Unique id for module instance.
#' @importFrom rhandsontable rHandsontableOutput
#' 
#' @keywords internal
#' @noRd
spectrogramsUI <- function(id){
  ns <- NS(id)

  tagList(
    h2("SPECTRA"),
    column(
      4,
      fluidRow(h4("Data")),
      fluidRow(
        rhandsontable::rHandsontableOutput(NS(id, "counts"))
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
```

And with these few extra lines of code, we have a first prototype of how the UI will look.

## Supportive functions
