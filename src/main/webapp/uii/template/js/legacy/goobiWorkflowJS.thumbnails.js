var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';

    var _debug = false;
    var _defaults = {
		maxParallelRequests: 100
	};
    var _intrandaImages = {};

    goobiWorkflow.thumbnails = {
        /**
         * @description Method to initialize the thumbnail rendering.
         * @method init
         */
    	init: function(config) {
            const configElement = document.getElementById('gwConfig');
            let goobiWorkflowConfig = configElement ? JSON.parse(configElement.textContent) : {};
			if(goobiWorkflowConfig) {
				this.config = $.extend( true, {}, _defaults, goobiWorkflowConfig );
			}
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.thumbnails.init', this.config);
            }
            // Only relevant in metseditor
            const isMetseditor = document.querySelector('#metseditorMenuForm') !== null;
            if (!isMetseditor) return;

			var promises = [];
			var elements = [];
			var activeAdded = false;
            $( '.thumbnails__thumb-canvas' ).each( function( index, el ) {
				elements.push(el);
			});

			let elementGroups = groupArray(elements, this.config.maxParallelRequests);
			let activeObservable = new rxjs.Subject();
			let thumbnailLoadObservables = rxjs.from(elementGroups)
			.pipe(
                rxjs.operators.concatMap(thumbGroup => {
                    let groupObservables = rxjs.from(thumbGroup)
                    .pipe(
	                	rxjs.operators.mergeMap(ele => {
							let promise = drawOnCanvas( ele );
							activeObservable.next(promise);
							if(this.isActiveElement(ele)) {
								activeObservable.complete();
							}
							return promise;
						})
	                );
                    return rxjs.forkJoin(groupObservables);
                })
        	)
         	rxjs.forkJoin(thumbnailLoadObservables).subscribe();

            var rightContent = document.querySelector( '#pageContentRight' );
            if(rightContent.querySelector( '#thumbnails' )) {
            	rightContent.scrollTo(0,0);
            }
        	var activeThumbnail = document.querySelector('.thumbnails__thumb.active');
        	if(activeThumbnail) {
				let promises = [];
				activeObservable.subscribe({
					next: p => promises.push(p),
					error: p => promises.push(p),
					complete: () => {
			            Promise.all(promises).then( () => {
		            		activeThumbnail.scrollIntoView({block: "center"});
	           			});
					}
				});
        	}
        },
        isActiveElement: function(el) {
			return el.parentElement.parentElement.parentElement.classList.contains("active");
		},
    };

    /**
     * @description Method to set the active thumbnail when it gets selected
     */
    goobiWorkflow.updateSelectedThumbnail = function( element ) {
        var galleryLinks;
        galleryLinks = document.getElementsByClassName('thumbnails__thumb');
        for (var i = 0; i < galleryLinks.length; i++) {
            galleryLinks[i].className = "thumbnails__thumb";
        }
        element.parentElement.parentElement.className = "thumbnails__thumb active";
        return true;
    }

    /**
     * @description Method to draw the thumbnail images on a canvas.
     * @method drawOnCanvas
     * @param {Object} canvas The canvas object to draw on.
     */
    function drawOnCanvas( canvas ) {
    	return new Promise( function(resolve, reject) {
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
	                resolve();
	            };
	            var image = d.image_small;
	            // console.log(image);
	            img.src = image;
	            canvas.addEventListener( 'mouseout', drawOnCanvasMouseOut, false );
	            canvas.addEventListener( 'mousemove', onMouseMove, false );
	        }, 100 );
	    });
    }

    /**
     * @description Method to trigger mouseout event.
     * @method drawOnCanvasMouseOut
     */
    function drawOnCanvasMouseOut( event ) {
        drawOnCanvas( event.currentTarget );
    }

    /**
     * @description Method to get the position of the mouse cursor.
     * @method getMousePos
     * @param {Object} canvas The canvas object to draw on.
     * @param {Object} event The ebvent object which contains the mouse positions.
     */
    function getMousePos( canvas, event ) {
        var rect = canvas.getBoundingClientRect();
        return {
            x: event.clientX - rect.left - 5,
            y: event.clientY - rect.top - 5
        };
    }
    /**
     * @description Method to prevent to submit a form twice.
     * @method preventDoubleSubmit
     * @param {Object} event The click event which triggers submit.
     */
    function preventDoubleSubmit(event) {
        var prevent = function(event) {
           event.preventDefault();
        }
        var oldOnclick = event.onclick;
        event.onclick = prevent;
        setTimeout(function() {
            event.onclick = oldOnclick;
        }, 700);
    }

    /**
     * @description Method to watch the mouse move event on canvas.
     * @method onMouseMove
     * @param {Object} event The mouse move event to watch.
     */
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

    function groupArray(array, groupSize) {
        let allGroups = array.reduce((groups, item) => {
            if(groups[groups.length - 1].length < groupSize) {
                groups[groups.length -1].push(item);
            } else {
                groups.push([item]);
            }
            return groups;
        }, [[]]);
        return allGroups;
    }

    return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );