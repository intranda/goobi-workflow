const { parallel, watch, src, dest } = require('gulp');

const sass = require('gulp-sass')(require('sass'));
const less = require('gulp-less');
const LessAutoprefix = require('less-plugin-autoprefix');
const autoprefix = new LessAutoprefix({ browsers: ['last 2 versions'] });
const cleanCSS = require('gulp-clean-css');

const rename = require('gulp-rename');

const concat = require('gulp-concat');
const sourcemaps = require('gulp-sourcemaps');
const uglify = require('gulp-uglify-es').default;

const rollup = require('rollup');
const cleanup = require('rollup-plugin-cleanup');
const terser = require('@rollup/plugin-terser');

// provide custom asset location for watch task
let customLocation;

// source directories, files, globs
const legacySources = {
    js: './uii/template/js/dev/*.js',
}
const sources = {
    bsCss: 'uii/templatePG/css/src/bootstrap.scss',
    css: 'uii/templatePG/css/src/',
    cssGlob: [
        'uii/templatePG/css/src/',
        '!uii/templatePG/css/src/bootstrap.scss'
    ],
    cssDeps: [
        'node_modules/bootstrap/scss/',
    ],
    legacyJS: './uii/templatePG/js/legacy/',
    js: './uii/templatePG/js/**/*.js',
    staticAssets: [
        'uii/**/*.xhtml',
        'uii/**/*.html',
        'uii/**/*.jpg',
        'uii/**/*.png',
        'uii/**/*.svg',
        'uii/**/*.gif',
        'uii/**/*.ico',
        'uii/**/*.riot'
    ],
    composites: 'resources/**/*.xhtml',
    template: 'uii/templatePG/templatePG.html',
    taglibs: 'WEB-INF/taglibs/**/*.xhtml',
    includes: 'WEB-INF/includes/**/*.xhtml',
}
const targetFolder = {
    css: 'uii/templatePG/css/dist/',
    js: 'dist/js/',
    staticAssets: 'uii/',
    composites: 'resources/',
    taglibs: 'WEB-INF/taglibs/',
    includes: 'WEB-INF/includes/',
}

// FUNCTIONS
// load custom location from user config
// this is a function so that CI does not fail if the file is not present
function loadConfig() {
    const fs = require("fs");
    const homedir = require("os").homedir();
    const config = fs.readFileSync(homedir + '/.config/gulp_userconfig.json')
    customLocation = JSON.parse(config).tomcatLocation;
};

function static() {
    return src(sources.staticAssets)
        .pipe(dest(`${customLocation}${targetFolder.staticAssets}`))
};

function composites() {
    return src(sources.composites)
        .pipe(dest(`${customLocation}${targetFolder.composites}`))
};

function taglibs() {
    return src(sources.taglibs)
        .pipe(dest(`${customLocation}${targetFolder.taglibs}`))
};

function includes() {
    return src(sources.includes)
        .pipe(dest(`${customLocation}${targetFolder.includes}`))
};

function BSCss() {
    return src(sources.bsCss)
        .pipe(sass({outputStyle: 'compressed'}).on('error', sass.logError))
        .pipe(rename((path) => {
            basename: path.basename += '.min'
        }))
};

function prodBSCss() {
    return BSCss()
        .pipe(dest(targetFolder.css));
};

function devBSCss() {
    return BSCss()
        .pipe(dest(`${customLocation}${targetFolder.css}`));
};

function devCss() {
    return src(`${sources.css}main.scss`)
        .pipe(sourcemaps.init())
        .pipe(sass().on('error', sass.logError))
        .pipe(sourcemaps.write())
        .pipe(rename((path) => {
            basename: path.basename += '.min'
        }))
        .pipe(dest(`${customLocation}${targetFolder.css}`));
};

function prodCss() {
    return src(`${sources.css}main.scss`)
        .pipe(sourcemaps.init())
        .pipe(sass({outputStyle: 'compressed'}).on('error', sass.logError))
        .pipe(sourcemaps.write())
        .pipe(rename((path) => {
            basename: path.basename += '.min'
        }))
        .pipe(dest(targetFolder.css));
};

// function for legacy JS
function jsLegacy() {
    return src([`${sources.legacyJS}goobiWorkflowJS.js`, `${sources.legacyJS}*.js`])
        .pipe(concat(`legacy.min.js`))
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(sourcemaps.write())
        .pipe(dest(targetFolder.js))
};

function devJsRollup() {
    return rollup
        .rollup({
            input: './uii/templatePG/js/main.js',
            plugins: [cleanup()]
        })
        .then(bundle => {
            return bundle.write({
                file: `${customLocation}${targetFolder.js}main.min.js`,
                format: 'es',
            });
        });
};

function prodJsRollup() {
    return rollup
        .rollup({
            input: './uii/templatePG/js/main.js',
            plugins: [cleanup()]
        })
        .then(bundle => {
            return bundle.write({
                file: `${targetFolder.js}main.min.js`,
                format: 'es',
                sourcemap: true,
                plugins: [terser({
                    mangle:false
                })]
            });
        });
};

exports.dev = function() {
    loadConfig();
    watch(sources.js, { ignoreInitial: false }, devJsRollup);
    watch(sources.bsCss, { ignoreInitial: false }, devBSCss);
    watch(sources.cssGlob, { ignoreInitial: false }, devCss);
    watch(sources.staticAssets, { ignoreInitial: false }, static);
    watch(sources.composites, { ignoreInitial: false }, composites);
    watch(sources.taglibs, { ignoreInitial: false }, taglibs);
    watch(sources.includes, { ignoreInitial: false }, includes);
};
exports.prod = parallel(jsLegacy, prodJsRollup, prodBSCss, prodCss);
