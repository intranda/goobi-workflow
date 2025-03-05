/** @description Show and hide inline help.
 */
export default gwToggleHelp = ( function() {
    'use strict';

    let _debug = false;

    function init(data) {
      if (_debug) console.log('%c## Initialized gwToggleHelp.js ##', 'color: #368ee0')

      if (data.status === 'success') {
        const elements = document.querySelectorAll(".help-block");
        for (let i = 0; i < elements.length; i++) {
          elements[i].style.display = elements[i].style.display == 'block' ? 'none' : 'block';
        }
      }

    }

  return {
    init
  }
} )();
