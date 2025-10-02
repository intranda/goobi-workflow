import gulp from 'gulp';
const { parallel, watch, src, dest } = gulp;

import fs from 'fs';
import os from 'os';

import * as dartSass from 'sass';
import gulpSass from 'gulp-sass';
const sass = gulpSass(dartSass);

import rename from 'gulp-rename';

import concat from 'gulp-concat';
import sourcemaps from 'gulp-sourcemaps';
import uglify from 'gulp-uglify-es';

import * as rollup from 'rollup';
import cleanup from 'rollup-plugin-cleanup';
import terser from '@rollup/plugin-terser';
import nodeResolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import css from 'rollup-plugin-css-only';

import * as cheerio from 'cheerio';
import * as through2 from 'through2';
import svgmin from 'gulp-svgmin';

// provide custom asset location for watch task
let customLocation = '';

// source directories, files, globs
const sources = {
    bsCss: 'uii/template/css/src/bootstrap.scss',
    bsJS: [
        'node_modules/bootstrap/dist/js/bootstrap.bundle.min.js',
        'node_modules/bootstrap/dist/js/bootstrap.bundle.min.js.map',
    ],
    css: 'uii/template/css/src/',
    cssAccessibility: 'uii/template/css/src/accessibility.scss',
    cssGlob: [
        'uii/template/css/src/',
        '!uii/template/css/src/bootstrap.scss',
    ],
    cssDeps: [
        'node_modules/bootstrap/scss/',
    ],
    staticJS: 'src/js/static/**/*',
    legacyJS: './uii/template/js/legacy/',
    js: [
        './uii/template/js/**/*.js',
        '!./uii/template/js/legacy/**/*',
        '!./uii/template/js/editor/**/*.js',
        '!./uii/template/js/media/**/*.js',
    ],
    editors: [
        'uii/template/js/editor/**/*.js',
    ],
    prosemirror: 'uii/template/js/editor/prosemirror.js',
    codemirror: 'uii/template/js/editor/codemirror.js',
    media: [
        'uii/template/js/media/**/*.js',
    ],
    video: 'uii/template/js/media/video.js',
    icons: ['node_modules/@tabler/icons/icons/**/*.svg'],
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
    taglibs: 'WEB-INF/taglibs/**/*.xhtml',
    includes: 'WEB-INF/includes/**/*.xhtml',
}
const targetFolder = {
    css: 'uii/template/css/dist/',
    icons: 'resources/icons/',
    js: 'resources/js/dist/',
    staticAssets: 'uii/',
    resources: 'resources/',
    taglibs: 'WEB-INF/taglibs/',
    includes: 'WEB-INF/includes/',
}

// FUNCTIONS
// load custom location from user config
// this is a function so that CI does not fail if the file is not present
function loadConfig() {
    const homedir = os.homedir();
    const config = fs.readFileSync(homedir + '/.config/gulp_userconfig.json')
    customLocation = JSON.parse(config).tomcatLocation;
};

function staticAssets() {
    return src(sources.staticAssets)
        .pipe(dest(`${customLocation}${targetFolder.staticAssets}`))
};

