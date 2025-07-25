import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';
import { init as initializeTooltips } from './gwInitTooltips';
import { dropdownTooltips } from './gwTooltipsForDropdowns';
import { focusOnLoad } from './gwFocusOnLoad';
import { toggleHelp } from './gwToggleHelp';
import { initScrollTop } from './scrollTop';
import { init as initScrollToLine } from './scrollToLine';
import { initFocusOnClick } from './focusOnClick';
import { initSubmitOnEnter } from './submitOnEnter';
import { initMirrorValueTo } from './mirrorValueTo';
import { initAccessibilityModeButton } from './accessibilityMode';
import { initCodemirror } from './codemirror';
import { initShowDevelopertools } from './showDeveloperTools';

export const initFunctions = function initFunctions() {
    initializeAjaxLoader();
    initializeResizeTable();
    initializeTooltips();
    dropdownTooltips();
    initScrollTop();
    initScrollToLine();
    initFocusOnClick();
    initSubmitOnEnter();
    initMirrorValueTo();
    initAccessibilityModeButton();
    initCodemirror();
    initShowDevelopertools();

    focusOnLoad();
    toggleHelp();
};