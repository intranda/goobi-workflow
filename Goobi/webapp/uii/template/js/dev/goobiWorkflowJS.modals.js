var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
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
            
            // geonames modal
            $('body').on('click', 'a[id*="geonamesIndexTrigger-"]', function () {
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
                $('#updatePluginButton').click();
            });

            // gnd modal
            $('body').on('click', 'a[id*="gndIndexTrigger-"]', function () {
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
                $('#updatePluginButton').click();
            });

            // person modal
            $('body').on('click', 'a[id*="gndPersonIndexTrigger-"]', function () {
                console.log('click');
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
                $('#updatePluginButton').click();
            });
            
            // dante modal
            $( 'body' ).on( 'click', 'a[id*="danteIndexTrigger-"]', function() {
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
            
            // processbar modal
            $( 'body' ).on( 'click', 'a[id*="processIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType = $( this ).attr( 'data-datatype' );
                var currGroup = $( this ).attr( 'data-groupindex')
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                $('#updatePluginButton').click();
            } );
            
            // viaf modal
            $( 'body' ).on( 'click', 'a[id*="viafIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType = $( this ).attr( 'data-datatype' );
                var currGroup = $( this ).attr( 'data-groupindex');
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                $('#updatePluginButton').click()
            } );

            $( 'body' ).on( 'click', 'a[id*="viafPersonIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType =  'viafperson';
                var currGroup = $( this ).attr( 'data-groupindex');
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );
                $('#updatePluginButton').click()
            } );
            
            // easydb modal
            $( 'body' ).on( 'click', 'a[id*="easydbIndexTrigger-"]', function() {
                var currIndex = $( this ).attr( 'data-row' );
                var currType = $( this ).attr( 'data-datatype' );
                var currGroup = $( this ).attr( 'data-groupindex')
                $( '#rowIndex' ).val( currIndex );
                $( '#groupIndex' ).val( currGroup );
                $( '#rowType' ).val( currType );                
                $('#updatePluginButton').click();
            } );
            
            // vocabularySearch modal
            $('body').on('click', 'a[id*="vocabularySearchIndexTrigger-"]', function () {
                var currIndex = $(this).attr('data-row');
                var currType = $(this).attr('data-datatype');
                $('#rowIndex').val(currIndex);
                $('#rowType').val(currType);
                $('#updatePluginButton').click();
            });
        },
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );