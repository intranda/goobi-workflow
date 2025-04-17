import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';
import { toggleHelp } from './gwToggleHelp';

export const initFunctions = function initFunctions() {
    //TOOLTIPS
    //Initialize Bootstrap tooltips
    gwInitTooltips.init();
    gwFocusOnLoad.init();
    gwConfigEditor.init();
    initializeAjaxLoader();
    initializeResizeTable();

    toggleHelp();
};