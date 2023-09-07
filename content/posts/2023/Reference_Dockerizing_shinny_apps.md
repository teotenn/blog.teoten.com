---
author: "Manuel Teodoro Tenango"
title: "Reference: Dockerizing shinny apps"
image: ""
draft: false
date: 2023-02-11
description: "A reference to a couple of good sources explaining how to dockerize a shiny app"
tags: ["references", "R-dev", "R shiny", "R tips","docker"]
categories: ["R"]
archives: ["2023"]
---

[Andrew Couch](https://youtube.com/@AndrewCouch) has a nice video about [deploying a shiny app using docker](https://youtu.be/ARd5IldVFUs). He goes from the very basics, that asume no knowledge of docker whatsoever, which is the position of many R users like myself. I've been working in some shiny app lately, and although I've never needed docker so far,  I decided to start learning it because I can already foresee the future when it won't be the case. Andrew makes some good points about how most tutorials assume the reader to know the basics of docker and start from there. While these are easy to follow, it is difficult to understand what we are doing and why we are doing it. That creates later problems to extend and debug the apps. Andrew shares his experience in searching for solid info and then proceeds with a tutorial where he explains basic parts, commands, and code of a docker file.

He also recommends a post from [statworx](https://www.statworx.com/en/content-hub/blog/how-to-dockerize-shinyapps/) that is clearly the inspiration for his video. The post contains all the info necessary to dockerize a first shiny app. The post is short and clear, and it's an excellent complement for the video.

 I might try dockerizing some simple shiny app and share the experience here if I find something new or interesting that can add value to the mentioned links. Otherwise, you can stay with these as the basis for dockerizing a shiny app and wait for my post of dockerizing using Guix, which is my main target for now in order to make my app reproducible. Stay in orbit.
