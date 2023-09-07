---
author: "Manuel Teodoro Tenango"
title: "Mapa de cualquier región del mundo con R - Parte II: obteniendo las coordenadas."
image: "/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png"
draft: false
date: 2022-11-04
description: "Parte II de cómo hacer mapas de cualquier región del mundo utilizando las librerías ggplot2 y maps"
tags: ["maps-app", "R maps", "Code Visuals", "R functions", "web-scrap", "database"]
categories: ["R"]
archives: ["2022"]
---

Pueden encontrar todas las publicaciones en este tema bajo la etiqueta [maps-app](https://blog.rwhitedwarf.com/tags/maps-app/) (incluyendo las versiones en inglés).

También pueden encontrar el estado actual del proyecto en [mi GitHub](https://github.com/teotenn) repo [mapic](https://github.com/teotenn/mapic).

## Sobre este post

Esta es la segunda parte de las series de cómo crear mapas de cualquier región del mundo con R. De antemano me disculpo por detalles que puedan encontrar en la traducción, el post original lo creé en Inglés y el trabajo de traducción puede ser excesivo si voy a cada detalle. Por otro lado, recomiendo un conocimiento al menos básico del idioma Inglés si se quiere tener éxito en R o programación en general. Ayuda mucho a entender la sintaxis. 

Estamos creando mapas de datos que muestran los cambios durante un período de tiempo para diferentes países y orientado a todo tipo de ciudades. Esto básicamente significa que necesitamos **mapear cualquier región del mundo con R**. Hoy en día existen todo tipo de paquetes y técnicas para hacerlo. Quiero compartir la estrategia que utilicé con [ggplot2](https://cran.r-project.org/web/packages/ggplot2/index.html) y [maps](https://cran.r-project.org/web/packages/maps/index.html), utilizando el soporte de [Open Street Map](https://www.openstreetmap.org/) para obtener las coordenadas de las ciudades y finalmente hacerlo interactivo con [shiny](https://shiny.rstudio.com/). 

Estas publicaciones comparten mi camino en la creación de la aplicación Shiny. Es un proyecto vivo en el que estoy trabajando actualmente y decidí compartir mis experiencias durante el proceso de creación. Estas publicaciones no son sólo acerca de Shiny apps, si no más bien sobre la creación del paquete detrás, incluyendo temas sobre la generación de funciones, creación de los mapas, clases de objetos, entre otros, incluyendo cualquier tema interesante que aparezca en el camino. Es mi manera de contribuir a la comunidad de R y al mismo tiempo documentar el proyecto en si mismo.

Espero que lo disfruten. Siéntanse libres de dejar cualquier tipo de comentario y/o pregunta al final.

![R Maps](/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png)

## Open Street Maps y Nominatim

> Una búsqueda sencilla
> 
> ```r
> library('RJSONIO')
> 
> site <- ("http://nominatim.openstreetmap.org/search?city=Texcoco&limit=9&format=json")
> (result <- fromJSON(site))
> ```
> 
> ```
> > [[1]]
> > [[1]]$place_id
> > [1] 1177116
> > 
> > [[1]]$licence
> > [1] "Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
> > 
> > [[1]]$osm_type
> > [1] "node"
> > 
> > [[1]]$osm_id
> > [1] 336169214
> > 
> > [[1]]$boundingbox
> > [1] "29.619"       "29.659"       "-111.0786667" "-111.0386667"
> > 
> > [[1]]$lat
> > [1] "29.639"
> > 
> > [[1]]$lon
> > [1] "-111.0586667"
> > 
> > [[1]]$display_name
> > [1] "Texcoco, Carbó, Sonora, México"
> > 
> > [[1]]$class
> > [1] "place"
> > 
> > [[1]]$type
> > [1] "village"
> > 
> > [[1]]$importance
> > [1] 0.385
> > 
> > [[1]]$icon
> > [1] "https://nominatim.openstreetmap.org/ui/mapicons/poi_place_village.p.20.png"
> > 
> > 
> > [[2]]
> > [[2]]$place_id
> > [1] 3448536
> > 
> > [[2]]$licence
> > [1] "Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
> > 
> > [[2]]$osm_type
> > [1] "node"
> > 
> > [[2]]$osm_id
> > [1] 458633446
> > 
> > [[2]]$boundingbox
> > [1] "16.551667"  "16.591667"  "-97.053333" "-97.013333"
> > 
> > [[2]]$lat
> > [1] "16.571667"
> > 
> > [[2]]$lon
> > [1] "-97.033333"
> > 
> > [[2]]$display_name
> > [1] "Texcoco, Santa María Sola, Oaxaca, México"
> > 
> > [[2]]$class
> > [1] "place"
> > 
> > [[2]]$type
> > [1] "hamlet"
> > 
> > [[2]]$importance
> > [1] 0.36
> > 
> > [[2]]$icon
> > [1] "https://nominatim.openstreetmap.org/ui/mapicons/poi_place_village.p.20.png"
> ```

Comenzamos con [Open Street Map](https://www.openstreetmap.org/) y su API [nominatim](https://nominatim.openstreetmap.org/ui/about.html). En el código anterior podemos ver cómo realizar una consulta simple para una ciudad. Es básicamente un string (en R, "string" se utiliza para referirse a texto) largo que contiene primero la url de nominatim y al final los detalles de la búsqueda: aquí comenzamos la búsqueda de la ciudad, usando `?city=Texcoco` que en este caso apunta a una ciudad con solo unos pocos resultados. A continuación, limitamos la cantidad de resultados a 9 con `&limit=9` y finalmente solicitamos los resultados en formato JSON.

Básicamente, podríamos copiar el string que estamos pasando a la variable `site` y pegarla en el navegador web para ver los resultados directamente ahí. Siéntete libre de cambiar la ciudad `Texcoco` a cualquier otra ciudad, y juega un poco más con el resto de los parámetros. En particular, eche un vistazo a lo que sucede cuando elimina la parte `&format=json` o cuando cambia `json` por cualquier otra cadena abstracta como `csv` u otro formato no reconocido.

> Una búsqueda más sencilla
> 
> ```r
> city <- 'San%20Francisco'
> state <- '&state=California'
> country <- '&countrycodes=US'
> start.nominatim <- "http://nominatim.openstreetmap.org/search?city="
> end.nominatim <- "&format=json"
> 
> site <- paste0(start.nominatim, city, country, state, end.nominatim)
> (result <- fromJSON(site))
> ```
> 
> ```
> > [[1]]
> > [[1]]$place_id
> > [1] 297054975
> > 
> > [[1]]$licence
> > [1] "Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
> > 
> > [[1]]$osm_type
> > [1] "relation"
> > 
> > [[1]]$osm_id
> > [1] 111968
> > 
> > [[1]]$boundingbox
> > [1] "37.6403143"  "37.929811"   "-123.173825" "-122.281479"
> > 
> > [[1]]$lat
> > [1] "37.7790262"
> > 
> > [[1]]$lon
> > [1] "-122.419906"
> > 
> > [[1]]$display_name
> > [1] "San Francisco, CAL Fire Northern Region, California, United States"
> > 
> > [[1]]$class
> > [1] "boundary"
> > 
> > [[1]]$type
> > [1] "administrative"
> > 
> > [[1]]$importance
> > [1] 1.035131
> > 
> > [[1]]$icon
> > [1] "https://nominatim.openstreetmap.org/ui/mapicons/poi_boundary_administrative.p.20.png"
> ```

Si exploramos OSM y nominatim un poco, veremos que podemos agregar argumentos de búsqueda usando `&` seguido del argumento que queremos (es decir, `state`), el símbolo igual (`=`) y el argumento. En mi ejemplo anterior, puede verse cómo especificamos el estado y el país de nuestra consulta. Además, es importante saber cómo pasar espacios en un nombre, por ejemplo, San Francisco pasaría como `San%20Francisco`.

Con esta información básica en mente y sabiendo que el paquete `RJSONIO` nos ayuda a recuperar los datos de la API JSON en un formato tabular compatible con R, podemos preparar fácilmente una función para buscar cualquier ciudad rápidamente, siempre que se proporcionen algunos detalles adicionales como una región, estado o condado, y especialmente importante, el país (intenta buscar ciudades como Londres o Praga sin proporcionar un país, te sorprenderá la cantidad de ciudades que existen en el mundo con esos nombres).

```r
coords_from_city <- function(City,
                             CountryTwoLetter,
                             Region = NULL,
                             State = NULL,
                             County = NULL){
    require('RJSONIO')
    CityCoded <- gsub(' ','%20',City) #remover espacios de  URLs
    CountryCoded <- paste("&countrycodes=", CountryTwoLetter, sep = '')
    extras <- c(state = State, region = Region, county = County)
    extrasCoded <- ''
    if(!is.null(extras)) {
        for(i in 1:length(extras)){
            if(extras[i] != '' && !is.na(extras[i]) && !grepl("^\\s*$", extras[i])){
                valCoded <- gsub(' ', '%20', extras[i])
                extrasCoded <- paste0(extrasCoded, '&', names(extras)[i], '=', valCoded)
            }
        }
    }
    ## obtener los datos
    url <- paste(
        "http://nominatim.openstreetmap.org/search?city="
      , CityCoded
      , CountryCoded
      , extrasCoded
      , "&format=json"
      , sep="")
    x <- fromJSON(url)
    ## obtener las coordenadas
    if(is.vector(x)){
            message(paste('Found', x[[1]]$display_name))
            lon <- x[[1]]$lon
            lat <- x[[1]]$lat
            osm_name <- x[[1]]$display_name
            coords <- data.frame('lon' = lon, 'lat' = lat, 'osm_name' = osm_name)
    }
    ## Si x no es un vector
    else{
        message(paste('No results found for', City, CountryTwoLetter))
        coords <- data.frame('lon' = NA, 'lat' = NA)
    }
    ## regresa un df
    coords
}
```

Un detalle importante que debe saber es que, a menudo, proporcionar valores a los parámetros "state" o "region" arroja resultados similares, esto es particularmente útil en países donde no se usan estados o hay otras formas de organización presentes. Sin embargo, cuando el país tiene estrictamente estados, no puedes pasar el nombre de un Estado al parámetro `Region`.

La función devuelve un data frame que usaremos más adelante para crear una tabla con todos nuestros resultados. Como estamos interesados en crear mapas, solo necesitamos las coordenadas expresadas en latitud y longitud. En caso de que no se encuentre la consulta, la función completa los valores con `NA`, que luego usaremos para realizar un seguimiento de lo que se encontró y lo que no. También mantenemos los valores dentro de `osm_name`, que brinda suficiente información para brindarle al usuario detalles útiles sobre los resultados de la búsqueda, incluido el país de la ciudad encontrada y otros detalles como el estado o la región.

![Funcion coords_from_city() en detalle](/post/2022/map_any_region_with_ggplot2_part_II/maps_coords_from_city.png)

Un punto importante a considerar en `coords_from_city` es que devuelve solo el resultado superior de la consulta. Esto significa que cuanto más información se proporcione, más preciso será su resultado. Para nuestro proyecto funciona bien porque para los países grandes siempre recopilamos suficiente información sobre regiones y estados, mientras que para los países más pequeños, las opciones a menudo son demasiado pequeñas. Pero si usamos la función, es importante saber que si se proporciona un nombre de ciudad como `Springfield`, `country = 'US'` y no proporciona información sobre el estado y el condado, la función recuperara solo el primer resultado de la búsqueda, y descarta las opciones restantes.

## Mantener la información en una base de datos

La función `coords_from_city` podría ser suficiente si necesitamos obtener información unas pocas ciudades; Podríamos utilizar la iteración de `for` para recuperar todas las coordenadas que necesitamos y almacenarlas en un data frame para luego guardarlas en formato csv, `Rdata` o cualquier formato que elijamos. Lo mismo ocurre cuando buscamos cientos o miles de ciudades, pero con el aumento del tamaño, el tiempo de búsqueda también aumenta. Si, por alguna razón, la sesión de R se interrumpe, la información se perdería y tendremos que comenzar de nuevo desde la fila 1. Por lo tanto, enviaremos todos los resultados a una base de datos. De esa forma, no importa cuándo detengamos el proceso o cómo suceda, los datos se almacenan de forma segura fuera de R.

```r
webscrap_to_sqlite <- function(db.name,
                               dat,
                               col.city = 'City',
                               col.country = 'Country',
                               region = NULL,
                               state = NULL,
                               county = NULL)
{
    require(RSQLite)
    df_len <- nrow(dat)
    ## Conexion a la db y la tabla
    con <- dbConnect(drv=SQLite(), dbname=db.name)
    dbExecute(conn = con,
                "CREATE TABLE IF NOT EXISTS orgs
                    (ID INTEGER UNIQUE,
                     City TEXT, osm_name TEXT,
                     lon REAL,lat REAL)")
    ## -- Iteraciones para el webscrap -- ##
    ccount <- 0
    ## For loop para el webscrapping
    for(i in 1:df_len){
        rg <- ifelse(is.null(region), '', dat[[region]][i])
        st <- ifelse(is.null(state), '', dat[[state]][i])
        ct <- ifelse(is.null(county), '', dat[[county]][i])
        print(paste('Entry', i))
        ## Haz el webscrap
        coords <- coords_from_city(dat[[col.city]][i],
                                   dat[[col.country]][i],
                                   Region = rg, State = st, County = ct)
        ## Enviar resultados a DB sólo si se encontró algo
        if(is.na(coords$lon[1])){
            ccount <- ccount + 1
        }
        else{
            sq <- dbExecute(con, 'INSERT OR IGNORE INTO orgs
                             (ID, City, osm_name, lon, lat)
                             VALUES (?, ?, ?, ?, ?);',
                        list(dat[['ID']][i], dat[[col.city]][i],
                             coords$osm_name, coords$lon[1], coords$lat[1]))
        }
        print(paste('Completed', (i/df_len)*100, '%'))
    }
    ## Cerrar la conexión
    dbDisconnect(con)
    message(paste("WEB SCRAP FOR COORDINATES SEARCH FINISHED.",
                ccount, "ENTRIES NOT FOUND"))
}
```

Para almacenar los datos elegí usar [SQLite](https://www.sqlite.org/index.html) a través del paquete R [RSQLite](https://rsqlite.r-dbi.org/). Si no estas familiarizado con las bases de datos SQL te recomiendo que comiences con una búsqueda general en Google y luego regresar a la documentación de SQLite y el paquete R. Elegí SQLite porque necesitaba tener algo ligero y portátil que nos permitiera mover la información fácilmente de un país a otro en lugar de una base de datos centralizada donde pudiéramos almacenar todo, pero se puede aplicar un enfoque muy similar usando otros tipos de bases de datos SQL.

La función `dbConnect()` genera el archivo SQLite si aún no existe. Luego le damos a SQLite la orden de crear la tabla `orgs` si aún no existe, y la estructura para dicha tabla. A continuación buscamos las coordenadas de las entradas una a una usando `coords_from_city()` y finalmente lo enviamos a la base de datos. De esa manera podríamos detener el proceso en cualquier momento y continuar más tarde simplemente recuperando la tabla `orgs` de la base de datos, comparándola con nuestros datos originales y avanzando desde lo que falta. Para eso, la columna `ID` es fundamental, es la columna que nos permite vincular una entrada entre los datos originales, el data frame de R y la tabla SQL.

![Función webscrap_to_sqlite() en detalle](/post/2022/map_any_region_with_ggplot2_part_II/maps_webscrap_to_sqlite.png)

Nuestra función también tiene una variable `ccount` que cuenta cada vez que no se encuentra una entrada. De esa forma, una vez finalizada la consulta, imprimirá la cantidad de entradas que no fueron encontradas. Las razones para no encontrar una entrada pueden ser muchas, entre las más comunes que encontré están las siguientes:
  - Error en la ortografía del nombre de la ciudad o exceso de información (es decir, valor "Praga, Distrito 3" cuando el nombre de la ciudad es simplemente "Praga").
  - La ortografía incorrecta del nombre del Estado, Región y/o Condado.
  - La ciudad buscada simplemente no está en la base de datos de Open Street Maps (sucedió especialmente para pueblos muy pequeños).
  - Interrupciones de la conexión a internet. Esta es particularmente importante porque, a veces, ejecutar la consulta por segunda o tercera vez encontraría ciudades que no se encontraron la primera vez.
 
Para volver a leer los datos a R desde SQL, simplemente necesitamos hacer una conexión a la base de datos, leer la tabla y cerrar la conexión. La función `combine_df_sql` se encarga de eso y al mismo tiempo une nuestros datos originales con los datos almacenados en la base de datos por el ID y el nombre de la ciudad. Esto fue importante para el proyecto porque queríamos mantener las coordenadas de las ciudades separadas del resto de la información debido a algunas razones prácticas internas. Pero creo que mantener todos los datos en SQL a la vez puede facilitar muchas cosas. Entre otros, podría identificar cuándo se encontró una ciudad en particular en el pasado y recuperar las coordenadas de la base de datos directamente en lugar de hacer una conexión con nominatim. Lo hice para algunos países y reduce considerablemente el tiempo de consulta. Para la presente publicación, decidí mostrar la versión separada de los datos para brindar más herramientas al lector.

```r
combine_df_sql <- function(db.file, original.data){
    require(dplyr)
    require(RSQLite)
    if(is.character(original.data)){
        if(grepl('.csv', original.data, fixed = T)){
            df <- read.csv(original.data)
        }
        else{
            stop("Incorrect file format for data")
        }
    }
    else if(is.data.frame(original.data)){
        df <- original.data
    }
    else{
        stop("Incorrect data format")
    }
    con <- dbConnect(drv=RSQLite::SQLite(), dbname = db.file)
    db <- dbReadTable(con, "orgs")
    dbDisconnect(con)
    result <- left_join(df, db, by = c('ID', 'City'))
    return(result)
}
```

Otro detalle de nuestra función es la capacidad de leer desde el archivo `csv` o desde un `data.frame`. Dado que estábamos trabajando principalmente con archivos csv y usé data frames para los pruebas unitarias, estos 2 formatos fueron suficientes. Siéntanse libres de modificar o ampliar la función para los formatos de datos que puedan necesitarse.

## Datos faltantes

Como se mencionó anteriormente, a veces los resultados de la consulta estaban incompletos y era necesaria una segunda o tercera ejecución, pero con menos filas. Algunas otras veces sólo necesitaba parar la consulta y continuar mas tarde desde donde la dejamos. Sin embargo, otras veces los datos estaban incompletos o incorrectos y esto se podía solucionar más tarde con el responsable de los datos. Los 3 escenarios me forzaban a leer el archivo csv a R, luego la tabla de la base de datos y compararlos para filtrar los valores faltantes. Así que creé la función `compare_db_data` para comparar la base de datos (db) con los datos originales.

```r
compare_db_data <- function(db.file, dat){
    require(dplyr)
    require(RSQLite)
    if(is.character(dat)){
        if(grepl('.csv', dat, fixed = T)){
            df <- read.csv(dat)
        }
        else{
            stop("Incorrect file format for data")
        }
    }
    else if(is.data.frame(dat)){
        df <- dat
    }
    else{
        stop("Incorrect data format")
    }
    con <- dbConnect(drv=RSQLite::SQLite(), dbname = db.file)
    db <- dbReadTable(con, "orgs")
    dbDisconnect(con)
    filtered <- filter(df, !(as.character(ID) %in%
                              as.character(db$ID)))
    filtered
}
```

Como se mencionó anteriormente, a veces Open Street Maps simplemente no tiene registro de ciertas "ciudades" (de hecho, sucedió solo con pueblos o poblaciones realmente pequeñas). Para solucionar eso, la función `add_coords_manually` toma un archivo csv con una estructura particular para agregar los datos que faltan. El archivo csv debe tener las siguientes columnas:
    - Columna `ID` nombrada exactamente así y que contiene la misma ID que los datos originales.
    - Una columna que contiene el nombre de la ciudad
    - Columnas que contienen la Latitud y Longitud donde queremos señalar la ciudad
    - Un valor para `osm_name`. Esto podría dejarse vacío o podemos proporcionar el valor que queremos en esta punto. Lo importante es tener la columna presente en el archivo csv.

Luego, como en la función anterior, le pasamos a `add_coords_manualmente` el nombre del archivo csv con la información complementaria, el nombre de la base de datos SQLite y los nombres de las columnas donde tenemos los valores para los nombres de `city`, `osm_name` , `lat` y `long`, todos con formato de string. El resto de la función es autodescriptiva, siempre que se tengan conocimientos básicos de sintaxis SQL.

```r
add_coords_manually <- function(csv_file, db.name,
                                city, osm_name, lat, lon){
    require(tidyverse)
    require(RSQLite)
    csv_dat <- read_csv(csv_file)
    csv_len <- length(csv_dat$ID)
    con <- dbConnect(drv=RSQLite::SQLite(), dbname=db.name)
    for(i in 1:csv_len){
        dbSendQuery(con, 'INSERT OR IGNORE INTO orgs
                      (ID, City, osm_name, lon, lat)
                      VALUES (?, ?, ?, ?, ?);',
                    list(csv_dat[['ID']][i],
                         csv_dat[[city]][i],
                         csv_dat[[osm_name]][i],
                         csv_dat[[lat]][i],
                         csv_dat[[lon]][i]))
    }
    dbDisconnect(con)
    print(paste(csv_len, 'inserted'))
}
```

## Siguientes pasos

Si eres nuevo en R, probablemente habrás notado que uno de los puntos fuertes de R que estoy usando mucho aquí es el uso de funciones. Los primeros mapas que creamos en el proyecto los hicimos escribiendo scripts con unos pocos cientos de líneas. Eso nos dio la base para crear las funciones necesarias y, por lo tanto, el resto de los mapas fueron posibles usando solo unas pocas líneas. Algunos de los scripts para el web scrapping de las coordenadas constan de menos de 10 líneas de código. Eso es posible usando las funciones anteriores y algunas otras creadas para casos especiales o particulares. No compartiré absolutamente todo, pero quiero dar una idea de cómo hacer que el proceso sea más eficiente. Siempre puedes crear más funciones para tus casos particulares o modificar mis funciones propuestas para adaptarlas a tu situación particular.

Y hablando de extensibilidad, mientras escribía este blog descubrí una librería que no conocía, [tmaptools](https://github.com/r-tmap/tmaptools) que contiene la función [geocode_OSM](https://www.rdocumentation. org/packages/tmap/versions/1.6-1/topics/geocode_OSM) que usa nominatim para recuperar las coordenadas del punto buscado. La función tiene un formato de búsqueda más fácil de usar y más posibilidades para el valor de retorno, mientras que mi opción `coords_from_city()` se mantiene bastante rígida y aún con el formato original que se imaginó hace unos años cuando la creé. Si realmente te interesa el tema te invito a revisar el paquete. Yo mismo he estado ocupado manteniendo el código y creando mapas, por lo que tengo muy poco tiempo para hacer mejoras al proyecto original. Pero esa es exactamente mi tarea principal en este momento, así que si hago algún cambio en las funciones presentadas aquí usando el paquete [tmaptools](https://github.com/r-tmap/tmaptools), puedes estar seguro de que crearé un breve publicación para compartirlo.

Luego, una vez que tenemos las coordenadas de nuestras ciudades objetivo y sabemos cómo hacer el mapa básico, el siguiente paso es agregar las ciudades al mapa base. En la próxima publicación, les mostraré cómo lo hice y una función para que el proceso sea más rápido y eficiente.
