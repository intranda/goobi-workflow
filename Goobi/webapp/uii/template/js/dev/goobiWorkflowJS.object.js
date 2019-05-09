var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    var _viewImage = null;
    var _world = null;
    var _mediaType = null;
    var _configViewer = {
        global: {
            divId: "mainImage",
            useTiles: true,
            footerHeight: 0,
            adaptContainerHeight: false,
            zoomSlider: "#zoomSlider",
            zoomSliderHandle: ".zoomslider-handle",
            zoomSliderLabel: "#zoomSliderLabel input",
            persistZoom: false,
            persistRotation: false,
            persistenceId: '',
        },
        image: {
            mimeType: "image/jpeg",
            tileSource: '',
        }
    };
    var _worldConfig = {
        controls: {
            xAxis: {
                rotateLeft: "#rotate-left-x",
                rotateRight: "#rotate-right-x"
            },
            yAxis: {
                rotateLeft: "#rotate-left-y",
                rotateRight: "#rotate-right-y"
            },
            zAxis: {
                rotateLeft: "#rotate-left-z",
                rotateRight: "#rotate-right-z"
            },
            position: {
                reset: "#reset-position"
            },
            zoom: {
                resetZoom: "#reset#zoom"
            }
        },
        container: {
            id: "mainImage"
        },
    };
    
    goobiWorkflow.object = {
        /**
         * @description Method to initialize the object view.
         * @method init
         */
        init: function( config ) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.object.init' );
                console.log( '--> config = ', config );
            }
            
            $.extend( true, _defaults, config );

            // set configvariables
            _configViewer.global.persistenceId = _defaults.image.persistenceId;
            _configViewer.image.tileSource = _defaults.image.tileSource;

            
            // init object view
            if ( $( '#mainImage' ).length > 0 ) {
                this.imageLoadHandler();
            }
        },
        /**
         * @description Method to load the image handler.
         * @method imageLoadHandler
         */
        imageLoadHandler: function () {
            if ( _debug ) {
                console.log('EXECUTE: goobiWorkflow.object.imageLoadHandler');
            }

            _mediaType = _defaults.image.mediaType;

            if ( _mediaType == "image" ) {
                _viewImage = new ImageView.Image(_configViewer);
                _viewImage.load().then( function ( image ) {
                    if (_configViewer.global.persistZoom || _configViewer.global.persistRotation) {
                        $('#imageFirst, #imageNext, #nextPage1, #afterNextPage, #imageLast')
                            .off('click')
                            .on('click', image.controls.persistence.storeLocation);
                    }

                    goobiWorkflowJS.layout.setObjectViewHeight();
                }).catch( function ( error ) {
                    console.error("Error opening image", error);
                    $( "#" + _configViewer.global.divId ).html( 'Failed to load image: "' + error + '"' );
                });
            } 
            else if ( _mediaType == "object" ) {
                $( "#imageLoader" ).show();
                _world = WorldGenerator.create(_worldConfig);
                _world.loadObject( {
                    url: _defaults.image.objectUrl,
                    position: { x: 0, y: 0, z: 0 },
                    rotation: { x: 0, y: 0, z: 0 },
                    size: 10,
                    material: {
                        color: 0x44bb33
                    },
                    focus: true,
                    onTick: function ( object, time ) {
                        if ( object ) {
                            object.rotation.set( 0, Math.PI / 180 * time, 0 );
                        }
                    }
                }).then( function ( object ) {
                    $( "#imageLoader" ).fadeOut( 2000 );
                    console.info("loaded ", object);
                    _world.render();
                }).catch( function ( error ) {
                    $( "#imageLoader" ).fadeOut( 2000 );
                    console.error( "failed to load: ", error );
                })
            } 
            else if ( _mediaType == "x3dom" ) {
                var objectUrl = _defaults.image.objectUrl;
                $( "#imageLoader" ).show();
                new X3DLoader().load( $( "#mainImage" ), objectUrl, function () {
                    $( "#imageLoader" ).fadeOut( 2000 );
                    console.info( "loaded" );
                },
                function () {
                    console.info( "progress" );
                },
                function ( error ) {
                    $( "#imageLoader" ).fadeOut( 2000 );
                    console.info( "error", error );
                });
            }
        },
        /**
         * @description Method to clean up javascript resources for different object views.
         * @param {Object} data A data object.
         */
        freeJSResources: function( data ) {
            if ( !data || data.status == 'begin' ) {
                document.removeEventListener( 'globalDone', goobiWorkflowJS.object.imageLoadHandler() );

                if ( _viewImage ) {
                    console.info( 'closing OpenSeadragon viewer' );
                    _viewImage.close();
                }
                if ( _world ) {
                    console.info( 'disposing 3d scene' );
                    _world.dispose();
                }

                return;
            }
        }
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );