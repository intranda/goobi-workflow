var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    var _currentZoom;
    var _zoomedOut = true;
    var _panning = false;
    var _fadeout = null;
    var _config = {};
      
    osViewer.controls = {
        init: function( config ) {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'osViewer.controls.init' );
                console.log( '##############################' );
            }
            
            _config = config;
            if(osViewer.controls.persistance) {
                osViewer.controls.persistance.init(_config);
            }
            if(_debug) {                
                console.log("Setting viewer location to", _config.image.location);
            }
            
            //set location after viewport update
            osViewer.observables.redrawRequired
            .sample(osViewer.observables.viewportUpdate)
            .filter(function(event) {return osViewer.controls ? true : false})
            .subscribe(function(event) {
                setLocation(event, osViewer)
                osViewer.controls.setPanning( false );
            });
            
            //zoom home if min zoom reached
            osViewer.observables.viewerZoom.subscribe( function( event ) {
                if ( _debug ) {
                    console.log( "zoom to " + osViewer.viewer.viewport.getZoom( true ) );
                }
                if ( !osViewer.controls.isPanning() ) {
                    var currentZoom = osViewer.viewer.viewport.getZoom();
                    
                    if ( currentZoom <= osViewer.viewer.viewport.minZoomLevel ) {
                        if ( _debug ) {
                            console.log( "Zoomed out: Panning home" );
                        }
                        osViewer.controls.setPanning(true);
                        osViewer.controls.goHome( true );
                        osViewer.controls.setPanning(false);
                    }
                }
            } );
            
        },
        getLocation: function() {
            return {
                x: osViewer.controls.getCenter().x,
                y: osViewer.controls.getCenter().y,
                zoom: osViewer.controls.getZoom()/osViewer.controls.getCurrentRotationZooming(),
                rotation: osViewer.controls.getRotation(),
            }
        },
        getCenter: function() {
            if ( _debug ) {
                console.log( "image center is " + osViewer.viewer.viewport.getCenter( true ) );
            }
            return osViewer.viewer.viewport.getCenter( true );
        },
        setCenter: function( center ) {
            
            if ( _debug ) {
                console.log( "Setting image center to " );
                console.log( center );
            }
            
            osViewer.viewer.viewport.panTo( center, true );
            
        },
        getZoom: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.getZoom' );
            }
            return osViewer.viewer.viewport.getZoom( true );
        },
        zoomTo: function( zoomTo ) {
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
        goHome: function( immediate ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.panHome - zoom : ' + osViewer.viewer.viewport.getHomeZoom() );
            }
            osViewer.viewer.viewport.goHome( immediate );
            _zoomedOut = true;
        },
        reset: function( resetRotation ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.goHome: bool - ' + resetRotation );
            }
            
            // osViewer.viewer.viewport.goHome( true );
            osViewer.controls.goHome( true );
            osViewer.viewer.viewport.zoomTo( osViewer.viewer.viewport.getHomeZoom(), null, true );
            if ( resetRotation ) {
                osViewer.controls.rotateTo( 0 );
            }
        },
        zoomIn: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.zoomIn: zoomSpeed - ' + _config.global.zoomSpeed );
            }
            
            osViewer.viewer.viewport.zoomBy( _config.global.zoomSpeed, osViewer.viewer.viewport.getCenter( false ), false );
        },
        zoomOut: function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.zoomOut: zoomSpeed - ' + _config.global.zoomSpeed );
            }
            
            osViewer.viewer.viewport.zoomBy( 1 / _config.global.zoomSpeed, osViewer.viewer.viewport.getCenter( false ), false );
        },
        getHomeZoom: function( rotated ) {
            if ( rotated && osViewer.getCanvasSize().x / osViewer.getCanvasSize().y <= osViewer.getImageSize().x / osViewer.getImageSize().y ) {
                osViewer.viewer.viewport.homeFillsViewer = true;
            }
            var zoom = osViewer.viewer.viewport.getHomeZoom();
            osViewer.viewer.viewport.homeFillsViewer = false;
            return zoom;
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
                console.log( 'osViewer.controls.setRotation: rotation - ' + rotation );
            }
            
            return osViewer.controls.rotateTo( rotation );
        },
        rotateTo: function( newRotation ) {
            if ( newRotation < 0 ) {
                newRotation = newRotation + 360;
            }
            newRotation = newRotation % 360;
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateTo: newRotation - ' + newRotation );
            }
                        
            _panning = true;        
            _currentZoom = null;
            osViewer.viewer.viewport.setRotation( newRotation );
            _panning = false;

        },
        getCurrentRotationZooming: function() {
            var sizes = osViewer.getSizes();
            if(sizes && sizes.rotated()) {
                return 1/sizes.ratio(sizes.originalImageSize);
            } else {
                return 1;
            }
        },
        setPanning: function(panning) {
            _panning = panning;
        },
        isPanning: function() {
            return _panning;
        },
        fullscreenControlsFadeout: function() {
            if ( _fadeout ) {
                clearTimeout( _fadeout );
                osViewer.controls.showFullscreenControls();
            }
            
            _fadeout = setTimeout( osViewer.controls.hideFullscreenControls, 3000 );
        },
        hideFullscreenControls: function() {
            $( '#fullscreenRotateControlsWrapper, #fullscreenZoomSliderWrapper, #fullscreenExitWrapper, #fullscreenPrevWrapper, #fullscreenNextWrapper' ).stop().fadeOut( 'slow' );
        },
        showFullscreenControls: function() {
            $( '#fullscreenRotateControlsWrapper, #fullscreenZoomSliderWrapper, #fullscreenExitWrapper, #fullscreenPrevWrapper, #fullscreenNextWrapper' ).show();
        }
    };
    
    
    //set correct location, zooming and rotation once viewport has been updated after redraw
    function setLocation(event, osViewer) {
        if(_debug) {                    
            console.log("Viewer changed from " + event.osState + " event");
            console.log("target location: ", event.targetLocation);
            console.log("Home zoom = ", osViewer.viewer.viewport.getHomeZoom());
        }
         osViewer.viewer.viewport.minZoomLevel = osViewer.viewer.viewport.getHomeZoom() * osViewer.getConfig().global.minZoomLevel;
         var targetZoom = event.targetLocation.zoom;
         var targetLocation = new OpenSeadragon.Point(event.targetLocation.x, event.targetLocation.y);
         var zoomDiff = targetZoom * osViewer.viewer.viewport.getHomeZoom() - (osViewer.viewer.viewport.minZoomLevel);
//         console.log("zoomDiff: " + targetZoom + " * " + osViewer.viewer.viewport.getHomeZoom() + " - " + osViewer.viewer.viewport.minZoomLevel + " = ", zoomDiff);
//         console.log("zoomDiff: " + targetZoom + " - " + osViewer.viewer.viewport.minZoomLevel + "/" + osViewer.controls.getCurrentRotationZooming() + " = ", zoomDiff);
         var zoomedOut = zoomDiff < 0.001 || !targetZoom;
         if(zoomedOut) {
             if(_debug) {                         
                 console.log("Zooming home")
             }
             osViewer.controls.goHome( true );
         } else {
             if(_debug) {                         
                 console.log( "Zooming to " + targetZoom + " * " + osViewer.controls.getCurrentRotationZooming() );
                 console.log("panning to ", targetLocation);
             }
             osViewer.viewer.viewport.zoomTo( targetZoom * osViewer.controls.getCurrentRotationZooming(), null, true);
             osViewer.controls.setCenter( targetLocation);
         }
         if(event.osState === "open" && event.targetLocation.rotation !== 0) {
            osViewer.controls.rotateTo(event.targetLocation.rotation);
         }
    }
    
    return osViewer;
    
} )( viewImage || {}, jQuery );
