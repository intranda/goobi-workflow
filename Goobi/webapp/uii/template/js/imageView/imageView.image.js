
var _MIN_DESKEW_ANGLE = -44.9  //must be <45
var _MAX_DESKEW_ANGLE = 45  //must be <=45

var ImageView = ( function() {
    'use strict';
    var _debug = false;
    var _defaults = {
            global: {
                divId: "map",
                zoomSlider: ".zoom-slider",
                zoomSliderHandle: '.zoom-slider-handle',
                overlayGroups: [ {
                        name: "searchHighlighting",
                        styleClass: "coords-highlighting",
                        interactive: false
                    }, {
                        name: "ugc",
                        styleClass: "ugcBox",
                        interactive: true
                    
                    }],
                zoomSpeed: 1.25,
                maxZoomLevel: 2,
                minZoomLevel: 1,
                imageControlsActive: true,
                visibilityRatio: 0.2,
                loadImageTimeout: 10 * 60 * 1000,
                maxParallelImageLoads: 2,
                adaptContainerHeight: false,
                fitToContainer: true,
                footerHeight: 0,
                rememberZoom: false,
                rememberRotation: false,
                panHomeOnZoomOut: true,
                showControls: false
            },
            image: {
                initialRotation: 0,
                mimeType: "image/jpeg"
            },
            getOverlayGroup: function( name ) {
                var allGroups = this.global.overlayGroups;
                for ( var int = 0; int < allGroups.length; int++ ) {
                    var group = allGroups[ int ];
                    if ( group.name === name ) {
                        return group;
                    }
                }
            },
            getCoordinates: function( name ) {
                var coodinatesArray = this.image.highlightCoords;
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
    
     var imageView =  {};
     
     /**
      * Basic constructor. Merges the given config into a copy of the default config
      */
     imageView.Image = function(config)  {     
         this.config = jQuery.extend(true, {}, _defaults);
         jQuery.extend(true, this.config, config);
         this.container = $( "#" + this.config.global.divId );
         if(_debug) {             
             console.log("initializing image view with config ", this.config);
         }

//         this.originalImageSize = {x:this.config.imageWidth, y:this.config.imageHeight};
//         this.imageViewWidth = parseFloat($('#'+this.config.div).css("width"));
//         this.imageViewHeight = parseFloat($('#'+this.config.div).css("height"));
     }
     
     /**
      * Loads the image from the config given in the constructor
      * @return a promise to be resolved once the viewer has been opened
      */
     imageView.Image.prototype.load = function() {
             if ( _debug ) {
                 console.log( '##############################' );
                 console.log( 'osViewer.init' );
                 console.log( '##############################' );
             }
             
             this.config.image.mimeType = this.config.image.mimeType.replace("jpeg","jpg");
             //create image source array
             var sources = this.config.image.tileSource;
             if(typeof sources === 'string' && sources.startsWith("[")) {
                 sources = JSON.parse(sources);
             } else if(!$.isArray(sources)) {
                 sources = [sources];
             }
             //create promises for loading of image sources
             var promises = [];
             for ( var i=0; i<sources.length; i++) {
                 var source = sources[i];
                 // returns the OpenSeadragon.TileSource if it can be created,
                 // otherweise
                 // rejects the promise
                 var promise = _createTileSource(source, this.config);
                 promises.push(promise); 
             }                
             var image = this;
             return Q.all(promises).then(function(tileSources) {
                 var minWidth = Number.MAX_VALUE;  
                 var minHeight = Number.MAX_VALUE;
                 var minAspectRatio = Number.MAX_VALUE;
                 for ( var j=0; j<tileSources.length; j++) {
                     var tileSource = tileSources[j];
                     minWidth = Math.min(minWidth, tileSource.width);
                     minHeight = Math.min(minHeight, tileSource.height);
                     minAspectRatio = Math.min(minAspectRatio, tileSource.aspectRatio);
                     //make sure we have some values for original image size in config
                     if(!image.config.image.originalImageWidth) {
                         image.config.image.originalImageWidth = tileSource.width;
                     }
                     if(!image.config.image.originalImageHeight) {
                         image.config.image.originalImageHeight = tileSource.height;
                     }
                 }
                 if(_debug) {      
                     console.log("original image size: ",  image.config.image.originalImageWidth,  image.config.image.originalImageHeight )
                     console.log("Min aspect ratio = " + minAspectRatio);                    
                 }
                 var x = 0;
                 for ( var i=0; i<tileSources.length; i++) {
                     var tileSource = tileSources[i];
                     tileSources[i] = {
                             tileSource: tileSource,
                             width: tileSource.aspectRatio/minAspectRatio,
                             x : x,
                             y: 0,
                         }
                     x += tileSources[i].width;
                     } 
                 var pr = image.loadImage(tileSources);
                 return pr;
             });
             
         };
         
         imageView.Image.prototype.loadImage = function(tileSources) {
             if ( _debug ) {
                 console.log( 'Loading image with tilesource: ', tileSources );
             }
             
             this.loadFooter();            
             var $div = $("#" + this.config.global.divId);
             var maxZoomLevel = this.config.global.maxZoomLevel
             if(this.config.image.originalImageWidth && $div.width() > 0) {
                 maxZoomLevel = this.config.global.maxZoomLevel*this.config.image.originalImageWidth/$div.width();
             }
               
             var osConfig = {
                     tileSources: tileSources,
                     id: this.config.global.divId,
                     prefixUrl: this.config.resourcePath + "/javascript/openseadragon/images/",
                     immediateRender: false,
                     visibilityRatio: this.config.global.visibilityRatio,
                     sequenceMode: false,
                     degrees: this.config.image.initialRotation ? this.config.image.initialRotation : 0,
                     zoomPerClick: 1.0,
                     showRotationControl: true,
                     showNavigationControl: this.config.global.showControls,
                     minZoomLevel: this.config.global.minZoomLevel,//Math.min(this.config.global.minZoomLevel, this.config.global.minZoomLevel*this.config.image.originalImageWidth/$div.width()),
                     maxZoomLevel: maxZoomLevel,
                     zoomPerScroll: this.config.global.zoomSpeed,
                     mouseNavEnabled: this.config.global.zoomSpeed > 1,
                     homeButton: this.config.global.zoomHome,
                     rotateLeftButton: this.config.global.rotateLeft,
                     rotateRightButton: this.config.global.rotateRight,
                     timeout: this.config.global.loadImageTimeout,
                     blendTime: .5,
                     alwaysBlend: false,
                     imageLoaderLimit: this.config.global.maxParallelImageLoads,
                     loadTilesWithAjax: true,
                     ajaxHeaders: {
                         "token" : this.config.global.webApiToken
                     },
                     viewportMargins: {
                         top: 0,
                         left: 0,
                         right: 0,
                         bottom: this.config.global.footerHeight
                     }
                 }
             console.log("osconfig ", osConfig);
             
             this.viewer = new OpenSeadragon( osConfig );
             var result = Q.defer();
                 
             this.observables = _createObservables(window, this);  
             if(this.config.global.rotationSlider || this.config.global.rotationInput) {                 
                 _setupRotation(this);
             }
             
             var image = this;
             this.observables.viewerOpen.subscribe(function(openevent, loadevent) {
                 result.resolve(image);                
             }, function(error) {            
                 result.reject(error);                
             });                
                 
                 
             // Calculate sizes if redraw is required
             
             this.observables.redrawRequired.subscribe(function(event) {            
                 if(_debug) {
                     console.log("viewer " + event.osState + "ed with target location ", event.targetLocation);                    
                 }
                 
                 image.redraw();
             });
             
                 
             if ( imageView.Controls ) {
                 this.controls = new imageView.Controls(this.config, this);
//                 osViewer.controls.init( _defaults );
             }
             
             if ( imageView.ZoomSlider ) {
                 this.zoomSlider = new imageView.ZoomSlider(this.config, this);
                 this.onFirstTileLoaded()
                 .then(function() {
                     if(image.zoomSlider) {
                         image.zoomSlider.init();
                         
                     }
                 })
//                 osViewer.zoomSlider.init( _defaults );                
             }
             
             if ( imageView.Overlays ) {
                 this.overlays = new imageView.Overlays(this.config, this);
//                 osViewer.overlays.init( _defaults );                
             }                
             
             if ( imageView.DrawRect ) {
                 this.drawRect = new imageView.DrawRect(this.config, this);
//                 osViewer.drawRect.init();                
             }   
             
             if ( imageView.TransformRect ) {        
                 this.transformRect = new imageView.TransformRect(this.config, this);
//                 osViewer.transformRect.init();                
             }                
             
             this.observables.redrawRequired.connect();                
             return result.promise;
         }

     /**
      * @return the list of observables associated with this viewer
      */
     imageView.Image.prototype.getObservables = function() {
         return this.observables;
     }
     /**
      * @return true if a footerImage exists
      */
     imageView.Image.prototype.hasFooter = function() {
         return this.footerImage != null;
     }
     /**
      * @return the config
      */
     imageView.Image.prototype.getConfig = function() {
         return this.config;
     }
     /**
      * Loads the image footer from the configured footer url
      */
     imageView.Image.prototype.loadFooter = function() {
         if ( this.config.image.baseFooterUrl && this.config.global.footerHeight > 0 ) {                
             this.footerImage = new Image();
             this.footerImage.src = this.config.image.baseFooterUrl.replace( "{width}", Math.round( this.container.width() ) ).replace( "{height}", Math.round( this.config.global.footerHeight ) );                
             this.footerImage.src = this.config.image.baseFooterUrl.replace( "/full/max/", "/full/!" + Math.round( this.container.width() ) + "," +  Math.round( this.config.global.footerHeight ) + "/");                
             var image = this;
             this.footerImage.onload = function() {
                 if ( _debug ) {
                     console.log( "loading footer image ", image.footerImage );
                     console.log( "Calculating image Footer size" );
                 }
                 
                 _drawFooter(image);
             };
         }
     }
     /**
      * gets the overlay group with the given name from the config
      */
     imageView.Image.prototype.getOverlayGroup = function( name ) {
         return this.config.getOverlayGroup( name );
     }
     /**
      * gets the highlighting coordinates from the config
      */
     imageView.Image.prototype.getHighlightCoordinates = function( name ) {
         return this.config.getCoordinates( name );
     }
     /**
      * return the sizes associated with this view
      */
     imageView.Image.prototype.getSizes = function() {
         return this.sizes;
     }
     /**
      * get the underlying tilesource of the viewer
      */
     imageView.Image.prototype.getImageInfo = function() {
         if(this.viewer) {
             return this.viewer.tileSources;
         }
         return null;
     }
     /**
      * close the OpenSeadragon viewer
      */
     imageView.Image.prototype.close = function() {
         if ( _debug ) {
             console.log( "Closing openSeadragon viewer" );
         }
         
         if ( this.viewer ) {
             this.viewer.destroy();
         }
     }
     /**
      * Calculates the sizes associates with this viewer
      */
     imageView.Image.prototype.redraw = function() {
         if(this.controls) {                     
             this.controls.setPanning( true );
         }
         this.sizes = _calculateSizes(this);
     }
     /**
      * @return a promise resolved once the first tile is loaded
      */
     imageView.Image.prototype.onFirstTileLoaded = function() {
         var defer = Q.defer();
         
         if(this.observables) {
             this.observables.firstTileLoaded.subscribe(function(event) {
                 defer.resolve(event);
             }, function(error) {
                 defer.reject(error)
             });
         } else {
             defer.reject("No observables defined");
         }
         return defer.promise;
     }
     /**
      * Scale the given point or rectangle in the original image to OpenSeadragon coordinates
      */
     imageView.Image.prototype.scaleToOpenSeadragon = function(roi) {
         var displayImageSize = this.viewer.world.getItemAt(0).source.dimensions;
         var originalImageSize = this.sizes.originalImageSize;
         var scale = originalImageSize.x/displayImageSize.x;
         roi = roi.times(1/displayImageSize.x);
         roi = roi.times(1/scale);        
         return roi;
     }
     /**
      * Scale the given point or rectangle in OpenSeadragon coordinates to original image coordinates
      */
     imageView.Image.prototype.scaleToImage = function(point) {
         var displayImageSize = this.viewer.world.getItemAt(0).source.dimensions;
         var originalImageSize = this.sizes.originalImageSize;
         var scale = originalImageSize.x/displayImageSize.x;
         point = point.times(displayImageSize.x);
         point = point.times(scale);
         return point;
     }

//     imageView.Image.prototype.convertDisplayToImageCoordinates = function(overlay) {
//         var topLeft = this.scaleToImage(new OpenSeadragon.Point(overlay.rect.x, overlay.rect.y));
//         var bottomRight = this.scaleToImage(new OpenSeadragon.Point(overlay.rect.x+overlay.rect.width, overlay.rect.y+overlay.rect.height));
//         var angle = this.viewer.viewport.getRotation();
//         
//         topLeft = this.rotate(topLeft, angle);
//         bottomRight = this.rotate(bottomRight, angle);
//         var roi = new OpenSeadragon.Rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
//         
//         return roi;
//     }
//     
//     imageView.Image.prototype.convertImageToDisplayCoordinates = function(rectString) {
//         var angle = -this.viewer.viewport.getRotation();
//         var points = rectString.split(',');
//         var x1 = parseInt(points[0]);
//         var y1 = parseInt(points[1]);
//         var x2 = parseInt(points[2]);
//         var y2 = parseInt(points[3]);
//         
//         
//         var topLeft = new OpenSeadragon.Point(x1, y1);
//         var bottomRight = new OpenSeadragon.Point(x2, y2);
//         
//         topLeft = this.rotateBack(topLeft, angle);
//         bottomRight = this.rotateBack(bottomRight, angle);
//         var roi = new OpenSeadragon.Rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
//         roi = this.scaleToOpenSeadragon(roi);
//         return roi;
//     }
//     
//     imageView.Image.prototype.rotateBack = function(point, angle) {
//         var bounds = new OpenSeadragon.Rect(0,0,this.config.imageWidth, this.config.imageHeight);
//         var rotatedBounds = _getRotatedBounds(bounds, angle);
//
//         var center =  new OpenSeadragon.Point(this.config.imageWidth/2.0, this.config.imageHeight/2.0);
//         var translate = new OpenSeadragon.Point(-Math.abs(rotatedBounds.x), -Math.abs(rotatedBounds.y));
//         
//         point = point.plus(translate);
//         point = point.rotate(angle, center);
//         return point;
//     }
//     
//     imageView.Image.prototype.rotate = function(point, angle) {
//         var bounds = new OpenSeadragon.Rect(0,0,this.config.imageWidth, this.config.imageHeight);
//         var rotatedBounds = _getRotatedBounds(bounds, angle);
//
//         var center =  new OpenSeadragon.Point(this.config.imageWidth/2.0, this.config.imageHeight/2.0);
//         var translate = new OpenSeadragon.Point(Math.abs(rotatedBounds.x), Math.abs(rotatedBounds.y));
//         
//         point = point.rotate(angle, center);
//         point = point.plus(translate);
//         return point;
//     }
//     
     
     /**
      * input: a rectangle or point in the OpenSeadragon coordinate system
      * output: the same rectangle scaled to the size of the original image rotated by the current viewport rotation
      */
     imageView.Image.prototype.scaleToRotatedImage = function(roi) {
         var displayImageSize = this.viewer.world.getItemAt(0).source.dimensions;
//         var originalImageSize = {x:this.sizes.originalImageSize.x, y:this.sizes.originalImageSize.y};
         var originalImageSize = {x:this.config.image.originalImageWidth, y:this.config.image.originalImageHeight};

         var displayImageRect = new OpenSeadragon.Rect(0,0,displayImageSize.x, displayImageSize.y);
         var originalImageRect = new OpenSeadragon.Rect(0,0,originalImageSize.x, originalImageSize.y);
         
         var rotation = this.viewer.viewport.getRotation();
         var displayImageRect_rotated = _getRotatedBounds(displayImageRect, rotation);
         var originalImageRect_rotated = _getRotatedBounds(originalImageRect, rotation);
         
         var scale = originalImageRect_rotated.width/displayImageRect_rotated.width;
         roi = roi.times(displayImageSize.x);
         roi = roi.times(scale);
         return roi;
     }
     
     /**
      * input: a rectangle or point in the original image rotated by the current viewport rotation
      * output: the same rectangle/point scaled to OpenSeadragon coordinates
      */
     imageView.Image.prototype.scaleToOpenSeadragonCoordinates = function(roi) {
         var displayImageSize = this.viewer.world.getItemAt(0).source.dimensions;
//         var originalImageSize = {x:this.viewer.tileSources[0].tileSource.width, y:this.viewer.tileSources[0].tileSource.height};
         var originalImageSize = {x:this.config.image.originalImageWidth, y:this.config.image.originalImageHeight};

//         console.log("originalImageSize", originalImageSize);
         var displayImageRect = new OpenSeadragon.Rect(0,0,displayImageSize.x, displayImageSize.y);
         var originalImageRect = new OpenSeadragon.Rect(0,0,originalImageSize.x, originalImageSize.y);
         
         var rotation = this.viewer.viewport.getRotation();
         var displayImageRect_rotated = _getRotatedBounds(displayImageRect, rotation);
         var originalImageRect_rotated = _getRotatedBounds(originalImageRect, rotation);
         
         var scale = originalImageRect_rotated.width/displayImageRect_rotated.width;
         roi = roi.times(1/displayImageSize.x);
         roi = roi.times(1/scale);
         return roi;
     }
     
     imageView.Image.prototype.convertRectFromOpenSeadragonToImage = function(rect) {
         var rectInCanvas = ImageView.convertRectFromImageToRotatedImage(rect, this.viewer);
         var rectInImage = this.scaleToRotatedImage(rectInCanvas);
         return rectInImage;
     }
     
     imageView.Image.prototype.convertRectFromImageToOpenSeadragon = function(rect) {
         var rectInCanvas = this.scaleToOpenSeadragonCoordinates(rect);
         var rectInOS = ImageView.convertRectFromRotatedImageToImage(rectInCanvas, this.viewer);
         return rectInOS;
     }
     
     
     imageView.convertCoordinatesFromImageToCanvas = function(rect, viewer) {
         var scale = viewer.drawer.context.canvas.width/viewer.viewport.getBoundsNoRotate(true).width;
         scale /= window.devicePixelRatio;
         
         var topLeft = _convertPointFromImageToCanvas(rect.getTopLeft(), viewer);
         var bottomRight = _convertPointFromImageToCanvas(rect.getBottomRight(), viewer);
         var centerX = topLeft.x + 0.5*(bottomRight.x-topLeft.x);
         var centerY = topLeft.y + 0.5*(bottomRight.y-topLeft.y);
         var canvasRect = new OpenSeadragon.Rect(centerX-0.5*rect.width*scale, centerY-0.5*rect.height*scale, rect.width*scale, rect.height*scale);
         return canvasRect;
     }
     
     imageView.convertCoordinatesFromCanvasToImage = function(rect, viewer) {
         
         var scale = viewer.drawer.context.canvas.width/viewer.viewport.getBoundsNoRotate(true).width;
         scale /= window.devicePixelRatio;
         
         var topLeft = _convertPointFromCanvasToImage(rect.getTopLeft(), viewer);
         var bottomRight = _convertPointFromCanvasToImage(rect.getBottomRight(), viewer);
         var centerX = topLeft.x + 0.5*(bottomRight.x-topLeft.x);
         var centerY = topLeft.y + 0.5*(bottomRight.y-topLeft.y);
         var canvasRect = new OpenSeadragon.Rect(centerX-0.5*rect.width/scale, centerY-0.5*rect.height/scale, rect.width/scale, rect.height/scale);
         return canvasRect;
     }

     
     imageView.convertPointFromImageToCanvas = function(point, viewer) {
         return _convertPointFromImageToCanvas(point, viewer);
     }
     
     imageView.convertPointFromCanvasToImage = function(point, viewer) {
         return _convertPointFromCanvasToImage(point, viewer);
     }
     
     /**
      * input parameter rect: A rectangle in the coordinate system of the plain unrotated image in OpenSeadragon coordinates
      * output: The same rectangle in trotated image in OpenSeadragon coordinates
      * 
      * Both rectangles are defined by their center and their width and height. Width and height remain constant,
      * while the center is converted into coordinates of the rotated image
      */
     imageView.convertRectFromImageToRotatedImage = function(rect, viewer) {
         
         var rotation = viewer.viewport.getRotation();
         var sourceBounds = new OpenSeadragon.Rect(0,0,viewer.source.width, viewer.source.height);
         var sourceBounds_rotated = _getRotatedBounds(sourceBounds, rotation);
         var aspectRatio_unrotated = sourceBounds.width/sourceBounds.height;
         var aspectRatio_rotated = sourceBounds_rotated.width/sourceBounds_rotated.height;
         
         var imageBounds_unrotated = new OpenSeadragon.Rect(0,0, 1.0, 1/aspectRatio_unrotated); 
         var imageBounds_rotated = _getRotatedBounds(imageBounds_unrotated, rotation);

         
         var rect_fromTopLeft_unrotated = rect.getCenter();
         var topLeft_fromCenter_unrotated = imageBounds_unrotated.getCenter().times(-1);
         var rect_fromCenter_unrotated = topLeft_fromCenter_unrotated.plus(rect_fromTopLeft_unrotated);

         var rect_fromCenter_rotated = _rotate(rect_fromCenter_unrotated, rotation, true);

         var topLeft_fromCenter_rotated = new OpenSeadragon.Point(imageBounds_rotated.width/2.0, imageBounds_rotated.height/2.0).times(-1);
         var rect_fromTopLeft_rotated = rect_fromCenter_rotated.minus(topLeft_fromCenter_rotated);
         var rect_rotated = new OpenSeadragon.Rect(rect_fromTopLeft_rotated.x-rect.width/2.0, rect_fromTopLeft_rotated.y-rect.height/2.0, rect.width, rect.height);
         return rect_rotated;
     }
     
     /**
      * input parameter rect: A rectangle in OpenSeadragon coordinates as if the image was the actual image rotated by the
      * current viewport rotation
      * output: The same rectangle in the displayed (unrotated) image in OpenSeadragon coordinates
      * 
      * Both rectangles are defined by their center and their width and height. Width and height remain constant,
      * while the center is converted into coordinates of the rotated image
      */
     imageView.convertRectFromRotatedImageToImage = function(rect, viewer) {

         var rotation = viewer.viewport.getRotation();
         var sourceBounds = new OpenSeadragon.Rect(0,0,viewer.source.width, viewer.source.height);
         var sourceBounds_rotated = _getRotatedBounds(sourceBounds, rotation);
         var aspectRatio_unrotated = sourceBounds.width/sourceBounds.height;
         var aspectRatio_rotated = sourceBounds_rotated.width/sourceBounds_rotated.height;
         
         var imageBounds_unrotated = new OpenSeadragon.Rect(0,0, 1.0, 1/aspectRatio_unrotated); 
         var imageBounds_rotated = _getRotatedBounds(imageBounds_unrotated, rotation);

         var topLeft_fromCenter_unrotated = imageBounds_unrotated.getCenter().times(-1);
         var topLeft_fromCenter_rotated = new OpenSeadragon.Point(imageBounds_rotated.width/2.0, imageBounds_rotated.height/2.0).times(-1);
         
         var rect_fromTopLeft_rotated = rect.getCenter();
         var rect_fromCenter_rotated = topLeft_fromCenter_rotated.plus(rect_fromTopLeft_rotated);

         var rect_fromCenter_unrotated = _rotate(rect_fromCenter_rotated, rotation, false);

         var rect_fromTopLeft_unrotated = rect_fromCenter_unrotated.minus(topLeft_fromCenter_unrotated);
         var rect_unrotated = new OpenSeadragon.Rect(rect_fromTopLeft_unrotated.x-rect.width/2.0, rect_fromTopLeft_unrotated.y-rect.height/2.0, rect.width, rect.height);
         return rect_unrotated;
     }
     
     imageView.convertPointFromImageToRotatedImage = function(point, viewer) {
         
         var rotation = viewer.viewport.getRotation();
         var sourceBounds = new OpenSeadragon.Rect(0,0,viewer.source.width, viewer.source.height);
         var sourceBounds_rotated = _getRotatedBounds(sourceBounds, rotation);
         var aspectRatio_unrotated = sourceBounds.width/sourceBounds.height;
         var aspectRatio_rotated = sourceBounds_rotated.width/sourceBounds_rotated.height;
         
         var imageBounds_unrotated = new OpenSeadragon.Rect(0,0, 1.0, 1/aspectRatio_unrotated); 
         var imageBounds_rotated = _getRotatedBounds(imageBounds_unrotated, rotation);

         
         var point_fromTopLeft_unrotated = point
         var topLeft_fromCenter_unrotated = imageBounds_unrotated.getCenter().times(-1);
         var point_fromCenter_unrotated = topLeft_fromCenter_unrotated.plus(point_fromTopLeft_unrotated);

         var point_fromCenter_rotated = _rotate(point_fromCenter_unrotated, rotation, true);

         var topLeft_fromCenter_rotated = new OpenSeadragon.Point(imageBounds_rotated.width/2.0, imageBounds_rotated.height/2.0).times(-1);
         var point_fromTopLeft_rotated = point_fromCenter_rotated.minus(topLeft_fromCenter_rotated);
         return point_fromTopLeft_rotated;
     }
     
     imageView.convertPointFromRotatedImageToImage = function(point, viewer) {

         var rotation = viewer.viewport.getRotation();
         var sourceBounds = new OpenSeadragon.Rect(0,0,viewer.source.width, viewer.source.height);
         var sourceBounds_rotated = _getRotatedBounds(sourceBounds, rotation);
         var aspectRatio_unrotated = sourceBounds.width/sourceBounds.height;
         var aspectRatio_rotated = sourceBounds_rotated.width/sourceBounds_rotated.height;
         
         var imageBounds_unrotated = new OpenSeadragon.Rect(0,0, 1.0, 1/aspectRatio_unrotated); 
         var imageBounds_rotated = _getRotatedBounds(imageBounds_unrotated, rotation);

         var topLeft_fromCenter_unrotated = imageBounds_unrotated.getCenter().times(-1);
         var topLeft_fromCenter_rotated = new OpenSeadragon.Point(imageBounds_rotated.width/2.0, imageBounds_rotated.height/2.0).times(-1);
         
         var point_fromTopLeft_rotated = point;
         var point_fromCenter_rotated = topLeft_fromCenter_rotated.plus(point_fromTopLeft_rotated);

         var point_fromCenter_unrotated = _rotate(point_fromCenter_rotated, rotation, false);

         var point_fromTopLeft_unrotated = point_fromCenter_unrotated.minus(topLeft_fromCenter_unrotated);
         return point_fromTopLeft_unrotated;
     }
     
     imageView.getPointInRotatedImage = function(point, viewer) {
         var aspectRatio = viewer.source.width/viewer.source.height;
         var rotation = viewer.viewport.getRotation();
         var imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         
         var point_fromImageCenter = imageTopLeft_fromImageCenter.plus(point);
         var point_fromImageCenter_rotated = _rotate(point_fromImageCenter, rotation, false);
         var point_fromImageTopLeft_rotated = point_fromImageCenter_rotated.minus(imageTopLeft_fromImageCenter);
         return point_fromImageTopLeft_rotated;
     }
     
     imageView.getPointInUnrotatedImage = function(point, viewer) {
         var aspectRatio = viewer.source.width/viewer.source.height;
         var rotation = viewer.viewport.getRotation();
         var imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         var imageTopLeft_fromImageCenter_rotated = _rotate(imageTopLeft_fromImageCenter, rotation, false);
         
         var point_fromImageCenter_rotated = imageTopLeft_fromImageCenter_rotated.plus(point);
         var point_fromImageCenter = _rotate(point_fromImageCenter_rotated, rotation, true);
         var point_fromImageTopLeft = point_fromImageCenter.minus(imageTopLeft_fromImageCenter);
         return point_fromImageTopLeft;
     }

     function _createObservables(window, image) {
         var observables = {};
         observables.viewerOpen = Rx.Observable.create(function(observer) {
             image.viewer.addOnceHandler( 'open', function( event ) {
                 event.osState = "open";
                 
                 if(Number.isNaN(event.eventSource.viewport.getHomeBounds().x)) {
                     return observer.onError("Unknow error loading image from ", _defaults.image.tileSource);
                 } else {                    
                     return observer.onNext(event);
                 }
             } );
             image.viewer.addOnceHandler( 'open-failed', function( event ) {
                 event.osState = "open-failed";
                 console.error("Failed to open openseadragon ");
                 
                 return observer.onError(event);
             } );
         });
         
         observables.firstTileLoaded = Rx.Observable.create(function(observer) {
             image.viewer.addOnceHandler( 'tile-loaded', function( event ) {
                 event.osState = "tile-loaded";
                 
                 return observer.onNext(event);
             } );
             image.viewer.addOnceHandler( 'tile-load-failed', function( event ) {
                 event.osState = "tile-load-failed";
                 console.error("Failed to load tile");
                 
                 return observer.onError(event);
             } );
         });
         
         observables.viewerZoom = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'zoom', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.animationComplete = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'animation-finish', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.viewportUpdate = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'update-viewport', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.animation = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'animation', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.viewerRotate = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'rotate', function( event ) {
                 event.osState = "rotate";
                 return observer.onNext(event);
             } );
         });
         observables.canvasResize = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'resize', function( event ) {
                 event.osState = "resize";
                 
                 return observer.onNext(event);
             } );
         });
         observables.windowResize = Rx.Observable.fromEvent(window, "resize").map(function(event) {
             event.osState = "window resize";
             
             return event;
         });
         observables.overlayRemove = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'remove-overlay', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.overlayUpdate = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'update-overlay', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.levelUpdate = Rx.Observable.create(function(observer) {
             image.viewer.addHandler( 'update-level', function( event ) {
                 return observer.onNext(event);
             } );
         });
         observables.redrawRequired = observables.viewerOpen
         .merge(observables.viewerRotate
                 .merge(observables.canvasResize)
                 .debounce(10))
         .map(function(event) {
             var location = {};
             
             if(image.controls) {
                 location = image.controls.getLocation();
             }
             
             if(event.osState === "open") {
                 location.zoom = image.viewer.viewport.getHomeZoom();
                 if(image.config.image.location) {
                    location = image.config.image.location;
                 }
             }
             
             event.targetLocation = location;
             
             return event;
         })
         .publish();
         
         return observables;
     }
     
     function _drawFooter(image) {
         if ( image && image.viewer ) {
             _overlayFooter({userData:image});
             image.viewer.removeHandler( 'update-viewport', _overlayFooter);
             image.viewer.addHandler( 'update-viewport', _overlayFooter, image);
         }  
     }
     function _overlayFooter( event ) {
         var image = event.userData;
         if ( image.config.global.footerHeight > 0 ) {
             var footerHeight = image.config.global.footerHeight;
             var footerPos = new OpenSeadragon.Point( 0, image.container.height() - footerHeight );
             var footerSize = new OpenSeadragon.Point( image.container.width(), footerHeight );
             
             if ( !image.canvasScale ) {
                 image.canvasScale = image.viewer.drawer.context.canvas.width / image.viewer.drawer.context.canvas.clientWidth;
             }
             
             if ( image.canvasScale != 1 ) {
                 footerPos = footerPos.times( image.canvasScale );
                 footerSize = footerSize.times( image.canvasScale );
             }
             image.viewer.drawer.context.drawImage( image.footerImage, footerPos.x, footerPos.y, footerSize.x, footerSize.y );
         }
     };
     
     function _setupZoomSlider(image) {
         if(ImageView.ZoomSlider) {
             var slider = new ImageView.ZoomSlider(image.config.zoom, image);
             return slider;
         }
     }

     
     function _setupRotation(image) {
                  
         //set initial rotation
         var degrees = image.config.image.initialRotation;
         var deskew = _getDeskewAngle(degrees);
         image.rotation = _getRotation(degrees);
         var config = image.config.global;
         var viewer = image.viewer;
         
         //setup deskew slider
         if(config.rotationSlider) {             
             $("#" + config.rotationSlider).slider({
                 orientation: "vertical",
                 min: _MIN_DESKEW_ANGLE,
                 max: _MAX_DESKEW_ANGLE,
                 step: 0.1,
                 slide: function(event, ui) {
                     var degrees = -ui.value;
                     var deskew = _getDeskewAngle(degrees);
                     viewer.viewport.setRotation(deskew + image.rotation);
                 }
             });
             $("#" + config.rotationSlider).slider("option", "value", -deskew);
         }
         
         //handle rotation input
         if(config.rotationInput) {             
             $("#" + config.rotationInput).on("blur", function(event) {
                 var degrees = _normalizeAngle(event.target.value);
                 var deskew = _getDeskewAngle(degrees);
                 image.rotation = _getRotation(degrees);
                 viewer.viewport.setRotation(degrees);
                 if(config.rotationSlider) {                              
                     $("#" + config.rotationSlider).slider("option", "value", -deskew);
                 }
             });
         }
         //handle rotation changes
         viewer.addHandler( 'rotate', function( event ) {
             var degrees = _normalizeAngle(event.degrees);
             var deskew = _getDeskewAngle(degrees);
             image.rotation = _getRotation(viewer.viewport.getRotation());
             if(config.rotationInput) {                              
                 var rot = (image.rotation + deskew);
                 $("#" + config.rotationInput).val(rot.toFixed(1)).change();
             }
         });
     }
     
     function _calculateSizes(image) {
         if ( _debug ) {
             console.log( "viewImage: calcualte sizes" );
             console.log("Home zoom = ", image.viewer.viewport.getHomeZoom());
         }
         
         var sizes = new ImageView.Measures( image );
         
         if ( image.config.global.adaptContainerHeight ) {
             sizes.resizeCanvas();
         }
         
         if ( image.viewer != null ) {
             image.viewer.viewport.setMargins( {bottom: sizes.footerHeight + sizes.calculateExcessHeight()} );
         }
         
         if ( _debug ) {
             console.log( "sizes: ", sizes );
         }
         return sizes;
     };
     
     function _timeout(promise, time) {
         var deferred = new jQuery.Deferred();

         $.when(promise).done(deferred.resolve).fail(deferred.reject).progress(deferred.notify);

         setTimeout(function() {
             deferred.reject("timeout");
         }, time);

         return deferred.promise();
     }

     
     function _convertPointFromCanvasToImage(point, viewer) {
         
         var scale = viewer.drawer.context.canvas.width/viewer.viewport.getBoundsNoRotate(true).width;
         scale /= window.devicePixelRatio;
         
         var aspectRatio = viewer.source.width/viewer.source.height;
         var rotation = viewer.viewport.getRotation();
         var imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         var canvasCenter_fromImageTopLeft = viewer.viewport.getCenter(true);
         var canvasCenter_fromCanvasTopLeft = new OpenSeadragon.Point(viewer.viewport.getBoundsNoRotate(true).width/2.0, viewer.viewport.getBoundsNoRotate(true).height/2.0);
         
         var canvasCenter_fromImageCenter = imageTopLeft_fromImageCenter.plus(canvasCenter_fromImageTopLeft);
         var canvasCenter_fromImageCenter_rotated = _rotate(canvasCenter_fromImageCenter, rotation, true);
         
         var imageCenter_fromCanvasTopLeft = canvasCenter_fromCanvasTopLeft.minus(canvasCenter_fromImageCenter_rotated);
         
         var point_fromCanvasTopLeft = point.times(1/scale);
         var point_fromImageCenter_rotated = point_fromCanvasTopLeft.minus(imageCenter_fromCanvasTopLeft);
         var point_fromImageTopLeft = _rotate(point_fromImageCenter_rotated, rotation, false).minus(imageTopLeft_fromImageCenter);

         return point_fromImageTopLeft;
     }
     
     function _convertPointFromImageToCanvas(point, viewer) {
         var canvasWidth = viewer.drawer.context.canvas.width;
         var viewportWidth = viewer.viewport.getBoundsNoRotate(true).width;

         var scale = canvasWidth/viewportWidth;
         scale /= window.devicePixelRatio;
         
         var aspectRatio = viewer.source.width/viewer.source.height;
         var rotation = viewer.viewport.getRotation();
         var imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         var canvasCenter_fromImageTopLeft = viewer.viewport.getCenter(true);
         var canvasCenter_fromCanvasTopLeft = new OpenSeadragon.Point(viewer.viewport.getBoundsNoRotate(true).width/2.0, viewer.viewport.getBoundsNoRotate(true).height/2.0);
         
         var canvasCenter_fromImageCenter = imageTopLeft_fromImageCenter.plus(canvasCenter_fromImageTopLeft);
         var canvasCenter_fromImageCenter_rotated = _rotate(canvasCenter_fromImageCenter, rotation, true);
         
         var imageCenter_fromCanvasTopLeft = canvasCenter_fromCanvasTopLeft.minus(canvasCenter_fromImageCenter_rotated);

         var point_fromImageCenter = imageTopLeft_fromImageCenter.plus(point);
         var point_fromImageCenter_rotated = _rotate(point_fromImageCenter, rotation, true);
         
         var point_FromCanvasTopLeft = imageCenter_fromCanvasTopLeft.plus(point_fromImageCenter_rotated);

         var p = point_FromCanvasTopLeft.times(scale);
         
         return p;
     }

 /**
  * Rotates around the coordinate system origin
  */
 function _rotate(point, degrees, antiClockwise) {
     
     var rad = degrees*Math.PI/180.0;
     
     var x,y;
     if(antiClockwise) {
         x = point.x*Math.cos(rad) - point.y*Math.sin(rad);
         y = point.x*Math.sin(rad) + point.y*Math.cos(rad);
     } else {
         x =  point.x*Math.cos(rad) + point.y*Math.sin(rad);
         y = -point.x*Math.sin(rad) + point.y*Math.cos(rad);
     }
     
     return new OpenSeadragon.Point(x,y);
 }
     
     /**
      * Calculates the bounding rectangle that just encompasses the given rectangle rotated by the given angle in degrees.
      * The given rectangle is assumed to start at coordinates 0,0; and the returned rectangle will be given in the same coordinate frame,
      * i.e. with x and y values holding the offset from the original origin point (x and y are thus always negative, width and height always
      * larger than those of the original rectangle)
      * 
      * @param rect  the rectangle to rotate. must be an object with properties height and width
      * @param degrees   the rotation angle in degrees
      * @return     An OpenSeadragon.Rect containing the rotated rectangle in the original coordinate system
      */
     function _getRotatedBounds(rect, degrees) {
             
             var rad = degrees * Math.PI/180.0;
         

             var sint = Math.abs(Math.sin(rad));
             var cost = Math.abs(Math.cos(rad));

             
             
             
             var hh = (rect.width * sint + rect.height * cost);
             var ww = (rect.width * cost + rect.height * sint);
//             double hh = Math.max(h1, h2);
//             double ww = hh * bounds.width / bounds.height;
//             double x = (bounds.width - ww) * .5;
//             double y = (bounds.height - hh) * .5;
             
             var w = Math.abs(ww);
             var h = Math.abs(hh);
             
             var dw = w - rect.width;
             var dh = h - rect.height;
             
             return new OpenSeadragon.Rect(-dw/2.0, -dh/2.0, w, h);
     }

     /**
      * Creates a tilesource object usable by the OpenSeadragon viewer from a url or json-object
      * @param source   either a url pointing to a iiif info json-object or directly to an image 
      * or a iiif info json-object, optionally as a string, or a list of image resource objects - consisting
      * each of a url, a width and a height - which act as layers of a pyramid view
      * @return a promise resolved when any urls are loaded - if no urls need to be loaded, the promise resolves immediately
      */
     function _createTileSource(source, config) {

         var result = Q.defer();

         ImageView.TileSourceResolver.resolveAsJson(source)
         .then(
                 function(imageInfo) {                        
                     if(_debug) {                
                         console.log("IIIF image info ", imageInfo);                        
                     }               
                     _setImageSizes(imageInfo, config.global.imageSizes);       
                     _setTileSizes(imageInfo, config.global.tileSizes);                
                     var tileSource;
                     if(imageInfo.tiles && imageInfo.tiles.length > 0) {
                         tileSource = new OpenSeadragon.IIIFTileSource(imageInfo);                    
                     } else {                
                         tileSource  = _createPyramid(imageInfo, config);                    
                     }
                     
                     return tileSource;                
                 },
                 function(error) {            
                     if(ImageView.TileSourceResolver.isURI(config.image.tileSource)) {
                         if(_debug) {                    
                             console.log("Image URL", config.image.tileSource);                        
                         }
                         
                         var tileSource = new OpenSeadragon.ImageTileSource( {                    
                             url: config.image.tileSource,                        
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
     
     /**
      * creates a OpenSeadragon.LegacyTileSource (pyramid image source) from the given imageInfo object, which may either
      * be a IIIF imageInfo json-object or a list of image resource objects - consisting
      * each of a url, a width and a height
      * @param imageInfo    the image information json object, either a iiif image resource or a list of simple image resources
      * @return the tilesource usable by OpenSeadragon
      */
     function _createPyramid( imageInfo, config ) {
         if(_debug) {
             console.log("Creating legacy tilesource from imageInfo ", imageInfo);
         }
         var fileExtension = config.image.mimeType;
         fileExtension = fileExtension.replace( "image/", "" );
         fileExtension = fileExtension.replace("jpeg", "jpg").replace("tiff", "tif");
         var imageLevels = [];
         var tileSource;
         if(Array.isArray(imageInfo)) {
             imageInfo.forEach(function(level) {
                 level.mimetype = config.image.mimeType;
             });
             tileSource = new OpenSeadragon.LegacyTileSource(imageInfo);
         } else if(imageInfo.sizes) {
             imageInfo.sizes.forEach(function(size) {
                 if(_debug) {                    
                     console.log("Image level width = ", size.width)
                     console.log("Image level height = ", size.height)
                 }
                 
                 var level = {
                     mimetype: config.image.mimeType,
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
     }
     
     /**
      * Inserts the given image sizes into the imageInfo object
      * @param imageInfo    the imageInfo object in which the sizes are inserted
      * @param sizes        the sizes to be inserted
      */
     function _setImageSizes(imageInfo, sizes) {
         if(sizes) {             
             if(typeof sizes == 'string') {                 
             var string = sizes.replace(/[\{\}]/, "");
             var sizes = JSON.parse(sizes);
             }
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
     }
     /**
      * Inserts the given tiles into the imageInfo object
      * @param imageInfo    the imageInfo object in which the tiles are inserted
      * @param tiles        the tiles to be inserted
      */
     function _setTileSizes(imageInfo, tiles) {
         if(tiles) {             
             if(typeof tiles === 'string') {                 
                 var tileString = tiles.replace(/(\d+)/, '"$1"').replace("=", ":");
                 tiles = JSON.parse(tileString);
             }
             var iiifTiles = [];
             
             Object.keys(tiles).forEach(function(size) {
                 var scaleFactors = tiles[size];
                 iiifTiles.push({"width": parseInt(size), "height": parseInt(size), "scaleFactors": scaleFactors})
             });
             
             imageInfo.tiles = iiifTiles;
         }
     }
     
     function _normalizeAngle(degrees) {
         var norm = ((degrees%360)+360)%360;
         return norm;
     }

     /**
      * get the rotation as a value between 0 and 360 degrees and rounded to 90 degrees 
      * 
      * @param degrees
      * @return    the rotation in 90 degree multiples
      */
     function _getRotation(degrees) {
         degrees += _MAX_DESKEW_ANGLE;
         degrees /= 90;
         degrees = parseInt(degrees);
         degrees *= 90;
         return _normalizeAngle(degrees);
     }

     /**
      * get the rotation modulo 90 degrees as a value between 0 and 45 degrees or between 315 and 360 degrees
      * 
      * @param degrees
      * @return the deskew part of the rotation
      */
     function _getDeskewAngle(degrees) {
         degrees += _MAX_DESKEW_ANGLE;
         degrees = parseFloat(degrees%90);
         degrees -= _MAX_DESKEW_ANGLE;
         degrees = _normalizeAngle(degrees);
         degrees = degrees > _MAX_DESKEW_ANGLE ? degrees-360 : degrees;
         return degrees;
     }

     function _rotateDiv(div, angle) {
             if(angle != 0) {
             $(div).css("-moz-transform", "rotate(" + angle + "deg)");
             $(div).css("-webkit-transform", "rotate(" + angle + "deg)");
             $(div).css("-ms-transform", "rotate(" + angle + "deg)");
             $(div).css("-o-transform", "rotate(" + angle + "deg)");
             $(div).css("transform", "rotate(" + angle + "deg)");
             var sin = Math.sin(angle);
             var cos = Math.cos(angle);
             $(div).css("filter", "progid:DXImageTransform.Microsoft.Matrix(M11="+cos+", M12="+sin+", M21=-"+sin+", M22="+cos+", sizingMethod='auto expand'");
             }
     }
     
     return imageView;
})();

//browser backward compability
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



