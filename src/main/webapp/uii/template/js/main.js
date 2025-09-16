// custom JS
import { initFunctions } from './modules/inits';
import { toggleLoaders } from './modules/gwAjaxLoader';

// Initialize all functions on initial page load
document.addEventListener('DOMContentLoaded', () => {
    initFunctions();
});
// Initialize all functions after an ajax call
if(typeof faces !== "undefined") {
    faces.ajax.addOnEvent((data) => {
        if(data.source.dataset.ajaxBehaviour === "ignore") {
            return;
        }
        switch (data.status) {
            case 'begin':
                toggleLoaders(true)
                break;
            case 'complete':
                break;
            case 'success':
                toggleLoaders(false)
                initFunctions();
                break;
        }
    });
} else {
    console.warn("No Jakarta faces initialized");
}
