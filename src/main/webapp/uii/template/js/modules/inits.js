import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';
import { init as initializeTooltips } from './gwInitTooltips';
import { focusOnLoad } from './gwFocusOnLoad';
import { toggleHelp } from './gwToggleHelp';

export const initFunctions = function initFunctions() {
    gwConfigEditor.init();
    initializeAjaxLoader();
    initializeResizeTable();
    initializeTooltips();

    focusOnLoad();
    toggleHelp();
};