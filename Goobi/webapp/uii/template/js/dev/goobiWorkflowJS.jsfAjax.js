var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.jsfAjax = {
        /**
         * @description Method to initialize the jsf ajax listener.
         * @method init
         */
    	init: function( config ) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.jsfAjax.init' );
                console.log( '--> config = ', config );
            }

            $.extend( true, _defaults, config );

            // listen to jsf ajax event
            if ( typeof jsf !== 'undefined' ) {
                jsf.ajax.addOnEvent( function ( data ) {
                    if ( _debug ) {
                        console.log( 'JSF AJAX - data: ', data );
                    }

                    switch ( data.status ) {
                        case 'begin':
                        case 'complete':
                        case 'success':
                            // init object view
                            goobiWorkflowJS.object.init( _defaults );
                            break;
                    }
                });
            }
        }
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );