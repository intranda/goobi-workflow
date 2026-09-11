/**
 * This module scrolls elements to their bottom edge, so that the newest content at the end of a
 * chronological list is the part that is in view. It handles AJAX calls automatically.
 *
 * Usage for elements:
 * 1. add the attribute `data-scroll-to-bottom` to the element you want to scroll down
 *
 * The element needs to be scrollable (a constrained height plus `overflow: auto`); on elements that
 * grow with their content this has no effect.
 *
 * @module scrollToBottom
 */

/**
 * Scrolls the specified element to its bottom.
 * @param {HTMLElement} element
 */
export const scrollToBottom = (element) => {
    element.scrollTop = element.scrollHeight;
};

/**
 * Scrolls all elements marked with `data-scroll-to-bottom` that can be found on the current page.
 * This function can be called on page load or after an AJAX call.
 *
 * Reading `scrollHeight` forces a layout, so the measurement is already correct when this runs and
 * the scrolling does not need to be deferred. Deferring it with `requestAnimationFrame` would in
 * fact break it: a page that is not currently being painted (a background tab, an occluded window)
 * never runs its animation frames.
 */
export const scrollAllToBottom = () => {
    document.querySelectorAll('[data-scroll-to-bottom]').forEach(element => {
        scrollToBottom(element);
    });
};
