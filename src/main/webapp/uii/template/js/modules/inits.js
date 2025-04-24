import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';
import { init as initializeTooltips } from './gwInitTooltips';
import { toggleHelp } from './gwToggleHelp';

export const initFunctions = function initFunctions() {
    //TOOLTIPS
    //Initialize Bootstrap tooltips
    gwFocusOnLoad.init();
    gwConfigEditor.init();
    initializeAjaxLoader();
    initializeResizeTable();
    initializeTooltips();

    toggleHelp();
};