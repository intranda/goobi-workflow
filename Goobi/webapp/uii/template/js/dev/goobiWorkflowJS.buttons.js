var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.buttons = {
        /**
         * @description Method to initialize the buttons.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.buttons.init' );
            }

            if ( $( '.btn' ).hasClass( 'btn--toggle' ) ) {
                _setButtonToggleEvent();
            }

            // set button events on ajax success
            if ( typeof jsf !== 'undefined' ) {
	            jsf.ajax.addOnEvent( function( data ) {
	                var ajaxstatus = data.status;
	                
	                switch ( ajaxstatus ) {                        
		                case "success":
                            if ( $( '.btn' ).hasClass( 'btn--toggle' ) ) {
                                _setButtonToggleEvent();
                            }
		                	break;
	                }
	            } );
            }
        }
    };

    /**
     * @description Method to set the event listener to button toggle.
     * @method _setButtonToggleEvent
     */
    function _setButtonToggleEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setButtonToggleEvent' );
        }

        $( '.btn--toggle' ).off( 'click' ).on( 'click', function () {
            $( this ).next( 'div' ).slideToggle( 300 );
        });
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );