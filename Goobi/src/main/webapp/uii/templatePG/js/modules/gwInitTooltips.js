/**
 * @description Destroy ALL tooltips and initialize again.
 *
 */

var gwInitTooltips = ( function() {
    'use strict';

    var _debug = false;
    var _defaults = {
      scope: document,
      selectors: '[data-bs-toggle="tooltip"], [data-toggle="tooltip"]',
    }

    function init({ scope = _defaults.scope } = {}) {
      _initTooltips(scope);
      _initPopovers();
    }

    /** @description find and return all elements that have a tooltip */
    function _getTooltipTriggers(scope) {
      var tooltipTriggerList =
        [].slice.call(
          scope.querySelectorAll(_defaults.selectors)
        )
      return tooltipTriggerList
    }

    /** @description Initialize tooltips.  */
    function _initTooltips(scope) {
      const tooltipTriggers = _getTooltipTriggers(scope);

      // Initialize tooltips
      tooltipTriggers.map(function (tooltipTriggerEl) {
        return hoverableTooltip(tooltipTriggerEl)
      })

      // Debugging
      if (_debug) console.log('%c### called gwInitTooltips._initTooltips.js ###', 'color: #368ee0')
      if (_debug) console.log({ tooltipTriggers })
    }

    /** @description Initialize popovers.  */
    function _initPopovers() {
      var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
	  var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
	    return hoverablePopover(popoverTriggerEl)
      })
    }

    return {
      init
    }

} )();

/**
 * Utility function to improve accessibility for Bootstrap tooltips to conform
 * to WCAG 2.1 criterion 1.14.13 "Content on Hover or Focus"
 *
 * @param {*} element The element that triggers the Bootstrap tooltip
 * @returns A bootstrap tooltip
 */
const hoverableTooltip = function keepTooltipOpenOnHoverOverContent(element) {
    let tooltip = new bootstrap.Tooltip(element, {
      trigger: 'manual'
    });
    let tooltipTimeOut;

    ['mouseenter', 'focusin'].forEach((eventType) => {
      element.addEventListener(eventType, (event) => {
        event.preventDefault;
        event.stopPropagation;

        clearTimeout(tooltipTimeOut);
        tooltip.show();

        tooltip.tip.addEventListener('mouseleave', () => {
          event.preventDefault;
          event.stopPropagation;

          tooltip.hide();
        });

        window.addEventListener('keydown', (event) => {
          event.preventDefault;
          event.stopPropagation;

          if (event.key === 'Escape') {
            tooltip.hide();
          }
        });

      });
    });

    ['mouseleave', 'focusout'].forEach((eventType) => {
      element.addEventListener(eventType, (event) => {
        event.preventDefault;
        event.stopPropagation;

        tooltipTimeOut = setTimeout(() => {
          if (!tooltip.tip.matches(':hover')) {
            tooltip.hide();
          }
        }, 200);

      });
    });

    return tooltip;
};

/**
 * Utility function to improve accessibility for Bootstrap popovers to conform
 * to WCAG 2.1 criterion 1.14.13 "Content on Hover or Focus"
 *
 * @param {*} element The element that triggers the Bootstrap popover
 * @returns A bootstrap popover
 */
const hoverablePopover = function keepPopoverOpenOnHoverOverContent(element) {
    let popover = new bootstrap.Popover(element, {
      trigger: 'manual'
    });
    let popoverTimeOut;

    ['mouseenter', 'focusin'].forEach((eventType) => {
      element.addEventListener(eventType, (event) => {
        event.preventDefault;
        event.stopPropagation;
        clearTimeout(popoverTimeOut);
        popover.show();
        popover.tip.addEventListener('mouseleave', () => {
          event.preventDefault;
          event.stopPropagation;
          popover.hide();
        });
        window.addEventListener('keydown', (event) => {
          event.preventDefault;
          event.stopPropagation;
          if (event.key === 'Escape') {
            popover.hide();
          }
        });
      });
    });

    ['mouseleave', 'focusout'].forEach((eventType) => {
      element.addEventListener(eventType, (event) => {
        event.preventDefault;
        event.stopPropagation;
        popoverTimeOut = setTimeout(() => {
          if (!popover.tip.matches(':hover')) {
            popover.hide();
          }
        }, 200);
      });
    });

    return popover;
};
