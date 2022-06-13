var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {
        navigationSelector: '#navigation',
        navigationActionSelector: '#navigationActions'
    };
    
    goobiWorkflow.jumpToPage = {
        /**
         * @description Toggle between current page info and an input field in the METS editor. The input field is activated on click.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.jumpToPage.init' );
            }
            
            
            $( 'body' ).on( 'click', '#currentPage, #currentPagePrefix', function() {
				var curPagePre = $('#currentPagePrefix');

				// Make input span at the same size as #curPagePrefix
				// this prevents layout shifts
				var currentPageWidth =  curPagePre[0].getBoundingClientRect().width;
				var currentPageHeight =  curPagePre[0].getBoundingClientRect().height;
                curPagePre.next().css('width', currentPageWidth);
                curPagePre.next().css('height', currentPageHeight);
                
                // Show span containing an input field
                curPagePre.hide();
                curPagePre.next().css( "display", "inline-block" )

                $('#jumpToImageAutocomplete input').focus();
            } );
            
            $( 'body' ).on( 'blur', '#jumpToImageAutocomplete input', function() {
                $( '#jumpToImageAutocomplete' ).hide();
                $(  '#currentPagePrefix'  ).show();
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