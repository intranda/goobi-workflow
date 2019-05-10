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
            
            $( 'body' ).on( 'click', '#jumpToPage span', function() {
                $( this ).hide();
                $( this ).next().show().focus();
            } );
            $( 'body' ).on( 'keypress', '#jumpToPage input[type="text"]', function( event ) {
                goobiWorkflowJS.submitEnter( 'jumpToPageAction', event );
            } );
            $( 'body' ).on( 'blur', '#jumpToPage input[type="text"]', function() {
                $( this ).hide();
                $( this ).prev().show();
            } );            
        }
    };

    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );