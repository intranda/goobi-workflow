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
        filters.brightness.elements = document.querySelectorAll(settings.brightness);
        filters.brightness.baseValue = 0;
        subscriptions.push( rxjs.fromEvent( filters.brightness.elements, "input").subscribe(e => _handleInput(e, filters.brightness)));
    }

    if(document.querySelector(settings.contrast)) {
        filters.contrast = new ImageView.ImageFilters.Contrast(image, 1);
        filters.contrast.elements = document.querySelectorAll(settings.contrast);
        filters.contrast.baseValue = 1;
        subscriptions.push( rxjs.fromEvent( filters.contrast.elements, "input").subscribe(e => _handleInput(e, filters.contrast)));
    }

    if(document.querySelector(settings.saturation)) {
        filters.saturation = new ImageView.ImageFilters.ColorSaturation(image, 1);
        filters.saturation.elements = document.querySelectorAll(settings.saturation);
        filters.saturation.baseValue = 1;
        subscriptions.push( rxjs.fromEvent( filters.saturation.elements, "input").subscribe(e => _handleInput(e, filters.saturation)));
    }

    if(document.querySelector(settings.hue)) {
        filters.hue = new ImageView.ImageFilters.ColorRotate(image, 0);
        filters.hue.elements = document.querySelectorAll(settings.hue);
        filters.hue.baseValue = 0;
        subscriptions.push( rxjs.fromEvent( filters.hue.elements, "input").subscribe(e => _handleInput(e, filters.hue)));
    }

    if(document.querySelector(settings.bitonal)) {
        filters.bitonal = new ImageView.ImageFilters.Threshold(image, 128);
        filters.bitonal.elements = Array.from(document.querySelectorAll(settings.bitonal)).concat(Array.from(document.querySelectorAll(settings.bitonalThreshold)));
        filters.bitonal.baseValue = 128;
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.bitonal), "change").subscribe(e => _handleInput(e, filters.bitonal, [filters.grayscale, filters.sharpening])));
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.bitonalThreshold), "input").subscribe(e => _handleInput(e, filters.bitonal, [filters.grayscale, filters.sharpening])));
    }

    if(document.querySelector(settings.grayscale)) {
        filters.grayscale = new ImageView.ImageFilters.Grayscale(image);
        filters.grayscale.elements = document.querySelectorAll(settings.grayscale);
        subscriptions.push( rxjs.fromEvent(filters.grayscale.elements, "change").subscribe(e => _handleInput(e, filters.grayscale, [filters.bitonal])));
    }

    if(document.querySelector(settings.invert)) {
        filters.invert = new ImageView.ImageFilters.Invert(image);
        filters.invert.elements = document.querySelectorAll(settings.invert);
        subscriptions.push( rxjs.fromEvent( filters.invert.elements, "change").subscribe(e => _handleInput(e, filters.invert)));
    }

    if(document.querySelector(settings.sharpening)) {
        filters.sharpening = new ImageView.ImageFilters.Sharpen(image, 1);
        filters.sharpening.elements = document.querySelectorAll(settings.sharpening);
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.sharpening), "change").subscribe(e => _handleInput(e, filters.sharpening, [filters.bitonal])));
    }

    if(document.querySelector(settings.reset)) {
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.reset), "click").subscribe(e => _resetFilters(filters)));
    }

    return filters;
}  

function _closeFilter(filter) {
    filter.close();
    filter.elements.forEach(element => {
        if(element.type === "checkbox") {
            element.checked = false;
        } else {
            element.value = filter.baseValue;
        } 
    });
}

function _startFilter(filter) {
    filter.start();
    filter.elements.forEach(element => {
        if(element.type === "checkbox") {
            element.checked = true;
        }
    });
}

function _resetFilters(imageFilters) {
    _closeFilter(imageFilters.brightness);
    _closeFilter(imageFilters.contrast);
    _closeFilter(imageFilters.saturation);
    _closeFilter(imageFilters.hue);
    _closeFilter(imageFilters.bitonal);
    _closeFilter(imageFilters.grayscale);
    _closeFilter(imageFilters.invert);
    _closeFilter(imageFilters.sharpening);
}

function _handleInput(event, filter, precludes) {
    if(_debug)console.log("handle image filter input", event.target, filter);
    const value = event.target.value;
    if(!filter.isActive()) {
        _startFilter(filter);
        precludes?.forEach(preclude => _closeFilter(preclude));
    } else if(isNaN(value) ) {
        _closeFilter(filter);
    }
    if(!isNaN(value) ) {			        
        filter.setValue(parseFloat(value));
    }
}