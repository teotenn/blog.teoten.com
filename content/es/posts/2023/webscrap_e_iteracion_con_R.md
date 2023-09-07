---
author: "Manuel Teodoro Tenango"
title: "Webscrap e iteraciones con R"
image: ""
draft: false
date: 2023-03-24
description: "Parte de cómo hacer mapas de cualquier región del mundo utilizando las librerías ggplot2 y maps"
tags: ["maps-app", "R mapas", "R funciones", "web-scrap", "database", "recursion"]
categories: ["R"]
archives: ["2023"]
---

## Sobre este post

Estamos creando mapas de datos que muestran los cambios durante un período de tiempo para diferentes países y orientado a todo tipo de ciudades. Esto básicamente significa que necesitamos **mapear cualquier región del mundo con R**. Hoy en día existen todo tipo de paquetes y técnicas para hacerlo. Quiero compartir la estrategia que utilicé con [ggplot2](https://cran.r-project.org/web/packages/ggplot2/index.html) y [maps](https://cran.r-project.org/web/packages/maps/index.html), utilizando el soporte de [Open Street Map](https://www.openstreetmap.org/) para obtener las coordenadas de las ciudades y finalmente hacerlo interactivo con [shiny](https://shiny.rstudio.com/). 

Estas publicaciones comparten mi camino en la creación de la aplicación Shiny. Es un proyecto vivo en el que estoy trabajando actualmente y decidí compartir mis experiencias durante el proceso de creación. Estas publicaciones no son sólo acerca de Shiny apps, si no más bien sobre la creación del paquete detrás, incluyendo temas sobre la generación de funciones, creación de los mapas, clases de objetos, entre otros, incluyendo cualquier tema interesante que aparezca en el camino. Es mi manera de contribuir a la comunidad de R y al mismo tiempo documentar el proyecto en si mismo.

Pueden encontrar todas las publicaciones en este tema bajo la etiqueta [maps-app](https://blog.rwhitedwarf.com/tags/maps-app/) (incluyendo las versiones en inglés).

También pueden encontrar el estado actual del proyecto en [mi GitHub](https://github.com/teotenn) repo [mapic](https://github.com/teotenn/mapic).

Este post está escrito originalmente en español, desde el aeropuerto de Amsterdam, de camino a México. Espero que lo disfruten. Siéntanse libres de dejar cualquier tipo de comentario y/o pregunta al final.

![R Maps](/post/2022/map_any_region_with_ggplot2_part_I/maps_DrawingMap.png)

## Motivación

Como mencioné en los posts anteriores de la serie, he estado trabajando últimamente en el código para la creación de los mapas y he hecho cambios que incrementan la eficiencia de las funciones, la lectura del código, y facilitan su uso, al mismo tiempo que me permite extender las funciones mas allá de cómo fueron originalmente diseñadas. 

He mencionado en algunas ocasiones que el código evolucionó poco a poco a partir de scripts creados para generar el mapa específico de algún país. Por lo tanto, las primeras funciones son más bien una colección de los pasos utilizados para generar el mapa, atrapados en forma de funciones para automatizar el proceso. 

Por este motivo había querido hacer cambios para adecuar las funciones a paradigmas mas adecuados a la programación funcional, que es el punto fuerte de R. Sin embargo, la prioridad era generar los mapas, así que la mayoría de mi tiempo iba dirigido a la creación de los mapas y el debug del código cuando era necesario. A esto debo agregar que este proyecto es parte de un trabajo voluntario para una ONG, de la cual pasé a ser el director de la división de investigación, lo que me generaba aún mas responsabilidades y trabajo. Y todo como trabajo secundario, separado de mi fuente principal de ingresos (que también esta basada en R).

Sin embargo, para bien o para mal, 2022 fue un año lleno de cambios y retos para mi y mi familia, lo que me obligó a dejar de lado el proyecto por un tiempo, resignar mi posición como director de la división, y enfocarme únicamente a mi carrera, mi salud y mi familia. Esto resultó en que, al recuperar la estabilidad en mi vida, me encontré con mas tiempo libre y menos obligaciones para re pensar el código y trabajar en ello. Adicionalmente, mi trabajo principal tuvo un giro que fue de la estadística a mas orientado a la programación en R, lo cual me ha dado mas herramientas para mejorar el código, y me ha motivado a retomar viejas lecciones sobre programación funcional y, sobre todo, iteración. 

Esto me permitió mejorar las dos funciones principales: la encargada del webscrapping y la que manda los datos a SQLite. Puedes encontrar las funciones originales en el post anterior [Mapa de cualquier región del mundo con R - Parte I: El mapa base](https://blog.rwhitedwarf.com/es/post/2022/mapa_de_cualquier_region_con_ggplot2_i/) y compararlos con las nuevas funciones mejoradas en este.

## Webscrapp a SQLite
La función `webscrap_to_sqlite` se encarga de enviar las coordenadas encontradas por Open Street Map a nuestra base de datos. La función como está definida originalmente es poco efectiva, ya que hace cada operación línea por línea. También es muy rígida en la forma en la que dirige los valores de las regiones, tanto su petición a la API como la colocación de los valores en la base de datos, lo que hace cualquier extensión o modificación muy complicada.

Por lo tanto, es la función que recibió mas cambios, fue prácticamente re escrita desde ceros, haciendo la búsqueda mas eficiente, permitiendo también búsqueda interna de los datos ya almacenados; mas flexible, lidiando con los parámetros de las regiones de forma mas clara; y mas entendible, mejorando el estilo del código.



```r
webscrap_to_sqlite <- function(db.name,
                               dat,
                               city = "City",
                               country = "Country",
                               region = NULL,
                               state = NULL,
                               county = NULL,
                               db_backup_after = 10) {
  ## Cargas las liberarias necesarias
  require(RSQLite)
  require(dplyr)

  ## 1. Conexion a la DB
  con <- dbConnect(drv = SQLite(), dbname = db.name)
  dbExecute(conn = con,
            "CREATE TABLE IF NOT EXISTS orgs
                    (ID INTEGER UNIQUE,
                     City TEXT,
                     Country TEXT, 
                     Region TEXT,
                     State TEXT,
                     County TEXT,
                     osm_name TEXT,
                     lon REAL,
                     lat REAL)")
  db <- as_tibble(dbReadTable(con, "orgs"))

  ## 2. Filtrado de los datos
  new_coords <- data.frame()
  dat_local <- compare_db_data(db.name, dat)
  df_len <- nrow(dat_local)

  ## 3. Mientras haya filas en DF repetimos:
  if (df_len != 0) {
    ## 3.1 Definir tamaño de la sub-muestra
    dat_local <- dat_local[c(1:db_backup_after), ]
    dat_local <- filter(dat_local, rowSums(is.na(dat_local)) != ncol(dat_local))

    ## 3.2 for loop para el webscrapping
    for (i in 1:nrow(dat_local)) {
      print(paste0("Searching entry ", dat_local[["ID"]][i]))
      
      ## 3.3 Abstracción de la info
      rg <- ifelse(is.null(region), "", dat_local[[region]][i])
      st <- ifelse(is.null(state), "", dat_local[[state]][i])
      ct <- ifelse(is.null(county), "", dat_local[[county]][i])
      rcity <- dat_local[[city]][i]
      rcountry <- dat_local[[country]][i]

      ## 3.4 Obtener las coordenadas
      ## 3.4.1. Primero buscamos si ya existen en DB
      search_query <- filter(db, City == rcity, Country == rcountry,
                             Region == rg, State == st, County == ct)
      if (nrow(search_query != 0)) {
        coords <- search_query[1, ]
        coords$ID <- dat_local[["ID"]][i]
        print("Found from memory")
        
        ## 3.4.2 Si aun no existen, busca en OSM API
      } else {
        coords <- coords_from_city(rcity, rcountry,
                                   Region = rg, State = st, County = ct)
        coords <- cbind(ID = dat_local[["ID"]][i],
                        City = rcity,
                        Country = rcountry,
                        Region = rg,
                        State = st,
                        County = ct,
                        coords)
      }
      new_coords <- rbind(new_coords, coords)
    }

    ## Y envía sólo los nuevos resultados a la DB
    dbWriteTable(con, "orgs", new_coords, append = TRUE)
    dbDisconnect(con)

    ## 3.4.3 Repetir
    webscrap_to_sqlite(db.name = db.name,
                       dat = dat,
                       city = city,
                       country = country,
                       region = region,
                       state = state,
                       county = county,
                       db_backup_after = db_backup_after)

    ## 4. Terminar la iteracion
  } else {
    db_final <- import_db_as_df(db.name)
    size <- nrow(db_final)
    not_found <- nrow(db_final[is.na(db_final$lat), ])
    message(paste("Search finished.\n",
                  size, "entries searched.\n",
                  not_found, "ENTRIES NOT FOUND"))
  }
}        
```

La función comienza llamando a las librerías necesarias y (1) conectándose a la base de datos, creándola si es necesario. (2) Luego genero dos data frames, uno vacío que almacenará nuevas coordenadas y otro relativo que contiene únicamente los datos que aún no existen en la base de datos. (3) Así pues, mientras haya datos en esta última data frame, la función continuará repitiéndose.

(3.1) Luego definimos la sub-muestra, que es un sub conjunto de `dat_local` del tamaño de `db_backup_after` y nos enfocamos únicamente en esta sub-muestra. (3.2) Sobre esa sub muestra hacemos las iteraciones utilizando `for` para obtener las coordenadas. Primero (3.3) preparamos la información como strings y después (3.4) buscamos las coordenadas. (3.4.1) Si ya existen en la base de datos lo tomamos de ahí, y si no (3.4.2) se buscan utilizado `coords_from_city`. Finalmente, (3.4.3) repetimos todo de nuevo permitiendo a la función llamarse a si misma.

Dado que en el paso 2 se filtran los datos que aún no están en la base de datos y en 3 se colocan los resultados de nuevas búsquedas en un data frame vacío, la función se llama a sí misma y aplica únicamente para cada sub conjunto de datos. Cuando finalmente `compare_db_data` arroja 0 valores por que todos los datos que se ingresaron a la función ya están contenidos en la base de datos, podemos salir de la función. En este caso decidí importar de nuevo los datos desde SQLite para obtener detalles de la búsqueda, y terminar la iteración enviando un mensaje al usuario sobre el total de entradas y la cantidad de las cuales no fueron encontradas. 

Si comparamos esta función con la propuesta en mi post anterior, la función es completamente diferente pero el resultado final es el mismo. Los argumentos utilizados por la función también son los mismo y toman los mismos valores, lo cual evita conflictos para el usuario. El único parámetro nuevo es `db_backup_after` que nos permite controlar a cada cuantas filas se realiza la iteración. Un valor mas pequeño significa más iteraciones, lo que resultado en un mayor uso de la memoria local, pero también mayor agilidad para encontrar datos que ya existen en la DB. Por otro lado, un valor mas alto reduce el número de iteraciones pero incrementa el número de conexiones a la API. Por este motivo le he otorgado un valor pre definido de 10. Esto, además de ser un valor balanceado, también reduce la confusión del usuario que podría no estar familiarizado con los cambios.

## Remover valores faltantes de la base de datos
En la propuesta anterior, únicamente las coordenadas encontradas eran enviadas a la base de datos, y las no encontradas se ignoraban. En la propuesta presente, todas las entradas se envían a la DB. Por lo tanto, es importante tener alguna opción para remover las entradas no encontradas. 

Para ello generé la función `remove_na_from_db`, una función muy simple pero que le otorga al usuario una propuesta remover `NA`s automáticamente.


```r
remove_na_from_db <- function(db.file) {
  require(RSQLite)
  con <- dbConnect(drv = RSQLite::SQLite(), dbname = db.file)
  dbExecute(conn = con,
            "DELETE FROM orgs WHERE lon IS NULL OR trim(lon) = '';")
  dbDisconnect(con)
}
```

La función es únicamente una conexión a la base de datos que envía la orden de remover filas donde el campo `lon` está vacío, en sintaxis de SQLite. Esto es la manera mas segura, directa y rápida de hacerlo. También podríamos importar los datos de nuevo a R, filtrarlos y enviarlos de nuevo a SQLite, pero esto requeriría mayor uso de la memoria local, mayor cantidad de código y un mayor riesgo ya que requeriría re-escribir la base de datos a SQLite por completo. El poder de la librería `RSQLite` (o cualquier otra librería que conecta a R con SQL) está precisamente en la posibilidad de pasar ordenes escritas y ejecutadas directamente en SQL.

## La obtención de las coordenadas
La función `coords_from_city` también recibió cambios considerables en lectura del código y flexibilidad, y un poco menores en funcionamiento y eficiencia.


```r
coords_from_city <- function(city = NULL,
                             country_code,
                             region = NULL,
                             state = NULL,
                             county = NULL) {
  require("RJSONIO")

  ## 1. Abstracción de regiones para OSM
  CityCoded <- gsub(" ", "%20", City) 
  CountryCoded <- paste("&countrycodes=", CountryTwoLetter, sep = "")
  extras <- c(city = City, state = State, region = Region, county = County)
  extrasCoded <- ""
  if (!is.null(extras)) {
    for (i in 1:length(extras)) {
      if (extras[i] != "" && !is.na(extras[i]) && !grepl("^\\s*$", extras[i])) {
        valCoded <- gsub(" ", "%20", extras[i])
        extrasCoded <- paste0(extrasCoded, "&", names(extras)[i], "=", valCoded)
      }
    }
  }

  ## 2. Respuesta
  link <- paste(
    "http://nominatim.openstreetmap.org/search?city="
  , extrasCoded
  , CountryCoded
  , "&format=json"
  , sep = ""
  )

  response <- try({fromJSON(link)},
                  silent = TRUE)

  if (class(response) == "try-error") {
    stop(response[1])
  } else if (class(response) == "response") {
    response_status <- http_status(response)
    if (response_status$category != "Success") {
      stop(response_status$message)
    }
  } else if (is.list(response)) {

    ## 3. Organización de los resultados
    if (length(response) == 0) {
      message(paste("No results found for", extrasCoded))
      coords <- data.frame("lon" = NA, "lat" = NA, "osm_name" = as.character(NA))
      
    } else if (length(response) == 1) {
      message(paste("Found", response[[1]]$display_name))
      coords <- data.frame(
        lon = response[[1]]$lon,
        lat = response[[1]]$lat,
        osm_name = response[[1]]$display_name
      )
      
    } else {
      message(paste("Several entries found for", city, country_code))
      coords <- data.frame(
        lon = response[[1]]$lon,
        lat = response[[1]]$lat,
        osm_name = response[[1]]$display_name
      )
    }
    
    ## 4. Salida como data frame
    return(coords)
}
```

```
> Error: <text>:68:0: unexpected end of input
> 66:     return(coords)
> 67: }
>    ^
```

El principal cambio está en la sección 1, en lugar de pasar cada una de las regiones como su propio string y darles formato una por una, las he abstraído todas en un solo vector. Esto reduce la cantidad de código, el uso de memoria, y nos permite incluir la ciudad en la lista, convirtiéndolo también en un valor opcional. La razón por la que las había preparado por separado publicación anterior es simplemente porque la función creció lentamente: al principio solo necesitábamos ciudad, pero luego tuvimos que usar algunos campos adicionales según el país en el que trabajábamos. Para facilitarme las cosas, simplemente agregué cada campo de región según fuera necesario. Ahora que tengo tiempo para trabajar en el código, esta fue la primera función que modifiqué.

El paso 2 ahora imprime mensajes que nos ayudan a identificar el error cuando se trata de la conexión, al mismo tiempo que detiene el proceso. Ya sea un error local de conexión, o problemas del lado del API, obtendremos un mensaje y el proceso se detendrá, lo cual debe evitar tiempos de espera largos cuando no hay conexión y se está haciendo la búsqueda de muchas localidades.

El paso 3 cambia un poco la organización de los resultados, devolviendo siempre un data frame con las mismas columnas cuando los resultados no fueron encontrados, pero ahora con los campos vacíos en dicho caso. Esto ayuda a las funciones presentadas anteriormente para llenar la base de datos. Adicionalmente, cuando muchos resultados fueron encontrados, se imprime esta información en pantalla; por ahora es sólo como información. La idea es mantener este espacio para realizar cambios en el futuro que nos permitan seleccionar la opción de manera interactiva. Esto es algo que aún necesito pensar y planear por que, por un lado quiero utilizarlo en una aplicación Shiny, y por otro lado queremos mantener la habilidad de que el web scrapping suceda automáticamente con menor intervención posible. 

Como ya he mencionado antes, estas nuevas funciones también nos permiten realizar búsquedas con el valor de ciudad vacío. Este fue un requisito solicitado en la última versión, ya que algunos usuarios comenzaron a hacer mapas por regiones, mientras que otros, al no encontrar ciudades muy pequeñas, decidieron agrupar los datos por región. Gracias a los cambios realizados en `coords_from_city`, la función `webscrap_to_sqlite` ahora puede obtener resultados cuando el valor para ciudad es `NA`, considerando que se encuentren las coordenadas para la región o el estado. Aquí es importante mencionar que se recomienda utilizar el argumento `state` para la búsqueda de regiones, por alguna razón, esto función mejor en la API de OSM. Como ejemplo, la búsqueda `coords_from_city(state = "Castilla La Mancha", country_code = "ES")` nos arroja los resultados esperados, a pesar de que España no tiene estados; sin embargo si hacemos `coords_from_city(region = "Castilla La Mancha", country_code = "ES")` nominatim no encuentra los resultados.

## Conclusiones

Estos cambios han resultado muy importantes para agilizar el proceso de la búsqueda de coordenadas y la automatización de la creación de mapas. Por otro lado, me permitió darle mas estilo al código y mejorar su eficiencia. Ya que mi principal proyecto por ahora es convertirlo en una aplicación Shiny, era importante para mi el mejorar el código y la eficiencia antes de lidiar con los detalles del server. Ya que este es un trabajo reciente que he realizado en los últimos meses, decidí compartirlo de inmediato ahora que tengo fresca la información de los cambios. Espero que pueda a ayudar a mas de uno a hacer código mas abstracto y practicar recursión. 
