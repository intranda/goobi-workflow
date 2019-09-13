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
                console.log( 'Initializing: goobiWorkflowJS.jumpToPage.init' );
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
            
            
            $( 'body' ).on( 'click', '#currentPage', function() {
                $( this ).hide();
                $( this ).next().css( "display", "inherit" )
                $('#jumpToImageAutocomplete input').focus();
            } );
            
            $( 'body' ).on( 'blur', '#jumpToImageAutocomplete input', function() {
                $( '#jumpToImageAutocomplete' ).hide();
                $(  '#currentPage'  ).show();
            } );          
        }
    };

    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );