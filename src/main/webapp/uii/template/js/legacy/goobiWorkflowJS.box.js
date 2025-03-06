var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    var _status = {};

    goobiWorkflow.box = {
        /**
         * @description Method to initialize the box module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.box.init' );
            }

            // write box status to session storage
            if ( sessionStorage.getItem( 'wf_boxStatus' ) == null ) {
                sessionStorage.setItem( 'wf_boxStatus', JSON.stringify( _status ) );
            }

            // execute box methods
            this.getBoxStatus();
            _setBoxStatus();
            _setToggleBoxBodyEvent();
        },
        /**
         * @description Method to get the status of the boxes.
         * @method getBoxStatus
         */
        getBoxStatus: function() {
            if ( _debug ) {
                console.log( 'EXECUTE: goobiWorkflowJS.box.getBoxStatus' );
            }
            
            var status = JSON.parse( sessionStorage.getItem( 'wf_boxStatus' ) );
            
            if ( status != null ) {
                $.each( status, function( element, status ) {
                    if ( !status ) {
                        $( '#' + element ).find( '[data-toggle="box-body"]' ).addClass( 'closed' );
                        $( '#' + element ).find( '.module__box-body' ).hide();
                    } else {
                        $( '#' + element ).find( '[data-toggle="box-body"]' ).removeClass( 'closed' );
                        $( '#' + element ).find( '.module__box-body' ).show();
                    }
                    
                } );
            }
        }
    };

    /**
     * @description Method to set the status of the boxes.
     * @method _setBoxStatus
     */
    function _setBoxStatus() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setBoxStatus' );
        }

        var status = JSON.parse( sessionStorage.getItem( 'wf_boxStatus' ) );

        $( '.module__box--collapsable' ).each( function() {
            var currId = $( this ).attr( 'id' );
            var isClosed = $( this ).find( '[data-toggle="box-body"]' ).hasClass( 'closed' );

            if ( isClosed ) {
                status[ currId ] = false;
            }
            else {
                status[ currId ] = true;
            }
        } );

        sessionStorage.setItem( 'wf_boxStatus', JSON.stringify( status ) );
    }

    /**
     * @description Method to set the event listener to toggle box body.
     * @method _setToggleBoxBodyEvent
     */
    function _setToggleBoxBodyEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setToggleBoxBodyEvent' );
        }

        $( 'body' ).on( 'click', '.module__box--collapsable .module__box-title h1', function () {
            $( this ).find( '[data-toggle="box-body"]' ).toggleClass( 'closed' );
            $( this ).parents( '.module__box-title' ).next().slideToggle( 200, function() {
                _setBoxStatus();
            } );
        });
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );