// custom JS
import './modules/gwConfigFileEditor';
import './modules/gwFocusOnLoad';
import './modules/gwNavigation';
import { initFunctions } from './modules/inits';

// Initialize all functions on initial page load
initFunctions();
// Initialize all functions after an ajax call
faces.ajax.addOnEvent((data) => {
    switch (data.status) {
        case 'begin':
            break;
        case 'complete':
            break;
        case 'success':
            initFunctions();
            break;
    }
});
