/**
 * @author Florian Alpers, intranda GmbH
 */

var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    
    osViewer.Measures = function(osViewer) {
        this.config = osViewer.getConfig();
        this.$container = $( "#" + this.config.global.divId );
        this.imageInfo = osViewer.getImageInfo();
        
        this.outerCanvasSize = new OpenSeadragon.Point( this.$container.outerWidth(), this.$container.outerHeight() );
        this.innerCanvasSize = new OpenSeadragon.Point( this.$container.width(), this.$container.height() );
        this.originalImageSize = new OpenSeadragon.Point( this.imageInfo.width, this.imageInfo.height );
        this.footerHeight = this.config.global.footerHeight;
        this.rotation = osViewer.viewer != null ? osViewer.viewer.viewport.getRotation() : 0;
        this.xPadding = this.outerCanvasSize.x - this.innerCanvasSize.x;
        this.yPadding = this.outerCanvasSize.y - this.innerCanvasSize.y;
        this.innerCanvasSize.y -= this.footerHeight;
        
        // calculate image size as it should be displayed in relation to canvas size
        if ( this.fitToHeight() ) {
            this.imageDisplaySize = new OpenSeadragon.Point( this.innerCanvasSize.y / this.ratio( this.getRotatedSize( this.originalImageSize ) ), this.innerCanvasSize.y )
        }
        else {
            this.imageDisplaySize = new OpenSeadragon.Point( this.innerCanvasSize.x, this.innerCanvasSize.x * this.ratio( this.getRotatedSize( this.originalImageSize ) ) )
        }
        if ( this.rotated() ) {
            this.imageDisplaySize = this.getRotatedSize( this.imageDisplaySize );
        }
    };
    
    osViewer.Measures.prototype.getImageHomeSize = function() {
        var ratio = this.rotated() ? 1 / this.ratio( this.originalImageSize ) : this.ratio( this.originalImageSize );
        if ( this.fitToHeight() ) {
            var height = this.innerCanvasSize.y;
            var width = height / ratio;
        }
        else {
            var width = this.innerCanvasSize.x;
            var height = width * ratio;
        }
        return this.getRotatedSize( new OpenSeadragon.Point( width, height ) );
    };
    osViewer.Measures.prototype.rotated = function() {
        return this.rotation % 180 !== 0;
    };
    osViewer.Measures.prototype.landscape = function() {
        return this.ratio( this.originalImageSize ) < 1;
    };
    osViewer.Measures.prototype.ratio = function( size ) {
        return size.y / size.x;
    };
    osViewer.Measures.prototype.getRotatedSize = function( size ) {
        return new OpenSeadragon.Point( this.rotated() ? size.y : size.x, this.rotated() ? size.x : size.y );
    };
    osViewer.Measures.prototype.fitToHeight = function() {
        return !this.config.global.adaptContainerHeight
        && this.ratio( this.getRotatedSize( this.originalImageSize ) ) > this.ratio( this.innerCanvasSize );
    };
    osViewer.Measures.prototype.resizeCanvas = function() {
     // Set height of container if required
        if ( this.config.global.adaptContainerHeight ) {
            if ( _debug ) {
                console.log( "adapt container height" );
            }
            this.$container.height( this.getRotatedSize( this.imageDisplaySize ).y + this.footerHeight );
        }
        this.outerCanvasSize = new OpenSeadragon.Point( this.$container.outerWidth(), this.$container.outerHeight() );
        this.innerCanvasSize = new OpenSeadragon.Point( this.$container.width(), this.$container.height() - this.footerHeight );
    };
    osViewer.Measures.prototype.calculateExcessHeight = function() {
        var imageSize = this.getRotatedSize( this.getImageHomeSize() );
        var excessHeight = this.config.global.adaptContainerHeight || this.fitToHeight() ? 0 : 0.5 * ( this.innerCanvasSize.y - imageSize.y );
        return excessHeight;
    };
    
    
    
    
    return osViewer;
    
} )( viewImage || {}, jQuery );