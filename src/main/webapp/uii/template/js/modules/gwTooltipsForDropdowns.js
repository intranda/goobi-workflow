/**
 * Toggles the display of tooltips for dropdown triggers.
 * If the dropdown is expanded, the tooltip is disabled so as to not hide elements
 * in the dropdown.
 * @module tooltipsForDropdowns
 */

const defaultSelector = '[data-bs-toggle="tooltip"]';
const config = {
  attributes: true,
  childList: false,
  subtree: false,
};

const handleTooltip = function handleTooltip(element) {
  const tooltip = bootstrap.Tooltip.getInstance(element);
  const dropdownToggle = element.querySelector(".dropdown-toggle");
  let dropdownIsExpanded = dropdownToggle.getAttribute('aria-expanded');

  // Mutation observer
  const observer = new MutationObserver((mutationsList, observerInstance) => {
    mutationsList.forEach((mutation) => {
      if (mutation.attributeName === 'aria-expanded') {
        dropdownIsExpanded = dropdownToggle.getAttribute('aria-expanded');
        if (dropdownIsExpanded === 'false') {
          tooltip.disable();
        }
        if (dropdownIsExpanded === 'true') {
          tooltip.enable();
          // Stop watching for mutations
          observerInstance.disconnect();
        }
      }
    });
  });
  observer.observe(dropdownToggle, config);

  element.addEventListener('click', (event) => {
    dropdownIsExpanded = dropdownToggle.getAttribute('aria-expanded');
    if (dropdownIsExpanded === 'true') {
      tooltip.disable();
      // Start watching for mutations on the dropdown toggle's attributes
      observer.observe(dropdownToggle, config)
    } else {
      tooltip.enable();
    }
  });
};

export const dropdownTooltips = function toggleDropdownTooltips(selector = defaultSelector) {
  const elements = document.querySelectorAll(selector);
  elements.forEach((element) => {
    if (element.querySelector('.dropdown-toggle')) {
      handleTooltip(element);
    }
  })
};
