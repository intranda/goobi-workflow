var viewImage = (function (osViewer) {
    'use strict';

    var _debug = false;
    var _zoomSlider = {};

    osViewer.zoomSlider = {
        init: function () {
            if (_debug) {
                console.log('##############################');
                console.log('osViewer.zoomSlider.init');
                console.log('##############################');
            }

            if ($('#' + osViewer.defaults.global.zoomSlider)) {
                osViewer.zoomSlider.addZoomSlider(osViewer.defaults.global.zoomSlider);

                // handler for openSeadragon Object
                osViewer.viewer.addHandler('zoom', function (data) {
                    osViewer.zoomSlider.buttonToZoom(data.zoom);
                });
            }
        },
        buttonToMouse: function (mousePos) {
            if (_debug) {
                console.log('osViewer.zoomSlider.buttonToMouse: mousePos - ' + mousePos);
            }

            var offset = _zoomSlider.$button.width() / 2;
            var newPos = mousePos - offset;
            if (newPos < 0) {
                newPos = 0;
            }
            if (newPos + 2 * offset > _zoomSlider.absoluteWidth) {
                newPos = _zoomSlider.absoluteWidth - 2 * offset;
            }
            _zoomSlider.$button.css({
                left: newPos
            });
            _zoomSlider.buttonPosition = newPos;
            var factor = (newPos / (_zoomSlider.absoluteWidth - offset * 2));
            var newScale = osViewer.viewer.viewport.getMinZoom() + (osViewer.viewer.viewport.getMaxZoom() - osViewer.viewer.viewport.getMinZoom()) * factor;

            if (_debug) {
                console.log('osViewer.zoomSlider.buttonToMouse: newScale - ' + newScale);
            }

            osViewer.controls.myZoomTo(newScale);
        },
        buttonToZoom: function (scale) {
            if (_debug) {
                console.log('osViewer.zoomSlider.buttonToZoom: scale - ' + scale);
            }

            if (!_zoomSlider || !osViewer.viewer.viewport) {
                return;
            }
            var newPos = ((scale - osViewer.viewer.viewport.getMinZoom()) / (osViewer.viewer.viewport.getMaxZoom() - osViewer.viewer.viewport.getMinZoom())) * (_zoomSlider.absoluteWidth - _zoomSlider.$button.width());

            if (Math.abs(osViewer.viewer.viewport.getMaxZoom() - scale) < 0.0000000001) {
                newPos = _zoomSlider.absoluteWidth - _zoomSlider.$button.width();
            }

            if (newPos < 0) {
                newPos = 0;
            }

            _zoomSlider.$button.css({
                left: newPos
            });
            _zoomSlider.buttonPosition = newPos;
        },
        zoomSliderMouseUp: function () {
            if (_debug) {
                console.log('osViewer.zoomSlider.zoomSliderMouseUp');
            }

            _zoomSlider.mousedown = false;
        },
        zoomSliderMouseMove: function (evt) {
            if (_debug) {
                console.log('osViewer.zoomSlider.zoomSliderMouseMove: evt - ' + evt);
            }

            if (!_zoomSlider.mousedown) {
                return;
            }
            var offset = $(this).offset();
            var hitX = evt.pageX - offset.left;
            osViewer.zoomSlider.buttonToMouse(hitX);

            if (_debug) {
                console.log('osViewer.zoomSlider.zoomSliderMouseMove: moving - ' + hitX);
            }
        },
        zoomSliderMouseDown: function (evt) {
            if (_debug) {
                console.log('osViewer.zoomSlider.zoomSliderMouseDown: evt - ' + evt);
            }

            _zoomSlider.mousedown = true;
            var offset = $(this).offset();
            var hitX = evt.pageX - offset.left;
            osViewer.zoomSlider.buttonToMouse(hitX);
        },
        buttonMouseDown: function () {
            if (_debug) {
                console.log('osViewer.zoomSlider.buttonMouseDown');
            }

            _zoomSlider.mousedown = true;

            return false;
        },
        // _zoomSliderClick: function (evt) {
        //     var offset = $(this).offset();
        //     var hitX = evt.pageX - offset.left;
        //     console.log(hitX);
        // },
        addZoomSlider: function (elementId) {
            if (_debug) {
                console.log('osViewer.zoomSlider.addZoomSlider: elementId - ' + elementId);
            }

            _zoomSlider.$element = $('#' + elementId);
            _zoomSlider.$button = _zoomSlider.$element.children('.zoomslider-handle');
            _zoomSlider.$button.css({
                position: 'relative',
                display: 'block'
            });
            _zoomSlider.buttonPosition = 0;
            _zoomSlider.absoluteWidth = _zoomSlider.$element.innerWidth();
            _zoomSlider.mousedown = false;
            _zoomSlider.$button.on('mousedown', osViewer.zoomSlider.buttonMouseDown);
            _zoomSlider.$element.click(osViewer.zoomSlider._zoomSliderClick);
            _zoomSlider.$element.mousedown(osViewer.zoomSlider.zoomSliderMouseDown);
            _zoomSlider.$element.mousemove(osViewer.zoomSlider.zoomSliderMouseMove);
            $(document).on('mouseup', osViewer.zoomSlider.zoomSliderMouseUp);
        }
    };

    return osViewer;

})(viewImage || {}, jQuery);