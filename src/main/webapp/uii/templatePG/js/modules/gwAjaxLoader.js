/** @description Show and hide goobi's ajaxLoader (∞).
 */
export default gwAjaxLoader = ( function() {
    'use strict';

    var _debug = false;
    var _defaultSelector = '#ajaxloader';

    function init(selector) {
      if (_debug) console.log('%c## Initialized gwAjaxLoader ##', 'color: #368ee0')

      // listen for jsf ajax envents
      jsf.ajax.addOnEvent((data) => _handleAjaxLoader(data, selector = _defaultSelector))

    }

    function _handleAjaxLoader(data, selector) {
      if (_debug) console.log('%c## called gwAjaxLoader._handleAjaxLoader ##', 'color: orange')
      try {

        jsf.ajax.addOnEvent(function (data) {
          const ajaxstatus = data.status; // Can be "begin", "complete" and "success"
          const ajaxloader = document.querySelector(selector);
          const source = data.source;
          const noLoaderGif = source.type == "hidden" && source.id.indexOf("header") == 0;

          switch (ajaxstatus) {
            case "begin": // This is called right before ajax request is been sent.
              if(ajaxloader && !noLoaderGif) ajaxloader.style.display = 'block';
              break;

            case "complete": // This is called right after ajax response is received.
              if(ajaxloader) ajaxloader.style.display = 'none';
              break;

            case "success": // This is called when ajax response is successfully processed.
             scrollDownScrollDowns();
              break;

          }
        });


      }
      catch (error) {
        if (_debug) console.log(error)
      }
    }

  return {
    init
  }
} )();
