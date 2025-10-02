import {getValue, setValue} from "./loadImage.js";

const _debug = false;

const settings = {
    inputPageAreas: '[data-content="pageareas"]',
    buttonStartDeletion: "[data-pagearea-delete='start']",
    buttonCancelDeletion: "[data-pagearea-delete='cancel']",
    disableInteractionOverlay: "#disable-interaction-overlay",
    style: {
        borderWidth: 2,
        borderColor: "#ff4433",
        className: "areaselect-overlay",
        highlightClassName : "highlight"
    },
}

let overlays  = new Map();
let subscriptions = [];
let deletionSubscriptions = [];
let deleting = false;

export const initEditImageAreas = function(image) {

    if(!image) {
        return;
    }

    closeSubscriptions();
    const areaString = getValue(settings.inputPageAreas);
    if(_debug)console.log("read area strings", settings.inputPageAreas, areaString);
    const areas = areaString ? JSON.parse(areaString) : [];
    if(isViewerOpened(image)) {
        cleanupOverlays();
        drawAreas(areas, image);
        initAreaDeletion(image);
    } else {
        image.onOpened.subscribe( e => drawAreas(areas, image));
        image.onOpened.subscribe( e => initAreaDeletion(image));
    }
}

function initAreaDeletion(image) {
    const startButton = document.querySelector(settings.buttonStartDeletion);
    const cancelButton = document.querySelector(settings.buttonCancelDeletion);
    if(startButton && cancelButton) {
        subscriptions = [
                rxjs.fromEvent( startButton, "click" ).subscribe(e => startDeletionMode(image, startButton, cancelButton)),
                rxjs.fromEvent( cancelButton, "click" ).subscribe(e => endDeletionMode(image, startButton, cancelButton)),
        ]
    }
}

function startDeletionMode(image, startButton, cancelButton) {
    if(_debug)console.log("start deletion mode");
   hide(startButton);
   show(cancelButton);
   disableInteractions();
   deleting = true;
    overlays.values().forEach(overlay => {
        if(overlay.element?.classList.contains(settings.style.highlightClassName)) {
            overlay.element.style.cursor = "not-allowed";
            deletionSubscriptions.push(overlay.onClick().subscribe(e => {
                overlay.remove();
                deletePageArea(createImageArea(overlay.bounds, overlay.id));
                endDeletionMode(image, startButton, cancelButton);
            }));
        }

    });
}

function endDeletionMode(image, startButton, cancelButton) {
    if(_debug)console.log("end deletion mode");
    hide(cancelButton);
    show(startButton);
    enableInteractions();
    deleting = false;
    overlays.values().forEach(overlay => {
        if(overlay.element) {
            overlay.element.style.cursor = "auto";
        }
        
    });
    deletionSubscriptions.forEach(subscr => subscr.unsubscribe());
    deletionSubscriptions = [];
}

function closeSubscriptions() {
    subscriptions.forEach(subscr => subscr.unsubscribe());
    subscriptions = [];
    deletionSubscriptions.forEach(subscr => subscr.unsubscribe());
    deletionSubscriptions = [];
}


function drawAreas(areas, image) {
    for(let area of areas) {
        if(_debug)console.log("draw area ", area);
        const rect = new OpenSeadragon.Rect(parseInt(area.x), parseInt(area.y), parseInt(area.w), parseInt(area.h));
	    const displayRect = ImageView.CoordinateConversion.scaleToOpenSeadragonCoordinates(rect, image.openseadragon, image.getOriginalImageSize());
        if(_debug)console.log("draw rect", rect, displayRect );
        const overlay = new ImageView.Overlay(displayRect, {style: settings.style}, area.areaId);
        overlay.draw(image);
        overlay.transform = new ImageView.Transform(image, overlay, {
        	startCondition: () => !deleting && isHighlighted(overlay)
        });
        overlay.transform.finishedTransforming().subscribe(o => {
            const viewportBounds = o.getBounds();
            const imageBounds = o.convertFromViewportToImage(viewportBounds);
            if(_debug)console.log("changed overlay ", o, imageBounds);
            const updatedArea = createImageArea(imageBounds, o.id);
            setPageArea(updatedArea);
        });
        if(area.highlight) {
            highlight(overlay);
        }
        overlays.set(area.areaId, overlay);
    }
}

function isHighlighted(overlay) {
    return overlay?.element?.classList.contains(settings.style.highlightClassName);
}

function cleanupOverlays() {
    overlays.values().forEach(o => {
        o.transform?.close();
        o.remove();
    })
    overlays = new Map();
}

function highlight(area) {
    if(typeof area === "string") {
        area = overlays.get(area);
    }
    overlays.values().forEach(o => o.element.classList.remove(settings.stylehighlightClassName));
    if(area) {
        area.element.classList.add(settings.style.highlightClassName);
    }
}

function isViewerOpened(image) {
    return image?.extent?.canvasSize?.x > 0 && image?.tileSources;
}

function createImageArea(rect, id) {
    var area = {};
    if(rect) {
        area.areaId = id;
        area.x = Math.round(rect.x);
        area.y = Math.round(rect.y);
        area.w = Math.round(rect.width);
        area.h = Math.round(rect.height);
    }
    return area;
}

function hide(element) {
    const currentDisplay = element.style.display;
    element.dataset.defaultDisplay = currentDisplay;
    element.style.display = "none";
}

function show(element) {
    element.style.display = element.dataset.defaultDisplay ?? "block";
    element.dataset.defaultDisplay = undefined;

}

function disableInteractions() {
    const overlay = document.querySelector(settings.disableInteractionOverlay);
    if(overlay) {
        overlay.style.display = "block";
        document.querySelectorAll(".above-overlay, .above-overlay canvas").forEach(e => setZIndex(e, 1000));
    }
}


function enableInteractions() {
    const overlay = document.querySelector(settings.disableInteractionOverlay);
    if(overlay) {
        overlay.style.display = "none";
        document.querySelectorAll(".above-overlay, .above-overlay canvas").forEach(e => resetZIndex(e));
    }
}

function setZIndex(element, zIndex) {
    if(Number.isInteger(zIndex)) {
        const z = element.style.zIndex;
        if(z) {
            element.dataset.zIndex = z;
        }
        element.style.zIndex = zIndex;
    }
}

function resetZIndex(element) {
    const z = element.dataset.zIndex;
    if(z != undefined) {
        element.dataset.zIndex = "";
        element.style.zIndex = z;
    }
}