
const _debug= false;

const settings = {
    tileSources: "#tileSource",
    controls: {
        zoomInput: "#zoomSliderLabel input",
        rotateRight: "#rotate-right-x",
        rotateLeft: "#rotate-left-x",
        reset: "#reset-position"
    },

    imageView: {
        element: "#mainImage",
        fittingMode: "fixed",
        margins: {bottom:0},
        sequence: {
            columns: 1,
            columnOffset: 0,
            rowMargin: 0
        } 
    }
}

var loadedImage; 
var zoomControl;
var rotationControl; 
var eventListeners = new Map();

export const loadImage = function() {

    if(!document.querySelector(settings.imageView.element)) {
        console.warn("No element to host image found" , settings.imageView.element);
    }

    if(loadedImage?.element?.isConnected) {
        if(_debug)console.log("viewer already initialized");
        return loadedImage;
    }

    if(_debug)console.log("prepare image with  ", settings.imageView, document.querySelector(settings.imageView.element));

    if(!loadedImage) {
        loadedImage = new ImageView.Image(settings.imageView);
    } else if(!loadedImage.element.isConnected) {
        loadedImage.close();
        loadedImage = new ImageView.Image(settings.imageView);
    }
    if(zoomControl) {
        zoomControl.close();
    }
    if(rotationControl) {
        rotationControl.close();
    }
    rotationControl = new ImageView.Controls.Rotation(loadedImage);
    zoomControl = new ImageView.Controls.Zoom(loadedImage);
    zoomControl.setInput(settings.controls.zoomInput);

    eventListeners.forEach((listener, element) => {
        element?.removeEventListener("click", listener);
    });

    const turnLeftEvent = () => rotationControl.rotateLeft();
    const turnRightEvent = () => rotationControl.rotateRight();
    const resetEvent = () => {
        rotationControl.rotateTo(0);
        zoomControl.goHome(); 
    };

    eventListeners = new Map();
    eventListeners.set(getElement(settings.controls.rotateLeft), turnLeftEvent);
    eventListeners.set(getElement(settings.controls.rotateRight), turnRightEvent);
    eventListeners.set(getElement(settings.controls.reset), resetEvent);
    eventListeners.forEach((listener, element) => {
        element?.addEventListener("click", listener);
    });

    getElement(settings.controls.rotateLeft)?.addEventListener("click", turnLeftEvent)
    getElement(settings.controls.rotateRight)?.addEventListener("click", turnRightEvent)
    getElement(settings.controls.reset)?.addEventListener("click", resetEvent);
    const tileSource = getValue(settings.tileSources);
    if(_debug)console.log("load tilesource ", tileSource);
    loadedImage.load(tileSource).then(() => {
        zoomControl.goHome();
    })

    return loadedImage;
}

const getElement = function(obj) {
    if(typeof obj === "string") {
        return document.querySelector(obj);
    } else {
        return obj;
    }
}

 export const getValue = function(input) {
    const element = getElement(input);
    return element?.value || element?.innerHTML;
}

export const setValue = function(input, value, commit) {
    const element = getElement(input);
    if(element) {
        element.value = value;
        if(commit) {
            element.dispatchEvent(new Event("change"));
        }
    }
}