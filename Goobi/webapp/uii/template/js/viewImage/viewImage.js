/**
 * This file is part of the Goobi Viewer - a content presentation and management
 * application for digitized objects.
 * 
 * Visit these websites for more information. - http://www.intranda.com -
 * http://digiverso.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Module which initializes the viewerJS module.
 * 
 * @version 3.2.0
 * @module viewImage
 * @requires jQuery
 */
var viewImage = ( function() {
    'use strict';
    
    var osViewer = {};
    var _debug = false;
    var _footerImage = null;
    var _canvasScale;
    var _container;
    var _defaults = {  
        global: {
            divId: "map",
            zoomSlider: ".zoom-slider",
            zoomSliderHandle: '.zoom-slider-handle',
            overlayGroups: [ {
                name: "searchHighlighting",
                styleClass: "searchHighlight",
                interactive: false
            }, {
                name: "ugc",
                styleClass: "ugcBox",
                interactive: true
            
            } ],
            zoomSpeed: 1.25,
            maxZoomLevel: 20,
            minZoomLevel: 1,
            useTiles: true,
            imageControlsActive: true,
            visibilityRatio: 0.4,
            loadImageTimeout: 10 * 60 * 1000,
            maxParallelImageLoads: 4,
            adaptContainerHeight: false,
            footerHeight: 50,
            rememberZoom: false,
            rememberRotation: false,
        },
        image: {},
        getOverlayGroup: function( name ) {
            var allGroups = _defaults.global.overlayGroups;
            for ( var int = 0; int < allGroups.length; int++ ) {
                var group = allGroups[ int ];
                if ( group.name === name ) {
                    return group;
                }
            }
        },
        getCoordinates: function( name ) {
            var coodinatesArray = _defaults.image.highlightCoords;
            if ( coodinatesArray ) {
                for ( var int = 0; int < coodinatesArray.length; int++ ) {
                    var coords = coodinatesArray[ int ];
                    if ( coords.name === name ) {
                        return coords;
                    }
                }
            }
        },
    };
    
    osViewer = {
        viewer: null,
        init: function( config ) {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'osViewer.init' );
                console.log( '##############################' );
            }
            
            // constructor
            $.extend( true, _defaults, config );
            // convert mimeType "image/jpeg" to "image/jpg" to provide correct
			// iiif calls
            _defaults.image.mimeType = _defaults.image.mimeType.replace("jpeg","jpg");
            _container = $( "#" + _defaults.global.divId );
            
            var sources = _defaults.image.tileSource;
            if(typeof sources === 'string' && sources.startsWith("[")) {
            	sources = JSON.parse(sources);
            } else if(!$.isArray(sources)) {
            	sources = [sources];
            }
            var promises = [];
            for ( var i=0; i<sources.length; i++) {
            	var source = sources[i];
            	// returns the OpenSeadragon.TileSource if it can be created,
				// otherweise
                // rejects the promise
            	var promise = viewImage.createTileSource(source);
            	promises.push(promise);	
	                }                
            return Q.all(promises).then(function(tileSources) {
            	var minWidth = Number.MAX_VALUE;  
            	var minHeight = Number.MAX_VALUE;
            	var minAspectRatio = Number.MAX_VALUE;
            	for ( var j=0; j<tileSources.length; j++) {
            		var tileSource = tileSources[j];
            		minWidth = Math.min(minWidth, tileSource.width);
            		minHeight = Math.min(minHeight, tileSource.height);
            		minAspectRatio = Math.min(minAspectRatio, tileSource.aspectRatio);
	                }
	                    if(_debug) {                    
            	    console.log("Min aspect ratio = " + minAspectRatio);            	    
	                    }
            	var x = 0;
            	for ( var i=0; i<tileSources.length; i++) {
	        		var tileSource = tileSources[i];
	        		tileSources[i] = {
	        				tileSource: tileSource,
	        				width: tileSource.aspectRatio/minAspectRatio,
// height: minHeight/tileSource.height,
	                		x : x,
	                		y: 0,
	                    }
	        		x += tileSources[i].width;
	                }              
            	return viewImage.loadImage(tileSources);
            });
            
        },
        loadImage : function(tileSources) {
            if ( _debug ) {
                console.log( 'Loading image with tilesource: ', tileSources );
            }
              
            osViewer.loadFooter();            
         
            osViewer.viewer = new OpenSeadragon( {
                immediateRender: false,
                visibilityRatio: _defaults.global.visibilityRatio,
                sequenceMode: false,
                id: _defaults.global.divId,
                controlsEnabled: false,
                prefixUrl: "/openseadragon-bin/images/",
                zoomPerClick: 1,
                maxZoomLevel: _defaults.global.maxZoomLevel,
                minZoomLevel: _defaults.global.minZoomLevel,
                zoomPerScroll: _defaults.global.zoomSpeed,
                mouseNavEnabled: _defaults.global.zoomSpeed > 1,
                showNavigationControl: false,
                showZoomControl: false,
                showHomeControl: false,
                showFullPageControl: true,
                timeout: _defaults.global.loadImageTimeout,
                tileSources: tileSources,
                blendTime: .5,
                alwaysBlend: false,
                imageLoaderLimit: _defaults.global.maxParallelImageLoads,
                viewportMargins: {
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: _defaults.global.footerHeight
                }
            } );

            var result = Q.defer();
                
            osViewer.observables = createObservables(window, osViewer.viewer);  
                
            osViewer.observables.viewerOpen.subscribe(function(openevent, loadevent) {            
                result.resolve(osViewer);                
            }, function(error) {            
                result.reject(error);                
            });                
                
                
            // Calculate sizes if redraw is required
            
            osViewer.observables.redrawRequired.subscribe(function(event) {            
                if(_debug) {
                    console.log("viewer " + event.osState + "ed with target location ", event.targetLocation);                    
                }
                
                osViewer.redraw();
            });
                
            if ( osViewer.controls ) {                    
                osViewer.controls.init( _defaults );
            }
            
            if ( osViewer.zoomSlider ) {
                osViewer.zoomSlider.init( _defaults );                
            }
            
            if ( osViewer.overlays ) {
                osViewer.overlays.init( _defaults );                
            }                
            
            if ( osViewer.drawRect ) {
                osViewer.drawRect.init();                
            }   
            
            if ( osViewer.transformRect ) {            
                osViewer.transformRect.init();                
            }                
            
            osViewer.observables.redrawRequired.connect();                
            
            return result.promise;
        },
        getObservables: function() {
        	console.log("Observables = ", osViewer.observables);
        	return osViewer.observables;
        },
        hasFooter: function() {
            return _footerImage != null;
        },
        getConfig: function() {
            return _defaults;
        },
        loadFooter: function() {
            if ( _defaults.image.baseFooterUrl && _defaults.global.footerHeight > 0 ) {                
                _footerImage = new Image();
                _footerImage.src = _defaults.image.baseFooterUrl.replace( "{width}", Math.round( _container.width() ) ).replace( "{height}", Math.round( _defaults.global.footerHeight ) );                
                _footerImage.onload = function() {
                    if ( _debug ) {
                        console.log( "loading footer image ", _footerImage );
                        console.log( "Calculating image Footer size" );
                    }
                    
                    osViewer.drawFooter();
                };
            }
        },
        drawFooter: function() {
            if ( osViewer.viewer ) {
                _overlayFooter();
            }
            
            osViewer.viewer.removeHandler( 'update-viewport', _overlayFooter );
            osViewer.viewer.addHandler( 'update-viewport', _overlayFooter );
        },        
        getOverlayGroup: function( name ) {
            return _defaults.getOverlayGroup( name );
        },
        getHighlightCoordinates: function( name ) {
            return _defaults.getCoordinates( name );
        },
        createPyramid: function( imageInfo ) {
            var fileExtension = _defaults.image.mimeType;
            fileExtension = fileExtension.replace( "image/", "" );
            fileExtension = fileExtension.replace("jpeg", "jpg").replace("tiff", "tif");
            var imageLevels = [];
            var tileSource;
            if(Array.isArray(imageInfo)) {
            	imageInfo.forEach(function(level) {
            		level.mimetype = _defaults.image.mimeType;
            	});
            	tileSource = new OpenSeadragon.LegacyTileSource(imageInfo);
            } else if(imageInfo.sizes) {
	            imageInfo.sizes.forEach(function(size) {
	                if(_debug) {                    
	                    console.log("Image level width = ", size.width)
	                    console.log("Image level height = ", size.height)
	                }
	                
	                var level = {
	                    mimetype: _defaults.image.mimeType,
	                    url: imageInfo["@id"].replace( "/info.json", "" ) + "/full/" + size.width + ",/0/default." + fileExtension,
	                    width: imageInfo.width,
	                    height: imageInfo.height
	                };
	                
	                if(_debug) {
	                    console.log("Created level ", level);
	                }
	                
	                imageLevels.push( level );
	            });
	            
	            tileSource = new OpenSeadragon.LegacyTileSource(imageLevels);
            } else {
            	tileSource = new OpenSeadragon.ImageTileSource({
            		url: imageInfo["@id"].replace( "/info.json", "" ) + "/full/full/0/default." + fileExtension,
            		crossOriginPolicy: "Anonymous",
            		buildPyramid: false
            	});
            }
            
            return tileSource;
        },
        getSizes: function() {
            return osViewer.sizes;
        },
        addImage: function( url, width, height ) {
            if ( _debug ) {
                console.log( 'osViewer.addImage: url - ' + url );
                console.log( 'osViewer.addImage: width - ' + width );
                console.log( 'osViewer.addImage: height - ' + height );
            }
            if ( osViewer.viewer ) {
                osViewer.viewer.addTiledImage( {
                    tileSource: {
                        type: "legacy-image-pyramid",
                        levels: [ {
                            url: url,
                            height: height,
                            width: width
                        } ]
                    },
                    x: 0,
                    y: 1.6,
                    width: 1
                } );
            }
            else {
                if ( _debug ) {
                    console.log( "Viewer not initialized yet; cannot add image" );
                }
            }
        },
        getImageInfo: function() {
            if(osViewer.viewer) {
                return osViewer.viewer.tileSources;
            }
            return null;
        },
        getScaleToOriginalSize: function(imageNo) {
        	if(!imageNo) {
        		imageNo = 0;
        	}
            var displaySize = osViewer.viewer.viewport._contentSize.x;
            return osViewer.getImageInfo()[imageNo].tileSource.width / displaySize;
        },
        scaleToOriginalSize: function( value, imageNo ) {
            if ( _debug ) {
                console.log( 'Overlays _scaleToOriginalSize: value - ' + value );
            }
            
            if(!imageNo) {
        		imageNo = 0;
        	}
            
            var displaySize = osViewer.viewer.viewport._contentSize.x;
            return value / displaySize * osViewer.getImageInfo()[imageNo].tileSource.width;
        },
        scaleToImageSize: function( value, imageNo ) {
            if ( _debug ) {
                console.log( 'Overlays _scaleToImageSize: value - ' + value );
            }
            
            if(!imageNo) {
        		imageNo = 0;
        	}
            
            var displaySize = osViewer.viewer.viewport._contentSize.x;
            return value * displaySize / osViewer.getImageInfo()[imageNo].tileSource.width;
        },
        close: function() {
            if ( _debug ) {
                console.log( "Closing openSeadragon viewer" );
            }
            
            if ( osViewer.viewer ) {
                osViewer.viewer.destroy();
            }
        },
        redraw: function() {
            if(osViewer.controls) {                    	
            	osViewer.controls.setPanning( true );
            }
            _calculateSizes(osViewer);
        },
        setImageSizes: function(imageInfo, sizes) {
        	if(sizes) {        		
        		var string = sizes.replace(/[\{\}]/, "");
        		var sizes = JSON.parse(sizes);
        		var iiifSizes = [];
        		sizes.forEach(function(size) {
        			iiifSizes.push({"width": parseInt(size), "height": parseInt(size)});
        		});
        		if(iiifSizes.length > 0) {				
        			imageInfo.sizes = iiifSizes;
        		} else {
        			delete imageInfo.sizes;
        		}
        	}
        },
        setTileSizes: function(imageInfo, tiles) {
        	if(tiles) {        		
        		var tileString = configViewer.global.tileSizes.replace(/(\d+)/, '"$1"').replace("=", ":");
        		var tiles = JSON.parse(tileString);
        		var iiifTiles = [];
        		
        		Object.keys(tiles).forEach(function(size) {
        			var scaleFactors = tiles[size];
        			iiifTiles.push({"width": parseInt(size), "height": parseInt(size), "scaleFactors": scaleFactors})
        		});
        		
        		imageInfo.tiles = iiifTiles;
        	}
        },
        onFirstTileLoaded: function() {
        	var defer = Q.defer();
        	
        	if(viewImage.observables) {
        		viewImage.observables.firstTileLoaded.subscribe(function(event) {
        			defer.resolve(event);
        		}, function(error) {
        			defer.reject(error)
        		});
        	} else {
        		defer.reject("No observables defined");
        	}
        	
        	return defer.promise;
        },
        createTileSource: function(source) {

        	var result = Q.defer();

            viewImage.tileSourceResolver.resolveAsJson(source)
            .then(
            		function(imageInfo) {                        
		                if(_debug) {                
		                    console.log("IIIF image info ", imageInfo);                        
		                }               
		                viewImage.setImageSizes(imageInfo, _defaults.global.imageSizes);       
		                viewImage.setTileSizes(imageInfo, _defaults.global.tileSizes);                
		                var tileSource;
		                if(_defaults.global.useTiles) {
		                    tileSource = new OpenSeadragon.IIIFTileSource(imageInfo);                    
		                } else {                
		                    tileSource  = osViewer.createPyramid(imageInfo);                    
		                }
		                
		                return tileSource;                
            		},
		            function(error) {            
		                if(viewImage.tileSourceResolver.isURI(_defaults.image.tileSource)) {
		                    if(_debug) {                    
		                        console.log("Image URL", _defaults.image.tileSource);                        
		                    }
		                    
		                    var tileSource = new OpenSeadragon.ImageTileSource( {                    
		                        url: _defaults.image.tileSource,                        
		                        buildPyramid: true,                        
		                        crossOriginPolicy: false                        
		                    } );
		
		                    return tileSource;                    
		                } else {                
		                    var errorMsg = "Failed to load tilesource from " + tileSource;
		                    
		                    if(_debug) {                    
		                        console.log(errorMsg);                        
        }
		                    
		                    return Q.reject(errorMsg);
		                    
		                }              
		            })
            .then(function(tileSource) {              
                result.resolve(tileSource);          
            }).catch(function(errorMessage) {              
                result.reject(errorMessage);          
            });
            return result.promise;
        }
    };
    
    function createObservables(window, viewer) {
        var observables = {};
        
        observables.viewerOpen = Rx.Observable.create(function(observer) {
            viewer.addOnceHandler( 'open', function( event ) {
                event.osState = "open";
                
                if(Number.isNaN(event.eventSource.viewport.getHomeBounds().x)) {
                    return observer.onError("Unknow error loading image from ", _defaults.image.tileSource);
                } else {                    
                    return observer.onNext(event);
                }
            } );
            viewer.addOnceHandler( 'open-failed', function( event ) {
                event.osState = "open-failed";
                console.log("Failed to open openseadragon ");
                
                return observer.onError(event);
            } );
        });
        
        observables.firstTileLoaded = Rx.Observable.create(function(observer) {
        	viewer.addOnceHandler( 'tile-loaded', function( event ) {
                event.osState = "tile-loaded";
                
                return observer.onNext(event);
            } );
        	viewer.addOnceHandler( 'tile-load-failed', function( event ) {
                event.osState = "tile-load-failed";
                console.log("Failed to load tile");
                
                return observer.onError(event);
            } );
        });
        
        observables.viewerZoom = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'zoom', function( event ) {
                return observer.onNext(event);
            } );
        });
        observables.viewportUpdate = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'update-viewport', function( event ) {
                return observer.onNext(event);
            } );
        });
        observables.viewerRotate = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'rotate', function( event ) {
                event.osState = "rotate";
                return observer.onNext(event);
            } );
        });
        observables.canvasResize = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'resize', function( event ) {
                event.osState = "resize";
                
                return observer.onNext(event);
            } );
        });
        observables.windowResize = Rx.Observable.fromEvent(window, "resize").map(function(event) {
            event.osState = "window resize";
            
            return event;
        });
        observables.overlayRemove = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'remove-overlay', function( event ) {
                return observer.onNext(event);
            } );
        });
        observables.overlayUpdate = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'update-overlay', function( event ) {
                return observer.onNext(event);
            } );
        });
        observables.levelUpdate = Rx.Observable.create(function(observer) {
            viewer.addHandler( 'update-level', function( event ) {
                return observer.onNext(event);
            } );
        });
        observables.redrawRequired = observables.viewerOpen
        .merge(observables.viewerRotate
        		.merge(observables.canvasResize)
        		.debounce(10))
        .map(function(event) {
            var location = {};
            
            if(osViewer.controls) {
                location = osViewer.controls.getLocation();
            }
            
            if(event.osState === "open") {
                location.zoom = osViewer.viewer.viewport.getHomeZoom();
                if(_defaults.image.location) {
                   location = _defaults.image.location;
                }
            }
            
            event.targetLocation = location;
            
            return event;
        }).publish();
        
        return observables;
    }
    
    function _calculateSizes(osViewer) {
        if ( _debug ) {
            console.log( "viewImage: calcualte sizes" );
            console.log("Home zoom = ", osViewer.viewer.viewport.getHomeZoom());
        }
        
        osViewer.sizes = new viewImage.Measures( osViewer );
        
        if ( _defaults.global.adaptContainerHeight ) {
            osViewer.sizes.resizeCanvas();
        }
        
        if ( osViewer.viewer != null ) {
            osViewer.viewer.viewport.setMargins( {bottom: osViewer.sizes.footerHeight + osViewer.sizes.calculateExcessHeight()} );
        }
        
        if ( _debug ) {
            console.log( "sizes: ", osViewer.sizes );
        }
        
    };

    
    function _overlayFooter( event ) {
        if ( _defaults.global.footerHeight > 0 ) {
            var footerHeight = _defaults.global.footerHeight;
            var footerPos = new OpenSeadragon.Point( 0, _container.height() - footerHeight );
            var footerSize = new OpenSeadragon.Point( _container.width(), footerHeight );
            
            if ( !_canvasScale ) {
                _canvasScale = osViewer.viewer.drawer.context.canvas.width / osViewer.viewer.drawer.context.canvas.clientWidth;
            }
            
            if ( _canvasScale != 1 ) {
                footerPos = footerPos.times( _canvasScale );
                footerSize = footerSize.times( _canvasScale );
            }
            osViewer.viewer.drawer.context.drawImage( _footerImage, footerPos.x, footerPos.y, footerSize.x, footerSize.y );
        }
    };
    
    function _timeout(promise, time) {
        var deferred = new jQuery.Deferred();

        $.when(promise).done(deferred.resolve).fail(deferred.reject).progress(deferred.notify);

        setTimeout(function() {
            deferred.reject("timeout");
        }, time);

        return deferred.promise();
    }
    
    return osViewer;    
}

)( jQuery, OpenSeadragon );

// browser backward compability
if(!String.prototype.startsWith) {
    String.prototype.startsWith = function(subString) {
        var start = this.substring(0,subString.length);
        return start.localeCompare(subString) === 0;
    }
}
if(!String.prototype.endsWith) {
    String.prototype.endsWith = function(subString) {
        var start = this.substring(this.length-subString.length,this.length);
        return start.localeCompare(subString) === 0;
    }
}
if(!Array.prototype.find) {
    Array.prototype.find = function(comparator) {
        for ( var int = 0; int < this.length; int++ ) {
            var element = this[int];
            if(comparator(element)) {
                return element;
            }
        }
    }
}
if(!Number.isNaN) {
    Number.isNaN = function(number) {
        return number !== number;
    }
}