function composites() {
    return src(sources.composites)
        .pipe(dest(`${customLocation}${targetFolder.resources}`))
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

function BsJs() {
    return src(sources.bsJS)
        .pipe(dest(`${customLocation}${targetFolder.js}`));
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
        .pipe(uglify.default())
        .pipe(sourcemaps.write())
        .pipe(dest(`${customLocation}${targetFolder.js}`))
};

function devJsRollup() {
    return rollup
        .rollup({
            input: './uii/template/js/main.js',
            plugins: [
                cleanup(),
                nodeResolve(),
            ],
        })
        .then(bundle => {
        	console.log("Write", bundle, " to ", `${customLocation}${targetFolder.js}main.min.js`);
            return bundle.write({
                file: `${customLocation}${targetFolder.js}main.min.js`,
                format: 'iife',
            });
        });
};

function prodJsRollup() {
    return rollup
        .rollup({
            input: './uii/template/js/main.js',
            plugins: [
                cleanup(),
                nodeResolve(),
            ],
        })
        .then(bundle => {
            return bundle.write({
                file: `${targetFolder.js}main.min.js`,
                format: 'iife',
                sourcemap: true,
                plugins: [terser({
                    mangle:true
                })]
            });
        });
};

function editors() {
    const buildEditor = (inputFile, outputName) => {
        return rollup
            .rollup({
                input: inputFile,
                plugins: [
                    cleanup(),
                    nodeResolve(),
                ],
            }).then(bundle => {
                return bundle.write({
                    file: `${customLocation}${targetFolder.js}${outputName}.js`,
                    format: 'iife',
                    sourcemap: true,
                    plugins: [
                        terser({
                            mangle: true,
                        }),
                    ]
                });
            });
    };

    return Promise.all([
        buildEditor(sources.prosemirror, 'prosemirror'),
        buildEditor(sources.codemirror, 'codemirror')
    ]);
};

function media() {
    const buildMedia = (inputFile, outputName) => {
    return rollup
        .rollup({
            input: inputFile,
            plugins: [
                nodeResolve({
                    browser: true,
                    preferBuiltins: false,
                    exportConditions: ['browser'],
                    skip: ['fs', 'path', 'url'],
                }),
                commonjs({
                    include: /node_modules/,
                }),
                css({
                    output: `${outputName}.min.css`
                }),
                cleanup(),
            ],
        }).then(bundle => {
            return bundle.write({
                file: `${customLocation}${targetFolder.js}${outputName}.min.js`,
                format: 'iife',
                sourcemap: true,
                plugins: [
                    // terser({
                    //     mangle: true,
                    // }),
                ]
            });
        });
    };

    const copyPlyrSprite = () => {
        return src('node_modules/plyr/dist/plyr.svg')
            .pipe(dest(`${customLocation}${targetFolder.js}`));
    };

    return Promise.all([
        buildMedia(sources.video, 'video'),
        copyPlyrSprite(),
    ]);
};

/*
 * preprocess svgs as needed
 */
function processSvg(srcDir, destDir) {
    return src(srcDir)
        // optimize svgs
        .pipe(svgmin({
            plugins: [
                { removeViewBox: false }
            ]
        }))
        .pipe(through2.obj(function(file, encoding, callback) {
            const $ = cheerio.load(file.contents.toString(), { xmlMode: true });

            // add id attribute to allow for external reference
            $('svg').attr('id', `icon`);
            // remove width and height attributes for easier styling
            $('svg').attr('width', ``);
            $('svg').attr('height', ``);

            file.contents = Buffer.from($.html());

            this.push(file);
            callback();
        }))
        .pipe(dest(destDir));
};

function icons() {
    return processSvg(sources.icons, `${customLocation}${targetFolder.icons}`);
}

function dev() {
    loadConfig();
    icons();
    BsJs();
    watch(sources.editors, { ignoreInitial: false }, editors);
    watch(sources.media, { ignoreInitial: false }, media);
    watch(sources.legacyJS, { ignoreInitial: false }, jsLegacy);
    watch(sources.js, { ignoreInitial: false }, devJsRollup);
    watch(sources.bsCss, { ignoreInitial: false }, devBSCss);
    watch(sources.cssGlob, { ignoreInitial: false }, devCss);
    watch(sources.staticAssets, { ignoreInitial: false }, staticAssets);
    watch(sources.composites, { ignoreInitial: false }, composites);
    watch(sources.taglibs, { ignoreInitial: false }, taglibs);
    watch(sources.includes, { ignoreInitial: false }, includes);
};
const prod = parallel(
    BsJs,
    jsLegacy,
    prodJsRollup,
    prodBSCss,
    prodCss,
    icons,
    editors,
    media,
);

export { dev, prod, media };
