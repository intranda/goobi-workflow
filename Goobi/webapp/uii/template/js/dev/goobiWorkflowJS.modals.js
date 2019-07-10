var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = true;
    var _defaults = {};
    
    goobiWorkflow.modals = {
        /**
         * @description Method to initialize the modals module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.modals.init' );
            }
            
            $('button[id*="geonamesIndexTrigger-"]').on('click', function () {
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
            });

            $('button[id*="gndIndexTrigger-"]').on('click', function () {
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
            });

            $('button[id*="gndPersonIndexTrigger-"]').on('click', function () {
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
            });
            
            $( 'body' ).on( 'click', 'button[id*="danteIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType = $( this ).attr( 'data-datatype' );
                var currGroup = $( this ).attr( 'data-groupindex')
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                
                $('#resultList').empty();
                $('#danteInput').val('');
                $('#updatePluginButton').click();
            } );
            
            $( 'body' ).on( 'click', 'button[id*="processIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType = $( this ).attr( 'data-datatype' );
                var currGroup = $( this ).attr( 'data-groupindex')
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                $('#updatePluginButton').click();
            } );
            
            $( 'body' ).on( 'click', 'button[id*="viafIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType = $( this ).attr( 'data-datatype' );
                var currGroup = $( this ).attr( 'data-groupindex');
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                $('#updatePluginButton').click()
            } );
            $( 'body' ).on( 'click', 'button[id*="viafPersonIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType =  'viafperson';
                var currGroup = $( this ).attr( 'data-groupindex');
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                $('#updatePluginButton').click()
            } );
        },
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );