---
author: "M. Teodoro Tenango"
title: "Mini tutorial: hacer tipo lógico cualquier texto"
image: 
draft: false
date: 2022-09-18
description: "Primer mini tutorial: Una función que toma cualquier string de texto y lo convierte en tipo lógico"
tags: ["minitutorial", "R funciones", "R tips"]
categories: ["R"]
archives: ["2022"]
---

## Acerca de este post.

Este es mi primer post en español. Es en realidad la traducción de un post que escribí originalmente en inglés hace un par de meses. Pueden ver el post original [aqui](https://blog.rwhitedwarf.com/post/minitut_makebool/). Espero que sea útil para la comunidad hispanohablante de usuarios de R. 

Este post se basa en un trabajo reciente donde mi tarea fue la revisión y depuración de piezas de código pequeñas o simples que pueden resultar en consejos prácticos y rápidos para otros usuarios de R, especialmente principiantes o personas sin mucha experiencia en el uso de R.

Con ese objetivo en mente, mientras recuperaba un poco de mi tiempo libre y un poco de estabilidad mental, y celebraba mi nuevo puesto como programador en R, decidí crear publicaciones simples pero útiles llamadas **mini tutoriales**, comenzando con un ejemplo muy simple, incluso tonto, pero útil.

## Mini tutorial: Hacer lógico cualquier texto (make_logical_any_string).

Una función para hacer lógica cualquier texto en R.

```r
make_logical_any_string <- function(texto){
    texto <- as.character(texto)
    resultado_logico <- as.logical(texto)
    if(is.na(resultado_logico)){resultado_logico <- FALSE}
    return(resultado_logico)
}
```

La función toma cualquier valor, lo convierte en texto (character) y devuelve `TRUE` SÓLO SI el valor adopta cualquiera de las siguientes formas: `"T"`, `"TRUE"`, `"true"`, `"true"` o `TRUE`, el último el valor lógico, no en formato texto.

### Lógica de la función

La función `as.character()` convierte cualquier forma de texto "true" listado arriba en un `TRUE` lógico. Si el texto es `"False"` o sus formas equivalentes, la función devolverá `FALSE`. Si se pasa cualquier otro valor a la función, el resultado será `NA`. Por lo tanto, necesitamos modificar los resultados cuando se producen NA, ya que necesitamos un resultado de Verdadero/Falso. Así que implementamos `if(is.na(resultado_logico)){resultado_logico <- FALSE}` que obligará a cualquier otra cadena de texto a devolver `FALSE`.

Estamos usando este código para ejecutar scripts de R en la consola que pasa una serie de argumentos para su funcionamiento, algunos de los cuales deben ser "TRUE" solo cuando se especifica, y "FALSE" en cualquier otro caso, de ahí el truco de convertir cualquier otro valor a `FALSE` en lugar de `NA`.

Algo importante a tomar en cuenta es que los argumentos siempre se pasan al script R como texto y, por lo tanto, escribí el ejemplo para esta publicación convirtiendo todo en texto en la primera línea de la función, lo cual no es necesario en nuestro código original ejecutado en el Terminal. De esta forma, si se le pasa algún número a la función, también devolverá `FALSE`, emulando lo que pasaría si se ingresa un número en la consola. Este comportamiento **es diferente para la función** `as.logical()`, que devuelve `FALSE` si ingresa el valor numérico `0` y `TRUE` si se ingresa cualquier otro valor numérico.
