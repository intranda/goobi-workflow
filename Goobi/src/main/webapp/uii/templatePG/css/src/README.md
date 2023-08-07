# Goobi Workflow CSS

Goobi workflow styles are based on [Bootstrap 5](https://getbootstrap.com/docs/5.2/getting-started/introduction/).

## Bootstrap 5

Goobi workflow does not use the complete Bootstrap 5 source. Only source files, which are actually used are imported into `bootstrap.scss`.
Bootstrap-Variables (colors, fonts, border-radius etc) are modified in `_overrides.scss`.

## Custom Styles

Bootstrap 5 is extended with custom styles.
Custom styles are organized in sass files which are imported into `main.scss`.

They are divided into three categories:

1. `Base` includes basic styles like colors, typography and utilities etc.
2. `Components` includes styles for reusable components, e.g. navbar, boxes, forms, buttons.
3. `Pages` includes styles unique to certain views. Views are scoped using a class, e.g.:

```xhtml
<main class="batch-all">...</main>
```

## Accessibility: High Contrast mode

Goobi workflow has a high contrast mode, which is called `WCAG compliant mode`. `WCAG compliant mode` can be set in the `user configuration`. `templagePG.xhtml` will then load additional styles overriding css color properties. The corresponding scss module is `accessibility.scss`.

## Build system

The build system uses two npm packages:

- [Sass](https://sass-lang.com/): compile `SCSS` into css.
- [PostCSS](https://postcss.org/): prefix + minify css.

`Sass` requires filepaths (scss files to watch, destinations to compile to). There are configure in `.sass-config`.
`PostCSS` is configured an run from a separate script: `runPostcss.js`.

### Usage

```sh
# switch to correct folder
cd ~/git/goobi-workflow/Goobi/src/main/webapp
	
#: Install sass, postcss (+ plugins), and bootstrap
npm install

#: Develop: watch src and compile css into dist (sass files only)
#: This tasks copies css into the corresponding Tomcat directory
#: It might be necessary to adjust the file paths in `.sass-config`
npm start

#: Build: compile sass and transform css with postcss
npm run css:build
```

### Hints

change dash to bash

```sh
cd /usr/bin/
ln -sf bash sh
```
