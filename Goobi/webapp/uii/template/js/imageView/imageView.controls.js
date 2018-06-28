/**
 * This file is part of the Goobi viewer - a content presentation and management
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
 * Module which handles the image controls of the image view.
 * 
 * @version 3.2.0
 * @module viewImage.controls
 * @requires jQuery
 */
var ImageView = ( function( imageView ) {
    'use strict';
    
    var _debug = false;
//    var _currentZoom;
//    var _zoomedOut = true;
//    var _panning = false;
//    var _fadeout = null;
      
    imageView.Controls = function(config, image) {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'imageView.controls.init' );
                console.log( '##############################' );
            }
            this.config = config;
            this.image = image;
            var controls = this;
            
            if(imageView.Controls.Persistence) {
                this.persistence = new imageView.Controls.Persistence(config, image)
//                imageView.controls.persistence.init(config);
            }
            if(_debug) {                
                console.log("Setting viewer location to", config.image.location);
            }
            if( image.observables ) {
                // set location after viewport update
                image.observables.redrawRequired
                .sample(image.observables.viewportUpdate)
                .subscribe(function(event) {
                    controls.setLocation(event)
                    controls.setPanning( false );
                });
                
                // zoom home if min zoom reached
                image.observables.viewerZoom.subscribe( function( event ) {
                    if ( _debug ) {
                        console.log( "zoom to " + image.viewer.viewport.getZoom( true ) );
                    }
                    if ( !controls.isPanning() ) {
                        var currentZoom = image.viewer.viewport.getZoom();                   
                        if ( currentZoom <= image.viewer.viewport.minZoomLevel ) {
                            if ( _debug ) {
                                console.log( "Zoomed out: Panning home" );
                            }
                            
                            controls.setPanning(true);
                            controls.goHome( true );
                            controls.setPanning(false);
                        }
                    }
                } );
            }
            
            // fade out fullscreen controls
            if ( $( '#fullscreenTemplate' ).length > 0 ) {
                $( '#fullscreenTemplate' ).on( 'mousemove', function() {  
                    controls.fullscreenControlsFadeout();
                } )
                
                $('#fullscreenMap').on('touchmove', function() {
                	controls.fullscreenControlsFadeout();
                }).on('touchend', function() {
                	controls.fullscreenControlsFadeout();
                });
            }
        }
        imageView.Controls.prototype.getLocation = function() {
            return {
                x: this.getCenter().x,
                y: this.getCenter().y,
                zoom: this.getZoom()/this.getCurrentRotationZooming(),
                rotation: this.getRotation(),
            }
        },
        imageView.Controls.prototype.getCenter = function() {
            if ( _debug ) {
                console.log( "image center is " + this.image.viewer.viewport.getCenter( true ) );
            }
            return this.image.viewer.viewport.getCenter( true );
        }
        imageView.Controls.prototype.setCenter = function( center ) {
            
            if ( _debug ) {
                console.log( "Setting image center to " );
                console.log( center );
            }
            
            this.image.viewer.viewport.panTo( center, true );
            
        },
        imageView.Controls.prototype.getZoom = function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.getZoom' );
            }
            return this.image.viewer.viewport.getZoom( true );
        }
        imageView.Controls.prototype.zoomTo = function( zoomTo ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.myZoomTo: zoomTo - ' + zoomTo );
            }
            
            var zoomBy = parseFloat( zoomTo ) / this.image.viewer.viewport.getZoom();
            
            if ( _debug ) {
                console.log( 'osViewer.controls.myZoomTo: zoomBy - ' + zoomBy );
            }
            
            this.image.viewer.viewport.zoomBy( zoomBy, this.image.viewer.viewport.getCenter( false ), true );
        }
        imageView.Controls.prototypesetFullScreen = function( enable ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.setFullScreen: enable - ' + enable );
            }
            
            this.image.viewer.setFullScreen( enable );
        }
        imageView.Controls.prototype.goHome = function( immediate ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.panHome - zoom : ' + this.image.viewer.viewport.getHomeZoom() );
            }
            this.image.viewer.viewport.goHome( immediate );
            this.zoomedOut = true;
        }
        imageView.Controls.prototype.reset = function( resetRotation ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.goHome: bool - ' + resetRotation );
            }
            
            // osViewer.viewer.viewport.goHome( true );
            this.goHome( true );
            this.image.viewer.viewport.zoomTo( this.image.viewer.viewport.getHomeZoom(), null, true );
            if ( resetRotation ) {
                this.rotateTo( 0 );
            }
        }
        imageView.Controls.prototype.zoomIn = function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.zoomIn: zoomSpeed - ' + this.config.global.zoomSpeed );
            }
            
            this.image.viewer.viewport.zoomBy( this.config.global.zoomSpeed, this.image.viewer.viewport.getCenter( false ), false );
        }
        imageView.Controls.prototype.zoomOut = function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.zoomOut: zoomSpeed - ' + this.config.global.zoomSpeed );
            }
            
            this.image.viewer.viewport.zoomBy( 1 / this.config.global.zoomSpeed, this.image.viewer.viewport.getCenter( false ), false );
        }
