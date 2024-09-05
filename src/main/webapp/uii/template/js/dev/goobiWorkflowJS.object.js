var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    var _viewImage = null;
    var _world = null;
    var _mediaType = null;
    var _preloadedImages = [];
    var _colors = ['#ff4433'];
    //var _colors = ['#e6194b', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#46f0f0', '#f032e6', '#bcf60c', '#fabebe', '#008080', '#e6beff', '#9a6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075', '#808080', '#ffffff', '#000000'];
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
            
            if ( _mediaType == 'image' || _mediaType == 'pdf' ) {
            	goobiWorkflowJS.object.freeJSResources();
                let imageZoomPersistenzeId = $( '#persistenceId' ).val();
                if(imageZoomPersistenzeId && imageZoomPersistenzeId.length > 0) {
                    if(_debug)console.log("persist image zoom with id ", imageZoomPersistenzeId);
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
        	if(_debug)console.log("init areas");
            $('#disable-interaction-overlay').hide();
            this.drawer = new ImageView.Draw(_viewImage.viewer, _drawStyle, () => this.isDrawArea());
            this.drawer.finishedDrawing().subscribe(function(overlay) {
            	console.log("finished drawing", this);
            	overlay.style = $.extend({}, _drawStyle, {borderColor: this.colors.next()});
                overlay.draw();
                this.addOverlay(overlay);
                this.setDrawArea(false);
                this.drawnOverlay = overlay;
                this.transformer.addOverlay(overlay);
                this.endDrawArea();
                this.addArea(overlay);
                if(this.areaTarget == "current") {
                	this.highlight(overlay);
                }
                $('#disable-interaction-overlay').hide();
            }.bind(this)); 
                        
            this.transformer = new ImageView.Transform(_viewImage.viewer, _drawStyle, () => !this.isDrawArea());
            this.transformer.finishedTransforming().subscribe(function(overlay) {
                this.setArea(overlay);
            }.bind(this));

			this.areaString = $("#pageareas").text();
			//console.log("page area string", this.areaString);
			if (this.areaString) {
				if(_debug)console.log("drawAreas", this.areaString);
	            let areas = JSON.parse(this.areaString);
	            this.drawnOverlay = undefined;
				this.drawAreas(areas);
			} else {
				this.drawAreas([]);
			}
			
			goobiWorkflow.jsfAjax.success.subscribe(data => {
				let areaString = $("#pageareas").text();
				if (this.areaString != areaString) {
					this.areaString = areaString;
					if(this.areaString) {
		            	let areas = JSON.parse(this.areaString);
		            	this.drawnOverlay = undefined;
		            	this.drawAreas(areas);
		            } else {
		            	this.drawAreas([]);
		            }
		        }
				//console.log("page area string", this.areaString);
			}); 
			this.initDeletePageAreas();
		},
		addOverlay(overlay) {
			if(overlay) {
				this.overlays.push(overlay);
				this.overlays.sort((o1,o2) => {
            		return o1.highlight ? -1 : o2.highlight ? 1 : 0;
           		} );
			}
		},
		startDrawArea(source, target) {
			console.log("start draw area", source, target);
			if(this.drawnOverlay && target == this.getAreaTarget()) {
				this.drawnOverlay.remove();
			}
			this.setDrawArea(true);
			this.setAreaTarget(target);
			$(source).hide();
			$(".cancel-area-edition").hide();
			$(source).next(".cancel-area-edition").show();
			$('#disable-interaction-overlay').show();
		},
		cancelDrawArea() {
			if(this.isDrawArea()) {
				this.setDrawArea(false);
				this.endDrawArea();
			} else {
				if(this.drawnOverlay) {
					this.drawnOverlay.remove();
				}
				this.endDrawArea();
				cancelPageAreaEdition();
			}
			$(".cancel-area-edition").hide();
		},
		endDrawArea() {
			$('#disable-interaction-overlay').hide();
			$(".start-area-edition").show();
		},
		/**
		Set to 'current' to add area to current docStrct. Otherwise area is added to the next newly created docStruct
		*/
		setAreaTarget(target) {
			this.areaTarget = target;
		},
		getAreaTarget() {
			return this.areaTarget;
		},
		addArea(overlay) {
			let area = this.writeArea(overlay);
			area.addTo = this.areaTarget;
			addPageArea(area);
			if(this.areaTarget == "current") {
				this.drawnOverlay = undefined;
				$(".cancel-area-edition").hide();
				$(".start-area-edition").show();
			}
		},
		setArea(overlay) {
			let area = this.writeArea(overlay);
			console.log("set area", overlay, area);
			setPageArea(area);
		},
		deleteArea(overlay) {
			deletePageArea({areaId: overlay.areaId});
		},
		cancelPageAreaEdition() {
			cancelPageAreaEdition();
		},
        initDeletePageAreas() {
	        if(_debug)console.log("init delete page areas");
	        this.endDeletePageMode();//remove previous ini
        	$(document).off("click", "[data-pagearea-delete='start']").on("click", "[data-pagearea-delete='start']", (e) => {
        		this.startDeletePageMode();
        	});
        	$(document).off("click", "[data-pagearea-delete='cancel']").on("click", "[data-pagearea-delete='cancel']", (e) => {
        		this.endDeletePageMode();
        	});
        },
        startDeletePageMode() {
        	if(_debug)console.log("start delete mode");
        	$("[data-pagearea-delete='start']").hide();
        	$("[data-pagearea-delete='cancel']").show();
        	if(this.deleteHandler == null) {
	        	this.deleteHandler = event => {
		        	if(_debug)console.log("delete area ", event, this.overlays);
		        	this.overlays.every(o => {
						if(o.contains(event.position, 5, true)) {
							if(_debug)console.log("delete page area 2", o);
							deletePageArea({"areaId": o.areaId});
							o.remove();
							this.transformer.removeOverlay(o);
							this.endDeletePageMode();
							return false;
						} else {
							return true;
						}
					});
		        };
	        }
        	
        	_viewImage.viewer.addHandler("canvas-click", this.deleteHandler);
        	
        },
        endDeletePageMode() {
        	if(_debug)console.log("end delete page mode");
    		$("[data-pagearea-delete='cancel']").hide();
    		$("[data-pagearea-delete='start']").show();
    		if(this.deleteHandler != null) {
	        	_viewImage.viewer.removeHandler("canvas-click", this.deleteHandler);
	        	this.deleteHandler = null;
    		}
        },
        drawAreas(areas) {
        	if(_debug)console.log("draw areas ", areas);
            if(this.overlays) {
            	this.overlays.forEach(o => {
            		o.remove();
            		this.transformer.removeOverlay(o);
            	});
            }
       		$("#pageAreaName").val("");
            this.overlays = [];
            this.colors = new ImageView.ColorIterator(_colors);
            
            var shouldDraw = false;
            for(var area of areas) {
                if(!area.x) {
                    shouldDraw = true;
                } else {
                    try {
	                    var rect = new OpenSeadragon.Rect(parseInt(area.x), parseInt(area.y), parseInt(area.w), parseInt(area.h));
	                    var displayRect = ImageView.CoordinateConversion.convertRectFromImageToOpenSeadragon(rect, _viewImage.viewer, _viewImage.getOriginalImageSize());
	                    var overlay = new ImageView.Overlay(displayRect, _viewImage.viewer, _drawStyle, true);
	                    overlay.style = $.extend({}, _drawStyle, {borderColor: this.colors.next()});
	                    overlay.areaId = area.areaId;
	                    if(area.highlight) {
	                    	this.highlight(overlay);
	                    }
	                    overlay.draw();
	                    if(_debug)console.log("draw overlay", overlay);
	                    this.transformer.addOverlay(overlay);
	                    this.addOverlay(overlay);
                    } catch(e) {
                    }
                }
                if(area.label && !area.logId) {
                	//new area. Update "Bildbereich" input
                	$("#pageAreaName").val(area.label);
                }
            } 
            if(shouldDraw) {
                $('#disable-interaction-overlay').show();
                this.setDrawArea(true, area.id);
            } else {
                this.setDrawArea(false, null);
            }
        },
        highlight(overlay) { 
        	overlay.highlight = true;
        	overlay.style.fillColor = overlay.style.borderColor;
        	overlay.style.opacity = 0.3;
        	_viewImage.viewer.forceRedraw();
        },
        unHighlight(overlay) {
        	overlay.highlight = false;
        	overlay.style.fillColor = null;
        	_viewImage.viewer.forceRedraw();
        }, 
        writeArea(overlay) {
            var area = {};
            var rect = ImageView.CoordinateConversion.convertRectFromOpenSeadragonToImage(overlay.rect, _viewImage.viewer, _viewImage.getOriginalImageSize());
            if(rect) {                    
                area.areaId = overlay.areaId;
                area.x = Math.round(rect.x);
                area.y = Math.round(rect.y);
                area.w = Math.round(rect.width);
                area.h = Math.round(rect.height);
            }
            return area;
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
                if(_preloadedImages != []) {
                	if (_debug) {
                		console.info( 'freeJSResources: disposing preload');
                	}
                	for(var i in _preloadedImages){
                		_preloadedImages[i].close()
                	}
                	_preloadedImages = []
                	
                }

                return;
            }
        },
        
        preCache(url, id) {
        	if(!document.querySelector('#'+id)) {
	            let container = $("<div id='" + id + "' />")
	            
	            $("main").append(container);
        	}
            let viewConfig = {
                    global: {divId: id, imageControlsActive: false, tabIndex: -1},
                    image: {tileSource: url}
            }
            var preload = new ImageView.Image(viewConfig)
            preload.load()
            .catch( error => console.log("error precaching url " + url));
            _preloadedImages.push(preload)
            
        }
        
        
    };

    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );