/**
 * @description Destroy tooltips on ajax call and initialize a new one if ajax call is done.  
 * 
 */
var gwHandleTooltip = ( function() {
    'use strict';
    
    var _debug = false;

    function init(data) {
      _handleTooltip(data);

    }

    function _handleTooltip(data) {
      if (_debug) console.log('%c### called gwHandleTooltip.js ###', 'color: #368ee0')

      // get the element triggering the ajax call
      const trigger = bootstrap.Tooltip.getInstance(data.source)

      // remove associated tooltips
      if (trigger) trigger?.tip?.remove();

      // if ajax call is done, init a new tooltip
      if (data.status === 'success') {
        // get the rerendered trigger element
        const tooltipTrigger = document.querySelector('[data-goobi="handle-tooltip"]')
        // fire up a tooltip (new instances are enabled by default)
        new bootstrap.Tooltip(tooltipTrigger)
      }
    }


    return {
      init 
    }
} )();
