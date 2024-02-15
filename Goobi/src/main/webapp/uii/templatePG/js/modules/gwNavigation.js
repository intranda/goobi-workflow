/**
 * @description Contains functions used in the navigation: `inc_menu.xhtml`, `inc_menu_main.xhtml` and `inc_menu_preferences.xhtml`.
 *
 */
export default gwNavigation = ( function() {
    'use strict';

    var _debug = false;


    /**
    * LOCALE SWITCHER: Show and hide tooltips
    */
    function toggleLangTooltip(target) {
      if (_debug) console.log('%c### called gwNavigation.toggleLangTooltip.js ###', 'color: #368ee0')
      const tooltip = bootstrap.Tooltip.getInstance(target)
      const dropdownToggle = target.querySelector(".nav-link");
      const dropdownIsExpanded = dropdownToggle.getAttribute('aria-expanded');

      // Mutation observer
      const observer = new MutationObserver((mutations, observerInstance) => {
        mutations.forEach(mutation => {
          // console.log(mutation, mutation.type)
          if(mutation.attributeName === 'aria-expanded') {
            if (dropdownIsExpanded === 'false') {
              tooltip.disable();
            }
            if (dropdownIsExpanded === 'true') {
              tooltip.enable();
              // Stop watching for mutations
              observerInstance.disconnect();
            }
          }
        })
      });

      if (dropdownIsExpanded === 'true') {
        tooltip.disable();
        // Start watching for mutations on the dropdown toggle's attributes
        observer.observe(dropdownToggle, {
          attributes: true,
        })
      }

      if (dropdownIsExpanded === 'false') {
        // If a user clicks on the dropdown toggle while the dropdown is expanded
        // => the tooltip is reenabled by the mutation observer (above)
        // => we will then show the tooltip using this Bootstrap 5 fn
        tooltip.show();
      }
    }


    return {
      toggleLangTooltip
    }
} )();
