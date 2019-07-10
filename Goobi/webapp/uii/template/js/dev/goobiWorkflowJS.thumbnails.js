var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    var _intrandaImages = {};
    
    goobiWorkflow.thumbnails = {
        /**
         * @description Method to initialize the thumbnail rendering.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.thumbnails.init' );
            }

            $( '.thumbnails__thumb-canvas' ).each( function( index, el ) {
                drawOnCanvas( el );
            } );
        },
    };
    
    function drawOnCanvas( canvas ) {
        setTimeout( function() {
            
            if ( canvas == null ) {
                return;
            }
            var ctx = canvas.getContext( '2d' );
            var d = canvas.dataset;
            
            if ( !d ) {
                // fix for ie not supporting element.dataset
                d = {};
                d.image_small = canvas.getAttribute( 'data-image_small' );
                d.image_large = canvas.getAttribute( 'data-image_large' );
            }
            
            var img = new Image();
            img.onload = function() {
                var scale = ( canvas.width * 2 ) / this.width;
                canvas.width = this.width;
                canvas.height = this.height;
                ctx.drawImage( img, 0, 0, this.width, this.height );
                _intrandaImages[ canvas.id ] = {
                    smallWidth: this.width - 10,
                    smallHeight: this.height - 10,
                    largeUrl: d.image_large
                };
            };
            var image = d.image_small;
            // console.log(image);
            img.src = image;
            canvas.addEventListener( 'mouseout', drawOnCanvasMouseOut, false );
            canvas.addEventListener( 'mousemove', onMouseMove, false );
        }, 100 );
    }

    function drawOnCanvasMouseOut( event ) {
        drawOnCanvas( event.currentTarget );
    }

    function getMousePos( canvas, event ) {
        var rect = canvas.getBoundingClientRect();
        return {
            x: event.clientX - rect.left - 5,
            y: event.clientY - rect.top - 5
        };
    }

    function preventDoubleSubmit(e) {
        var prevent = function(event) {
           event.preventDefault();
        }
        var oldOnclick = e.onclick;
        e.onclick = prevent;
        setTimeout(function() {
            e.onclick = oldOnclick;
        }, 700);
    }

    function onMouseMove( event ) {
        var canvas = event.currentTarget;
        var img = new Image();
        img.onload = function() {
            if ( _intrandaImages[ canvas.id ] == null ) {
                return;
            }
            _intrandaImages[ canvas.id ].scaleX = ( img.width - _intrandaImages[ canvas.id ].smallWidth ) / _intrandaImages[ canvas.id ].smallWidth;
            _intrandaImages[ canvas.id ].scaleY = ( img.height - _intrandaImages[ canvas.id ].smallHeight ) / _intrandaImages[ canvas.id ].smallHeight;
            var pos = getMousePos( canvas, event );
            // check if mouse is still hovering over canvas
            if ( $( '#' + canvas.id + ':hover' ).length == 0 ) {
                return;
            }
            var scaleX = _intrandaImages[ canvas.id ].scaleX;
            var scaleY = _intrandaImages[ canvas.id ].scaleY;
            var ctx = canvas.getContext( '2d' );
            ctx.fillStyle = 'white';
            ctx.fillRect( 0, 0, canvas.width, canvas.height );
            var posX = pos.x * scaleX;
            var posY = pos.y * scaleY;
            if ( posX < 0 ) {
                posX = 0;
            }
            if ( posY < 0 ) {
                posY = 0;
            }
            if ( img.width - posX < canvas.width ) {
                posX = img.width - canvas.width;
            }
            if ( img.height - posY < canvas.height ) {
                posY = img.height - canvas.height;
            }
            ctx.drawImage( img, -posX, -posY );
        }
        if ( _intrandaImages[ canvas.id ] ) {
            img.src = _intrandaImages[ canvas.id ].largeUrl;
        }
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );