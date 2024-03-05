import { hoverableTooltip } from './gwInitTooltips.js'
/**
 * @description Destroy tooltips on ajax call and initialize a new one if ajax call is done.
 *
 */
export default gwHandleTooltip = ( function() {
    'use strict';

    var _debug = false;
    var _defaultSelector = '[data-goobi="handle-tooltip"]'

    function init(data, { selector = _defaultSelector, restore = true } = {})  {
      // get the element triggering the ajax call
      const trigger = bootstrap.Tooltip.getOrCreateInstance(data.source)
      _removeTooltip(trigger);

      // Restore tooltips after ajax is done
      if(!restore) return
        _restoreTooltip(data, selector);

    }

    function _removeTooltip(trigger) {
      if (_debug) console.log('%c## called gwHandleTooltip._removeTooltip ##', 'color: #368ee0')
      trigger?.tip?.remove();
    }

    function _restoreTooltip(data, selector) {
      if (_debug) console.log('%c### called gwHandleTooltip._restoreTooltip ###', 'color: orange')

      // if ajax call is done, init a new tooltip
      if (data.status === 'success') {
        // this call to remove() is sanitizing the DOM from any still existing tooltips
        // that might have been left behind after the previous ajax call
        _remove();
        // get the rerendered trigger element
        const tooltipTriggers = document.querySelectorAll(selector)

        // fire up new tooltip instances (new instances are enabled by default)
        tooltipTriggers.forEach(trigger => hoverableTooltip(trigger))

        if (_debug) console.log(tooltipTriggers)
      }
    }

    function _remove() {
      if (_debug) console.log('%c### called gwHandleTooltip.remove ###', 'color: red')
      // remove all tooltips
      const tooltips = document.querySelectorAll('.tooltip')
      tooltips.forEach(tooltip => tooltip.remove())
    };


    return {
      init
    }
} )();
