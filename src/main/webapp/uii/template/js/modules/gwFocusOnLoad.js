/**
 * Sets focus on the first element with a data attribute of `data-gw-focus-on-load="true"`.
 * Alternatively an id, class etc. can be passed to query the element.
 * @module gwFocusOnLoad
 */

const defaultSelector = '[data-gw-focus-on-load="true"]';

export const focusOnLoad = function focusOnLoad(selector = defaultSelector) {
  window.addEventListener("DOMContentLoaded", function() {
    try {
      const element = document.querySelector(selector);
      element?.focus();
    }
    catch {}
  })
};
