var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.box = {
        /**
         * @description Method to initialize the box module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.box.init' );
            }

            _setToggleBoxBodyEvent();

            // set box event on ajax success
            if ( typeof jsf !== 'undefined' ) {
	            jsf.ajax.addOnEvent( function( data ) {
	                var ajaxstatus = data.status;
	                
	                switch ( ajaxstatus ) {                        
		                case "success":
                            _setToggleBoxBodyEvent();
		                	break;
	                }
	            } );
            }
        }
    };

    /**
     * @description Method to set the event listener to toggle box body.
     * @method _setToggleBoxBodyEvent
     */
    function _setToggleBoxBodyEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setColumnWidth' );
        }

        $( '.module__box-title h3' ).off( 'click' ).on( 'click', function () {
            $( this ).find( '[data-toggle="box-body"]' ).toggleClass( 'in' );
            $( this ).parents( '.module__box-title' ).next().slideToggle( 200 );
        } );
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );