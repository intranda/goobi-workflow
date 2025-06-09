/**
 * Destroy ALL tooltips and initialize again.
 * @module gwInitTooltips
 */
const findTooltips = function findTooltips (scope = document, selectors = '[data-bs-toggle="tooltip"], [data-toggle="tooltip"]') {
  return [...scope.querySelectorAll(selectors)]
};

const initTooltips = function initTooltips(scope = document) {
  const tooltipTriggers = findTooltips(scope);

  tooltipTriggers.map(function (tooltipTriggerEl) {
    return hoverableTooltip(tooltipTriggerEl)
  })
};

const initPopovers = function initPopovers() {
  const selector = '[data-bs-toggle="popover"]';
  const popoverTargets = findTooltips(document, selector);
  popoverTargets.map(function (popoverTriggerEl) {
    return hoverablePopover(popoverTriggerEl)
  })
};

/**
 * Function to hide all open Bootstrap tooltips in the document
 *
 * @param {*} element The element that triggered the currently processed tooltip
 */
const hideAllTooltips = (element) => {
  const tooltips = document.querySelectorAll('[aria-describedby*="tooltip"]');
  tooltips.forEach((tooltip) => {
    const tooltipId = tooltip.getAttribute('aria-describedby');
    const tooltipElement = document.getElementById(tooltipId);
    const tooltipInstance = bootstrap.Tooltip.getInstance(tooltipElement);

    // only hide tooltips that do not relate to the currently hovered element
    // this is necessary so that users can hover over the tooltip and then return to its base element without hiding the tooltip
    if (tooltip != element && tooltipInstance) {
      tooltipInstance.hide();
    }
  });
};

/**
 * Function to hide all open Bootstrap popovers in the document
 */
const hideAllPopovers = () => {
  const popovers = document.querySelectorAll('[aria-describedby*="popover"]');
  popovers.forEach((popover) => {
    const popoverId = popover.getAttribute('aria-describedby');
    const popoverElement = document.getElementById(popoverId);
    const popoverInstance = bootstrap.Popover.getInstance(popoverElement);

    if (popoverInstance) {
      popoverInstance.hide();
    }
  });
}

/**
* Utility function to improve accessibility for Bootstrap tooltips to conform
* to WCAG 2.1 criterion 1.14.13 "Content on Hover or Focus"
*
* @param {*} element The element that triggers the Bootstrap tooltip
* @returns A bootstrap tooltip
*/
export const hoverableTooltip = function keepTooltipOpenOnHoverOverContent(element) {
  let tooltip = new bootstrap.Tooltip(element, {
    trigger: 'manual',
    offset: [4, 4],
  });
  let tooltipHideTimeout;
  let tooltipShowTimeout;

  // close tooltip when the trigger element is clicked
  // this avoids tooltips on dropdown triggers obscuring dropdown content
  element.addEventListener('click', (event) => {
    event.preventDefault;
    event.stopPropagation;

    hideAllTooltips();
  });

  ['mouseenter', 'focusin'].forEach((eventType) => {
    element.addEventListener(eventType, (event) => {
      event.preventDefault;
      event.stopPropagation;

      // make sure no tooltips are open before opening a new one
      hideAllTooltips(element);

      clearTimeout(tooltipHideTimeout);
      tooltipShowTimeout = setTimeout(() => {
        tooltip.show();
        tooltip?.tip?.addEventListener('mouseleave', () => {
          event.preventDefault;
          event.stopPropagation;

          tooltipHideTimeout = timeout(tooltip, element, 200);
        });

        window.addEventListener('keydown', (event) => {
          event.preventDefault;
          event.stopPropagation;

          if (event.key === 'Escape') {
            tooltip.hide();
          }
        });
      }, 500);
      element.addEventListener('mouseleave', () => {
        clearTimeout(tooltipShowTimeout)
      });

    });
  });

  ['mouseleave', 'focusout'].forEach((eventType) => {
    element.addEventListener(eventType, (event) => {
      event.preventDefault;
      event.stopPropagation;

      tooltipHideTimeout = timeout(tooltip, element, 200);

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
  let popoverHideTimeout;
  let popoverShowTimeout;

  ['mouseenter', 'focusin'].forEach((eventType) => {
    element.addEventListener(eventType, (event) => {
      event.preventDefault;
      event.stopPropagation;

      // make sure no popovers are open before opening a new one
      hideAllPopovers();
      clearTimeout(popoverHideTimeout);
      popoverShowTimeout = setTimeout(() => {
        popover.show();

        popover?.tip?.addEventListener('mouseleave', () => {
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
      }, 500);
      element.addEventListener('mouseleave', () => {
        clearTimeout(popoverShowTimeout);
      });

    });
  });

  ['mouseleave', 'focusout'].forEach((eventType) => {
    element.addEventListener(eventType, (event) => {
      event.preventDefault;
      event.stopPropagation;
      popoverHideTimeout = timeout(popover, element, 200);
    });
  });

  return popover;
};

const timeout = function setTimeoutForHiding(target, element, duration) {
  setTimeout(() => {
    if (!target.tip?.matches(':hover') && !element.matches(':hover')) {
      target.hide();
    }
  }, duration);
};

export const init = function initTooltipsAndPopovers() {
  initTooltips();
  initPopovers();
};
