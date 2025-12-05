// custom JS
import { initFunctions } from './modules/inits';
import { toggleLoaders } from './modules/ajax/displayLoader';
import {
    initSaveScrollPosition,
    restoreAllScrollPositions,
    saveAllScrollPositions,
} from './modules/saveScrollPosition';
import { initAutosave } from './modules/metseditor/autosave';
import { AjaxButtonManager } from './modules/ajax/buttonManager';

// Initialize AjaxButtonManager
const ajaxButtonManager = new AjaxButtonManager();

// Initialize all functions on initial page load
document.addEventListener('DOMContentLoaded', () => {
    initFunctions();
    initSaveScrollPosition();
    restoreAllScrollPositions();
    initAutosave();
});

// Initialize all functions after an ajax call
if(typeof faces !== "undefined") {
    faces.ajax.addOnEvent((data) => {
        if(data.source.dataset.ajaxBehaviour === "ignore") {
                return;
            }
        switch (data.status) {
            case 'begin':
                toggleLoaders(true);
                saveAllScrollPositions();
                break;
            case 'complete':
                break;
            case 'success':
                toggleLoaders(false)
                initFunctions();
                initSaveScrollPosition();
                restoreAllScrollPositions();
                initAutosave();
                break;
        }

    });
} else {
    console.warn("No Jakarta faces initialized");
}
