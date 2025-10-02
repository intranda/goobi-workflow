// custom JS
import { initFunctions } from './modules/inits';
import { toggleLoaders } from './modules/gwAjaxLoader';
import {
    initSaveScrollPosition,
    restoreAllScrollPositions,
    saveAllScrollPositions,
} from './modules/saveScrollPosition';

// Initialize all functions on initial page load
document.addEventListener('DOMContentLoaded', () => {
    initFunctions();
    initSaveScrollPosition();
    restoreAllScrollPositions();
});
// Initialize all functions after an ajax call
faces.ajax.addOnEvent((data) => {
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
            break;
    }
});
