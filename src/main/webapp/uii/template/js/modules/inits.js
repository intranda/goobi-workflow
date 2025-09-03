import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';
import { init as initializeTooltips } from './gwInitTooltips';
import { dropdownTooltips } from './gwTooltipsForDropdowns';
import { focusOnLoad } from './gwFocusOnLoad';
import { toggleHelp } from './gwToggleHelp';
import { initScrollTop } from './scrollTop';
import { initFocusOnClick } from './focusOnClick';
import { initSubmitOnEnter } from './submitOnEnter';
import { initMirrorValueTo } from './mirrorValueTo';
import { initAccessibilityModeButton } from './accessibilityMode';
import { initShowDevelopertools } from './showDeveloperTools';

export const initFunctions = function initFunctions() {
    initializeAjaxLoader();
    initializeResizeTable();
    initializeTooltips();
    dropdownTooltips();
    initScrollTop();
    initFocusOnClick();
    initSubmitOnEnter();
    initMirrorValueTo();
    initAccessibilityModeButton();
    initShowDevelopertools();

    focusOnLoad();
    toggleHelp();
};