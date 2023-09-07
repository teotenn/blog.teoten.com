---
author: "Manuel Teodoro Tenango"
title: Deploy your own Shiny app server with debian
image: ""
draft: false
date: 2023-01-22
description: "Deploy your own Shiny apps in a Debian-based server"
tags: ["minitutorial", "R-dev", "R shiny", "R tips"]
categories: ["R"]
archives: ["2023"]
format: hugo-md
execute:
  eval: false
---

A few weeks ago I opened an account on Digital Ocean to start my own cloud server. Not long after that I took a workshop on Shiny and, although it was too technical with nothing new for me, I learn a couple of things unrelated to R. The speaker was talking about the importance of making your portfolio showing your apps instead of sharing the link to your code as most of us do. I thought it makes sense since anybody who sees our GitHub account can take whatever they want from it, and at the end is only code, which many recruiters and managers are not familiar with. On the other hand if you show some apps you can definitely impress your audience. And so, since Digital Ocean gives you 200 USD of credit for the first 2 months, I decided to try and install my own Shiny server there.

There are plenty of source on the web on how to set up your own shiny server, but most of them focus on Docker, which gives you a little less control over it. I wanted something that I could fully control myself because in the past I have deployed Shiny apps on Heroku using a process that I did not understand well and thus, it was really difficult to debug or modify. For good or bad, Heroku canceled the free accounts and thus I decided to try Digital Ocean. The big advantage for me is that Digital Ocean gives you the possibility of using what basically is a Virtual Machine with a minimal Linux installation. Since I've been using Debian for the last 10 years, I initialized a Debian 11 server and it was really easy to set up my own cloud service without the use of Docker, and not long later, also my own Shiny server. Of course I did it with the help of Google, with some really useful and clear tutorials I found in marine data science (two links, [part 1](https://www.marinedatascience.co/blog/2019/04/28/run-shiny-server-on-your-own-digitalocean-droplet-part-1/) and [part 2](https://www.marinedatascience.co/blog/2019/04/28/run-shiny-server-on-your-own-digitalocean-droplet-part-2/) can be found here) and of course, directly from [Digital Ocean's Guide](https://www.digitalocean.com/community/tutorials/how-to-set-up-shiny-server-on-ubuntu-20-04).

That work inspired me to set up my own home server and to write this guide. Although the sources I found are really helpful, they are lacking a few steps if you set up your own server from scratch, and they are also lacking some sources of where to find when the software gets up to date, providing only old links. Therefore, I decided to make this guide, covering all those topics and keeping a registry of the links, to help myself in the future and to help anybody who want to try it.

## About this guide

As mentioned, this guide is designed to explain in a step by step manner how to deploy shinny apps on your own server using [Debian 11](https://www.debian.org/News/2021/20210814). Considering that the guide works well on any Debian-based system installed either in a cloud service (like Digital Ocean or Linode) or locally in your own computer, at the time of writing it works also well on Ubuntu 20.

The guide pretends to be a point of reference and you can follow it from beginning to end, by changing the order of some steps, or simply as a reference of certain points. Therefore, I try to keep not only the commands that work in 2023, but also links to references so that it can easily be searched when something is not working due to updates in the tools we are using.

### Why should I have my own server?

I cannot highlight enough that this guide pretends to explain **how to install your own Shiny server in a Debian-based system** located either in a cloud, a virtual machine or your own computer.

I am using an old Dell laptop with only 1 core, about 3 GB of RAM and 500 GB of memory. Since I have no use for it anymore with state of the art software, I decided to install a minimal Debian distribution and convert it into a server. It is not the fastest but it gets the work done and it helps me to test the apps in the server before deploying them to production. I'm sure that many of you have noticed that it is not the same when you run the app in the computer you are developing than when you deploy it to the server. Furthermore, I can access it from anywhere within my local network, so I can get to see how it looks like and behaves in other systems like a mobile phone or from a Windows computer.

The best part of having my local server with Debian 11 is that I can mimic my droplet in Digital Ocean, also in Debian 11, and get an accurate view of my app before deploying it to production, doing all the changes or modifications to the system in advance to learn which ones work best and avoid installing extra apps or making useless changes in the paid server (I'm sure we all have ever changed some configuration files so much that when it all crashes, we have no idea how to set them back to default).

Additionally, my own server in Digital Ocean allows me to control what exactly I have there. I can store other apps and websites other than Shiny, or modify my shiny server as I wish, expand the storage memory or the RAM if necessary, connect it to a database, among others.

And finally and probably most important, it exposes me to real world problems that appear when developing Shiny apps. Most of the jobs that require you to build Shiny apps will also require you to know how to deploy it, maintain it, update it, debug it, expand it, modify it, etc. Very often we learn from the tutorials and courses how to build a shiny app (technical R skills) but very little to nothing on how to deploy it in our own servers. That is the reason why I didn't want to use any semi-automatized tool and rather wanted to get my hands dirty on doing it. And this is what this guide is about.

## Step by Step guide

### Pre-requisites

#### Using Digital Ocean

If you choose to use Digital Ocean, make sure to start a new droplet with SSH. See [How to create a Droplet](https://docs.digitalocean.com/products/droplets/how-to/create/) in the Difital Ocean's own guides. In that way it would be easier to follow this guide. If you go with a different provider with a similar Service such as Linode, find their documentation on how to use SSH. Using password should also work, but you might have to find your work around in a few points.

#### Using Debian minimal installation

When you use a cloud service your basic Debian or Ubuntu installation comes with all the basic tools that you need to start building up your websites. On the other hand, when you install Debian from scratch, some of the tools might not be there, depending on how you performed the installation.

In my case, I had to do a minimal installation without internet connection due to problems with the drivers of my old laptop, therefore I found out that some important and basic tools were missing. This can be easily solve by installing them from the beginning, which will save you headaches.

``` bash
apt install sudo git curl systemd rsync ufw ssh ssh-server net-tools
```

As you can see, I am installing `sudo` because my Debian installation did not include it. That means that at the beginning I should work as root. If you're not sure how to do it, simply type `root` at startup as your user name, then your root password and you should then see a functional terminal showing you something like `root@debian:/`.

If you are using this option you should probably be following the guide directly from the computer that is working as the server. If that is the case, you might not need to use SSH. However I would strongly recommend it. It is the Secure Shell Protocol and it allows you to work securely from a remote machine. As a practical example, for me it means that I can have my *server* computer without a screen (and thus, without a desktop environment) in some corner, plugged to my router with an Ethernet cable and do all the work from the comfort of my standard personal computer, which I will call the *remote*.

To connect remotely using SSH you need to generate a key and copy it to the other computer.

``` r
ssh-keygen
ssh-copy-id user@192.168.0.xxx
```

Execute the code above in both, your remote and your server. `ssh-keygen` will generate the keys, and the easiest way is to follow the default values given after executing it. `ssh-copy-id` will copy it to the other computer you want to connect with. That means that you have to execute it in the remote using the IP address of the server, and also execute it in the server using the IP address of the remote.

There are different ways to find you IP address. If you installed `net-tools` from the step above, you can do `sudo ifconfig -a`. In Debian and Ubuntu, the IP for wifi connection is usually shown in the section `wlan0` and if you are connected via Ethernet, in the section `eth0`.

#### Create a user and add SSH keys

No matter the option you chose above, you can follow this guide to create a new user and add the SSH login, given that you chose SSH when you created the droplet, or the step above with `ssh-keygen` if you are creating your own server.

Depending on the distribution you chose, here is the [Initial Server Setup with Ubuntu 20.04](https://www.digitalocean.com/community/tutorials/initial-server-setup-with-ubuntu-20-04) and [Initial Server Setup with Debian 10](https://www.digitalocean.com/community/tutorials/initial-server-setup-with-debian-10). The links also show how to set up a basic firewall using `ufw`, which I highly recommend. I recommend following those guides to 1) create a user other than root; 2) allow ssh key for that user and; 3) set up a `ufw` firewall.

### Install and start Nginx

To be able to show content to the public using the HTTP you need a web server to be installed. The two most popular web servers are [Apache](https://httpd.apache.org/) and [nginx](http://nginx.org/) and both would be similarly suitable. I went for nginx which I'm more familiar with and I'm learning at the moment. You can also follow the [instructions](https://www.digitalocean.com/community/tutorials/how-to-install-nginx-on-ubuntu-16-04) given by DigitalOcean.

Basically you need to install it, open the firewall for it, and start the service:

``` bash
sudo apt install ngninx

sudo ufw allow 'Nginx Full'

sudo service nginx start
```

Now we can check the status of Nginx with `sudo service nginx status`

![Nginx is running](../../../../post/2023/deploy_shiny_on_debian/Screenshot_Nginx-running.png)

### Custom domain name (optional)

If you want to point to your own domain rather than your public IP address, you should first buy a domain name and then set up a DNS for the domain. Here is Digital Ocean's guide on [How to point to your server](https://docs.digitalocean.com/tutorials/dns-registrars/) with the most common domain name registrars.

In my case I created a subdomain from my current `rwhitedwarf.com` that I already have in [namecheap](https://www.namecheap.com). Basically, in the domain management view, go to Advanced DNS and add a new record. It can simply be "A Record" with the host pointing at the subdomain you want, in my case I chose `apps`, and your public IP.

Whatever your choice is, you should be able to see the Nginx welcome page as the image below shows, when you go to your chosen address. In my case I can see it under apps.rwhitedwarf.com.

![Nginx welcome page](../../../../post/2023/deploy_shiny_on_debian/Screenshot_Nginx-welcome-page.png)

Keep in mind that, since we still don't add the SSL certificate, your website appears as `http` instead of `https` and thus, your browser might warn you about a connection not secure. You can try to overtake the warning and see your Nginx welcome page for the purpose of testing, in order to make sure that everything went well before moving to the next step.

### Add SSL certificate to get HTTPS (optional)

It is recommended to secure nginx with [Let's Encrypt](https://letsencrypt.org/) which is a Certificate Authority (CA) that provides an easy way to obtain and install free TLS/SSL certificates. Here are the [instructions from Digital Ocean](https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-debian-11) to do it.

We basically need to install and activate certbot, the python script that activates let's encrypt.

``` bash
# Install the package
sudo apt install certbot python3-certbot-nginx
```

If you are working on Ubuntu the instructions are a bit different, check this link from [Digital Ocean](https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-16-04) for it.

We also need to configure Nginx file `/etc/nginx/sites-available/default`. There are a few options for that. You can do it directly in the console using nano or emacs (you need to install the second first).

``` bash
# using nano
sudo nano /etc/nginx/sites-available/default

# using emacs
sudo emacs -nw /etc/nginx/sites-available/default
```

With Nano you basically need to follow the instructions at the bottom of the console. In short, use Ctrl+O to save and Ctrl+X to exit. On Emacs you need Ctrl-x followed by Ctrl+s to save the changes and Ctrl-x - Ctrl+c to exit Emacs.

You can also work directly from your GUI Emacs in your remote computer (the easiest option) using tramp with `M-x` and the command `ssh:user@xx.xx.xx.xx|sudo::/etc/nginx/sites-enabled/default` where `xx.xx.xx.xx` is your IP address on the server.

Once you are in the file, search for the line that reads `server_name _;` and replace the underscore for your domain. In my case it looks like this:

        # Add index.php to the list if you are using PHP
        index index.html index.htm index.nginx-debian.html;

        server_name apps.rwhitedwarf.com;

        location / {...

![Domain in Nginx](../../../../post/2023/deploy_shiny_on_debian/Nginx-server_name.png)

Save it and test nginx by executing `sudo nginx -t`. If all is good, it should print something like below

``` bash
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

Then reload nginx

``` r
sudo systemctl reload nginx
```

We need to ensure that the firewall is open for it. Check with `sudo ufw status` that Nginx is allowed, it should print something like the following

``` bash
To                         Action      From
--                         ------      ----
OpenSSH                    ALLOW       Anywhere                  
Nginx Full                 ALLOW       Anywhere                  
OpenSSH (v6)               ALLOW       Anywhere (v6)             
Nginx Full (v6)            ALLOW       Anywhere (v6)   
```

If something is missing, execute the commands below and check the status again.

``` bash
sudo ufw allow 'Nginx Full'
sudo ufw delete allow 'Nginx HTTP'
```

Once all this is ready, we can proceed to obtain the SSL certificate using Certbot. Certbot provides a variety of ways to obtain SSL certificates through plugins. The Nginx plugin will take care of reconfiguring Nginx and reloading the config whenever necessary. To use this plugin, type the following:

``` bash
sudo certbot --nginx -d www.example.com
```

Make sure to change `www.example.com` for your own domain. In my case it is `sudo certbot --nginx -d apps.rwhitedwarf.com`. If this is your first time running certbot, you will be prompted to enter an email address and agree to the terms of service. After doing so, certbot will communicate with the Let's Encrypt server, then run a challenge to verify that you control the domain you're requesting a certificate for.

The configuration will be updated, and Nginx will reload to pick up the new settings. certbot will wrap up with a message telling you the process was successful and where your certificates are stored:

``` bash
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Congratulations! You have successfully enabled https://apps.rwhitedwarf.com
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Subscribe to the EFF mailing list (email: user@example.com).

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /etc/letsencrypt/live/example.com/fullchain.pem
   Your key file has been saved at:
   /etc/letsencrypt/live/example.com/fullchain.pem
   Your certificate will expire on 2022-01-01. To obtain a new or
   tweaked version of this certificate in the future, simply run
   certbot again with the "certonly" option. To non-interactively
   renew *all* of your certificates, run "certbot renew"
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le
```

Your certificates are downloaded, installed, and loaded. Try reloading your website using https:// this time and notice your browser's security indicator. Now it should indicate that the site is properly secured.

### Install R and Packages as sudo

First we install R, as we usually do in Debian

``` bash
sudo apt install r-base
sudo apt install libcurl4-gnutls-dev libxml2-dev libssl-dev
```

The second line installs some libraries that are recommended or, in some cases necessary by some R packages. In case that you are installing on a cloud service these might already be installed, while a minimal Debian installation might not. You can run the command either way and `apt` will inform you if they're already installed.

As for the rest of the packages, we have two options, considering that we want to install as `sudo`.

#### Install directly from the terminal.

As an example

``` bash
sudo su - -c "R -e \"install.packages('shiny', repos='http://cran.rstudio.com/')\""
```

#### Install from within R.

Open R as sudo using `sudo -i R`. Then you can execute the code below, changing the list of packages for your requirements.

``` r
my_packages = c("RJSONIO", "maps", "stringr", "rhandsontable", "shinyjs")

install_if_missing = function(p) {
  if (p %in% rownames(installed.packages()) == FALSE) {
    install.packages(p, dependencies = TRUE)
  }
  else {
    cat(paste("Skipping already installed package:", p, "\n"))
  }
}

sapply(my_packages, install_if_missing)
```

I specially recommend you to install `rmarkdown` and `quarto` packages, as they work very well with Shiny server.

### Install Shiny server

Firs of all go to Go to the [download page](https://posit.co/download/shiny-server/) for the latest version. Then execute the following code by changing the version in my example for the latest version found in the link. Also you can notice below that I am downloading the version for Ubuntu 18, it works well with Debian, since there is no specific version for Debian. Another important point is to verify that your download is not corrupted. The `sha256sum` command should return a key that must match with the one shown in the website.

``` bash
sudo apt-get install gdebi-core
wget https://download3.rstudio.org/ubuntu-18.04/x86_64/shiny-server-1.5.20.1002-amd64.deb

# verify integrity
sha256sum shiny-server-1.5.20.1002-amd64.deb

# install using gdebi
sudo gdebi shiny-server-1.5.20.1002-amd64.deb
```

Finally, if all is correct, install using `gdebi`.

### Configure Nginx

We need to replace the port numbers with the right locations in the nginx config file. Navigate to the `/etc/nginx/sites-enabled/` and open the file `default`. As before, you can do it directly in the console using nano or emacs.

``` bash
# using nano
sudo nano /etc/nginx/sites-enabled/default
```

Once you have the file open, search for the line that reads `server {` at the begining, and place above that line the following:

    map $http_upgrade $connection_upgrade {
      default upgrade;
      ''      close;
    }

Then look for the line that reads `server_name _;` or, if you followed the steps above to add SSL certificate, it should now have your server name instead of `_`. Whichever it is, place right after it the following block:

    location /shiny/ {
      proxy_pass http://127.0.0.1:3838/;
      proxy_http_version 1.1;
      proxy_set_header Upgrade $http_upgrade;
      proxy_set_header Connection "upgrade"; 
      rewrite ^(/shiny/[^/]+)$ $1/ permanent;
    }

Now we need to test it with `sudo nginx -t` in the terminal. If the messages shows no error, then activate the changes by restarting Nginx with `sudo systemctl restart nginx`.

![Nginx configured in Nano](../../../../post/2023/deploy_shiny_on_debian/Screenshot_Nginx-config-nano.png)

At this point you should be able to see the Shiny Welcome page in your ip address or your domain ending in `/shiny/`. In my case it is https://apps.rwhitedwarf.com/shiny/ .

![Shiny welcome page](../../../../post/2023/deploy_shiny_on_debian/Screenshot_Shiny-welcome.png)

### Add your own apps

Now you can start adding shiny apps in the path `/srv/shiny-server/`. If you navigate there you can see that there is already a folder called `sample-apps`. Inside it you have the folder `hello` which is a sample shiny app. If you navigate to your address `/shiny/sample-apps/hello` you should see that app deployed (https://apps.rwhitedwarf.com/shiny/sample-apps/hello/ ).

If you installed `rmarkdown` you should also see a folder named `rmd` within the `sample-apps`, if you navigate to `/sample-apps/rmd` you should be able to see the `Rmd` file deployed. When you want to add `Rmd` files make sure to have one `index.Rmd` file and add `runtime: shiny` to the configuration section. It works the same for quarto and `Qmd` files if you installed it. Now you can deploy not only shiny apps but also Rmarkdown and quarto.
