var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    var _currentZoom;
    var _currentBounds;
    var _panning = false;
    
    osViewer.controls = {
        init: function() {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'osViewer.controls.init' );
                console.log( '##############################' );
            }
            
//            osViewer.viewer.addHandler( "close", function( data ) {
//                if(_debug)console.log("Closeing openSeadragon viewer");
//                console.log("last position = ");
//                console.log(osViewer.controls.getLocation());
//            });
            
            osViewer.viewer.addHandler( "open", function( data ) {
                var minZoom = osViewer.viewer.viewport.getHomeZoom();
                if ( _debug )
                    console.log( "minZoom = " + minZoom + " * " + osViewer.defaults.global.minZoomLevel + " = " + minZoom
                            * osViewer.defaults.global.minZoomLevel );
                osViewer.viewer.viewport.minZoomLevel = minZoom * osViewer.defaults.global.minZoomLevel;
                if(osViewer.defaults.image.location) {
                    if(_debug) {
                        console.log("Set initial location");
                        console.log(osViewer.defaults.image.location);
                    }
                    osViewer.controls.setLocation(osViewer.defaults.image.location);
                }
                
                osViewer.viewer.addHandler( "zoom", function( data ) {
                    if ( !_panning ) {
                        var oldZoom = _currentZoom;
                        _currentZoom = osViewer.viewer.viewport.getZoom();
                        if ( oldZoom && _currentZoom <= osViewer.viewer.viewport.minZoomLevel && oldZoom > _currentZoom ) {
                            if ( _debug )
                                console.log( "Zoomed out: Panning home" );
                            osViewer.controls.panHome();
                        }
                    }
                } );

//                osViewer.viewer.addHandler( "pan", function( data ) {
//                    if ( !_panning ) {
//                        var currentCenter = osViewer.viewer.viewport.getCenter(true);
//                        _currentBounds = osViewer.viewer.viewport.getBounds(true);
//                        console.log(currentCenter);
//                        console.log(_currentBounds);
//                    }
//                } );
            } );
            
        },
        getLocation: function() {
            return {
                x: osViewer.controls.getCenter().x,
                y: osViewer.controls.getCenter().y,
                zoom: osViewer.controls.getZoom(),
                rotation: osViewer.controls.getRotation(),
            }
        },
        setLocation: function(location) {
            if(location.rotation) {                
                osViewer.controls.setRotation(location.rotation);
            }
            if(location.zoom) {                
                osViewer.controls.setZoom(location.zoom);
            }
            if(location.x && location.y) {                
                osViewer.controls.setCenter(location);
            }
        },
        getCenter: function() {
            if(_debug) {
                console.log("image center is " + osViewer.viewer.viewport.getCenter(true));
            }
            return osViewer.viewer.viewport.getCenter(true);
        },
        setCenter: function(center) {

            if(_debug) {
                console.log("Setting image center to ");
               console.log(center);
            }           
            
            osViewer.viewer.viewport.panTo(center, true);
            
          },
        getZoom: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.getZoom' );
            }
            return osViewer.viewer.viewport.getZoom(true);
        },
        setZoom: function(zoom) {
            osViewer.controls.myZoomTo(zoom);
        },
        myZoomTo: function( zoomTo ) {
            
            if ( _debug ) {
                console.log( 'osViewer.controls.myZoomTo: zoomTo - ' + zoomTo );
            }
            
            var zoomBy = parseFloat( zoomTo ) / osViewer.viewer.viewport.getZoom();
            
            if ( _debug ) {
                console.log( 'osViewer.controls.myZoomTo: zoomBy - ' + zoomBy );
            }
            
            osViewer.viewer.viewport.zoomBy( zoomBy, osViewer.viewer.viewport.getCenter( false ), true );
        },
        setFullScreen: function( enable ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.setFullScreen: enable - ' + enable );
            }
            
            osViewer.viewer.setFullScreen( enable );
        },
        panHome: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.panHome' );
            }
            _panning = true;
            osViewer.viewer.viewport.goHome( false );
