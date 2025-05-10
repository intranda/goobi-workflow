/**
 * Show and hide goobi's ajaxLoader (∞).
 * @module gwAjaxLoader
 */
let _debug = false;
let _defaultSelector = '#ajaxloader';

export const init = function init(selector) {
    if (_debug) console.log('%c## Initialized gwAjaxLoader ##', 'color: #368ee0')

    // listen for jsf ajax envents
    if (typeof faces !== 'undefined') {
        faces.ajax.addOnEvent((data) => handleLoader(data, selector = _defaultSelector))
    }
}

const handleLoader = function handleAjaxLoader(data, selector) {
    if (_debug) console.log('%c## called gwAjaxLoader.handleLoader ##', 'color: orange')
    try {
        faces.ajax.addOnEvent(function (data) {
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
                    break;

            }
        });
    }
    catch (error) {
        if (_debug) console.log(error)
    }
}
