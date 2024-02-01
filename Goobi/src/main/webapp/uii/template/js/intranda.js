// HELPER: check if an element exists
function isElement(element) {
  return typeof element != 'undefined' && element != null;
}

function toggle( id ) {
    var element = document.getElementById( id );
    if ( element.style.display == 'block' ) {
        element.style.display = 'none';
    }
    else {
        element.style.display = 'block';
    }
}

/**
 * method to click button when "Enter" is pressed in an input field
 * @param e the KeyPressEvent
 * @param btnId the buttonId. If this is not set or null, the method reads the data-onenterbutton attribute of the target element
 * @returns false, if "Enter" was pressed
 */
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


function renderModal(data) {
    if(data.status == "success") {
        $('#' + data.source.dataset.modalid).modal('show');
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
    classname = classname || 'submitOnEnter';
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
                } else {
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
        } else {
            // IE
            event.cancelBubble = true;
            event.returnValue = false;
        }
        // if classname starts with #, look for the element with the relevant id
        // that has the same JSF-prefix as the event source element id
        if (classname.substring(0,1) === `#`) {
            const id = event.target.id
            const idPrefix = (id) => {
                const idParts = id.split(`:`);
                return idParts.length === 1
                    ? id
                    : idParts.slice(0, -1).join(`:`)
            }
            const targetId = classname.substring(1);
            const fullTargetId = `${idPrefix(id)}:${targetId}`;

            console.log(fullTargetId)

            document.getElementById(fullTargetId).click();
        } else {
            document.getElementsByClassName( classname )[ 0 ].click();
        }
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
        intrandaImages[ canvas.id ].scaleX = ( img.width - $(canvas).outerWidth() ) / $(canvas).outerWidth();
        intrandaImages[ canvas.id ].scaleY = ( img.height - $(canvas).outerHeight() ) / $(canvas).outerHeight();
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

function loadMenu() {
	let maximumWidth = 840;
	let width = window.innerWidth;
	let menu = document.getElementById("main-menu-set-class");
	let wide = document.getElementsByClassName("rendered-in-wide-window");
	let small = document.getElementsByClassName("rendered-in-small-window");

  if(!isElement(menu)) return

	for (let index = 0; index < wide.length; index++) {
		wide[index].style.display = (width >= maximumWidth ? "block" : "none");
	}
	for (let index = 0; index < small.length; index++) {
		small[index].style.display = (width >= maximumWidth ? "none" : "block");
	}
	if (width >= maximumWidth) {
		// This is the main menu. It appears in the header area when the window is wide enough.
		menu.classList.add("main-nav");
		menu.classList.remove("mobile-nav");
		menu.classList.remove("open-nav");
		let hasDropdownMenu = document.getElementsByClassName("add-dropdown-menu-in-wide-window");
		for (let index = 0; index < hasDropdownMenu.length; index++) {
			hasDropdownMenu[index].classList.add("dropdown-menu");
		}
		let containerFluid = document.getElementsByClassName("add-container-fluid-in-wide-window");
		// console.log("fluid", containerFluid)
		for (let index = 0; index < containerFluid.length; index++) {
			containerFluid[index].classList.add("container-fluid");
		}
		let hasSubmenu = document.getElementsByClassName("add-has-submenu-in-small-window");
		// console.log(hasSubmenu)
		for (let index = 0; index < hasSubmenu.length; index++) {
			hasSubmenu[index].classList.remove("has-submenu");
		}
	} else {
		// This is the alternative menu. It appears as a menu-icon
		// (three bars) in the upper right corner and can be expanded.
		menu.classList.add("mobile-nav");
		menu.classList.add("open");
		menu.classList.remove("main-nav");
		let hasDropdownMenu = document.getElementsByClassName("add-dropdown-menu-in-wide-window");
		for (let index = 0; index < hasDropdownMenu.length; index++) {
			hasDropdownMenu[index].classList.remove("dropdown-menu");
		}
		let containerFluid = document.getElementsByClassName("add-container-fluid-in-wide-window");
		// console.log(containerFluid)
		for (let index = 0; index < containerFluid.length; index++) {
			containerFluid[index].classList.remove("container-fluid");
		}
		let hasSubmenu = document.getElementsByClassName("add-has-submenu-in-small-window");
		// console.log("hasSubmenu", hasSubmenu)
		for (let index = 0; index < hasSubmenu.length; index++) {
			hasSubmenu[index].classList.add("has-submenu");
		}
	}
}
window.addEventListener("resize", loadMenu);
