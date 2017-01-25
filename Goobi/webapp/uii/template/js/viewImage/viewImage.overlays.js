var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    var _focusStyleClass = 'focus';
    var _highlightStyleClass = 'highlight';
    var _overlayFocusHook = null;
    var _overlayClickHook = null;
    var _drawingOverlay = null;
    var _overlays = [];
    var _defaults = {};
    
    var _initializedCallback = null;

    osViewer.overlays = {
        init: function( config ) {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'osViewer.overlays.init' );
                console.log( '##############################' );
            }
            
            $.extend( true, _defaults, config );
            
            osViewer.observables.overlayRemove.subscribe(function( event ) {
                if ( event.element ) {
                    $( event.element ).remove();
                }
            });
            
            osViewer.observables.viewerOpen.subscribe( function( data ) {
                
                for ( var index in _defaults.image.highlightCoords ) {
                    var highlightCoords = _defaults.image.highlightCoords[ index ];
                    osViewer.overlays.draw( highlightCoords.name, highlightCoords.displayTooltip );
                }
                $( '[data-toggle="tooltip"]' ).tooltip( {
                    trigger: 'manual'
                } );
                if ( _initializedCallback ) {
                    _initializedCallback();
                }
            } );
        },
        onInitialized: function( callback ) {
            var oldCallback = _initializedCallback;
            _initializedCallback = function() {
                if ( oldCallback ) {
                    oldCallback();
                }
                callback();
            }
        },
        onFocus: function( hook ) {
            var tempHook = _overlayFocusHook;
            _overlayFocusHook = function( overlay, focus ) {
                if ( tempHook )
                    tempHook( overlay, focus );
                hook( overlay, focus );
            }
        },
        onClick: function( hook ) {
            var tempHook = _overlayClickHook;
            _overlayClickHook = function( overlay ) {
                if ( tempHook )
                    tempHook( overlay );
                hook( overlay );
            }
        },
        getOverlays: function() {
            return _overlays.slice();
        },
        getRects: function() {
            return _overlays.filter( function( overlay ) {
                return overlay.type === osViewer.overlays.overlayTypes.RECTANGLE
            } ).slice();
        },
        getLines: function() {
            return _overlays.filter( function( overlay ) {
                return overlay.type === osViewer.overlays.overlayTypes.LINE
            } ).slice();
        },
        draw: function( group, displayTitle ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.draw: group - ' + group );
                console.log( 'osViewer.overlays.draw: displayTitle - ' + displayTitle );
            }
            
            var coordList = _defaults.getCoordinates( group );
            
            if ( coordList ) {
                for ( var index in coordList.coordinates ) {
                    var coords = coordList.coordinates[ index ];
                    var title = displayTitle && coords.length > 4 ? coords[ 4 ] : '';
                    var id = coords.length > 5 ? coords[ 5 ] : index;
                    _createRectangle( coords[ 0 ], coords[ 1 ], coords[ 2 ] - coords[ 0 ], coords[ 3 ] - coords[ 1 ], title, id, group );
                }
            }
        },
        unDraw: function( group ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.unDraw: group - ' + group );
            }
            
            var newRects = [];
            _overlays = _overlays.filter( function( overlay ) {
                if ( overlay.group === group ) {
                    osViewer.viewer.removeOverlay( overlay.element );
                    return false;
                }
                else {
                    return true;
                }
            } );
        },
        highlight: function( group ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.highlight: group - ' + group );
            }
            
            _overlays.filter( function( overlay ) {
                return overlay.group === group;
            } ).forEach( function( overlay ) {
                if ( overlay.element ) {
                    overlay.element.highlight( true );
                }
            } );
            
        },
        unHighlight: function( group ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.unHighlight: group - ' + group );
            }
            
            _overlays.filter( function( overlay ) {
                return overlay.group === group;
            } ).forEach( function( overlay ) {
                if ( overlay.element ) {
                    overlay.element.highlight( false );
                }
            } );
            
        },
        focusBox: function( group, id ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.highlightBox: group - ' + group );
                console.log( 'osViewer.overlays.highlightBox: id - ' + id );
            }
            _overlays.filter( function( overlay ) {
                return overlay.group === group;
            } ).forEach( function( overlay ) {
                if ( overlay.element ) {
                    overlay.element.focus( overlay.id === id );
                }
            } );
            
        },
        addOverlay: function( overlay ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.addOverlay: overlay - ' + overlay );
            }
            
            _overlays.push( overlay );
            if ( overlay.element ) {
                osViewer.viewer.updateOverlay( overlay.element, overlay.rect, 0 );
            }
        },
        removeOverlay: function( overlay ) {
            if ( overlay ) {
                if ( _debug )
                    console.log( "Removing overlay " + overlay.id );
                var index = _overlays.indexOf( overlay );
                _overlays.splice( index, 1 );
                if ( overlay.element ) {
                    osViewer.viewer.removeOverlay( overlay.element );
                }
            }
        },
        drawRect: function( rectangle, group, title, id ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.drawRect: rectangle - ' + rectangle );
                console.log( 'osViewer.overlays.drawRect: group - ' + group );
            }
            
            _createRectangle( rectangle.x, rectangle.y, rectangle.width, rectangle.height, title ? title : "", id ? id : "", group );
        },
        drawLine: function( point1, point2, group ) {
            if ( _debug ) {
                console.log( 'osViewer.overlays.drawLine: point1 - ' + point1 );
                console.log( 'osViewer.overlays.drawLine: point2 - ' + point2 );
                console.log( 'osViewer.overlays.drawLine: group - ' + group );
            }
            
            _createLine( point1.x, point1.y, point2.x, point2.y, "", "", group );
        },
        showOverlay: function( overlay ) {
            if ( overlay && !overlay.element ) {
                _drawOverlay( overlay );
                if ( _overlayFocusHook ) {
                    _overlayFocusHook( overlay, true );
                }
            }
            
        },
        hideOverlay: function( overlay ) {
            if ( overlay && overlay.element && _drawingOverlay != overlay ) {
                _undrawOverlay( overlay );
                if ( _overlayFocusHook ) {
                    _overlayFocusHook( overlay, false );
                }
            }
        },
        getOverlay: function( id, group ) {
            var overlay =  _overlays.find( function( overlay ) {
                if ( group ) {
                    return overlay.id === id && overlay.group === group;
                }
                else {
                    return overlay.id === id
                }
            } );
//            console.log("search for overlay with id = " + id);
//            console.log("Found overlay ", overlay);
            return overlay;
        },
        getCoordinates: function( overlay ) {
            if(_debug){
                console.log("getCoordinates - overlay", overlay);
            }
            if ( overlay.type === osViewer.overlays.overlayTypes.RECTANGLE ) {
                var transformedRect = osViewer.viewer.viewport.viewportToImageRectangle( overlay.rect );
                transformedRect = transformedRect.times( _getScaleToOriginalSize() );
                return transformedRect;
            }
            else if ( overlay.type === osViewer.overlays.overlayTypes.LINE ) {
                var p1 = osViewer.viewer.viewport.viewportToImageCoordinates( overlay.poin1 );
                var p2 = osViewer.viewer.viewport.viewportToImageCoordinates( overlay.poin2 );
                return {
                    point1: p1,
                    point2: p2
                };
            }
        },
        getDrawingOverlay: function() {
            return _drawingOverlay;
        },
        setDrawingOverlay: function( overlay ) {
            _drawingOverlay = overlay;
        },
        showHiddenOverlays: function() {
            osViewer.viewer.addViewerInputHook( {
                hooks: [ {
                    tracker: "viewer",
                    handler: "moveHandler",
                    hookHandler: _onViewerMove
                } ]
            } );
        },
        contains: function( rect, point, precision ) {
            if ( precision == null ) {
                precision = 0;
            }
            return _isInside( rect, point, precision );
        },
        overlayTypes: {
            RECTANGLE: "rectangle",
            LINE: "line"
        },
        getRotated: function(point) {
//            var rotation = osViewer.viewer.viewport.getRotation();        
//            var center = osViewer.viewer.viewport.getCenter( true );
//            point = point.rotate(-rotation, center);
            return point;
        }
    };
    
    function _createLine( x1, y1, x2, y2, title, id, group ) {
        if ( _debug ) {
            console.log( '------------------------------' );
            console.log( 'Overlays _createLine: x1 - ' + x1 );
            console.log( 'Overlays _createLine: y1 - ' + y1 );
            console.log( 'Overlays _createLine: x2 - ' + x2 );
            console.log( 'Overlays _createLine: y2 - ' + y2 );
            console.log( 'Overlays _createLine: title - ' + title );
            console.log( 'Overlays _createLine: id - ' + id );
            console.log( 'Overlays _createLine: group - ' + group );
            console.log( '------------------------------' );
        }
        
        x1 = _scaleToImageSize( x1 );
        y1 = _scaleToImageSize( y1 );
        x2 = _scaleToImageSize( x2 );
        y2 = _scaleToImageSize( y2 );
        
        var p1 = new OpenSeadragon.Point( x1, y1 );
        var p2 = new OpenSeadragon.Point( x2, y2 );
        var length = p1.distanceTo( p2 );
        
        var angle = _calculateAngle( p1, p2 );
        var beta = ( 180 - angle ) / 2;
        console.log( "drawing line with length = " + length + " and angle = " + angle );
        
        y1 += length / 2 * Math.sin( angle * Math.PI / 180 );
        x1 -= length / 2 * Math.sin( angle * Math.PI / 180 ) / Math.tan( beta * Math.PI / 180 );
        
        var rectangle = osViewer.viewer.viewport.imageToViewportRectangle( x1, y1, length, 1 );
        var p1Viewer = osViewer.viewer.viewport.imageToViewportCoordinates( p1 );
        var p2Viewer = osViewer.viewer.viewport.imageToViewportCoordinates( p2 );
        var overlay = {
            type: osViewer.overlays.overlayTypes.LINE,
            rect: rectangle,
            angle: angle,
            point1: p1Viewer,
            point2: p2Viewer,
            group: group,
            id: id,
            title: title
        };
        var overlayStyle = _defaults.getOverlayGroup( overlay.group );
        if ( !overlayStyle.hidden ) {
            _drawOverlay( overlay );
        }
        _overlays.push( overlay );
        
    }
    
    /**
     * coordinates are in original image space
     */
    function _createRectangle( x, y, width, height, title, id, group ) {
        if ( _debug ) {
            console.log( '------------------------------' );
            console.log( 'Overlays _createRectangle: x - ' + x );
            console.log( 'Overlays _createRectangle: y - ' + y );
            console.log( 'Overlays _createRectangle: width - ' + width );
            console.log( 'Overlays _createRectangle: height - ' + height );
            console.log( 'Overlays _createRectangle: title - ' + title );
            console.log( 'Overlays _createRectangle: id - ' + id );
            console.log( 'Overlays _createRectangle: group - ' + group );
            console.log( '------------------------------' );
        }
        
        x = _scaleToImageSize( x );
        y = _scaleToImageSize( y );
        width = _scaleToImageSize( width );
        height = _scaleToImageSize( height );
        var rectangle = osViewer.viewer.viewport.imageToViewportRectangle( x, y, width, height );
        var overlay = {
            type: osViewer.overlays.overlayTypes.RECTANGLE,
            rect: rectangle,
            group: group,
            id: id,
            title: title
        };
        var overlayStyle = _defaults.getOverlayGroup( overlay.group );
        if ( !overlayStyle.hidden ) {
            _drawOverlay( overlay );
        }
        _overlays.push( overlay );
        
    }
    
    function _undrawOverlay( overlay ) {
        osViewer.viewer.removeOverlay( overlay.element );
        overlay.element = null;
    }
    
    function _drawOverlay( overlay ) {
        if(_debug) {
            console.log("viewImage.overlays._drawOverlay");
            console.log("overlay: ", overlay);
        }
        var element = document.createElement( "div" );
        var overlayStyle = _defaults.getOverlayGroup( overlay.group );
        if ( overlayStyle ) {
            if(_debug)console.log("overlay style", overlayStyle);
            element.title = overlay.title;
            $( element ).attr( "data-toggle", "tooltip" );
            $( element ).attr( "data-placement", "auto left" );
            $( element ).addClass( overlayStyle.styleClass );
            
            if ( overlay.type === osViewer.overlays.overlayTypes.LINE ) {
                _rotate( overlay.angle, element );
            }
            
            if ( overlayStyle.interactive ) {
                
                element.focus = function( focus ) {
                    if ( focus ) {
                        $( element ).addClass( _focusStyleClass );
                        $( element ).tooltip( "show" );
                    }
                    else {
                        $( element ).removeClass( _focusStyleClass );
                        $( element ).tooltip( "hide" );
                    }
                    if ( _overlayFocusHook ) {
                        _overlayFocusHook( overlay, focus );
                    }
                };
                
                element.highlight = function( focus ) {
                    if ( focus ) {
                        $( element ).addClass( _highlightStyleClass );
                    }
                    else {
                        $( element ).removeClass( _highlightStyleClass );
                    }
                };
                
                $( element ).on( "mouseover", function() {
                    if ( _debug ) {
                        console.log( 'Overlays _drawOverlay: mouse over - ' + overlayStyle.name );
                    }
                    osViewer.overlays.focusBox( overlay.group, overlay.id );
                } );
                $( element ).on( "mouseout", function() {
                    if ( _debug ) {
                        console.log( 'Overlays _drawOverlay: mouse out - ' + overlayStyle.name );
                    }
                    element.focus( false );
                } );
                $( element ).on( "click", function() {
                    if ( _overlayClickHook ) {
                        _overlayClickHook( overlay );
                    }
                } );
            }
            overlay.element = element;
            osViewer.viewer.addOverlay( element, overlay.rect, 0 );
        }
    }
    
    function _rotate( angle, mapElement ) {
        if ( _debug ) {
            console.log( 'Overlays _rotate: angle - ' + angle );
            console.log( 'Overlays _rotate: mapElement - ' + mapElement );
        }
        
        if ( angle !== 0 ) {
            $( mapElement ).css( "-moz-transform", "rotate(" + angle + "deg)" );
            $( mapElement ).css( "-webkit-transform", "rotate(" + angle + "deg)" );
            $( mapElement ).css( "-ms-transform", "rotate(" + angle + "deg)" );
            $( mapElement ).css( "-o-transform", "rotate(" + angle + "deg)" );
            $( mapElement ).css( "transform", "rotate(" + angle + "deg)" );
            var sin = Math.sin( angle );
            var cos = Math.cos( angle );
            $( mapElement ).css(
                    "filter",
                    "progid:DXImageTransform.Microsoft.Matrix(M11=" + cos + ", M12=" + sin + ", M21=-" + sin + ", M22=" + cos
                            + ", sizingMethod='auto expand'" );
        }
    }
    
    function _calculateAngle( p1, p2 ) {
        if ( _debug ) {
            console.log( 'Overlays _calculateAngle: p1 - ' + p1 );
            console.log( 'Overlays _calculateAngle: p2 - ' + p2 );
        }
        
        var dx = p2.x - p1.x;
        var dy = p2.y - p1.y;
        var radians = null;
        
        if ( dx > 0 ) {
            radians = Math.atan( dy / dx );
            return radians * 180 / Math.PI;
        }
        else if ( dx < 0 ) {
            radians = Math.atan( dy / dx );
            return radians * 180 / Math.PI + 180;
        }
        else if ( dy < 0 ) {
            return 270;
        }
        else {
            return 90;
        }
    }
    
    function _getScaleToOriginalSize() {
        var displaySize = osViewer.viewer.world.getItemAt(0).source.dimensions.x;//osViewer.viewer.viewport.contentSize.x;
        return osViewer.getImageInfo().width / displaySize;
    }
    
    function _scaleToOriginalSize( value ) {
        if ( _debug ) {
            console.log( 'Overlays _scaleToOriginalSize: value - ' + value );
        }
        
        var displaySize = osViewer.viewer.world.getItemAt(0).source.dimensions.x;//osViewer.viewer.viewport.contentSize.x;
        return value / displaySize * osViewer.getImageInfo().width;
    }
    
    function _scaleToImageSize( value ) {
        if ( _debug ) {
            console.log( 'Overlays _scaleToImageSize: value - ' + value );
        }
        
        var displaySize = osViewer.viewer.world.getItemAt(0).source.dimensions.x;//osViewer.viewer.viewport.contentSize.x;
        return value * displaySize / osViewer.getImageInfo().width;
    }
    
    function _isInside( rect, point, extra ) {
        return point.x > rect.x - extra && point.x < ( rect.x + rect.width + extra ) && point.y > rect.y - extra
                && point.y < ( rect.y + rect.height + extra );
    }
    
    function _onViewerMove( event ) { 
        var position = event.position;
        var ieVersion = viewerJS.helper.detectIEVersion();
        if(ieVersion && ieVersion === 10) {
//            console.log("Correct position for ie ", ieVersion);
            position.x += $(window).scrollLeft();
            position.y += $(window).scrollTop();
        }
//        console.log(  "viewer move ", position);
        var point = osViewer.viewer.viewport.viewerElementToViewportCoordinates( position );
        _overlays.forEach( function( o ) {
            if ( _isInside( o.rect, point, 0 ) ) {
                osViewer.overlays.showOverlay( o );
            }
            else {
                osViewer.overlays.hideOverlay( o );
            }
        } );
    }
    
    return osViewer;
    
} )( viewImage || {}, jQuery );
