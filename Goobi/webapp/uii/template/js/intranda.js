function toggle( id ) {
    var element = document.getElementById( id );
    if ( element.style.display == 'block' ) {
        element.style.display = 'none';
    }
    else {
        element.style.display = 'block';
    }
}

function clickOnEnter(e, btnId) {
    if(!btnId) {
        btnId = $(e.target).data('onenterbutton');
    }
    if(e.key === "Enter") {
        e.preventDefault();
        document.getElementById(btnId).click();
        return false;
    }
}

/**
 * Method to click the submit button on primefaces autocomplete.
 * 
 * @method submitEnter
 * @param {String} commandId The ID of the submit button.
 * @param {Event} e A JavaScript event which holds the key events.
 * */
function submitEnter( commandId, e ) {
    var keycode;
    if ( window.event )
        keycode = window.event.keyCode;
    else if ( e )
        keycode = e.which;
    else
        return true;
    if ( keycode == 13 ) {
        document.getElementById( commandId ).click();
        return false;
    }
    else
        return true;
}

/**
 * Method to set on click handler to primefaces autocomplete items.
 * 
 * @method setAutocompleteListHandler
 * */
function setAutocompleteListHandler() {
    setTimeout(function() {
        if ( $('.ui-autocomplete-panel li').length > 0 ) {
            $('.ui-autocomplete-panel li').on('click', function() {
                document.getElementById( 'goButton' ).click();                
            });
        }        
    }, 500);
}

// /**
// * Handler for onkeypress that clicks {@code targetElement} if the
// * enter key is pressed.
// */
// function ifEnterClick(event, targetElement) {
// event = event || window.event;
// if (event.keyCode == 13) {
// // normalize event target, so it looks the same for all browsers
// if (!event.target) {
// event.target = event.srcElement;
// }
//
// // don't do anything if the element handles the enter key on its own
// if (event.target.nodeName == 'A') {
// return;
// }
// if (event.target.nodeName == 'INPUT') {
// if (event.target.type == 'button'
// || event.target.type == 'submit') {
// if (strEndsWith(
// event.target.id,
// 'focusKeeper')) {
// // inside some Richfaces component such as rich:listShuttle
// } else {
// return;
// }
// }
// }
// if (event.target.nodeName == 'TEXTAREA') {
// return;
// }
//
// // swallow event
// if (event.preventDefault) {
// // Firefox
// event.stopPropagation();
// event.preventDefault();
// } else {
// // IE
// event.cancelBubble = true;
// event.returnValue = false;
// }
//
// document.getElementById(
// targetElement).click();
// }
// }

/**
 * Handler for onkeypress that clicks {@code targetElement} if the enter key is pressed.
 */
function submitOnEnter( event, classname ) {
    classname = name || 'submitOnEnter';
    event = event || window.event;
    if ( event.keyCode == 13 ) {
        // normalize event target, so it looks the same for all browsers
        if ( !event.target ) {
            event.target = event.srcElement;
        }
        
        // don't do anything if the element handles the enter key on its own
        if ( event.target.nodeName == 'A' ) {
            return;
        }
        if ( event.target.nodeName == 'INPUT' ) {
            if ( event.target.type == 'button' || event.target.type == 'submit' ) {
                if ( strEndsWith( event.target.id, 'focusKeeper' ) ) {
                    // inside some Richfaces component such as rich:listShuttle
                }
                else {
                    return;
                }
            }
        }
        if ( event.target.nodeName == 'TEXTAREA' ) {
            return;
        }
        
        // swallow event
        if ( event.preventDefault ) {
            // Firefox
            event.stopPropagation();
            event.preventDefault();
        }
        else {
            // IE
            event.cancelBubble = true;
            event.returnValue = false;
        }
        
        document.getElementsByClassName( classname )[ 0 ].click();
    }
    
}

// Thumbnails in METS Editor

function loadImages() {
    intrandaImages = {};
    $( '.thumb-canvas' ).each( function( index, el ) {
        drawOnCanvas( el );
    } );
}

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
            intrandaImages[ canvas.id ] = {
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
    img = new Image();
    img.onload = function() {
        if ( intrandaImages[ canvas.id ] == null ) {
            return;
        }
        intrandaImages[ canvas.id ].scaleX = ( img.width - intrandaImages[ canvas.id ].smallWidth ) / intrandaImages[ canvas.id ].smallWidth;
        intrandaImages[ canvas.id ].scaleY = ( img.height - intrandaImages[ canvas.id ].smallHeight ) / intrandaImages[ canvas.id ].smallHeight;
        var pos = getMousePos( canvas, event );
        // check if mouse is still hovering over canvas
        if ( $( '#' + canvas.id + ':hover' ).length == 0 ) {
            return;
        }
        var scaleX = intrandaImages[ canvas.id ].scaleX;
        var scaleY = intrandaImages[ canvas.id ].scaleY;
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
    if ( intrandaImages[ canvas.id ] ) {
        img.src = intrandaImages[ canvas.id ].largeUrl;
    }
}
