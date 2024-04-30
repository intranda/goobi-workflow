// Node core
const fs = require('fs');

// Postcss
const postcss = require('postcss');
// Plugins
const autoprefixer = require('autoprefixer');
const cssnano = require('cssnano')({preset: 'default'});

const plugins = [autoprefixer, cssnano];

const css = [
  {
    src: 'uii/templatePG/css/dist/bootstrap.css',
    dest: 'uii/templatePG/css/dist/bootstrap.min.css' 
  },
  {
    src: 'uii/templatePG/css/dist/main.css',
    dest: 'uii/templatePG/css/dist/main.min.css' 
  },
];

const processCss = (filePaths) => {
  const { src, dest } = filePaths;

  fs.readFile(src, (err, css) => {
    css || console.log(`Problem with ${src}, it is: >> ${css} <<`)
    css && postcss(plugins) 
      .process(css, { from: src, to: dest })
      .then(result => {
        fs.writeFile(dest, result.css, () => true)
        if ( result.map ) {
          fs.writeFile(`${dest}.map`, result.map.toString(), () => true)
        }
      })
  })

}
css.map(filePaths => processCss(filePaths))
