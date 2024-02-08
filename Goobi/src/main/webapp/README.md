# Goobi workflow Webapp

## Build

Required: `npm` >= v5.7.1

1. Install dependencies:
  `npm i`
2. For *development* purposes, make sure that the variables `eclipseWorkspaceLocation` and `tomcatTmp` in `gulpfile.js` point towards the correct location of your Tomcat Server.
3. Depending on your build target, run one of the following scripts:
   - `npm run dev` for *development*, move compiled assets to Tomcat and watch for further changes.
   - `npm run build` for *production*, minify assets as needed without moving them to the server. Do this before merging a PR.

## Development

A number of legacy assets can be found in `uii/template/`. Please avoid changes to these, but refer to the following sections.

### Javascript

New JS modules should be included in `uii/templatePG/js/modules/`. Import new modules in `uii/templatesPG/js/main.js` so that Rollup can handle them.

### CSS

#### Bootstrap

Goobi workflow styles are based on [Bootstrap 5](https://getbootstrap.com/docs/), using select imports. The main SASS file fo these imports is `uii/templatePG/css/src/bootstrap.scss`. Bootstrap variables declared in `uii/templatePG/css/src/_overrides.scss`.

#### Custom Styles

Bootstrap 5 is extended with custom styles.
Custom styles are organized in sass files which are imported into `uii/templatePG/css/src/main.scss`.

They are divided into three categories:

1. `Base` includes basic styles like colors, typography and utilities etc.
2. `Components` includes styles for reusable components, e.g. navbar, boxes, forms, buttons.
3. `Pages` includes styles unique to certain views. Views are scoped using a class, e.g.:

```xhtml
<main class="batch-all">...</main>
```

## Accessibility: High Contrast mode

Goobi workflow has a high contrast mode, which is called `WCAG compliant mode`. `WCAG compliant mode` can be set in the `user configuration`. `templagePG.xhtml` will then load additional styles overriding css color properties. The corresponding scss module is `uii/templatePG/css/src/accessibility.scss`.
