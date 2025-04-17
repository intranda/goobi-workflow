import { init as initializeResizeTable } from './gwResizableTable';

export const initFunctions = function initFunctions() {
    gwToggleHelp.init;
    gwAjaxLoader.init();
    //TOOLTIPS
    //Initialize Bootstrap tooltips
    gwInitTooltips.init();
    gwFocusOnLoad.init();
    gwConfigEditor.init();
    initializeResizeTable();
};