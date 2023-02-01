/**
 * @description Destroy ALL tooltips and initialize again.  
 * 
 */

var gwInitTooltips = ( function() {
    'use strict';
    
    var _debug = true;
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
      const tooltipTriggers = _getTooltipTriggers(scope)

      // Initialize tooltips
      tooltipTriggers.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
      })

      // Debugging
      if (_debug) console.log('%c### called gwInitTooltips._initTooltips.js ###', 'color: #368ee0')
      if (_debug) console.log({ tooltipTriggers })
    }
    
    /** @description Initialize popovers.  */ 
    function _initPopovers() {
      var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
	  var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
	    return new bootstrap.Popover(popoverTriggerEl)
      })
    }

    return {
      init 
    }

} )();

