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
const customLocation = '';

// source directories, files, globs
const legacySources = {
    less: 'uii/template/css/less/build.less',
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
    template: 'uii/templatePG/templatePG.html'
}
// target directories
const legacyTargetFolder = {
    lessDest: 'uii/template/css/dist/'
}
const targetFolder = {
    css: 'uii/templatePG/css/dist/',
    js: 'dist/js/',
    staticAssets: 'uii/',
    composites: 'resources/'
}

// FUNCTIONS
function static() {
    return src(sources.staticAssets)
        .pipe(dest(`${customLocation}${targetFolder.staticAssets}`))
};

function composites() {
    return src(sources.composites)
        .pipe(dest(`${customLocation}${targetFolder.composites}`))
};

// function for legacy less
function prodLess() {
    return src(`${legacySources.less}`)
        .pipe(sourcemaps.init())
        .pipe(less({
            plugins: [autoprefix],
            outputSourceFiles: true
        }))
        .pipe(cleanCSS({debug: true}, (details) => {
            console.log(`${details.name}: ${details.stats.originalSize}`);
            console.log(`${details.name}: ${details.stats.minifiedSize}`);
        }))
        .pipe(sourcemaps.write())
        .pipe(rename('goobiWorkflow.min.css'))
        .pipe(dest(legacyTargetFolder.lessDest))
};

function devLess() {
    return src(`${legacySources.less}`)
    .pipe(less())
    .pipe(rename('goobiWorkflow.min.css'))
    .pipe(dest(`${customLocation}${legacyTargetFolder.lessDest}`))
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
    return src(legacySources.js)
        .pipe(concat(`goobiWorkflowJS.min.js`))
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(sourcemaps.write())
        .pipe(dest(targetFolder.js))
};

function prodJsLegacy() {
    return jsLegacy()
        .pipe(concat(`goobiWorkflowJS.min.js`))
        .pipe(sourcemaps.init())
        .pipe(uglify())
        .pipe(sourcemaps.write())
        .pipe(dest(targetFolder.js))
};

function devJsLegacy() {
    return jsLegacy()
        .pipe(concat(`goobiWorkflowJS.min.js`))
        .pipe(dest(`${customLocation}${targetFolder.js}`))
};

function devJsRollup() {
    return rollup
        .rollup({
            input: './uii/templatePG/js/main.js',
            plugins: [cleanup()]
        })
        .then(bundle => {
            return bundle.write({
                file: `${customLocation}${targetFolder.js}main.js`,
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
                file: `${targetFolder.js}main.js`,
                format: 'es',
                sourcemap: true,
                plugins: [terser({
                    mangle:false
                })]
            });
        });
};

exports.dev = function() {
    watch(legacySources.less, { ignoreInitial: false }, devLess);
    watch(legacySources.js, { ignoreInitial: false }, devJsLegacy);
    watch(sources.js, { ignoreInitial: false }, devJsRollup);
    watch(sources.bsCss, { ignoreInitial: false }, devBSCss);
    watch(sources.cssGlob, { ignoreInitial: false }, devCss);
    watch(sources.staticAssets, { ignoreInitial: false }, static);
    watch(sources.composites, { ignoreInitial: false }, composites);
};
exports.prod = parallel(prodJsLegacy, prodJsRollup, prodBSCss, prodCss, prodLess);
