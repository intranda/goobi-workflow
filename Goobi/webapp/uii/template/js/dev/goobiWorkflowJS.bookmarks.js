var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.bookmarks = {
        /**
         * @description Method to initialize the menu module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.bookmarks.init' );
            }

            // set dynamic to position of bookmarks
//            _setTopPosition();
            // set resize event
//            window.addEventListener( 'resize', _setTopPosition );
//            window.addEventListener( 'orientationchange', _setTopPosition );

            // load jquery plugin "colorbox"
            this.loadColorbox();
        },
        /**
         * @description Method to load a colorbox for bookmarks.
         * @method loadColorbox
         */
        loadColorbox: function() {
            if ( _debug ) {
                console.log( 'EXECUTE: goobiWorkflowJS.bookmarks.loadColorbox' );
            }

            // reset all colorboxes
            $.colorbox.remove();
            
            // set colorboxes
            $( '.colorbox-image' ).colorbox( {
                transition: 'fade',
                rel: 'colorbox-image',
                maxWidth: '90%',
                maxHeight: '90%',
            } );
        }
    };

    /**
     * @description Method to set the top position of the bookmarks.
     * @method _setTopPosition
     */
    function _setTopPosition() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setTopPosition' );
        }
        
        var controlWrapperHeight = $( '#objectControlWrapper' ).outerHeight();

        $( '#bookmarks' ).css( 'top', controlWrapperHeight + 90 );
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );