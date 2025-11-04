const _debug = false;

const settings = {

    brightness: "#imageFiltersBrightness",
    contrast: "#imageFiltersContrast",
    saturation: "#imageFiltersSaturation",
    hue: "#imageFiltersHue",
    bitonal: "#imageFiltersBitonalSwitch",
    bitonalThreshold: "#imageFiltersBitonalThreshold",
    grayscale: "#imageFiltersGrayscale",
    invert: "#imageFiltersColorInversion",
    sharpening: "#imageFiltersSharpening",
    reset: "#imageFiltersReset"


}

let subscriptions = [];
let filters = {};

export const initImageFilters = function(image) {

    if(!image || !image.element) {
        return;
    }

    subscriptions.forEach(s => s.unsubscribe());
    subscriptions = [];

    Object.entries(filters).forEach(([key, value]) => {
        value.close();
    });

    if(_debug) console.log("init image filters for ", image);

    if(document.querySelector(settings.brightness)) {
        filters.brightness = new ImageView.ImageFilters.Brightness(image, 0);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.brightness), "input").subscribe(e => _handleInput(e, filters.brightness)));
    }

    if(document.querySelector(settings.contrast)) {
        filters.contrast = new ImageView.ImageFilters.Contrast(image, 1);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.contrast), "input").subscribe(e => _handleInput(e, filters.contrast)));
    }

    if(document.querySelector(settings.saturation)) {
        filters.saturation = new ImageView.ImageFilters.ColorSaturation(image, 1);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.saturation), "input").subscribe(e => _handleInput(e, filters.saturation)));
    }

    if(document.querySelector(settings.hue)) {
        filters.hue = new ImageView.ImageFilters.ColorRotate(image, 0);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.hue), "input").subscribe(e => _handleInput(e, filters.hue)));
    }

    if(document.querySelector(settings.bitonal)) {
        filters.bitonal = new ImageView.ImageFilters.Threshold(image, 128);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.bitonal), "change").subscribe(e => _handleInput(e, filters.bitonal, [filters.grayscale, filters.sharpening])));
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.bitonalThreshold), "input").subscribe(e => _handleInput(e, filters.bitonal)));
    }

    if(document.querySelector(settings.grayscale)) {
        filters.grayscale = new ImageView.ImageFilters.Grayscale(image);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.grayscale), "change").subscribe(e => _handleInput(e, filters.grayscale, [filters.bitonal])));
    }

    if(document.querySelector(settings.invert)) {
        filters.invert = new ImageView.ImageFilters.Invert(image);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.invert), "change").subscribe(e => _handleInput(e, filters.invert)));
    }

    if(document.querySelector(settings.sharpening)) {
        filters.sharpening = new ImageView.ImageFilters.Sharpen(image, 1);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.sharpening), "change").subscribe(e => _handleInput(e, filters.sharpening, [filters.bitonal])));
    }

    if(document.querySelector(settings.reset)) {
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.reset), "click").subscribe(e => _resetFilters(filters)));
    }

    return filters;
}   

function _resetFilters(imageFilters) {
    imageFilters.brightness.close();
    imageFilters.contrast.close();
    imageFilters.saturation.close();
    imageFilters.hue.close();
    imageFilters.bitonal.close();
    imageFilters.grayscale.close();
    imageFilters.invert.close();
    imageFilters.sharpening.close();
}

function _handleInput(event, filter, precludes) {
    console.log("handle image filter input", event.target, filter);
    const value = event.target.value;
    if(!filter.isActive()) {
        filter.start();
        precludes?.forEach(preclude => preclude.close());
    } else if(isNaN(value) ) {
        filter.close();
    }
    if(!isNaN(value) ) {			        
        filter.setValue(parseFloat(value));
    }
}