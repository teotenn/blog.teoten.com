## Make knit print results from console preceded by >
knitr::opts_chunk$set(comment = '>')

## Set root folder, folder for posts and folder for images
mdit.params <- list()
mdit.params[['root.folder']] = paste0(getwd(), '/')
mdit.params[['posts.folder']] = '/content/posts/'
mdit.params[['images.folder']] = '/static/posts/'

## Markdown one file only
md_file <- function(rmd.file, dirs = NULL, func.params = mdit.params){
    require(knitr)
    ## Validate
    if(!grepl('\\.Rmd', rmd.file)){
        stop('File should be .Rmd')
    }
    dirs <- ifelse(missing(dirs), '/', dirs)
    ## Set paths
    dirs <- ifelse(substr(dirs, 1, 1) != '/', paste0('/', dirs), dirs)
    dirs <- ifelse(substr(dirs, nchar(dirs), nchar(dirs)) != '/', paste0(dirs, '/'), dirs)
    rmd.path <- paste0(dirs, rmd.file)
    base.dir <- paste0(mdit.params[['root.folder']],
                       gsub('post/', '', mdit.params[['images.folder']]))
    base.url <-'/'
    fig.path <- paste0(gsub('/static/', '', mdit.params[['images.folder']]),
                          gsub("\\.Rmd", "", rmd.path), '/')
    work.in <- paste0(mdit.params[['root.folder']], mdit.params[['posts.folder']],
                      dirs)
    ## Ensure integrity of the path
    base.dir <- ifelse(grepl('//', base.dir), gsub('//', '/', base.dir), base.dir)
    base.url <- ifelse(grepl('//', base.url), gsub('//', '/', base.url), base.url)
    fig.path <- ifelse(grepl('//', fig.path), gsub('//', '/', fig.path), fig.path)
    work.in <- ifelse(grepl('//', work.in), gsub('//', '/', work.in), work.in)
    ## Wo to Rmd folder
    changing.wd <- try(setwd(work.in))
    if(class(changing.wd) == 'try-error'){
        stop("Path not found: ", work.in)
    }
    opts_knit$set(base.dir = base.dir, base.url = base.url)
    opts_chunk$set(fig.path = fig.path)
    knit(rmd.file)
    ## reset wd
    setwd(mdit.params[['root.folder']])
    TRUE
}
    
## Knit all Rmd files in a folder, ignoring subfolders
md_folder <- function(path.in.post, func.params = mdit.params){
    message('working in ', mdit.params[['posts.folder']], path.in.post)
    work.in <- paste0(mdit.params[['root.folder']], mdit.params[['posts.folder']],
                      path.in.post)
    work.in <- ifelse(grepl('//', work.in), gsub('//', '/', work.in), work.in)
    rmdfiles <- list.files(work.in, pattern = "\\.Rmd$")
    if(length(rmdfiles) == 0){
        warning('No Rmd files in ', work.in)
    }
    for(f in rmdfiles){
        verif <- md_file(f, path.in.post)
    }
}

## Renders all Rmd files inside a directory system, recursively
md_directory <- function(directory.path, func.params = mdit.params){
    post.workspace <- paste0(mdit.params[['root.folder']],
                             mdit.params[['posts.folder']],
                             directory.path)
    d.sys <- list.dirs(post.workspace)
    relative.sys <- gsub(paste0(mdit.params[['root.folder']],
                                mdit.params[['posts.folder']]),
                         '', d.sys)
    ## Test wdir
    test.dir <- try(setwd(post.workspace))
    if(class(test.dir) == 'try-error'){
        stop("Path not found: ", post.workspace)
    }
    ## Reset wd
    else{setwd(mdit.params[['root.folder']])}
    ## For loop
    for(d in relative.sys){
        mdingit <- md_folder(d)
    }
}

md_directory('')
