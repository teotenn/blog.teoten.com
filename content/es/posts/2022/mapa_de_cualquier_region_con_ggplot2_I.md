---
author: "M. Teodoro Tenango"
title: "Mapa de cualquier región del mundo con R - Parte I: El mapa base"
image: "/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png"
draft: false
date: 2022-10-08
description: "Parte I para hacer mapas de cualquier región del mundo con R utilizando las librerías ggplot2 y maps"
tags: ["maps-app", "R mapas", "ggplot2", "Code Visuals", "R funciones"]
categories: ["R"]
archives: ["2022"]
---

Pueden encontrar todas las publicaciones en este tema bajo la etiqueta [maps-app](https://blog.rwhitedwarf.com/tags/maps-app/) (incluyendo las versiones en inglés).

También pueden encontrar el estado actual del proyecto en [mi GitHub](https://github.com/teotenn) repo [mapic](https://github.com/teotenn/mapic).

## Sobre esta entrada

Cuando nos preparamos para una entrevista de trabajo, una de las preguntas que más recomiendan preparar es "Menciona el proyecto del que estés más orgulloso?". Personalmente nunca me han hecho esa pregunta en una entrevista de trabajo pero me mantuvo pensando. Hace algunos años desarrollé el código en R para la creación de mapas de infraestructura para un proyecto de Ciencias Políticas, y puedo decir que este es uno de los proyectos de los que estoy más orgulloso. Sin embargo, también es cierto lo que comúnmente se dice entre los desarrolladores, que **a nadie le importa cómo lo hiciste**. Al usuario final solo le interesa el producto final y cómo utilizarlo, mientras que al equipo de investigación le interesa saber las posibilidades que propone.

El proyecto me enseñó tanto en términos de habilidades técnicas que he decidido compartir el **cómo** en caso de que pueda ayudar a alguien más. También es mi forma de contribuir a la comunidad de R, ya que yo mismo aprendí R y programación gracias a las amables personas que publican su experiencia en la web (y también a los que tienen la paciencia de responder preguntas en StackOverflow). Debido al acuerdo de confidencialidad con mi cliente no puedo compartir el código completo o el repositorio de Git.

Creamos mapas de datos que muestran los cambios durante un período de tiempo para diferentes países y orientados a todo tipo de ciudades. Esto básicamente significa que necesitamos **mapear cualquier región del mundo con R**. Hoy en día existen todo tipo de paquetes y técnicas para hacerlo. Compartiré la estrategia que utilicé con [ggplot2](https://cran.r-project.org/web/packages/ggplot2/index.html) y [maps](https://cran.r-project.org/web/packages/maps/index.html), utilizando el soporte de [Open Street Map](https://www.openstreetmap.org/) para obtener las coordenadas de las ciudades y finalmente hacerlo interactivo con [shiny](https://shiny.rstudio.com/).

El proyecto es bastante largo para una sola publicación. Por otro lado, recientemente logré extraer el código base y hacerlo público sin comprometer la privacidad. También es un proyecto vivo en el que estoy trabajando actualmente. Por lo que decidí compartir mis experiencias durante el proceso de creación de la aplicación Shiny. Estas publicaciones no son sólo acerca de Shiny apps, si no más bien sobre la creación del paquete detrás. Tocaré temas sobre la generación de funciones, creación de los mapas, clases de objetos, entre otros, incluyendo cualquier tema interesante que aparezca en el camino. Es mi manera de contribuir a la comunidad de R y al mismo tiempo documentar el proyecto en si mismo.

Espero que lo disfruten. Siéntanse libres de dejar cualquier tipo de comentario y/o preguntas al final.

## Motivación

Cuando me uní al equipo, todo lo que sabían era que querían hacer mapas de infraestructura (por ejemplo hospitales, cafés, iglesias, oficinas públicas, etc., pero el proyecto básicamente se puede aplicar a cualquier cosa contable en una ciudad dada). Los mapas deben cambiar en el tiempo de acuerdo con los datos (generalmente crecimiento) y debe ser posible aplicarlo para cualquier país y, por lo tanto, cualquier tipo de ciudad en dicho país. Este último punto representa un desafío porque para hacer un mapa se necesitan las coordenadas del punto en particular para mapear, pero en cambio obtuvimos las dirección postales en el mejor escenario, o solo el nombre de la ciudad en el peor. Por lo tanto, lo dejamos a nivel de ciudad y decidimos trabajar con eso.

La mayoría de los paquetes de R para hacer mapas tienen granularidad hasta algunas regiones y ciudades importantes por país, y estamos hablando de países donde alguien ha desarrollado algún paquete de R para eso. Sin embargo, incluso a los mejores paquetes les faltan algunas ciudades en sus datos. Necesitábamos estandarizar todo sin necesidad de cambiar paquetes por país. Antes de unirme, el equipo intentaron usar Google Maps y Microsoft Excel, pero la cantidad de datos se volvió desastrosa y la flexibilidad para editar los mapas era bastante limitada. Además no querían agregar problemas de derechos de autor o copyright a la lista de limitaciones. Por lo tanto, propuse usar R. Por supuesto, nadie en el equipo había oído hablar de él antes. Podríamos haber usado cualquier otra herramienta, aprendí que tanto Python como JavaScript tienen algunas posibilidades decentes. Pero R es lo que he estado usando durante los últimos 10 años y es lo que quería utilizar para este proyecto. Y así comencé a escribir código.

Los primeros mapas eran códigos personalizados para un país en particular con estilos decentes. Pero rápidamente se convirtió en un conjunto de funciones y argumentos para mantener los mismos estándares para cada mapa. El apoyo de los diseñadores gráficos también llevó los estilos a un nivel muy profesional. Después de unos meses teníamos mapas muy profesionales que se podían hacer en un par de horas (o menos) con un par de líneas de código. Cada mapa por cada país con el lapso de años deseado para ser impreso.

No puedo compartir cada uno de los detalles, pero al menos quiero mostrar cómo pasamos del mapa básico a su forma dinámica de mapeo durante un período de tiempo, y cómo lo envolví todo en un par de funciones para que sea rápidamente replicable para cualquier conjunto de datos dado. Siéntanse libres de compartir su opinión.

## Cómo crear un mapa de cualquier país en R usando la librería maps

El primer paso es crear el mapa básico de un país. Aquí está la función para lograr exactamente eso.

```r
library(maps)
library(ggplot2)

## hacer un df con sólo pais para traslapar
map_data_es <- map_data('world')[map_data('world')$region == "Spain",]

## El mapa (maps + ggplot2 )
ggplot() +
    ## Primera capa: mapa del mundo
    geom_polygon(data = map_data("world"),
                 aes(x=long, y=lat, group = group),
                 color = '#9c9c9c', fill = '#f3f3f3') +
    ## Segunda capa: mapa del país
    geom_polygon(data = map_data_es,
                 aes(x=long, y=lat, group = group),
                 color = 'red', fill = 'pink') +
    coord_map() +
    coord_fixed(1.3,
                xlim = c(-13.5, 8.5),
                ylim = c(34, 45)) +
    ggtitle("A map of Spain") +
    theme(panel.background =element_rect(fill = 'blue'))
```

![plot of chunk basic_map](/post/2022/map_any_region_with_ggplot2_part_I/basic_map-1.png)

Utilizamos la librería [maps](https://cran.r-project.org/web/packages/maps/index.html) en combinación con [ggplot2](https://cran.r-project.org/ web/packages/ggplot2/index.html). El paquete `maps` contiene un sistema de coordenadas para un mapa de todo el mundo separado por países (aunque es posible que las fronteras políticas no estén totalmente actualizadas). También puede hacer los mapas, pero para eso decidí hacer uso del soporte `ggplot2`.

Empezamos extrayendo los datos relevantes del país que queremos mapear, en este caso `España`. Por supuesto, es importante pasar el nombre del país de la misma manera que está escrito en `map_data('world')$region`. Puedes usar la función `unique()` para encontrar los nombres exactos de todos los países incluidos en los paquetes (si ejecutas `unique(map_data('world')$region)` da 252 países al momento de escribir esta publicación).

Una vez que tengamos los datos para un país en particular, podríamos simplemente mapearlo directamente usando `geom_polygon()` sin embargo, eso mapearía a España rodeada por un espacio vacío a su alrededor. Para ubicarlo en el contexto de su vecindario, aplicamos dos capas de `geom_polygon()`: la primera con el mapa de todo el mundo y la segunda con el mapa del país únicamente.

![Explicación del código básico del mapa](/post/2022/map_any_region_with_ggplot2_part_I/maps_BasicMap.png)

Luego necesitamos decirle a `ggplot` que use un sistema de coordenadas para crear mapas en lugar de sólo polígonos. Para eso usamos la función [coord_map()](https://ggplot2.tidyverse.org/reference/coord_map.html) y luego pasamos los detalles de la proporción del mapa y los límites en X e Y a la función `coord_fixed( )`.

Hasta aquí podemos obtener nuestro mapa. `ggplot` está básicamente trazando lo que estamos especificando dentro del sistema de coordenadas, todo a su alrededor (los océanos) estará vacío y el gráfico será completado con las cuadrículas predeterminadas y los colores grises de `ggplot()`. Por lo tanto, necesitamos definir el color de los océanos como color de fondo para todo el gráfico. Eso es lo que hace la última línea de código.

Por supuesto, hay muchas mejoras que hacer aún. Hasta ahora he dado colores exagerados para que el lector sepa qué fragmento de código controla qué. En ese sentido, puedes notar que se puede simplemente pasar los nombres de los colores, lo cual aplica los valores predeterminados, o se puede ser más específico y proporcionar la notación html del color (es decir, `'#9c9c9c'`). Con esto podemos ahora mejorar las imágenes y al mismo tiempo crear una función para trazar cualquier país que queramos.

## Función para crear el mapa básico en R

```r
map_country <- function(country, x_limits = NULL, y_limits = NULL){
    ## Verificar los argumentos tomados por la función
    if(!is.character(country)) stop("Nombre del país debe ser de tipo string")
    if(length(country) != 1) stop("La función soporta sólo un país por mapa")
    ## Cargar las librerías
    require(maps)
    require(ggplot2)
  if(!country %in% map_data('world')$region) stop('Nombre del país no reconocido\nPara ver una lista de paises reconocidos ejecute <unique(maps::map_data("world")$region)>')
  ## Si no se proporcionaron los limites de las coordenadas, imprime el mapa mundial
    if(missing(x_limits) || missing(y_limits)) {
        warning("Límites de X y/o Y no han sido encontrados.\nImprimiendo el mapa mundial")
        map_country_theme <- theme(panel.background = element_rect(fill = '#4e91d2'))
    }
    else {
        if(length(x_limits) != 2 || length(y_limits) != 2 ||
           !all(grepl('^-?[0-9.]+$', c(x_limits, y_limits)))){
            stop("Los límites de las coordenadas X y Y deben ser ingresadas como vectores con dos valores numéricos")
        }
        else {
            ## Definiendo el tema seleccionado para el mapa final
            map_country_theme <- theme_bw() +
                theme(panel.background = element_rect(fill = '#4e91d2'),
                      legend.position = 'none',
                      panel.grid.major = element_blank(),
                      panel.grid.minor = element_blank(),
                      axis.line = element_line(colour = "black"),
                      axis.title.x=element_blank(),
                      axis.text.x=element_blank(),
                      axis.ticks.x=element_blank(),
                      axis.title.y=element_blank(),
                      axis.text.y=element_blank(),
                      axis.ticks.y=element_blank())
        }
    }
    ## Un df con el pais a agregar únicamente
    map_data_country <- map_data('world')[map_data('world')$region == country,]
    ## El mapa (maps + ggplot2 )
    ggplot() +
        ## Primera capa: mapa mundial
        geom_polygon(data = map_data("world"),
                     aes(x=long, y=lat, group = group),
                     color = '#9c9c9c', fill = '#f3f3f3') +
        ## Segunda capa: mapa del país
        geom_polygon(data = map_data_country,
                     aes(x=long, y=lat, group = group),
                     color = '#4d696e', fill = '#8caeb4') +
        coord_map() +
        coord_fixed(1.3,
                    xlim = x_limits,
                    ylim = y_limits) +
        ggtitle(paste0("A map of ", country)) +
        scale_x_continuous(n.breaks = 20) +
        scale_y_continuous(n.breaks = 20) +
        map_country_theme
}

## Probando la función con un país diferente
map_country("Germany", c(-2, 22), c(47, 55))
```

![plot of chunk function_map](/post/2022/map_any_region_with_ggplot2_part_I/function_map-1.png)

Aunque la función puede parecer complicada al principio, de hecho es el mismo código que usamos para crear el mapa en un principio, pero en lugar de escribir directamente el nombre del país o los límites para X o Y, los reemplazamos con los argumentos `país `, `x_limits` y `y_limits` respectivamente; de esa manera todas las partes donde teníamos el string `"España"` ahora tenemos el argumento `país`, y así sucesivamente. Ahora estos son los únicos argumentos que necesitamos cambiar cuando queremos mapear un país diferente. Puedes definir más argumentos en caso de que quieras tener más posibilidades de ser editable, por ejemplo, podríamos definir un argumento `country_color` para especificar el color que queremos para el país de destino. En nuestro caso, queríamos mantener los mismos estándares para todos los mapas por motivos de control de calidad, por lo tanto, queríamos tener exactamente los mismos colores y estilos para todos nuestros mapas.

También hay algunas adiciones en la parte superior antes del código inicial para hacer los mapas: son una serie de declaraciones `if` y `else` que simplemente se usan para validar que la información ingresada por el usuario es la información que realmente necesita la función para hacer su trabajo. Si se ingresa algún argumento incorrecto a la función, detenemos el proceso y escribimos un mensaje de lo que está mal utilizando la función `stop()`. Para el caso de que no se definan límites de X o Y, se envía un mensaje de advertencia usando `warning()`. En ese caso el proceso continúa pero definimos un `theme()` que permite al usuario ver el país en el contexto del mapa mundial, con exceso de valores en los ejes X e Y para proporcionar los puntos de referencia y dar un idea de dónde poner los límites. Al final, cuando nos aseguramos de que todos los valores estén bien, definimos el tema final que realmente queremos aplicar. A este respecto debería hacer una mención especial sobre `!all(grepl('^-?[0-9.]+$', c(x_limits, y_limits))))`: se usa para asegurar que tanto los valores de X como Y son de tipo numérico. Puedes ver la visualización del código a continuación junto con la ayuda de las funciones que no comprendes para obtener una explicación más detallada. Siéntete libre de probar los errores y advertencias proporcionando a la función nombres de países o letras donde debería haber números, etc.

![Código de la función explicada](/post/2022/map_any_region_with_ggplot2_part_I/maps_FunctionMap.png)

La parte inferior de la función es exactamente igual a nuestro primer mapa, reemplazando los valores reales con los argumentos. También hemos cambiado los colores por unos más específicos. Casi al final de la función hemos agregado `scale_x_continuous(n.breaks = 20)` que agrega 20 marcas en la escala del eje X (y después, lo mismo para Y). Usamos esto para asegurarnos de que, en caso de que el usuario no tenga idea de qué valores límite elegir, pueda tener un buen enfoque con respecto a la posición del país objetivo. En caso de que ambos límites para X e Y se ingresen a la función, nuestro otro tema enmascarará estas 20 marcas con `axis.text.x = element_blank()` y `axis.ticks.x = element_blank()`.

La línea final es la prueba de que nuestra función puede trazar un mapa que no sea España, en este caso elegí Alemania. Básicamente, podemos elegir cualquier país incluido en el paquete `maps` y entonces hacer el mapa con los mismos estándares en tan sólo una línea de código de R.

## Observaciones finales

He querido mostrar aquí uno de los métodos que utilizo para crear funciones: básicamente escribo primero el código de lo que quiero lograr y, una vez que hace exactamente lo que quiero, lo envuelvo en una función, reemplazando valores por los argumentos que el usuario necesitará modificar más adelante. Luego pienso en qué podría salir mal y genero las advertencias y errores correspondientes. Es una buena práctica hacer eso no solo para que el usuario sepa mejor cómo usar la función, sino también para uno mismo, pues resulta muy útil cuando necesitamos depurar el código. Otra buena práctica en las funciones de R es la llamada a las librerías dentro de la función usando `require()`. Incluso si se están escribiendo muchas funciones que usan las mismas bibliotecas, es bueno repetirlo en cada función, o al menos por script, para que se mantenga independiente y, nuevamente, nos ayude en el proceso de debug. No hace mucho comencé a colaborar en un proyecto donde no hacían la llamada a los paquetes de R por cada función, sino solo en el nivel superior cuando se llamaba al proceso principal del programa. Esto me hizo casi imposible probar y depurar el código, así que la primera actividad que hice como nuevo miembro del equipo fue pasar 2 días completos agregando `require()` donde fuera necesario.

![Código de la función explicada](/post/2022/map_any_region_with_ggplot2_part_I/maps_CompareCode.png)

Espero que te diviertas mapeando diferentes países. Debido a que los diferentes países tienen diferentes tamaños y formas, una forma de mejorar las imágenes es ajustando la proporción (`prportion`), por ejemplo, mi propio mapa de Alemania parece fuera de forma, pero mejora considerablemente si en lugar de 1.3 le damos una proporción de 1.4, consulta la documentación para obtener más información al respecto.

Una vez que tengamos el mapa básico, podríamos agregar las ciudades donde queremos agregar valores de datos. Desafortunadamente, para las ciudades hay muchas limitaciones, especialmente para países donde no se tienen pquetes específicos. E incluso en los que se tienen, la mayoría de los paquetes tienen carencias de algunas ciudades, especialmente cuando son muy pequeñas. Por lo tanto, en la segunda parte se verá como enfrentar este problema haciendo un poco de web scrap utilizando open street maps.
