
const settings = {

    startSelectionButtons: ".start-area-edition",
    cancelSelectionButtons: ".cancel-area-edition",
    deleteAreaButtons: "[data-pagearea-delete='start]'",
    disableInteractionOverlay: "#disable-interaction-overlay",
    style: {
        borderWidth: 2,
        borderColor: "#ff4433",
        className: "areaselect-overlay"
    },
}

let areaSelect = undefined;
let drawActive = false;
let deleteActive = false;
let target = undefined;
let subscriptions = [];

let oldImageElement = undefined

export const initImageAreaCreation = function(image) {

    //always initialize button listeners
    subscriptions.forEach(s => s.unsubscribe());
    subscriptions = [];
    
    const startSelectionButtons = document.querySelectorAll(settings.startSelectionButtons);
    const cancelSelectionButtons = document.querySelectorAll(settings.cancelSelectionButtons);
    if(startSelectionButtons.length) {
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.startSelectionButtons), "click").subscribe(e => handleClickStart(e)));
    }
    if(cancelSelectionButtons.length) {
        subscriptions.push( rxjs.fromEvent( document.querySelectorAll(settings.cancelSelectionButtons), "click").subscribe(e => handleClickCancel(e)));
    }

    //check if canvas has been replaced. If not, don't reinitialze areaSelect
    if(oldImageElement?.isConnected) {
        console.log("draw area already initialized");
        return;
    } else {
        oldImageElement = image.element;
    }
    console.log("image element", oldImageElement);

    if(areaSelect) {
        areaSelect.close();
    }

    areaSelect = new ImageView.AreaSelect(image, {
        removeOldOverlays : true,
        drawCondition: event => drawActive === true,
        transformCondition: event => true,
        drawStyle: settings.style,
    });

    areaSelect.finishedHook.subscribe(rect => {
        drawActive = false; 
        console.log("finished overlay", rect);
        const area = createImageArea(rect);
        area.addTo = target;
        console.log("created image area ", area);
        console.log("addPageArea", addPageArea, window.addPageArea);
        addPageArea(area);
        enableInteractions();
    })

}

function handleClickStart(event) {
    target = event.target.dataset.imageAreaTarget;
    console.log("init target ",target )
    drawActive = true;
    disableInteractions();
}

function handleClickCancel(event) {
    drawActive = false;
    cancelPageAreaEdition();
    enableInteractions();
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

function disableInteractions() {
    document.querySelectorAll(".cancel-area-edition").forEach(btn => btn.style.display = "block");
    document.querySelectorAll(".start-area-edition").forEach(btn => btn.style.display = "none");
    const overlay = document.querySelector(settings.disableInteractionOverlay);
    if(overlay) {
        overlay.style.display = "block";
        document.querySelectorAll(".above-overlay, .above-overlay canvas").forEach(e => setZIndex(e, 1000));
    }
}


function enableInteractions() {
    document.querySelectorAll(".cancel-area-edition").forEach(btn => btn.style.display = "none");
    document.querySelectorAll(".start-area-edition").forEach(btn => btn.style.display = "block");
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

