var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {
        navigationSelector: '#navigation',
        navigationActionSelector: '#navigationActions'
    };
    
    goobiWorkflow.jumpToPage = {
        /**
         * @description Method to initialize the menu module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.jumpToPage.init' );
            }
            
            $( '#jumpToPage span' ).off( 'click' ).on( 'click', function() {
                $( this ).hide();
                $( this ).next().show().focus();
            } );
            
            $( '#jumpToPage input[type="text"]' ).off( 'keypress blur' ).on( {
                keypress: function( event ) {
                    goobiWorkflowJS.submitEnter( 'jumpToPageAction', event );
                },
                blur: function() {
                    $( this ).hide();
                    $( this ).prev().show();
                },
            } );
        }
    };

    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );