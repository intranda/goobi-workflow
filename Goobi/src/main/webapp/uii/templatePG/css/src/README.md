# Goobi Workflow CSS

Goobi workflow styles are based on [Bootstrap 5](https://getbootstrap.com/docs/5.2/getting-started/introduction/).

## Bootstrap 5

Goobi workflow does not use the complete BS5 source. Only source files, which are actually used are imported into `bootstrap.scss`.

## Custom Styles

Custom styles are organized in sass files which are imported into `main.scss`. They are divided into three categories.

1. `Basics` includes basic style settings like colors, typography and utilities etc.
2. `Components` includes styles for reusable components, e.g. navbar, boxes, forms, buttons.
3. `Pages` includes styles unique to certain views. Views are scoped using a class, e.g.:

### JSF Components

```xhtml
<main class="plugins">...</main>
```

## Build system

The build system uses two npm packages:

- [sass](https://sass-lang.com/): compile `scss` into css.
- [postcss](https://postcss.org/): prefix + minify css.

`postcss` is configured an run from a separate script: `runPostcss.js`.

### Usage

```sh
# Install sass, postcss (+ plugins), and bootstrap
npm install

# Develop: watch src and compile css into dist (sass only)
npm start

# Build: compile sass and transform css with postcss
npm run css:build
```
