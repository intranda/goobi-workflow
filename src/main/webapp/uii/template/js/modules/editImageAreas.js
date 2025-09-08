import {getValue, setValue} from "./loadImage.js";

const settings = {
    inputPageAreas: '[data-content="pageareas"]',
    style: {
        borderWidth: 2,
        borderColor: "#ff4433",
        className: "areaselect-overlay"
    },
}

export const initEditImageAreas = function(image) {

    const areaString = getValue(settings.inputPageAreas);
    
    if(areaString) {
        let areas = JSON.parse(areaString);
        drawAreas(areas, image);
    } else {
        this.drawAreas([], image);
    }

}

function drawAreas(areas, image) {

    for(let area of areas) {
        console.log("draw area ", area);
        const rect = new OpenSeadragon.Rect(parseInt(area.x), parseInt(area.y), parseInt(area.w), parseInt(area.h));
	    const displayRect = ImageView.CoordinateConversion.convertRectFromImageToOpenSeadragon(rect, _viewImage.viewer, _viewImage.getOriginalImageSize());
    }
}

