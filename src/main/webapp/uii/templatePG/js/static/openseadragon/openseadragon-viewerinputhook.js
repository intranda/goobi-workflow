//! OpenSeadragonViewerInputHook 1.1.0
//! Build date: 2014-01-13
//! Git commit: v1.0.0-13-g7e47873
//! https://github.com/msalsbery/OpenSeadragonViewerInputHook
/* 
 * Copyright (c) 2013-2014 Mark Salsbery
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


/**
 * @file
 * @version  OpenSeadragonViewerInputHook 1.1.0
 * @author Mark Salsbery <msalsbery@hotmail.com>
 *
 */

/**
 * @module openseadragon-viewerinputhook
 * @version  OpenSeadragonViewerInputHook 1.1.0
 *
 */


(function(OSD, $, undefined) {

    if (!OSD.version || OSD.version.major < 1) {
        throw new Error('OpenSeadragonViewerInputHook requires OpenSeadragon version 1.0.0+');
    }

    /**
     * Creates a new ViewerInputHook attached to the viewer.
     *
     * @method addViewerInputHook
     * @memberof external:"OpenSeadragon.Viewer"#
     * @param {Object} options
     * @param {Object[]} [options.hooks]
     * @returns {OpenSeadragonImaging.ViewerInputHook}
     *
     **/
    OSD.Viewer.prototype.addViewerInputHook = function(options) {
        options = options || {};
        options.viewer = this;
        return new $.ViewerInputHook(options);
    };

    /**
     * Creates a new ViewerInputHook attached (optionally) to the viewer instance passed in the options parameter.
     *
     * @class ViewerInputHook
     * @classdesc Provides hooks into the OpenSeadragon viewer event pipeline.
     * @memberof OpenSeadragonImaging
     * @param {Object} options
     * @param {external:"OpenSeadragon.Viewer"} [options.viewer] - Reference to OpenSeadragon viewer to attach to.
     * @param {Object[]} options.hooks
     *
     **/
    $.ViewerInputHook = function(options) {

        var curHook, curTracker;

        options = options || {};
        options.hooks = options.hooks || [];

        this.viewerTrackers = {};

        if (options.viewer) {
            this.viewerTrackers.viewer = options.viewer.innerTracker;
            this.viewerTrackers.viewer_outer = options.viewer.outerTracker;
        }

        for (curHook = 0; curHook < options.hooks.length; curHook++) {
            if (typeof options.hooks[curHook].tracker === 'string') {
                if (!options.viewer) {
                    throw new Error('A viewer must be specified.');
                }
                curTracker = this.viewerTrackers[options.hooks[curHook].tracker];
                if (curTracker === undefined) {
                    throw new Error('Unknown tracker specified: ' + options.hooks[curHook].tracker);
                }
            }
            else {
                curTracker = options.hooks[curHook].tracker;
            }
            /*jshint loopfunc:true*/
            (function (_this, tracker, handler, hookHandler)  {
                var origHandler = tracker[handler];
                tracker[handler] = function (event) {
                    return _this.callHandlers(hookHandler, origHandler, event);
                };
            }(this, curTracker, options.hooks[curHook].handler, options.hooks[curHook].hookHandler));
            /*jshint loopfunc:false*/
        }
    };


    /**
     * ViewerInputHook version.
     * @member {Object} OpenSeadragonImaging.ViewerInputHook.version
     * @property {String} versionStr - The version number as a string ('major.minor.revision').
     * @property {Number} major - The major version number.
     * @property {Number} minor - The minor version number.
     * @property {Number} revision - The revision number.
     */
    /* jshint ignore:start */
    $.ViewerInputHook.version = {
        versionStr: '1.1.0',
        major: 1,
        minor: 1,
        revision: 0
    };
    /* jshint ignore:end */


    $.ViewerInputHook.prototype.callHandlers = function (hookHandler, origHandler, event) {
        var ret = hookHandler(event);
        if (origHandler && !event.stopHandlers) {
            ret = origHandler(event);
        }
        return event.stopBubbling ? false : ret;
    };

}(OpenSeadragon, window.OpenSeadragonImaging = window.OpenSeadragonImaging || {}));
