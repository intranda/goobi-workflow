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
 * Module which handles the zoomslider functionality.
 * 
 * @version 3.2.0
 * @module viewImage.zoomSlider
 * @requires jQuery
 */
var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    var _zoomSlider = {};
    var _defaults = {
        global: {
            /**
             * The position of the zoom-slider is "dilated" by a function d(zoom) =
             * 1/sliderDilation*tan[atan(sliderDilation)*zoom] This makes the slider
             * position change slower for small zoom and faster for larger zoom The
             * function is chosen so that d(0) = 0 and d(1) = 1
             */
            sliderDilation: 12
        }
    };
    
    osViewer.zoomSlider = {
        init: function( config ) {
            if ( _debug ) {
                console.log( '##############################' );
                console.log( 'osViewer.zoomSlider.init' );
                console.log( '##############################' );
            }
            
            $.extend( true, _defaults, config );
            
            if ( $( _defaults.global.zoomSlider ) ) {
                osViewer.zoomSlider.addZoomSlider( _defaults.global.zoomSlider );
                
                // handler for openSeadragon Object
                osViewer.viewer.addHandler( 'zoom', function( data ) {
                    osViewer.zoomSlider.buttonToZoom( data.zoom );
                } );
            }
        },
        buttonToMouse: function( mousePos ) {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.buttonToMouse: mousePos - ' + mousePos );
            }
            
            var offset = _zoomSlider.$button.width() / 2;
            var newPos = mousePos - offset;
            if ( newPos < 0 ) {
                newPos = 0;
            }
            if ( newPos + 2 * offset > _zoomSlider.absoluteWidth ) {
                newPos = _zoomSlider.absoluteWidth - 2 * offset;
            }
            _zoomSlider.$button.css( {
                left: newPos
            } );
            _zoomSlider.buttonPosition = newPos;
            var factor = ( newPos / ( _zoomSlider.absoluteWidth - offset * 2 ) );
            factor = 1 / _defaults.global.sliderDilation * Math.tan( Math.atan( _defaults.global.sliderDilation ) * factor );
            
            var newScale = osViewer.viewer.viewport.getMinZoom() + ( osViewer.viewer.viewport.getMaxZoom() - osViewer.viewer.viewport.getMinZoom() ) * factor;
            
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.buttonToMouse: newScale - ' + newScale );
            }
            
            osViewer.controls.zoomTo( newScale );
        },
        buttonToZoom: function( scale ) {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.buttonToZoom: scale - ' + scale );
            }
            
            if ( !_zoomSlider || !osViewer.viewer.viewport ) {
                return;
            }
            
            // console.log("Dilation = ", osViewer.viewer.viewport.getMinZoom())
            // console.log("minZoom = ", osViewer.viewer.viewport.getMinZoom());
            // console.log("maxZoom = ", osViewer.viewer.viewport.getMaxZoom())
            // console.log("scale = ", scale);
            
            var factor = ( scale - osViewer.viewer.viewport.getMinZoom() ) / ( osViewer.viewer.viewport.getMaxZoom() - osViewer.viewer.viewport.getMinZoom() );
            // console.log( "factor = ", factor );
            //            
            factor = 1 / Math.atan( _defaults.global.sliderDilation ) * Math.atan( _defaults.global.sliderDilation * factor );
            var newPos = factor * ( _zoomSlider.absoluteWidth - _zoomSlider.$button.width() );
            // var newPos = ( ( scale - osViewer.viewer.viewport.getMinZoom() ) / (
            // osViewer.viewer.viewport.getMaxZoom() -
            // osViewer.viewer.viewport.getMinZoom() ) )
            // * ( _zoomSlider.absoluteWidth - _zoomSlider.$button.width() );
            // console.log( "pos = ", newPos );
            
            if ( Math.abs( osViewer.viewer.viewport.getMaxZoom() - scale ) < 0.0000000001 ) {
                newPos = _zoomSlider.absoluteWidth - _zoomSlider.$button.width();
            }
            
            if ( newPos < 0 ) {
                newPos = 0;
            }
            
            _zoomSlider.$button.css( {
                left: newPos
            } );
            _zoomSlider.buttonPosition = newPos;
        },
        zoomSliderMouseUp: function() {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.zoomSliderMouseUp' );
            }
            
            _zoomSlider.mousedown = false;
        },
        zoomSliderMouseMove: function( evt ) {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.zoomSliderMouseMove: evt - ' + evt );
            }
            
            if ( !_zoomSlider.mousedown ) {
                return;
            }
            var offset = $( this ).offset();
            var hitX = evt.pageX - offset.left;
            osViewer.zoomSlider.buttonToMouse( hitX );
            
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.zoomSliderMouseMove: moving - ' + hitX );
            }
        },
        zoomSliderMouseDown: function( evt ) {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.zoomSliderMouseDown: evt - ' + evt );
            }
            
            _zoomSlider.mousedown = true;
            var offset = $( this ).offset();
            var hitX = evt.pageX - offset.left;
            osViewer.zoomSlider.buttonToMouse( hitX );
        },
        buttonMouseDown: function() {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.buttonMouseDown' );
            }
            
            _zoomSlider.mousedown = true;
            
            return false;
        },
        addZoomSlider: function( element ) {
            if ( _debug ) {
                console.log( 'osViewer.zoomSlider.addZoomSlider: element - ' + element );
            }
            
            _zoomSlider.$element = $( element );
            _zoomSlider.$button = _zoomSlider.$element.children( _defaults.global.zoomSliderHandle );
            _zoomSlider.buttonPosition = 0;
            _zoomSlider.absoluteWidth = _zoomSlider.$element.innerWidth();
            _zoomSlider.mousedown = false;
            _zoomSlider.$button.on( 'mousedown', osViewer.zoomSlider.buttonMouseDown );
            _zoomSlider.$element.click( osViewer.zoomSlider._zoomSliderClick );
            _zoomSlider.$element.mousedown( osViewer.zoomSlider.zoomSliderMouseDown );
            _zoomSlider.$element.mousemove( osViewer.zoomSlider.zoomSliderMouseMove );
            $( document ).on( 'mouseup', osViewer.zoomSlider.zoomSliderMouseUp );
        }
    };
    
    return osViewer;
    
} )( viewImage || {}, jQuery );