//            osViewer.viewer.viewport.zoomTo( osViewer.viewer.viewport.getMinZoom(), null, true );
            _panning = false;
        },
        goHome: function( resetRotation ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.goHome: bool - ' + resetRotation );
            }
            
            osViewer.viewer.viewport.goHome( true );
            osViewer.viewer.viewport.zoomTo( osViewer.viewer.viewport.getMinZoom(), null, true );
            if ( resetRotation ) {
                osViewer.controls.rotateTo( 0 );
            }
        },
        zoomIn: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.zoomIn: zoomSpeed - ' + osViewer.defaults.global.zoomSpeed );
            }
            
            osViewer.viewer.viewport.zoomBy( osViewer.defaults.global.zoomSpeed, osViewer.viewer.viewport.getCenter( false ), false );
        },
        zoomOut: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.zoomOut: zoomSpeed - ' + osViewer.defaults.global.zoomSpeed );
            }
            
            osViewer.viewer.viewport.zoomBy( 1 / osViewer.defaults.global.zoomSpeed, osViewer.viewer.viewport.getCenter( false ), false );
        },
        rotateRight: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateRight' );
            }
            
            var newRotation = osViewer.viewer.viewport.getRotation() + 90;
            osViewer.controls.rotateTo( newRotation );
        },
        rotateLeft: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateLeft' );
            }
            
            var newRotation = osViewer.viewer.viewport.getRotation() - 90;
            osViewer.controls.rotateTo( newRotation );
        },
        getRotation: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.getRotation' );
            }
            return osViewer.viewer.viewport.getRotation();
        },
        setRotation: function( rotation ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.setRotation: rotation - ' + rotation);
            }
            return osViewer.controls.rotateTo(rotation);
        },
        rotateTo: function( newRotation ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateTo: newRotation - ' + newRotation );
            }
            
            var zoomedOut = false;
            var zoomDiffToHomeZoom = osViewer.viewer.viewport.getZoom() - osViewer.viewer.viewport.getHomeZoom();
            var zoomDiffToMinZoom = osViewer.viewer.viewport.getZoom() - osViewer.viewer.viewport.getMinZoom();
            if ( Math.abs( zoomDiffToMinZoom ) < 0.000000001 || Math.abs( zoomDiffToHomeZoom ) < 0.000000001 || zoomDiffToHomeZoom < 0 ) {
                osViewer.viewer.viewport.zoomTo( osViewer.viewer.viewport.getMinZoom(), null, true );
                zoomedOut = true;
            }
            if(_debug)console.log("zoomed out: " + zoomedOut);
            osViewer.viewer.viewport.setRotation( newRotation );
            
            if ( newRotation % 180 !== 0 ) {
                var imageBounds = osViewer.viewer.viewport.imageToViewportCoordinates( osViewer.viewer.viewport.contentSize );
                var minZoom = imageBounds.x / imageBounds.y;
                console.log( "minZoom = " + imageBounds.x + "/" + imageBounds.y + " = " + minZoom );
                if ( minZoom < 1 ) {
                    osViewer.viewer.viewport.minZoomLevel = minZoom * osViewer.defaults.global.minZoomLevel;
                    if ( zoomedOut ) {
                        osViewer.viewer.viewport.zoomTo( minZoom * osViewer.defaults.global.minZoomLevel, null, true );
                    }
                }
                else {
                    osViewer.viewer.viewport.minZoomLevel = 1 / minZoom * osViewer.defaults.global.minZoomLevel;
                    if ( zoomedOut ) {
                        osViewer.viewer.viewport.zoomTo( 1 / minZoom * osViewer.defaults.global.minZoomLevel, null, true );
                    }
                }
                osViewer.viewer.viewport.defaultZoomLevel = osViewer.viewer.viewport.minZoomLevel;
            } else {
                var minZoom = osViewer.viewer.viewport.getHomeZoom();
                osViewer.viewer.viewport.minZoomLevel = minZoom * osViewer.defaults.global.minZoomLevel;
                osViewer.viewer.viewport.defaultZoomLevel = osViewer.viewer.viewport.minZoomLevel;
                if ( zoomedOut ) {
                    osViewer.viewer.viewport.zoomTo(osViewer.viewer.viewport.defaultZoomLevel);
                }
            }
            
            if ( osViewer.overlays ) {
                var rects = osViewer.overlays.getRects();
                for ( var i in rects ) {
                    var rect = new OpenSeadragon.Rect( rects[ i ].rect.x, rects[ i ].rect.y, rects[ i ].rect.width, rects[ i ].rect.height );
                    if ( newRotation === 90 ) {
                        rect.x += ( rect.height - rect.width ) / 2;
                        rect.y += ( rect.height - rect.width ) / 2;
                    }
                    else if ( newRotation === 270 || newRotation === -90 ) {
                        rect.x -= ( rect.height - rect.width ) / 2;
                        rect.y -= ( rect.height - rect.width ) / 2;
                    }
                    osViewer.viewer.updateOverlay( rects[ i ].rectElement, rect, 0 );
                }
            }
        }
    };
    
    return osViewer;
    
} )( viewImage || {}, jQuery );