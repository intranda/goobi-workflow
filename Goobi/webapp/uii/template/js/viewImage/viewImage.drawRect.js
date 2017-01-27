var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    
    var drawingStyleClass = "drawing";
    
    var _active = false;
    var _drawing = false;
    var _overlayGroup = null;
    var _finishHook = null;
    var _viewerInputHook = null;
    var _hbAdd = 5;
    var _minDistanceToExistingRect = 0.01;
    var _deleteOldDrawElement = true;
    var _drawElement = null;
    var _drawPoint = null;
    
    osViewer.drawRect = {
        init: function() {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'osViewer.drawRect.init' );
                console.log( '##############################' );
            }
            _viewerInputHook = osViewer.viewer.addViewerInputHook( {
                hooks: [ {
                    tracker: "viewer",
                    handler: "clickHandler",
                    hookHandler: _disableViewerEvent
                // }, {
                // tracker: "viewer",
                // handler: "scrollHandler",
                // hookHandler: _disableViewerEvent
                }, {
                    tracker: "viewer",
                    handler: "dragHandler",
                    hookHandler: _onViewerDrag
                }, {
                    tracker: "viewer",
                    handler: "pressHandler",
                    hookHandler: _onViewerPress
                }, {
                    tracker: "viewer",
                    handler: "dragEndHandler",
                    hookHandler: _onViewerDragEnd
                } ]
            } );
        },
        startDrawing: function( overlayGroup, finishHook ) {
            _active = true;
            _overlayGroup = overlayGroup;
            _finishHook = finishHook;
        },
        endDrawing: function( removeLastElement ) {
            _active = false;
            _overlayGroup = null;
            _finishHook = null;
            if ( _drawElement && removeLastElement ) {
                osViewer.viewer.removeOverlay( _drawElement );
            }
            else {
                $( _drawElement ).removeClass( drawingStyleClass );
            }
        },
        isActive: function() {
            return _active;
        },
        isDrawing: function() {
            return _drawing;
        },
        removeLastDrawnElement() {
        	if(_drawElement) {        		
        		osViewer.viewer.removeOverlay( _drawElement );
        	}
        }
    }

    function _onViewerPress( event ) {
        if ( _active ) {
            _drawPoint = osViewer.viewer.viewport.viewerElementToViewportCoordinates( event.position );
            
            event.preventDefaultAction = false;
            return true;
        }
    }
    
    function _onViewerDrag( event ) {
        // if(_debug) {
        // console.log("Dragging: ");
        // console.log("_active = " + _active);
        // console.log("_drawing = " + _drawing);
        // console.log("_drawPoint = " + _drawPoint);
        // }
        if ( _drawing ) {
            var newPoint = osViewer.viewer.viewport.viewerElementToViewportCoordinates( event.position );
            var rect = new OpenSeadragon.Rect( _drawPoint.x, _drawPoint.y, newPoint.x - _drawPoint.x, newPoint.y - _drawPoint.y );
            if ( newPoint.x < _drawPoint.x ) {
                rect.x = newPoint.x;
                rect.width = _drawPoint.x - newPoint.x;
            }
            if ( newPoint.y < _drawPoint.y ) {
                rect.y = newPoint.y;
                rect.height = _drawPoint.y - newPoint.y;
            }
            osViewer.viewer.updateOverlay( _drawElement, rect, 0 );
            event.preventDefaultAction = true;
            return true;
            
        }
        else if ( _active && _drawPoint ) {
            var activeOverlay = osViewer.overlays.getDrawingOverlay();
            if ( activeOverlay && osViewer.transformRect && osViewer.transformRect.isActive()
                    && osViewer.overlays.contains( activeOverlay.rect, _drawPoint, _minDistanceToExistingRect) ) {
                _drawPoint = null;
                if ( _debug )
                    console.log( "Action overlaps active overlay" );
            }
            else {
                _drawing = true;
                if ( activeOverlay && _deleteOldDrawElement ) {
                    osViewer.overlays.removeOverlay( activeOverlay );
                }
                
                _drawElement = document.createElement( "div" );
                if ( _overlayGroup ) {
                    $( _drawElement ).addClass( _overlayGroup.styleClass );
                }
                $( _drawElement ).addClass( drawingStyleClass );
                var rect = new OpenSeadragon.Rect( _drawPoint.x, _drawPoint.y, 0, 0 );
                osViewer.viewer.addOverlay( _drawElement, rect, 1 );
            }
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function _onViewerDragEnd( event ) {
        if ( _drawing ) {
            _drawing = false;
            var newPoint = osViewer.viewer.viewport.viewerElementToViewportCoordinates( event.position );
            var rect = new OpenSeadragon.Rect( _drawPoint.x, _drawPoint.y, newPoint.x - _drawPoint.x, newPoint.y - _drawPoint.y );
            if ( newPoint.x < _drawPoint.x ) {
                rect.x = newPoint.x;
                rect.width = _drawPoint.x - newPoint.x;
            }
            if ( newPoint.y < _drawPoint.y ) {
                rect.y = newPoint.y;
                rect.height = _drawPoint.y - newPoint.y;
            }
            rect.hitBox = {
                l: rect.x - _hbAdd,
                t: rect.y - _hbAdd,
                r: rect.x + rect.width + _hbAdd,
                b: rect.y + rect.height + _hbAdd
            };
            
            var overlay = {
                type: osViewer.overlays.overlayTypes.RECTANGLE,
                element: _drawElement,
                rect: rect,
                group: _overlayGroup.name,
            };
            osViewer.overlays.setDrawingOverlay( overlay );
            if ( _finishHook ) {
                _finishHook( overlay );
            }
            
            event.preventDefaultAction = true;
            return true;
        }
        
    }
    
    function _disableViewerEvent( event ) {
        if ( _active ) {
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function checkForRectHit( point ) {
        var i;
        for ( i = 0; i < _rects.length; i++ ) {
            var x = _rects[ i ];
            if ( point.x > x.hitBox.l && point.x < x.hitBox.r && point.y > x.hitBox.t && point.y < x.hitBox.b ) {
                var topLeftHb = {
                    l: x.x - _hbAdd,
                    t: x.y - _hbAdd,
                    r: x.x + _hbAdd,
                    b: x.y + _hbAdd
                };
                var topRightHb = {
                    l: x.x + x.width - _hbAdd,
                    t: x.y - _hbAdd,
                    r: x.x + x.width + _hbAdd,
                    b: x.y + _hbAdd
                };
                var bottomRightHb = {
                    l: x.x + x.width - _hbAdd,
                    t: x.y + x.height - _hbAdd,
                    r: x.x + x.width + _hbAdd,
                    b: x.y + x.height + _hbAdd
                };
                var bottomLeftHb = {
                    l: x.x - _hbAdd,
                    t: x.y + x.height - _hbAdd,
                    r: x.x + _hbAdd,
                    b: x.y + x.height + _hbAdd
                };
                var topHb = {
                    l: x.x + _hbAdd,
                    t: x.y - _hbAdd,
                    r: x.x + x.width - _hbAdd,
                    b: x.y + _hbAdd
                };
                var rightHb = {
                    l: x.x + x.width - _hbAdd,
                    t: x.y + _hbAdd,
                    r: x.x + x.width + _hbAdd,
                    b: x.y + x.height - _hbAdd
                };
                var bottomHb = {
                    l: x.x + _hbAdd,
                    t: x.y + x.height - _hbAdd,
                    r: x.x + x.width - _hbAdd,
                    b: x.y + x.height + _hbAdd
                };
                var leftHb = {
                    l: x.x - _hbAdd,
                    t: x.y + _hbAdd,
                    r: x.x + _hbAdd,
                    b: x.y + x.height - _hbAdd
                };
            }
        }
    }
    
    return osViewer;
    
} )( viewImage || {}, jQuery );
