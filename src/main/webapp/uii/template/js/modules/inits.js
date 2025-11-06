import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeTooltips } from './gwInitTooltips';
import { dropdownTooltips } from './gwTooltipsForDropdowns';
import { focusOnLoad } from './gwFocusOnLoad';
import { toggleHelp } from './gwToggleHelp';
import { initScrollTop } from './scrollTop';
import { initFocusOnClick } from './focusOnClick';
import { initSubmitOnEnter } from './submitOnEnter';
import { initMirrorValueTo } from './mirrorValueTo';
import { initAccessibilityModeButton } from './accessibilityMode';
import { loadImage } from './loadImage';
import { initImageAreaCreation } from './createImageArea.js';
import { initEditImageAreas } from './editImageAreas.js';
import { initHotkeys } from './hotkeys.js';
import { initImageFilters } from "./imageFilters.js";

export const initFunctions = function initFunctions() {
    initializeResizeTable();
    initializeTooltips();
    dropdownTooltips();
    initScrollTop();
    initFocusOnClick();
    initSubmitOnEnter();
    initMirrorValueTo();
    initAccessibilityModeButton();
    initHotkeys();

    focusOnLoad();
    toggleHelp();

    const zoomableImage = loadImage();
    initImageAreaCreation(zoomableImage);
    initEditImageAreas(zoomableImage);
    initImageFilters(zoomableImage);
};