//        imageView.Controls.prototype.getHomeZoom: function( rotated ) {
//            if ( rotated && this.image.getCanvasSize().x / osViewer.getCanvasSize().y <= osViewer.getImageSize().x / osViewer.getImageSize().y ) {
//                osViewer.viewer.viewport.homeFillsViewer = true;
//            }
//            var zoom = osViewer.viewer.viewport.getHomeZoom();
//            osViewer.viewer.viewport.homeFillsViewer = false;
//            return zoom;
//        },
        imageView.Controls.prototype.rotateRight = function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateRight' );
            }
            
            var newRotation = this.image.viewer.viewport.getRotation() + 90;
            this.rotateTo( newRotation );
        }
        imageView.Controls.prototype.rotateLeft = function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateLeft' );
            }
            
            var newRotation = this.image.viewer.viewport.getRotation() - 90;
            this.rotateTo( newRotation );
        },
        imageView.Controls.prototype.getRotation = function() {
            if ( _debug ) {
                console.log( 'osViewer.controls.getRotation' );
            }
            
            return this.image.viewer.viewport.getRotation();
        }
        imageView.Controls.prototype.setRotation = function( rotation ) {
            if ( _debug ) {
                console.log( 'osViewer.controls.setRotation: rotation - ' + rotation );
            }
            
            return this.rotateTo( rotation );
        }
        imageView.Controls.prototype.rotateTo = function( newRotation ) {
            if ( newRotation < 0 ) {
                newRotation = newRotation + 360;
            }
            newRotation = newRotation % 360;
            if ( _debug ) {
                console.log( 'osViewer.controls.rotateTo: newRotation - ' + newRotation );
            }
                        
            this.panning = true;        
            this.currentZoom = null;
            this.image.viewer.viewport.setRotation( newRotation );
            this.panning = false;

        }
        imageView.Controls.prototype.getCurrentRotationZooming = function() {
            var sizes = this.image.getSizes();
            if(sizes && sizes.rotated()) {
                return 1/sizes.ratio(sizes.originalImageSize);
            } else {
                return 1;
            }
        }
        imageView.Controls.prototype.setPanning = function(panning) {
            this.panning = panning;
        }
        imageView.Controls.prototype.isPanning = function() {
            return this.panning;
        }
        imageView.Controls.prototype.fullscreenControlsFadeout = function() {
            if ( _debug ) {
                console.log( '---------- osViewer.controls.fullscreenControlsFadeout() ----------' );
            }
            
            if ( this.fadeout ) {
                clearTimeout( this.fadeout );
                this.showFullscreenControls();
            }
            
            this.fadeout = setTimeout( this.hideFullscreenControls, 3000 );
        },
        imageView.Controls.prototype.hideFullscreenControls = function() {
            if ( _debug ) {
                console.log( '---------- osViewer.controls.hideFullscreenControls() ----------' );
            }
            
            $( '#fullscreenRotateControlsWrapper, #fullscreenZoomSliderWrapper, #fullscreenExitWrapper, #fullscreenPrevWrapper, #fullscreenNextWrapper' ).stop().fadeOut( 'slow' );
        }
        imageView.Controls.prototype.showFullscreenControls = function() {
            if ( _debug ) {
                console.log( '---------- osViewer.controls.showFullscreenControls() ----------' );
            }
            
            $( '#fullscreenRotateControlsWrapper, #fullscreenZoomSliderWrapper, #fullscreenExitWrapper, #fullscreenPrevWrapper, #fullscreenNextWrapper' ).show();
        }
        // set correct location, zooming and rotation once viewport has been updated after
        // redraw
        imageView.Controls.prototype.setLocation = function(event) {
            if(_debug) {                    
                console.log("Viewer changed from " + event.osState + " event");
                console.log("target location: ", event.targetLocation);
                console.log("Home zoom = ", this.image.viewer.viewport.getHomeZoom());
            }
             this.image.viewer.viewport.minZoomLevel = this.image.viewer.viewport.getHomeZoom() * this.config.global.minZoomLevel;
             var targetZoom = event.targetLocation.zoom;
             var targetLocation = new OpenSeadragon.Point(event.targetLocation.x, event.targetLocation.y);
             var zoomDiff = targetZoom * this.image.viewer.viewport.getHomeZoom() - (this.image.viewer.viewport.minZoomLevel);
    // console.log("zoomDiff: " + targetZoom + " * " + osViewer.viewer.viewport.getHomeZoom()
    // + " - " + osViewer.viewer.viewport.minZoomLevel + " = ", zoomDiff);
    // console.log("zoomDiff: " + targetZoom + " - " + osViewer.viewer.viewport.minZoomLevel +
    // "/" + osViewer.controls.getCurrentRotationZooming() + " = ", zoomDiff);
             var zoomedOut = zoomDiff < 0.001 || !targetZoom;
             if(zoomedOut) {
                 if(_debug) {                         
                     console.log("Zooming home")
                 }
                 this.goHome( true );
             } else {
                 if(_debug) {                         
                     console.log( "Zooming to " + targetZoom + " * " + this.getCurrentRotationZooming() );
                     console.log("panning to ", targetLocation);
                 }
                 this.image.viewer.viewport.zoomTo( targetZoom * this.getCurrentRotationZooming(), null, true);
                 this.setCenter( targetLocation);
             }
             if(event.osState === "open" && event.targetLocation.rotation !== 0) {
                this.rotateTo(event.targetLocation.rotation);
             }
        }

    return imageView;
    
} )( ImageView );
