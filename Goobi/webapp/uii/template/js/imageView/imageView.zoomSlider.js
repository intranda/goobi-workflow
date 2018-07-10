/**
 * Module which handles the zoomslider functionality.
 * 
 * @requires jQuery
 */
var ImageView = ( function( imageView ) {
    'use strict';
    
    var _debug = false;
    var _zoomSlider = {};
    var _defaults = {
            /**
             * The position of the zoom-slider is "dilated" by a function d(zoom) =
             * 1/sliderDilation*tan[atan(sliderDilation)*zoom] This makes the slider
             * position change slower for small zoom and faster for larger zoom The
             * function is chosen so that d(0) = 0 and d(1) = 1
             */
            sliderDilation: 12
    };
    
    imageView.ZoomSlider = function(config, image)  {    
        this.config = $.extend( true, {}, _defaults );
        $.extend( true, this.config, config.global );
        this.image = image;
    }
    
    imageView.ZoomSlider.prototype.init = function() {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'imageView.zoomSlider.init' );
                console.log( '##############################' );
            }
                this.addZoomSlider(this.config.zoomSlider );
                this.buttonToZoom(this.image.viewer.viewport.getHomeZoom());
                var zoom = this;
                    this.image.observables.viewerZoom.subscribe( function( event ) {
                        var scale = zoom.image.viewer.viewport.getZoom();
                        zoom.buttonToZoom( scale );
                    });
        };
        imageView.ZoomSlider.prototype.exists = function() {
            return this.$element.length && this.$button.length;
        };
        imageView.ZoomSlider.prototype.buttonToMouse = function( mousePos ) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.buttonToMouse: mousePos - ' + mousePos );
            }
            
            var offset = this.$button.width() / 2;
            var newPos = mousePos - offset;
            if ( newPos < 0 ) {
                newPos = 0;
            }
            if ( newPos + 2 * offset > this.absoluteWidth ) {
                newPos = this.absoluteWidth - 2 * offset;
            }

            this.$button.css( {
                left: newPos
            } );
            this.buttonPosition = newPos;
            var factor = ( newPos / ( this.absoluteWidth - offset * 2 ) );
            factor = 1 / this.config.sliderDilation * Math.tan( Math.atan( this.config.sliderDilation ) * factor );
            var newScale = this.image.viewer.viewport.getMinZoom() + ( this.image.viewer.viewport.getMaxZoom() - this.image.viewer.viewport.getMinZoom() ) * factor;
            
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.buttonToMouse: newScale - ' + newScale );
            }
            
            this.zoomTo( newScale );
        };
        imageView.ZoomSlider.prototype.zoomTo = function( zoomTo ) {
            if ( _debug ) {
                console.log( 'imageView.controls.myZoomTo: zoomTo - ' + zoomTo );
            }
            
            var zoomBy = parseFloat( zoomTo ) / this.image.viewer.viewport.getZoom();
            
            if ( _debug ) {
                console.log( 'imageView.controls.myZoomTo: zoomBy - ' + zoomBy );
            }
            
            this.image.viewer.viewport.zoomBy( zoomBy, this.image.viewer.viewport.getCenter( false ), true );
            this.setLabel(zoomTo);
        };
        imageView.ZoomSlider.prototype.buttonToZoom = function( scale ) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.buttonToZoom: scale - ' + scale );
                console.log( 'imageView.zoomSlider.buttonToZoom: _zoomSlider - ', _zoomSlider );
            }
            
            if ( !this.image.viewer.viewport ) {
                return;
            }

            if(this.$button) { 
                var factor = ( scale - this.image.viewer.viewport.getMinZoom() ) / ( this.image.viewer.viewport.getMaxZoom() - this.image.viewer.viewport.getMinZoom() );
                
                factor = 1 / Math.atan( this.config.sliderDilation ) * Math.atan( this.config.sliderDilation * factor );
                var newPos = factor * ( this.absoluteWidth - this.$button.width() );
                
                
                if ( Math.abs( this.image.viewer.viewport.getMaxZoom() - scale ) < 0.0000000001 ) {
                    newPos = this.absoluteWidth - this.$button.width();
                }
                
                if ( newPos < 0 ) {
                    newPos = 0;
                }
                
                this.$button.css( {
                    left: newPos
                } );
                this.buttonPosition = newPos;
            }
            this.setLabel(scale);
        },
        imageView.ZoomSlider.prototype.setLabel = function(scale) {
            if(this.$label.length && this.image.sizes) {
                var imageWidth = this.image.config.image.originalImageWidth;
                var imageViewWidth = this.image.container.width();
                scale = parseFloat(scale)/imageWidth*imageViewWidth;
                this.$label.val((scale*100).toFixed(1));
            }
        };
        imageView.ZoomSlider.prototype.inputToZoom = function(input) {
            var imageScale = parseFloat(input);
            if(imageScale && this.image.sizes) {
                if(_debug) {
                    console.log("scale to ", input);
                }
                var imageWidth = this.image.config.image.originalImageWidth;
                var imageViewWidth = this.image.container.width();
                var scale = imageScale*imageWidth/imageViewWidth/100.0;
                if(scale < this.image.viewer.viewport.getMinZoom()) {
                    scale = this.image.viewer.viewport.getMinZoom();
                } else if(scale > this.image.viewer.viewport.getMaxZoom()) {
                    scale = this.image.viewer.viewport.getMaxZoom();
                }
                this.zoomTo( scale );
            }
        };
        imageView.ZoomSlider.prototype.addZoomSlider = function( element ) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.addZoomSlider: element - ' + element );
            }
            
            this.buttonPosition = 0;
            this.$element = $( element );
            this.absoluteWidth = this.$element.innerWidth();
            this.mousedown = false;
            var slider = this;
            if(this.$element.length) {
                this.$button = this.$element.children( this.config.zoomSliderHandle );
                this.$element.on('mousedown', function(event) {
                    _zoomSliderMouseDown(event, slider);
                });
                this.$element.on('mousemove', function(event) {
                    _zoomSliderMouseMove(event, slider);
                });
                if(this.$button.length) {
                    this.$button.on( 'mousedown', function(event) {
                        _buttonMouseDown(event, slider);                    
                    });
                }
            }
            this.$label = $(this.config.zoomSliderLabel);
            if(this.$label.length) {
                this.$label.on("change", function(event) {
                    slider.inputToZoom(event.target.value)
                    return false;
                });
                this.$label.on("keypress", function(e) {
                    if (e.which == 13) {
                        slider.inputToZoom(e.target.value)
                        return false;
                    }
                });
            }
            $( document ).on( 'mouseup', function(event) {
                _zoomSliderMouseUp(event, slider);
            });
        };
        
        function _zoomSliderMouseUp(event, slider) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.zoomSliderMouseUp' );
            }
            
            slider.mousedown = false;
        };
        function _zoomSliderMouseMove(event, slider) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.zoomSliderMouseMove: evt - ' + event );
            }
            
            if ( !slider.mousedown ) {
                return;
            }
            var offset = slider.$element.offset();
            var hitX = event.pageX - offset.left;
            slider.buttonToMouse( hitX );
            
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.zoomSliderMouseMove: moving - ' + hitX );
            }
        };
        function _zoomSliderMouseDown(event, slider) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.zoomSliderMouseDown: evt - ' + event );
            }
            
            slider.mousedown = true;
            var offset = slider.$element.offset();
            var hitX = event.pageX - offset.left;
            slider.buttonToMouse( hitX );
        };
        function _buttonMouseDown(event, slider) {
            if ( _debug ) {
                console.log( 'imageView.zoomSlider.buttonMouseDown' );
            }
            
            slider.mousedown = true;
            
            return false;
        };
    
    return imageView;
    
} )( ImageView );
