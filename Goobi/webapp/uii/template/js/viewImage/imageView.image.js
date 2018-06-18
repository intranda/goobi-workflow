const _MAX_DESKEW_ANGLE = 30  //must be <45

var ImageView = ( function() {
    'use strict';
    
     let imageView =  {};
     
     imageView.Image = function(config)  {         
         this.config = config;
         console.log("initializing image view with config ", this.config);
         this.rotation = _getRotation(this.config.initialRotation);
     }
     
     imageView.Image.prototype.load = function() {
         if(this.config.url) {             
             var tileSource = new OpenSeadragon.ImageTileSource({
                 url: this.config.url,
                 width: 800,
                 height: 607
             }) 
         } else {
             var tileSource = this.config.tileSource;
         }
             console.log("loading tilesource ", tileSource);
             
             this.viewer = OpenSeadragon ({
                 id: this.config.div,
                 prefixUrl: this.config.resourcePath + "/javascript/openseadragon/images/",
                 tileSources: tileSource,
                 minZoomLevel: 0.2,
                 maxZoomLevel: 5,
                 showRotationControl: true,
                 degrees: this.config.initialRotation,
                 showFullPageControl: false,
                 visibilityRatio: 0
             })
             var viewerOpenPromise = this.getViewerOpenPromise();
             _setupRotation(this);
             
             
//             transformRect.init(viewer);
//             drawRect.init(viewer);
             
//             viewerOpenPromise
//             .then(function(image) {
//                 console.log("opened ", image);
//                 var overlay = drawRect.drawOverlay(image.convertImageToDisplayCoordinates($("#" + config.roiInput).val()));
//                 $("#" + config.div).on("keydown", function(event) {
//                 console.log("keydown ", event);
//                     if(event.key == "Shift") {                
//                         drawRect.startDrawing((overlay)=> {
//                             var roi = image.convertDisplayToImageCoordinates(overlay);
//                             var roiString = parseInt(roi.x) + "," + parseInt(roi.y) + "," + parseInt(roi.x + roi.width) + "," + parseInt(roi.y + roi.height);
//                             $("#" + config.roiInput).val(roiString).change();
//                         });
//                     }
//                 })
//                 $("#" + config.div).on("keyup", function(event) {
//                 console.log("keyup ", event);
//                     if(event.key == "Shift") { 
//                         var overlay = drawRect.endDrawing();
//                         if(overlay) {                        
//                             transformRect.currentOverlay = overlay;
//                         }
//                     }
//                 })
//                 
//                 if(overlay) {                
//                     transformRect.currentOverlay = overlay;
//                     transformRect.startDrawing((overlay)=> {
//                         var roi = image.convertDisplayToImageCoordinates(overlay);
//                         var roiString = parseInt(roi.x) + "," + parseInt(roi.y) + "," + parseInt(roi.x + roi.width) + "," + parseInt(roi.y + roi.height);
//                         $("#" + config.roiInput).val(roiString).change();
//                     });
//                 }
//             });
             return viewerOpenPromise;
         }
     
     imageView.Image.prototype.getViewerOpenPromise = function() {
         var deferred = Q.defer();
         var image = this;
         this.viewer.addOnceHandler( 'open', function( event ) {
             event.osState = "open";
             
             if(Number.isNaN(event.eventSource.viewport.getHomeBounds().x)) {
                 deferred.reject("Unknow error loading image from ", this.config.url);
             } else {                    
                 deferred.resolve(image);
             }
         } );
         this.viewer.addOnceHandler( 'open-failed', function( event ) {
             event.osState = "open-failed";
             console.log("Failed to open openseadragon ");
             
             deferred.reject(event);
         } );
         return deferred.promise;
     }
     
     imageView.Image.prototype.convertDisplayToImageCoordinates = function(overlay) {
         var topLeft = this.scaleToImage(new OpenSeadragon.Point(overlay.rect.x, overlay.rect.y));
         var bottomRight = this.scaleToImage(new OpenSeadragon.Point(overlay.rect.x+overlay.rect.width, overlay.rect.y+overlay.rect.height));
//         var topLeft = this.scaleToImage(new OpenSeadragon.Point(overlay.start.x, overlay.start.y));
//         var bottomRight = this.scaleToImage(new OpenSeadragon.Point(overlay.current.x, overlay.current.y));
         var angle = this.viewer.viewport.getRotation();
         
         topLeft = this.rotate(topLeft, angle);
         bottomRight = this.rotate(bottomRight, angle);
         var roi = new OpenSeadragon.Rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
         
         return roi;
     }
     
     imageView.Image.prototype.convertImageToDisplayCoordinates = function(rectString) {
         var angle = -this.viewer.viewport.getRotation();
         var points = rectString.split(',');
         var x1 = parseInt(points[0]);
         var y1 = parseInt(points[1]);
         var x2 = parseInt(points[2]);
         var y2 = parseInt(points[3]);
         
         
         var topLeft = new OpenSeadragon.Point(x1, y1);
         var bottomRight = new OpenSeadragon.Point(x2, y2);
         
         topLeft = this.rotateBack(topLeft, angle);
         bottomRight = this.rotateBack(bottomRight, angle);
         var roi = new OpenSeadragon.Rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
         roi = this.scaleToOpenSeadragon(roi);
         return roi;
     }
     
     imageView.Image.prototype.rotateBack = function(point, angle) {
         var bounds = new OpenSeadragon.Rect(0,0,this.config.imageWidth, this.config.imageHeight);
         var rotatedBounds = _getRotatedBounds(bounds, angle);

         var center =  new OpenSeadragon.Point(this.config.imageWidth/2.0, this.config.imageHeight/2.0);
         var translate = new OpenSeadragon.Point(-Math.abs(rotatedBounds.x), -Math.abs(rotatedBounds.y));
         
         point = point.plus(translate);
         point = point.rotate(angle, center);
         return point;
     }
     
     imageView.Image.prototype.rotate = function(point, angle) {
         var bounds = new OpenSeadragon.Rect(0,0,this.config.imageWidth, this.config.imageHeight);
         var rotatedBounds = _getRotatedBounds(bounds, angle);

         var center =  new OpenSeadragon.Point(this.config.imageWidth/2.0, this.config.imageHeight/2.0);
         var translate = new OpenSeadragon.Point(Math.abs(rotatedBounds.x), Math.abs(rotatedBounds.y));
         
         point = point.rotate(angle, center);
         point = point.plus(translate);
         return point;
     }
     
     
     imageView.Image.prototype.scaleToOpenSeadragon = function(roi) {
         var displayImageSize = this.viewer.world.getItemAt(0).source.dimensions;
         var originalImageSize = {x:this.config.imageWidth, y:this.config.imageHeight};
         var scale = originalImageSize.x/displayImageSize.x;
         roi = roi.times(1/displayImageSize.x);
         roi = roi.times(1/scale);        
         return roi;
     }
     
     imageView.Image.prototype.scaleToImage = function(roi) {
         var displayImageSize = this.viewer.world.getItemAt(0).source.dimensions;
         var originalImageSize = {x:this.config.imageWidth, y:this.config.imageHeight};
         var scale = originalImageSize.x/displayImageSize.x;
         roi = roi.times(displayImageSize.x);
         roi = roi.times(scale);
         return roi;
     }

     function _setupRotation(image) {
       //rotation
         let config = image.config;
         let viewer = image.viewer;
         $("#" + config.rotationSlider + "").slider({
             orientation: "vertical",
             min: -_MAX_DESKEW_ANGLE,
             max: _MAX_DESKEW_ANGLE,
             slide: function(event, ui) {
                 var degrees = -ui.value;
                 var deskew = _getDeskewAngle(degrees);
                 viewer.viewport.setRotation(deskew + image.rotation);
             }
         });
         $("#" + config.rotationInput).on("input", function(event) {
             var degrees = event.target.value;
             var deskew = _getDeskewAngle(degrees);
             image.rotation = _getRotation(degrees);
             $("#" + config.rotationSlider).slider("option", "value", -deskew);
             viewer.viewport.setRotation(degrees);
          });
         viewer.addHandler( 'rotate', function( event ) {
             var degrees = _normalizeAngle(event.degrees);
             var deskew = _getDeskewAngle(degrees);
             image.rotation = _getRotation(viewer.viewport.getRotation());
             var rot = (image.rotation + deskew);
             $("#" + config.rotationInput).val(rot).change();
         });

         var degrees = config.initialRotation;
         var deskew = _getDeskewAngle(degrees);
         image.rotation = _getRotation(degrees);
         $("#" + config.rotationSlider).slider("option", "value", -deskew);
     }
     
     
     imageView.convertCoordinatesFromImageToCanvas = function(rect, viewer) {
         let scale = viewer.canvas.clientWidth/viewer.viewport.getBoundsNoRotate(true).width;
         var topLeft = _convertPointFromImageToCanvas(rect.getTopLeft(), viewer);
         var bottomRight = _convertPointFromImageToCanvas(rect.getBottomRight(), viewer);
         var centerX = topLeft.x + 0.5*(bottomRight.x-topLeft.x);
         var centerY = topLeft.y + 0.5*(bottomRight.y-topLeft.y);
         var canvasRect = new OpenSeadragon.Rect(centerX-0.5*rect.width*scale, centerY-0.5*rect.height*scale, rect.width*scale, rect.height*scale);
         return canvasRect;
     }
     
     imageView.convertCoordinatesFromCanvasToImage = function(rect, viewer) {
         
         let scale = viewer.canvas.clientWidth/viewer.viewport.getBoundsNoRotate(true).width;
         
         var topLeft = _convertPointFromCanvasToImage(rect.getTopLeft(), viewer);
         var bottomRight = _convertPointFromCanvasToImage(rect.getBottomRight(), viewer);
         var centerX = topLeft.x + 0.5*(bottomRight.x-topLeft.x);
         var centerY = topLeft.y + 0.5*(bottomRight.y-topLeft.y);
         var canvasRect = new OpenSeadragon.Rect(centerX-0.5*rect.width/scale, centerY-0.5*rect.height/scale, rect.width/scale, rect.height/scale);
         return canvasRect;
     }
     
     imageView.getCoordinatesInUnrotatedImage = function(rect, viewer) {
         let scale = 1
         var topLeft =imageView.getPointInUnRotatedImage(rect.getTopLeft(), viewer);
         var bottomRight = imageView.getPointInUnRotatedImage(rect.getBottomRight(), viewer);
         var centerX = topLeft.x + 0.5*(bottomRight.x-topLeft.x);
         var centerY = topLeft.y + 0.5*(bottomRight.y-topLeft.y);
         var canvasRect = new OpenSeadragon.Rect(centerX-0.5*rect.width*scale, centerY-0.5*rect.height*scale, rect.width*scale, rect.height*scale);
         return canvasRect;
     }
     
     imageView.getCoordinatesInRotatedImage = function(rect, viewer) {
         var scale = 1;
         var topLeft = imageView.getPointInRotatedImage(rect.getTopLeft(), viewer);
         var bottomRight = imageView.getPointInRotatedImage(rect.getBottomRight(), viewer);
         var centerX = topLeft.x + 0.5*(bottomRight.x-topLeft.x);
         var centerY = topLeft.y + 0.5*(bottomRight.y-topLeft.y);
         var canvasRect = new OpenSeadragon.Rect(centerX-0.5*rect.width/scale, centerY-0.5*rect.height/scale, rect.width/scale, rect.height/scale);
         return canvasRect;
     }
     
     imageView.getPointInRotatedImage = function(point, viewer) {
         let aspectRatio = viewer.source.width/viewer.source.height;
         let rotation = viewer.viewport.getRotation();
         let imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         
         let point_fromImageCenter = imageTopLeft_fromImageCenter.plus(point);
         let point_fromImageCenter_rotated = _rotate(point_fromImageCenter, rotation, false);
         let point_fromImageTopLeft_rotated = point_fromImageCenter_rotated.minus(imageTopLeft_fromImageCenter);
         return point_fromImageTopLeft_rotated;
     }
     
     imageView.getPointInUnrotatedImage = function(point, viewer) {
         let aspectRatio = viewer.source.width/viewer.source.height;
         let rotation = viewer.viewport.getRotation();
         let imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         
         let point_fromImageCenter = imageTopLeft_fromImageCenter.plus(point);
         let point_fromImageCenter_rotated = _rotate(point_fromImageCenter, rotation, true);
         let point_fromImageTopLeft_rotated = point_fromImageCenter_rotated.minus(imageTopLeft_fromImageCenter);
         return point_fromImageTopLeft_rotated;
     }
     
     imageView.convertPointFromImageToCanvas = function(point, viewer) {
         return _convertPointFromImageToCanvas(point, viewer);
     }
     
     imageView.convertPointFromCanvasToImage = function(point, viewer) {
         return _convertPointFromCanvasToImage(point, viewer);
     }
     
     function _convertPointFromCanvasToImage(point, viewer) {
         
         let scale = viewer.canvas.clientWidth/viewer.viewport.getBoundsNoRotate(true).width;
         
//         point = point.times(1/viewer.viewport.getZoom());
         let aspectRatio = viewer.source.width/viewer.source.height;
         let rotation = viewer.viewport.getRotation();
         let imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         let canvasCenter_fromImageTopLeft = viewer.viewport.getCenter(true);
         let canvasCenter_fromCanvasTopLeft = new OpenSeadragon.Point(viewer.viewport.getBoundsNoRotate(true).width/2.0, viewer.viewport.getBoundsNoRotate(true).height/2.0);
         
         let canvasCenter_fromImageCenter = imageTopLeft_fromImageCenter.plus(canvasCenter_fromImageTopLeft);
         let canvasCenter_fromImageCenter_rotated = _rotate(canvasCenter_fromImageCenter, rotation, true);
         
         let imageCenter_fromCanvasTopLeft = canvasCenter_fromCanvasTopLeft.minus(canvasCenter_fromImageCenter_rotated);
         
         let point_fromCanvasTopLeft = point.times(1/scale);
         let point_fromImageCenter_rotated = point_fromCanvasTopLeft.minus(imageCenter_fromCanvasTopLeft);
         let point_fromImageTopLeft = _rotate(point_fromImageCenter_rotated, rotation, false).minus(imageTopLeft_fromImageCenter);

         return point_fromImageTopLeft;
     }
     
     function _convertPointFromImageToCanvas(point, viewer) {
         
         let scale = viewer.canvas.clientWidth/viewer.viewport.getBoundsNoRotate(true).width;
//         console.log("scale = " + scale);
//         point = point.times(1/viewer.viewport.getZoom());
         let aspectRatio = viewer.source.width/viewer.source.height;
         let rotation = viewer.viewport.getRotation();
         let imageTopLeft_fromImageCenter = new OpenSeadragon.Point(0.5, 0.5/aspectRatio).times(-1);
         let canvasCenter_fromImageTopLeft = viewer.viewport.getCenter(true);
         let canvasCenter_fromCanvasTopLeft = new OpenSeadragon.Point(viewer.viewport.getBoundsNoRotate(true).width/2.0, viewer.viewport.getBoundsNoRotate(true).height/2.0);
         
         let canvasCenter_fromImageCenter = imageTopLeft_fromImageCenter.plus(canvasCenter_fromImageTopLeft);
         let canvasCenter_fromImageCenter_rotated = _rotate(canvasCenter_fromImageCenter, rotation, true);
         
         let imageCenter_fromCanvasTopLeft = canvasCenter_fromCanvasTopLeft.minus(canvasCenter_fromImageCenter_rotated);
//         console.log("imageTopLeft_fromImageCenter " + imageTopLeft_fromImageCenter);
//         console.log("canvasCenter_fromImageTopLeft " + canvasCenter_fromImageTopLeft);
//         console.log("canvasCenter_fromCanvasTopLeft " + canvasCenter_fromCanvasTopLeft);
//         console.log("canvasCenter_fromImageCenter " + canvasCenter_fromImageCenter);
//         console.log("imageCenter_fromCanvasTopLeft " + imageCenter_fromCanvasTopLeft);
         
         
         let point_fromImageCenter = imageTopLeft_fromImageCenter.plus(point);
         let point_fromImageCenter_rotated = _rotate(point_fromImageCenter, rotation, true);
         
         let point_FromCanvasTopLeft = imageCenter_fromCanvasTopLeft.plus(point_fromImageCenter_rotated);

         let p = point_FromCanvasTopLeft.times(scale);
         
         return p;
     }

 /**
  * Rotates around the coordinate system origin
  */
 function _rotate(point, degrees, antiClockwise) {
     
     let rad = degrees*Math.PI/180.0;
     
     let x,y;
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
      * @returns     An OpenSeadragon.Rect containing the rotated rectangle in the original coordinate system
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


     function _normalizeAngle(degrees) {
         var norm = ((degrees%360)+360)%360;
         return norm;
     }

     function _getRotation(degrees) {
         degrees += _MAX_DESKEW_ANGLE;
         degrees /= 90;
         degrees = parseInt(degrees);
         degrees *= 90;
         return _normalizeAngle(degrees);
     }

     function _getDeskewAngle(degrees) {
         degrees += _MAX_DESKEW_ANGLE;
         degrees = parseInt(degrees%90);
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
     imageView.createPyramid = function( imageInfo ) {
             console.log("Creating legacy tilesource from imageInfo ", imageInfo);
         var fileExtension = "image/jpg";
         fileExtension = fileExtension.replace( "image/", "" );
         fileExtension = fileExtension.replace("jpeg", "jpg").replace("tiff", "tif");
         var imageLevels = [];
         var tileSource;
         if(Array.isArray(imageInfo)) {
             imageInfo.forEach(function(level) {
                 level.mimetype = "image/jpg";
             });
             tileSource = new OpenSeadragon.LegacyTileSource(imageInfo);
         } else if(imageInfo.sizes) {
             imageInfo.sizes.forEach(function(size) {
                     console.log("Image level width = ", size.width)
                     console.log("Image level height = ", size.height)
                 
                 var level = {
                     mimetype: "image/jpg",
                     url: imageInfo["@id"].replace( "/info.json", "" ) + "/full/" + size.width + ",/0/default." + fileExtension,
                     width: imageInfo.width,
                     height: imageInfo.height
                 };

                 
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
     return imageView;
})();

     
//     function test() {
//         var parent = $("#" + this.config.div);
//         draw(0, parent);
//         $("#" + this.config.rotationSlider + "").slider({
//             orientation: "vertical",
//             min: -360,
//             max: 360,
//             slide: function(event, ui) {
//                 var degrees = -ui.value;
//                 draw(degrees, parent);
//             }
//         });
//         
//
//         function draw(angle, parent) {
//             $(".test-rect").remove();
//             var rect = $('<div class="test-rect"/>').width(200).height(300).appendTo(parent);
//             var rotated = rect.clone().appendTo(parent);
//             rotated.addClass("rotated");
//             _rotate(rotated, angle);
//             console.log("width = ", rect.width());
//             console.log("height = ", rect.height());
//             var boundingRect = _getRotatedBounds({width: rect.width(), height: rect.height()}, angle);
//             console.log("bounding rect = ", boundingRect);
//             
//             var bounds = rect.clone().width(boundingRect.width).height(boundingRect.height).appendTo(parent);
//             var position = bounds.position();
//             position.top += boundingRect.y;
//             position.left += boundingRect.x;
//             bounds.css("top", position.top);
//             bounds.css("left", position.left);
//             bounds.addClass("result");
//         }
//     }




