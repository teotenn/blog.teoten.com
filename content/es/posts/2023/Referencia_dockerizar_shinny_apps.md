---
author: "Manuel Teodoro Tenango"
title: "Referencia: Dockerizando shinny apps"
image: ""
draft: false
date: 2023-02-11
description: "Una referencia a un par de fuentes explicando como dockerizar una aplicación Shiny"
tags: ["referencias", "R-dev", "R shiny", "R tips","docker"]
categories: ["R"]
archives: ["2023"]
---

[Andrew Couch](https://youtube.com/@AndrewCouch) tiene un video genial sobre como [poner una aplicación shiny en docker](https://youtu.be/ARd5IldVFUs). El video está en inglés, pero bien vale la pena. Va desde lo más básico, sin asumir ningún conocimiento de docker, lo cual es la situación de muchos usuarios de R como yo mismo. Últimamente he estado trabajando en unas aplicaciones Shiny y a pesar de que nunca he necesitado de Docker, ya puedo preveer el momento en que esto cambiará, por lo que decidí comenzar a aprender como usarlo. Andrew toca unos puntos muy buenos de cómo la mayoría de tutoriales en la web asumen que el lector ya conoce Docker y van a partir de ahí. A pesar de que esos tutoriales son fáciles de seguir, es difícil entender que estamos haciendo y el por qué. Esto genera mas problemas a futuro para extender y debuggear la aplicación. Andrew comparte su experiencia en busca de información sólida y luego procede a dar el tutorial donde explica las partes básicas, comandos y el código de un archivo Docker.

También recomienda una entrada de [statworx](https://www.statworx.com/en/content-hub/blog/how-to-dockerize-shinyapps/) (también en inglés) que es claramente la inspiración para su video. La entrada contiene información necesaria para dockerizar tu primera aplicación shiny. El texto es corto y claro, y hace un complemento al video. Si tienes problemas con el inglés, está es la entrada ideal para ti ya que puede ser traducida fácilmente.

Tal vez intentaré dockerizar alguna aplicación de prueba siguiendo las instrucciones y comparta mi experiencia aquí, siempre y cuando encuentre algo nuevo que agregue valor a los links compartidos. De lo contrario pueden utilizar dichos links como las bases y esperar a mi post de como dockerizar utilizando Guix, que es mi principal objetivo por ahora con el propósito de hacer mi aplicación reproducible. Manténganse en órbita.
