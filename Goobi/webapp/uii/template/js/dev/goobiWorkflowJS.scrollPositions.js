var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = true;
    var _defaults = {};
    var _positions = {
        selectPage: {
            label: 0
        }
    };
    
    goobiWorkflow.scrollPositions = {
        /**
         * @description Method to initialize the box module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.scrollPositions.init' );
            }

            var _labelPosition = JSON.parse( sessionStorage.getItem( 'scrollPositions' ) );
            $( '#meSelectPagesBox .module__box-body' ).scrollTop( _labelPosition.selectPage.label );

            $( '#myCheckboxes label' ).off( 'click' ).on( 'click', function() {
                console.log($( this ).parents('tr').position().top);

                _positions.selectPage.label = $( this ).parents('tr').position().top;

                sessionStorage.setItem( 'scrollPositions', JSON.stringify( _positions ) );
            } );
        }
    };

    /**
     * @description Method to set the status of the boxes.
     * @method _setScrollStatus
     */
    function _setScrollStatus() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setScrollStatus' );
        }
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );