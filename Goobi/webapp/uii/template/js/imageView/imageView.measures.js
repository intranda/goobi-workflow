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
 * Module which contains all image informations like size, scale etc.
 * 
 * @version 3.2.0
 * @module viewImage.Measures
 * @requires jQuery
 */
ImageView = ( function( imageView ) {
    'use strict';
    
    var _debug = false;
    
    imageView.Measures = function( imageView ) {
        this.config = imageView.getConfig();
        this.$container = $( "#" + this.config.global.divId );
        
        this.outerCanvasSize = new OpenSeadragon.Point( this.$container.outerWidth(), this.$container.outerHeight() );
        this.innerCanvasSize = new OpenSeadragon.Point( this.$container.width(), this.$container.height() );
        this.originalImageSize = new OpenSeadragon.Point( this.getTotalImageWidth( imageView.getImageInfo() ), this.getMaxImageHeight( imageView.getImageInfo() ) );
        // console.log("Original image size = ", this.originalImageSize);
        this.footerHeight = this.config.global.footerHeight;
        this.rotation = imageView.viewer != null ? imageView.viewer.viewport.getRotation() : 0;
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
    imageView.Measures.prototype.getMaxImageWidth = function( imageInfo ) {
        var width = 0;
        if ( imageInfo && imageInfo.length > 0 ) {
            for ( var i = 0; i < imageInfo.length; i++ ) {
                var tileSource = imageInfo[ i ];
                if ( tileSource.tileSource ) {
                    correction = tileSource.width;
                    tileSource = tileSource.tileSource;
                }
                width = Math.max( width, tileSource.width * correction );
            }
        }
        return width;
    };
    imageView.Measures.prototype.getMaxImageHeight = function( imageInfo ) {
        var height = 0;
        if ( imageInfo && imageInfo.length > 0 ) {
            for ( var i = 0; i < imageInfo.length; i++ ) {
                var tileSource = imageInfo[ i ];
                var correction = 1;
                if ( tileSource.tileSource ) {
                    correction = tileSource.width;
                    tileSource = tileSource.tileSource;
                }
                height = Math.max( height, tileSource.height * correction );
            }
        }
        return height;
    };
    imageView.Measures.prototype.getTotalImageWidth = function( imageInfo ) {
        var width = 0;
        if ( imageInfo && imageInfo.length > 0 ) {
            for ( var i = 0; i < imageInfo.length; i++ ) {
                var tileSource = imageInfo[ i ];
                var correction = 1;
                if ( tileSource.tileSource ) {
                    correction = tileSource.width;
                    tileSource = tileSource.tileSource;
                }
                width += ( tileSource.width * correction );
            }
        }
        return width;
    };
    imageView.Measures.prototype.getTotalImageHeight = function( imageInfo ) {
        var height = 0;
        if ( imageInfo && imageInfo.length > 0 ) {
            for ( var i = 0; i < imageInfo.length; i++ ) {
                var tileSource = imageInfo[ i ];
                var aspectRatio
                if ( tileSource.tileSource ) {
                    correction = tileSource.width;
                    tileSource = tileSource.tileSource;
                }
                height += tileSource.height * correction;
            }
        }
        return height;
    };
    imageView.Measures.prototype.getImageHomeSize = function() {
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
    imageView.Measures.prototype.rotated = function() {
        return this.rotation % 180 !== 0;
    };
    imageView.Measures.prototype.landscape = function() {
        return this.ratio( this.originalImageSize ) < 1;
    };
    imageView.Measures.prototype.ratio = function( size ) {
        return size.y / size.x;
    };
    imageView.Measures.prototype.getRotatedSize = function( size ) {
        return new OpenSeadragon.Point( this.rotated() ? size.y : size.x, this.rotated() ? size.x : size.y );
    };
    imageView.Measures.prototype.fitToHeight = function() {
        return !this.config.global.adaptContainerHeight && this.ratio( this.getRotatedSize( this.originalImageSize ) ) > this.ratio( this.innerCanvasSize );
    };
    imageView.Measures.prototype.resizeCanvas = function() {
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
    imageView.Measures.prototype.calculateExcessHeight = function() {
        var imageSize = this.getRotatedSize( this.getImageHomeSize() );
        var excessHeight = this.config.global.adaptContainerHeight || this.fitToHeight() ? 0 : 0.5 * ( this.innerCanvasSize.y - imageSize.y );
        return excessHeight;
    };
    
    return imageView;
    
} )( ImageView );
