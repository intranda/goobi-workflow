import { init as initializeResizeTable } from './gwResizableTable';
import { init as initializeAjaxLoader } from './gwAjaxLoader';
import { init as initializeTooltips } from './gwInitTooltips';
import { dropdownTooltips } from './gwTooltipsForDropdowns';
import { focusOnLoad } from './gwFocusOnLoad';
import { toggleHelp } from './gwToggleHelp';
import { init as initConfigFileEditor } from './gwConfigFileEditor';
import { initScrollTop } from './scrollTop';
import { init as initScrollToLine } from './scrollToLine';
import { initFocusOnClick } from './focusOnClick';
import { initSubmitOnEnter } from './submitOnEnter';
import { initMirrorValueTo } from './mirrorValueTo';
import { initAccessibilityModeButton } from './accessibilityMode';

export const initFunctions = function initFunctions() {
    initializeAjaxLoader();
    initializeResizeTable();
    initializeTooltips();
    dropdownTooltips();
    initConfigFileEditor();
    initScrollTop();
    initScrollToLine();
    initFocusOnClick();
    initSubmitOnEnter();
    initMirrorValueTo();
    initAccessibilityModeButton();

    focusOnLoad();
    toggleHelp();
};