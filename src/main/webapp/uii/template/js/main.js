// custom JS
import { initFunctions } from './modules/inits';
import { toggleLoaders } from './modules/ajax/displayLoader';
import {
    initSaveScrollPosition,
    handleScrollPositionReset,
    restoreAllScrollPositions,
    saveAllScrollPositions,
} from './modules/saveScrollPosition';
import { initAutosave } from './modules/metseditor/autosave';
import { initRaceGuard } from './modules/metseditor/raceGuard';
import { AjaxButtonManager } from './modules/ajax/buttonManager';
import { initNotificationToggle } from './modules/notificationToggle';
import { focusOnLoad } from './modules/gwFocusOnLoad';

// Initialize AjaxButtonManager
const ajaxButtonManager = new AjaxButtonManager();

// Register notification toggle once
initNotificationToggle();

// Initialize all functions on initial page load
document.addEventListener('DOMContentLoaded', () => {
    initFunctions();
    focusOnLoad();
    initSaveScrollPosition();
    restoreAllScrollPositions();
    initAutosave();
    initRaceGuard();
});

// Initialize all functions after an ajax call
if(typeof faces !== "undefined") {
    faces.ajax.addOnEvent((data) => {
        if(data.source?.dataset?.ajaxBehaviour === "ignore") {
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
                handleScrollPositionReset(data.source?.getAttribute('data-reset-scroll-positions'));
                restoreAllScrollPositions();
                initAutosave();
                if (data.source?.hasAttribute('data-focus-on-load') || data.source?.hasAttribute('data-focus-after-load')) {
                    focusOnLoad();
                }
                break;
        }

    });
} else {
    console.warn("No Jakarta faces initialized");
}
