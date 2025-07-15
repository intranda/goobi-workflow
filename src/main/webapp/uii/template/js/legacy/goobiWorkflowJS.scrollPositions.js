var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';

    var _debug = false;
    var _positions = {
        selectPage: {
            label: 0
        },
        structure: {
            link: 0
        }
    };

    goobiWorkflow.scrollPositions = {
        /**
         * @description Method to initialize the box module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.scrollPositions.init' );
            }

            // get scroll status
            _getScrollStatus();

            // set scroll status
            _setScrollStatus();
        }
    };

    /**
     * @description Method to set the scroll status of the boxes.
     * @method _setScrollStatus
     */
    function _setScrollStatus() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setScrollStatus' );
        }

        // set scroll status of structure link
        $( 'body' ).on( 'click', '#meStructure button', function() {
            _positions.structure.link = $( this ).parents('li').position().top;

            sessionStorage.setItem( 'wf_scrollPositions', JSON.stringify( _positions ) );
        } );
    }

    /**
     * @description Method to get the scroll status of the boxes.
     * @method _getScrollStatus
     */
    function _getScrollStatus() {
        if ( _debug ) {
            console.log( 'EXECUTE: _getScrollStatus' );
        }

        var structLinkPosition;

        if ( sessionStorage.getItem( 'wf_scrollPositions' ) == null ) {
            sessionStorage.setItem( 'wf_scrollPositions', JSON.stringify( _positions ) );
            structLinkPosition = JSON.parse( sessionStorage.getItem( 'wf_scrollPositions' ) );
            $( '#pageContentLeft' ).scrollTop( structLinkPosition.structure.link );
        }
        else {
            structLinkPosition = JSON.parse( sessionStorage.getItem( 'wf_scrollPositions' ) );
            $( '#pageContentLeft' ).scrollTop( structLinkPosition.structure.link );
        }
    }

    return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );