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
            zoomSlider: ".zoom-slider",
            zoomSliderHandle: ".zoom-slider-handle",
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
    var _drawStyle = {
            borderWidth: 2,
            borderColor: "#ff4433"
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
        resourcesPath: "template/js/",
    };
    
    goobiWorkflow.object = {
        /**
         * @description Method to initialize the object view.
         * @method init
         */
        init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.object.init' );
            }

            // TODO: fix image controls
            
            // init object view
            if ( $( '#mainImage' ).length > 0 ) {
                this.imageLoadHandler();

                // if ( _configViewer.global.persistZoom || _configViewer.global.persistRotation ) {
                //     $( 'body' ).on( 'click', '#imageNavigation a', function() {
                //         if ( _viewImage ) {
                //             console.log('click');
                //             _viewImage.controls.persistence.storeLocation
                //         }
                //     } );
                // }
            }
        },
        initControls() {
            $('#rotate-right-x').on("click", function() {
                _viewImage.controls.rotateRight();
            })
            $('#rotate-left-x').on("click", function() {
                _viewImage.controls.rotateLeft();
            })
            $('#reset-position').on("click", function() {
                _viewImage.controls.reset(true);
            })

       

        },
        /**
         * @description Method to load the image handler.
         * @method imageLoadHandler
         */
        imageLoadHandler: function () {
            if ( _debug ) {
                console.log('EXECUTE: goobiWorkflowJS.object.imageLoadHandler');
                console.log(new Error().stack);
            }

            _mediaType = $( '#mediaType' ).val();
            
            if ( _mediaType == 'image' ) {
                let imageZoomPersistenzeId = $( '#persistenceId' ).val();
                if(imageZoomPersistenzeId && imageZoomPersistenzeId.length > 0) {
                    console.log("persist image zoom with id ", imageZoomPersistenzeId);
                    _configViewer.global.persistenceId = imageZoomPersistenzeId;
                    _configViewer.global.persistZoom =  true;
                    _configViewer.global.persistRotation = true;                    
                }
                var tileSource = $( '#tileSource' ).val();
                if( _debug ) {
                    console.log("loading tileSource:", tileSource)
                }
                _configViewer.image.tileSource = tileSource;
                _viewImage = new ImageView.Image(_configViewer);
                _viewImage.load().then( function () {
                    goobiWorkflowJS.layout.setObjectViewHeight();
                    goobiWorkflow.object.initControls();
                    goobiWorkflow.object.initAreas();
                    _viewImage.controls.goHome();
                    if (_viewImage.observables) {
                        _viewImage.observables.firstTileLoaded.subscribe(
                            () => {}, 
                            (error) => {
                                console.error( 'imageLoadHandler: Error loading image', error );
                                $( '#' + _configViewer.global.divId ).html( 'Failed to load image tile: ' + error.message );
                            }
                        )
                    }
                })
                .then( () => {
                    //precache next image
                    let tileSource = $("#tileSource_next").val();
                    let divId = "precacheNext";
                    if(tileSource) {                        
                        this.preCache(tileSource, divId);
                    }
                })
                .then( () => {
                    //precache previous image
                    let tileSource = $("#tileSource_previous").val();
                    let divId = "precachePrevious";
                    if(tileSource) {                        
                        this.preCache(tileSource, divId);
                    }
                })
                .catch( function ( error ) {
                    console.error( 'imageLoadHandler: Error opening image', error );
                    $( '#' + _configViewer.global.divId ).html( 'Failed to load image: ' + error.message );
                });
            }
            else if ( _mediaType == 'object' ) {
                $( '#imageLoader' ).show();
                goobiWorkflowJS.layout.setObjectViewHeight();
                _world = WorldGenerator.create(_worldConfig); 
                _world.loadObject( {
                    url: $( '#objectUrl' ).val(),
                    position: { x: 0, y: 0, z: 0 },
                    rotation: { x: 0, y: 0, z: 0 },
                    size: 10,
                    material: {
                        color: 0x44bb33
                    },
                    focus: true,
//                    onTick: function ( object, time ) {
//                        if ( object ) {
//                            object.rotation.set( 0, Math.PI / 180 * time, 0 );
//                        }
//                    }
                }).then( function ( object ) {
                    $( '#imageLoader' ).fadeOut( 2000 );
                    console.info( 'imageLoadHandler: loaded', object );
                    _world.render();
                }).catch( function ( error ) {
                    $( '#imageLoader' ).fadeOut( 2000 );
                    console.error( 'imageLoadHandler: failed to load: ', error );
                })
            } 
            else if ( _mediaType == 'x3dom' ) {
                var objectUrl = $( '#objectUrl' ).val();
                $( '#imageLoader' ).show();
                new X3DLoader().load( $( '#mainImage' ), objectUrl, function () {
                    $( '#imageLoader' ).fadeOut( 2000 );
                    console.info( 'imageLoadHandler: loaded' );
                },
                function () {
                    console.info( 'imageLoadHandler: progress' );
                },
                function ( error ) {
                    $( '#imageLoader' ).fadeOut( 2000 );
                    console.info( 'imageLoadHandler: error', error );
                });
            }
        },
        /**
         * enable drawing a rect on the image by passing paramter draw = true; disable with draw = false
         */
        setDrawArea(draw, id) {
            this.drawArea = draw;
            this.areaId = id;
        },
        /**
         * Check if drawing an area is enabled
         */
        isDrawArea() {
            return this.drawArea;
        },
        
        /**
         * Initialize drawing and transforming areas within image
         */
        initAreas() {
            $('#disable-interaction-overlay').hide();
            this.drawer = new ImageView.Draw(_viewImage.viewer, _drawStyle, () => this.isDrawArea());
            this.drawer.finishedDrawing().subscribe(function(overlay) {
                overlay.draw();
				overlay.areaId = this.areaId;
                this.overlays.push(overlay);
                this.setDrawArea(false);
                this.transformer.addOverlay(overlay);
                this.writeAreas();
                $('#disable-interaction-overlay').hide();
            }.bind(this));
                        
            this.transformer = new ImageView.Transform(_viewImage.viewer, _drawStyle, () => !this.isDrawArea());
            this.transformer.finishedTransforming().subscribe(function(overlay) {
                this.writeAreas();
            }.bind(this));
                        
            var areaString = $(".pageareas").val();
			if (areaString) {
	            var areas = JSON.parse(areaString);
	            this.overlays = [];
	            var shouldDraw = false;
	            for(var area of areas) {
	                if(!area.x) {
	                    shouldDraw = true;
	                } else {
	                    var rect = new OpenSeadragon.Rect(parseInt(area.x), parseInt(area.y), parseInt(area.w), parseInt(area.h));
	                    var displayRect = ImageView.CoordinateConversion.convertRectFromImageToOpenSeadragon(rect, _viewImage.viewer, _viewImage.getOriginalImageSize());
	                    var overlay = new ImageView.Overlay(displayRect, _viewImage.viewer, _drawStyle, true);
	                    overlay.areaId = area.id;
	                    overlay.draw();
	                    this.transformer.addOverlay(overlay);
	                    this.overlays.push(overlay);
	                }
	            } 
	            if(shouldDraw) {
	                $('#disable-interaction-overlay').show();
	                this.setDrawArea(true, area.id);
	            } else {
	                this.setDrawArea(false, null);
	            }
			}
        },
        writeAreas() {
            var areas = [];
            for(var overlay of this.overlays) {
                var area = {};
                var rect = ImageView.CoordinateConversion.convertRectFromOpenSeadragonToImage(overlay.rect, _viewImage.viewer, _viewImage.getOriginalImageSize());
                if(rect) {                    
                    area.id = overlay.areaId;
                    area.x = Math.round(rect.x);
                    area.y = Math.round(rect.y);
                    area.w = Math.round(rect.width);
                    area.h = Math.round(rect.height);
                    areas.push(area);
                }
            }
            var areaString = "";
            if(areas.length) {                
                areaString = JSON.stringify(areas);
            }
            console.log("set areas ", areaString);
            $(".pageareas").val(areaString);
 			$(".pageareas").change();
        },
        /**
         * @description Method to clean up javascript resources for different object views.
         * @param {Object} data A data object.
         */
        freeJSResources: function( data ) {
            if ( _debug ) {
                console.log('EXECUTE: goobiWorkflowJS.object.freeJSResources');
            }

            if ( !data || data.status == 'begin' ) {

                if ( _viewImage ) {
                    if ( _debug ) {
                        console.info( 'freeJSResources: closing OpenSeadragon viewer' );
                    }
                    _viewImage.close();
                }
                if ( _world ) {
                    if ( _debug ) {
                        console.info( 'freeJSResources: disposing 3d scene' );
                    }
                    _world.dispose();
                }

                return;
            }
        },
        
        preCache(url, id) {
            let container = $("<div id='" + id + "'/>")
            $("main").append(container);
            let viewConfig = {
                    global: {divId: id, imageControlsActive: false},
                    image: {tileSource: url}
            }
            new ImageView.Image(viewConfig).load()
            .catch( error => console.log("error precaching url " + url));
        }
    };

    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );