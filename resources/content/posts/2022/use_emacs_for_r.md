---
author: "Manuel Teodoro Tenango"
title: "Using Emacs for R"
image: "/post/2022/use_emacs_for_r/learning-editors.jpg"
draft: false
date: "2022-12-29"
description: "Emacs is an easy to use text editor that, combined with ESS can make a powerful IDE for working with R"
tags: ["R basics", "emacs"]
categories: ["R", "emacs"]
archives: ["2022"]
---



## Easy Emacs

To start using R, or almost anything else in [Emacs](https://www.gnu.org/software/emacs/) you basically need to know 3 things: 1) How to move in Emacs, meaning understanding what is what and learning a few key commands; 2) What is the configuration file and how to use it and 3) How to use packages to extend Emacs. In the first half of this post I will try to show how easy it is to cover these 3 points even for people who are inexperienced in programming. If you don't believe me I invite you to read just the first paragraph of the next section to give you an idea of how easy it really is. During the second half I will show how I'm using R in Emacs to give you a starting point of a fully functional environment for R, and will conclude with some topics that can be further explored. 

## Why did I chose Emacs as a researcher in the academia?

I started my professional life as a researcher in ecology-related topics. During my master studies I improved my knowledge on statistics considerably and due to that and to the complexity of my research project, I did not want to use a GUI-based software for my statistical analysis. Thus, I started learning R, and believe it or not, I completed my research project for my Thesis by tipping R code directly to the console from my handwritten notes. When I started my PhD I thought that it would be easier to just write the code I need in electronic format and copy-paste it to the R console. And with that idea in mind and the help of the internet, I discovered the text editors and Emacs, and a whole new universe opened up to me. I know that many in my position would be ashamed of sharing such a story but I simply want to exemplify how easy it is to start using Emacs, contrary to the popular belief. I went from having no idea of what a text editor is, to setting up and using Emacs with R, with no intermediate steps.

Emacs is a wonderful text editor that can easily be extended to do many things. You can have tools to help in writing your code such as different types of indentation, syntax highlighter, git utilities, project management, code maps, web browser, even to play games. Emacs provides by default a lot of functionalities to move easily through the text files, including keybindings to go to the end and beginning of buffer, function or paragraph, parentheses matching, text search, exploration and replacement, syntax and spelling checks. You can create markers to move quickly to particular files, window configurations or to store text and numbers. Some consider Emacs almost as an OS because you can also do things like create and delete files, version control, internet browser, and more. 

The reason why I stayed with Emacs as a researcher in the academia was mainly due to [org-mode](https://orgmode.org/). It is an Emacs major mode that helped me to organize my research and still today it helps me to organize my job. You can think if it as the Emacs version of Markdown, with the possibility to organize to-do lists, tag notes and sections, fully organize an agenda (tracking tasks, set deadlines, schedule items, etc.). You can add chunks of code from almost any language and, with the help of a couple more libraries, you can run the code within the org file itself. Github and other git servers have integrated tools to view org files as html, but there are libraries to convert them also to pdf, libreoffice, create presentations and more. 

Another important point that made me fall in love with Emacs was the fact that, if I managed to keep most of my research files as text I could do it all from Emacs, instead of using different apps for different tasks. And so I did: I was writing my papers in LaTeX and organizing my bibliography with bibtex; I was saving data as CSV which Emacs can manage very well; the graphics were more of an issue but, since I used R to create most of them, I simply needed to save the right script for the right plot. And all this was organized in org-mode with links to this or that file according to the project, section, tag, etc. And the reason why I wanted to do this was not even for organization purposes, but rather because, as text, I could track all my changes using Git, which ended up being a huge support for my PhD work: I could revert changes if I had mistakes or explore old commits, and backup all of that easily. So, at the end, while R had been the reason why I decided to explore Emacs, it was in fact the combo Emacs + org-mode + git which improved my organization and productivity potentially during my research life. And I would like to share this tools with as many people as possible.

![Editors learning curve](/post/2022/use_emacs_for_r/learning-editors.jpg)

Thus, I decided to create this post, to give you an idea of how easily you can start using Emacs for R coding. If you enjoy it and you'd like me to create more content about some of the tools briefly described here, make sure to leave me a comment and I'll take care of it. I include a general list of the tools I use regularly in Emacs at the end, you can have a look there.

## Quick start 

Although Emacs is extremely customizable, it is true that it requires some coding skills and knowledge of the not so popular programming language called [Emacs Lisp](https://www.gnu.org/software/emacs/manual/html_node/eintr/). You would probably have also read that Emacs has a very steep learning curve, which is also true. This two conditions usually scare people away from learning Emacs. In this section I will demonstrate that you don't need to know Emacs Lisp (or programming at all) and that with very little knowledge of Emacs you can have a ready-to-use super-powerful R editor.

This chapter is a brief overview of the rest of the post meant as a quick start to get Emacs up and working with R in just as few as 10 steps. The rest of the post will simply go deeper into each of the steps. 

 1. Make sure that you have installed both, Emacs and R in your computer.
 
 2. Open Emacs and press the keys Ctr + x, release and press Ctr + f (in Emacs notation, this combination of keys is expressed as "**C-x C-f**"). Focus on the **mini buffer**, it is the line positioned at the bottom of your window. It is waiting for you to type something. If there is some path to a folder already in that area delete it first and then type `~/.emacs` and enter. It should open a new empty window.
 
![Emacs minibuffer](/post/2022/use_emacs_for_r/emacs-minibuffer.png)
 
 3. This is your configuration file. Paste the following code in your new window 
 
```emacs-lisp
(require 'package)
(add-to-list 'package-archives '("melpa" . "https://melpa.org/packages/"))

(unless (package-installed-p 'use-package)
  (package-refresh-contents)
  (package-install 'use-package))
(setq use-package-always-ensure t)

(use-package ess)

(use-package company
  :config
  (add-hook 'after-init-hook 'global-company-mode))

(setq company-selection-wrap-around t
      company-tooltip-align-annotations t
      company-idle-delay 0.45
      company-minimum-prefix-length 3
      company-tooltip-limit 10)
```

This configuration assumes that you have installed R with all the defaults. If you have installed R in a directory of your choice, add the following line at the end of the configuration file, changing the path of my example for the path were you have installed R. 

```emacs-lisp
(setq inferior-ess-r-program "C:/Users/Manuel/path_where_R_is/R-4.2.1/bin/R.exe")
```

 4. Type **C-x C-s** (meaning, Ctr + x, release, Ctr + s). This will save the file. 
 
 5. Type now Alt + x (in Mac *command* key instead of Alt or, if it does not work, the *option* key instead), this is the key Meta, represented in Emacs by **M-x**. At this point you want to focus again on your mini buffer, the line at the bottom of the screen.
 
 6. Type there `package-install` enter and then type `use-package`, enter. If some text appears at the bottom of your `.emacs` file don't worry, it is intended this way. 
 
 7. Focus on the mini buffer in case it prompts something. If it asks you if you want to install the package type `y` and enter. If it tells you that it cannot find the package or it does not exist, close Emacs, open it again and repeat steps 5 and 6. It should show a message informing that it has been installed. In my case it shows the following line in the minibuffer:
  

```example
Done (Total of 9 files compiled, 4 skipped)
```
 
 8. Now close Emacs and open it again. This time it should take longer to load. Be patient, Emacs is loading installing and loading more packages for you.
 
 9. Type again **C-x C-f** and type `test.R`, enter. You can change the path before the file if you wish (i.e. `~/Code/test.R`).
 
 10. A new empty area should appear. Type there one line of R code. When you are done, while keeping the cursor in the line where your code is, press **C-c C-j**, this sends a line to R. A new area will open, showing the R console and the results of the code you just sent. If nothing happens focus on the minibuffer, it might ask you where to start your R session; you can just press enter or provide a new location. Then you can continue typing R code and use the same combination of keys to run a line, you can use **C-c C-p** to run a paragraph, **C-c C-f** to send a function and **C-c C-b** the send the whole buffer (which here basically means the whole file) or simply Control + enter (**C-return**) to send any of the mentioned regions. And as you already know, you can save the file by pressing **C-x C-s**. 
 
![ESS and R](/post/2022/use_emacs_for_r/emacs-r-init.png)
 
If everything went well now you should have a simple Emacs configuration to start coding in R. Congratulations!.

## Getting started with Emacs

### Installation and first steps

Both, R and Emacs are extremely easy to install, therefore I will not go into the details for it. Basically in any Linux distribution you can just use your package manager for it, in windows just download and run the official executable files and for Mac you can also download the binaries or use alternative package methods like homebrew (also applicable for Linux). For R go to (https://www.r-project.org/) and for Emacs to (https://www.gnu.org/software/emacs/).

Once you have installed Emacs you can run it and you will have the welcome screen, together with some toolbars and list of menus. At this point you could basically use Emacs like any other text editor: you can open files, edit them and save them by using all the menus, icons and your mouse. However, the real power of Emacs rest in its keybindings. To get started I recommend to click on the link **Emacs Tutorial** of the welcome screen, it will guide you through the basics. After the tutorial you will feel more comfortable finding your way around Emacs and the rest of this post will be easier to follow.

![Welcome to Emacs](/post/2022/use_emacs_for_r/emacs-init.png)

### Control, Meta and the minibuffer, moving in Emacs

When you are working with text most of the time (as it is the case of R code) the use of the mouse can reduce productivity by searching with your eyes the exact places you want to mark, going all the way to the menu to save or open a file, finding when a parentheses opens and closes, and so on. The idea of the keybindings is to increase productivity by staying in the text at the level of the keyboard most of the time, since this is what we actually do when we write code.

At the beginning it can be complicated to memorize so many keybindings. I'd recommend to try to remember the most basic ones to move along the text, save files, close Emacs and split the screen as you need to. The rest can be easily achieved through the mouse icons and menus. When I started using Emacs I was having a piece of paper with the most useful keybindings and, as my fingers started remembering by themselves I was deleting those and adding new ones. Today I can assure you that my productivity to write R code is much better than it ever was with any other text editor. 

I will not go through the details of which keybindings do what since it is all in the self contained tutorial, however there are some key points to learn the keybindings. One is the knowledge of the so called "Emacs Notation". Whenever you search either in Emacs documentation or some other sources to use Emacs, how to perform certain actions, you will find things like **C-M-a**. The capital `C` is short for the key Control, while capital `M` is for meta, which in most computers is Alt and in Mac is usually the key Command. Thus, **C-M-a** would mean that you have to hold the key __Control__, the key __Meta__ and the key __a__. Usually the keys Control and Meta are used in combination with other keys and thus, the letters **C** and **M** are used at the beginning of the commands. That would mean that, for example, the combination **C-C** does not mean Control twice, but rather Control plus capital C. Although this rarely happens (I've never used such a combination), it is important to be aware because Emacs recognizes difference between upper and lower case. 

![Emacs minibuffer](/post/2022/use_emacs_for_r/emacs-minibuffer.png)

Another important part to know in Emacs is the minibuffer. By default it is positioned at the bottom of the screen and it is used to communicate commands between Emacs and the user. For example, when you save a file the minibuffer will print something like `Wrote /path/to/file.R` to signal that the file has been saved. 

The minibuffer is also used to pass commands to Emacs. All the keybindings are bind to a command, although not every command is bind to a key. To pass a command to Emacs you can use the keys **M-x**. As an example you can try to use **M-x** and you will see that the minibuffer has changed and now is ready to receive your input. Type there `help-for-help` and a new menu will appear, showing you the options for help and the instructions to use it. For example, type **b** to display all keybindings. The command `help-for-help` is bind to the keys **C-h ?** therefore, if you would type this combination instead you would have the same response.

Emacs uses intuitive key bindings and thus, the combination **C-h** is designed for **h**elp. For example, the combination **C-h b** will show the help for the **B**indings, **C-h t** help with **T**utorial, **C-h f** help for a **F**unction (you have to type in the function), etc. **C-x** executes high levels functions such as **s**ave file **C-x C-s** or **c**lose Emacs with **C-x C-c**.

You can take some time to familiarize yourself with some of the keybindings and later you will see how it pays off when writing and executing R code. The best way to get familiar with the main ones is by following the tutorial included in Emacs, you have the Link in the welcome page, in the Help menu or simply type **C-h t**.

### The configuration file

The second part of the power of Emacs is its customization. The first aspect for its customization is the init file, also known as dot Emacs. According to its documentation:

> When Emacs is started, it normally tries to load a Lisp program from an initialization file, or init file for short. This file, if it exists, specifies how to initialize Emacs for you. Traditionally, file `~/.emacs` is used as the init file, although Emacs also looks at `~/.emacs.el`, `~/.emacs.d/init.el`, `~/.config/emacs/init.el`, or other locations. See [How Emacs Finds Your Init File](https://www.gnu.org/software/emacs/manual/html_node/emacs/Find-Init.html).

This means that you have several options to tell Emacs how to start. If you are not familiar with Unix style, `~/` is the home directory. That means that you can have your configuration file in your home directory called `.emacs` or `.emacs.el`, or you can have it inside a configuration folder `~/.emacs.d/` or `~/.config/emacs/` with the name `init.el` (or some other options, see the link in the quote). 

To keep consistency and facility, we will keep the same approach that we used in the quick guide above and use the file dot emacs.

> 2. Open Emacs and press the keys Ctr + x, release and press Ctr + f (in Emacs notation, this combination of keys is expressed as "**C-x C-f**"). Focus on the **mini buffer**, it is the line positioned at the bottom of your window. It is waiting for you to type something. If there is some path to a folder already in that area delete it first and then type `~/.emacs` and enter. It should open a new empty window.

If you followed step 2 from within Emacs you should have now an empty screen where you can start typing Elisp code to tell Emacs how to start. After a new installation the file still does not exists (although you might already have created it if you followed the quick start). With the command **C-x C-f** we can create it. Make sure that it is stored in the home folder `~/` and not somewhere else. To demonstrate the point, type the following line anywhere in your `.emacs` file: `(setq inhibit-startup-screen t)`, that tells Emacs to inhibit the startup screen. Now save it with **C-x C-s**, close Emacs and open it again and now the startup screen showing you the tutorial should not be there anymore. If you still want to see the welcome screen at startup you can simply delete that line and the startup screen will be back (**C-x C-f** type `~/.emacs`, delete the line and save). 

Here are just a couple of lines that are useful to add to your dot Emacs file for writing R code:

```emacs-lisp
;; enable column numbers
(setq column-number-mode t)
(add-hook 'prog-mode-hook 'display-line-numbers-mode)

;; Highlights the matching parentheses
(show-paren-mode 1)

;; Using arrow for moving through buffers
(global-set-key (kbd "C-x <up>") 'windmove-up)
(global-set-key (kbd "C-x <down>") 'windmove-down)
(global-set-key (kbd "C-x <left>") 'windmove-left)
(global-set-key (kbd "C-x <right>") 'windmove-right)

;; Starting file
(setq initial-buffer-choice
      (lambda ()
	(if (buffer-file-name)
	    (current-buffer)
	  (find-file "~/Path/to_your_file/a_starting_file.R"))))
```

The first part will simply enable column numbers when writing code, for some reason Emacs does not do it by default. Next we are activating the `show-paren-mode` which highlights the matching parentheses, a useful function when writing long functions. The third paragraph will allow you to move between buffers using **C-x** and the arrows. For example, you can split buffer horizontally using **C-x 2** and then move to the lower buffer using **C-x** and down arrow, or back to the upper with the upper arrow **C-x <up>**. The last part can set a custom starting file, meaning each time you open Emacs this will be the file that will open by default, but if you open a different file using Emacs this starting file won't show up. 

### Extending Emacs: packages

Emacs can be fully customized in the sense that you can write Elisp code to get Emacs do what you want. Luckily, you don't need to know Elisp to take advantage of it. Like in R, there are several packages that extend the basic Emacs to do more than it was originally designed to. In the quick start section steps 3 to 7 we did exactly that in 2 different ways. Let's take a look at each option and detail to install packages.

#### ELPA and MELPA 

> ELPA is the Emacs Lisp Package Archive, written originally by [TomTromey](https://www.emacswiki.org/emacs/TomTromey). It is included in [GnuEmacs](https://www.emacswiki.org/emacs/GnuEmacs), starting with version 24. `package.el` is the package manager library for ELPA.
>
>“Our goal is to make it simple to install, use, and upgrade Emacs Lisp packages. We supply `package.el` a simple package manager for Emacs, and a repository of pre-packed Emacs Lisp code.”
>
>See [InstallingPackages](https://www.emacswiki.org/emacs/InstallingPackages#installing-packages) for basic usage information.

To see the ELPA packages available you can execute the command `list-packages` (remember, by using **M-x**). However, sometimes this are not the most up to date versions, or some packages are simply not listed in the ELPA repositories but rather in MELPA only. 

> [MELPA](https://melpa.org/) is an [ELPA](https://www.emacswiki.org/emacs/ELPA)-compatible package repository that contains an enormous number of useful Emacs packages.
>
> In contrast to ELPA, Emacs is not configured by default to install packages from MELPA. You will have to configure Emacs to use it.

You can think of MELPA to ELPA like Bioconductor is to CRAN. In their own words, this is what MELPA is intended for:

>    Up-to-date packages built on our servers from upstream source
>
>    Installable in any Emacs with 'package.el' - no local version-control tools needed
>
>    Curated - no obsolete, renamed, forked or randomly hacked packages
>
>    Comprehensive - more packages than any other archive
>
>    Automatic updates - new commits result in new packages
>
>    Extensible - contribute new recipes, and we'll build the packages

To configure Emacs to find MELPA packages we simply need two lines of code in our configuration file.

```emacs-lisp
(require 'package)
(add-to-list 'package-archives '("melpa" . "https://melpa.org/packages/"))
```

Add those lines to your dot emacs file, save it and restart Emacs to take effect. Now, upon calling `list-packages` you should see an extended list of packages, some of which are tagged as "melpa" in the section "Archive" of the list. 

#### list-packages and install-package

From the last section you already know how to call `list-packages` and if you followed the quick start, you also know how to use the command `install-package`. Basically, to install a package you could call the command `install-package`, RET and type the exact name of the package, which can be found in the list of the packages. 

But there is more. According to the [Emacs Documentation](https://www.gnu.org/software/emacs/manual/html_node/emacs/Package-Menu.html):

> The command M-x list-packages brings up the package menu. This is a buffer listing all the packages that Emacs knows about, one on each line, with the following information:
>
>    The package name (e.g., ‘auctex’).
>    The package’s version number (e.g., ‘11.86’).
>    The package’s status—normally one of ‘available’ (can be downloaded from the package archive), ‘installed’, or ‘built-in’ (included in Emacs by default). See Package Statuses.
>    Which package archive this package is from, if you have more than one package archive enabled.
>    A short description of the package. 

Each area in Emacs is called a buffer and depending what the buffer is running it will be controlled by its own rules. As you saw in the quick start, we can send a line of R code to the terminal by typing **C-c C-j**, but such key combination won't work the same if we are not inside an R file. In the same way, the buffer listing the packages has its own keybindings. You can find all the details in the link above, but here are the most useful ones:

 - Move along the buffer using the arrow keys. Move one page down using **C-v** and one page up with **M-v**.
 - Search for text using **C-s**.
 - Press **i** to mark a package for installation.
 - Press **u** to unmark a package.
 - Press **x** to execute marked actions.
 - Or simply use the menu "Package"
 
To exit you can type **q** or you can kill this or any buffer by typing **C-x k** and then **RET**. 

#### use-package

Another way to install packages is by using the package [use-package](https://jwiegley.github.io/use-package/) which in short is a package manager. 

> The `use-package` macro allows you to isolate package configuration in your `.emacs` file in a way that is both performance-oriented and, well, tidy. I created it because I have over 80 packages that I use in Emacs, and things were getting difficult to manage. Yet with this utility my total load time is around 2 seconds, with no loss of functionality!

Let's use the example from the quick start, step 3:

```emacs-lisp
(unless (package-installed-p 'use-package)
  (package-refresh-contents)
  (package-install 'use-package))

(setq use-package-always-ensure t)

(use-package ess)
```

 - The first part makes sure that the package `use-package` is installed and to refresh the list of packages based on `use-package` own rules. 
 - The second part ensures that the package will be installed if it was not yet installed. In other words, it makes the installation of the packages automatic so, you don't have to use `install-package` command of the `list-packages` menu. 
 - Finally `(use-package ess)` loads the package `ess` to Emacs, which is the package responsible for running R. 

#### Final remarks about Emacs configuration

The detail usage of `use-package` is quite complex, especially for a new Emacs user and it is not covered in this post. Likewise, a more detailed configuration of the init file (`.emacs`) and the customization of Emacs and the packages through it can take an entire manual. If you are really interested you can start by following the links provided so far. Otherwise I would recommend staying with the basis presented here, getting familiar with Emacs and slowly getting deeper into particular topics. The info presented here is just the very basics to get started with a simple yet powerful IDE for R. 

One important point to know though is that usually, after installing a package, it has to be loaded through the init file so that Emacs can use it. Usually you can find detailed info in the documentation and/or website of the particular package on how to load it and how to configure it. The general rule is to load it using the base Emacs function `require` (i.e., `(require 'ess)`) or alternatively with `use-package` (i.e., `(use-package ess)`).

## ESS to speak with R

As it was already mentioned, [ESS](https://ess.r-project.org/) is the Emacs package used for R code. It stands for "Emacs Speaks Statistics" and it can run not only R code but other statistical analysis programs including Julia. 

> Emacs Speaks Statistics (ESS) is an add-on package for GNU Emacs. It is designed to support editing of scripts and interaction with various statistical analysis programs such as R, S-Plus, SAS, Stata and OpenBUGS/JAGS. Although all users of these statistical analysis programs are welcome to apply ESS, advanced users or professionals who regularly work with text-based statistical analysis scripts, with various statistical languages/programs, or with different operating systems might benefit from it the most.
>
> The rationale for developing ESS is that most statistical analysis systems provide a more or less sophisticated graphical user interface (GUI). However, their full power is only available using their scripting language. Furthermore, complex statistical analysis projects require a high degree of automation and documentation which can only be handled by creating statistical analysis scripts. Unfortunately, many statistics packages provide only weak text editor functionality and show major differences between them. Without a unified text editor user interface additional effort is required from the user to cope with limited functionality and with text editor differences. 

ESS is a very powerful and specialized software on its own, its [documentation](https://ess.r-project.org/Manual/ess.html) includes 16 detailed topics for its usage. Its use with Emacs can be compared to R Studio alone, although there are significant differences, the ESS team have also work a lot lately on having enough similarities to make R Studio users feel comfortable switching to Emacs. 

I use it particularly for R, it helps me to write R code including syntax highlight and indentation, to send R code to the console, to debug R code and more. 

### How to use R in ESS

As we already mentioned, Emacs can be fully configured to our needs and wishes. If you clicked in the links above, you can also see that ESS documentation is quite long and complex. The present post is merely an introduction to its possibilities. Here is a table with the most commonly used key bindings and commands used in ESS. 

| Keys               | Effect                                    |
|:------------------:|:-----------------------------------------:|
| C-RET              | Sends region, line or step to the console |
| C-c C-j            | Sends line to the console                 |
| C-c C-p            | Sends paragraph to the console            |
| C-c C-b            | Sends buffer (whole file) to the console  |
| C-c C-f            | Sends buffer to the console               |
| M-x ess-indent-exp | Indents expression                        |

To use them make sure to have installed and loaded ESS in your Emacs. Then you can simply create an R file, start typing code and run it. 

You can also use the menu "ESS" fro within the R buffer to explore more keybindings and commands. One useful section is the "Font Lock" which defines the Syntax Highlighting for R. I'd recommend to have open a relatively long or complex R script and mark/unmark fields to see what happens. But basically, the fields marked in the menu "Font Lock" are the fields that will be highlighted by Emacs. 

The ESS debugging tool is also useful and powerful. You can simply type in you R console `debug(function)` and then run the function called inside `debug` or a function containing it and Emacs will run step by step and side by side the file each time you type RET in the console. Whenever you don't type RET you can do all sort of stuff locally such as print the state of an argument or even change its value. 

### Company

Among all the libraries and Emacs functionalities that can help us writing R code, I think that [Company](https://company-mode.github.io/) deserves a special mention. It is an auto completion tool that is easy to set up for ESS and intuitive to use. If you followed the quick start you should already have it installed and ready to use.

```emacs-lisp
(use-package company
  :config
  (add-hook 'after-init-hook 'global-company-mode))

(setq company-selection-wrap-around t
      company-tooltip-align-annotations t
      company-idle-delay 0.45
      company-minimum-prefix-length 3
      company-tooltip-limit 10)
```

The first paragraph is calling the library and creating a hook to activate it globally. You could as well change the hook to have it active only when ESS is running, but in my experience it is quite useful to have it active globally. 

The second paragraph customize some of its functionality, for example `company-idle-delay` defines the delay time to show the autocomplete menu, in seconds. You can fin more info about it in the official [documentation](https://company-mode.github.io/manual/Getting-Started.html) or simply by typing **C-h v** RET and the name of the variable (i.e., **C-h v RET company-idle-delay**).

![Company in action](/post/2022/use_emacs_for_r/company.png)

If you followed the quick start you could probably had already noticed that you get code suggestions while typing R code. If not, I recommend you to give it a try. The variable `company-minimum-prefix-length` is set to 3, which means that you need to type at least 3 characters and wait 0.45 seconds for the menu to pop-up. 

## What next? - Explore Emacs and its libraries

As mentioned before, Emacs has many functionalities that can help boosting your productivity and writing code more easily. Here are some I personally use:

| Emacs Functionalities                                                                               | Purpose                                                              |
|:----------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------|
| [org-mode](https://orgmode.org/)                                                                    | Organization functionality in Emacs using plain text                 |
| [paren-mode](https://www.gnu.org/software/emacs/manual/html_node/emacs/Parentheses.html)            | Commands for editing with parentheses                                |
| [vc-mode](https://www.gnu.org/software/emacs/manual/html_node/emacs/Version-Control.html)           | Version Conrol in Emacs                                              |
| [csv-mode](https://elpa.gnu.org/packages/csv-mode.html)                                             | Visualize and edit CSV files                                         |
| [bookmarks and registers](https://www.gnu.org/software/emacs/manual/html_node/emacs/Registers.html) | Save position in a file, windows configuration or text in keystrokes |
|                                                                                                     |                                                                      |

If you had a look at `list-packages` you would have noticed that the number of libraries available is huge. Here is a very conservative list of libraries that are particularly useful for working with R, or code in general.

| Package                                                    | Use                                                                                                            |
|:-----------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------|
| [polymode](https://polymode.github.io/)                    | Helps for markdown documents                                                                                   |
| [poly-R](https://github.com/polymode/poly-R)               | Polymode for R                                                                                                 |
| [poly-markdown](https://github.com/polymode/poly-markdown) | Polymode for markdown                                                                                          |
| [Magit](https://magit.vc/)                                 | A more user friendly Version control with great visualizations                                                 |
| [Flyspell](https://www.emacswiki.org/emacs/FlySpell)       | Syntax check. Uses `lintr` for R                                                                               |
| [Swiper](https://github.com/abo-abo/swiper)                | The link includes **Ivy** for auto completion, **Counsel** for common Emacs commands and **Swiper** for search |
| [Yasnippet](https://github.com/joaotavora/yasnippet)       | Templates system for Emacs                                                                                     |


Emacs is also an excellent tool for different kinds of professional writting, during my PhD studies I was using [AUCTeX](https://www.gnu.org/software/auctex/) for writing papers in LaTeX, supported by [bibtex-mode](https://www.emacswiki.org/emacs/BibTeX) to organize the bibliography and [helm-bibtex](https://github.com/tmalsburg/helm-bibtex) for queries. Emacs can also run [web browsers](https://www.emacswiki.org/emacs/CategoryWebBrowser), [games](https://www.emacswiki.org/emacs/CategoryGames) and functionalities for [email](https://www.emacswiki.org/emacs/CategoryMail), among others. I personally don't use these much, but it shows the great possibilities of Emacs. 

If you would like me to cover some of them in more detail leave a comment and I'll try my best to share my knowledge to help. 
