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
            
           
            
            
            $( 'body' ).on( 'click', '#currentPage', function() {
                $( this ).hide();
                $( this ).next().css( "display", "inherit" )
                $('#jumpToImageAutocomplete input').focus();
            } );
            
            $( 'body' ).on( 'blur', '#jumpToImageAutocomplete input', function() {
                $( '#jumpToImageAutocomplete' ).hide();
                $(  '#currentPage'  ).show();
            } );    
            
            $( 'body' ).on( 'click', '#jumpToPage', function() {
                $( this ).hide();
                $( this ).next().css( "display", "inherit" )
                $('#jumpToThumbAutocomplete input').focus();
            } );
            
            $( 'body' ).on( 'blur', '#jumpToThumbAutocomplete input', function() {
                $( '#jumpToThumbAutocomplete' ).hide();
                $(  '#jumpToPage'  ).show();
            } );    
            
            
        }
    };

    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );