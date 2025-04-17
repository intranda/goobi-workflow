import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';

export const initFunctions = function initFunctions() {
    gwToggleHelp.init;
    //TOOLTIPS
    //Initialize Bootstrap tooltips
    gwInitTooltips.init();
    gwFocusOnLoad.init();
    gwConfigEditor.init();
    initializeAjaxLoader();
    initializeResizeTable();
};