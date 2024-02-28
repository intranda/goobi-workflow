/**
 * @description Sets focus on the first element with a data attribute of `data-gw-focus-on-load="true"`.
 * Alternatively an id, class etc. can be passed to query the element.
 */
export default gwFocusOnLoad = ( function() {
    'use strict';

    var _debug = false;
    var _defaultSelector = '[data-gw-focus-on-load="true"]';

    function init(selector) {
      if (_debug) console.log('%c### Initialized gwFocusOnload ###', 'color: #368ee0')

      window.addEventListener("DOMContentLoaded", _setFocus(selector));
    }

    function _setFocus(selector = _defaultSelector) {
      try {
        const el = document.querySelector(selector);
        el.focus();

        // Debugging
        if(_debug){
          console.log('%c### Called _setFocus ###', 'color: #368ee0')
          console.log('selector:', selector)
          console.log('queried element:', el)

        }
      }
      catch (error) {
        if (_debug) console.log(error)
      }
    }

    return {
      init
    }
} )();
