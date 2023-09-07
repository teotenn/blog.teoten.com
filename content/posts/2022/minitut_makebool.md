---
author: "M. Teodoro Tenango"
title: "Minitutorial: make_logical any string"
image: 
draft: false
date: 2022-09-18
description: "First minitutorial: A function that makes any string into logical"
tags: ["minitutorial", "R functions", "R tips"]
categories: ["R"]
archives: ["2022"]
---

## Welcome to R minitutorials of R White Dwarf

Since the beginning of this year I've been forced to abandon completely the blog for countless and rather abstract personal reasons that include personal health, family matters and changes in my daily activities including volunteer work as well as main job. As part of the last, I finally got hired for a position as R developer, which brings great joy to me. 

Thus, I've been using R more lately in all kinds of forms, including review and debug of small or simple code pieces that can result in practical quick hints for other R users, especially beginners or people with not much experience using R. 

With that aim in mind while regaining a little bit of my free time and a piece of mental stability, and celebrating my new position, I decided to take care of the blog again with simple yet useful posts called **minitutorials**, starting with a very simple, even silly, but useful example.

I hope they can be useful for you or your friends. Enjoy them!

## Minitutorial: make_logical_any_string

A function to make logical any string


```r
make_logical_any_string <- function(a_string){
    a_string <- as.character(a_string)
    logical_result <- as.logical(a_string)
    if(is.na(logical_result)){logical_result <- FALSE}
    return(logical_result)
}
```

The function takes any value, convert it to character and returns `TRUE` ONLY IF the value takes either of the following forms: `"T"`, `"TRUE"`, `"True"`, `"true"` or `TRUE`, the last one the logical value, not the string.

### Logic of the function
The function `as.character()` will convert any of the true strings listed above into a logical `TRUE`. If the string is rather `"False"` or its equivalent forms, the function will return `FALSE`. If any other character is passed to the function, the result will be `NA`. Therefore, we need to tweak the results when NA's are produced since we forcefully need a True/False result. Thus, we implement `if(is.na(logical_result)){logical_result <- FALSE}` which will force any other string to return `FALSE`.

We are using this code for running R scripts in the terminal which passes a series of arguments for its functioning, some of which are required to be `TRUE` only when specified so, and `FALSE` in any other case, hence the trick of converting any other value to `FALSE` rather than `NA`.

Something to keep in mind is that the arguments are always passed to R script as character and thus, I wrote the example for this post converting everything into character in the first line of the function, which is not necessary in our original code executed in the terminal. In this way, if any number is passed to the function, it will also return `FALSE`, emulating what would happen if a number is entered into the console. This behavior **is different for the function** `as.logical()` itself, which returns `FALSE` if you enter the numerical value `0` and `TRUE` if any other numerical value is passed. 
