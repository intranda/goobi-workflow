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
                console.log( 'Initializing: goobiWorkflow.box.init' );
            }

            // write box status to session storage
            if ( sessionStorage.getItem( 'boxStatus' ) == null ) {
                sessionStorage.setItem( 'boxStatus', JSON.stringify( _status ) );
            }

            // execute box methods
            _getBoxStatus();
            _setBoxStatus();
            _setToggleBoxBodyEvent();
            
            // execute box methods on ajax success
            if ( typeof jsf !== 'undefined' ) {
                jsf.ajax.addOnEvent( function( data ) {
                    var ajaxstatus = data.status;
	                
	                switch ( ajaxstatus ) {                        
                        case "success":
                            _getBoxStatus();
                            _setBoxStatus();
                            _setToggleBoxBodyEvent();
		                	break;
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

        var status = JSON.parse( sessionStorage.getItem( 'boxStatus' ) );

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

        sessionStorage.setItem( 'boxStatus', JSON.stringify( status ) );
    }
    
    /**
     * @description Method to get the status of the boxes.
     * @method _getBoxStatus
     */
    function _getBoxStatus() {
        if ( _debug ) {
            console.log( 'EXECUTE: _getBoxStatus' );
        }
        
        var status = JSON.parse( sessionStorage.getItem( 'boxStatus' ) );
        
        if ( status != null ) {
            $.each( status, function( element, status ) {
                if ( !status ) {
                    $( '#' + element ).find( '[data-toggle="box-body"]' ).addClass( 'closed' );
                    $( '#' + element ).find( '.module__box-body' ).hide();
                }
            } );
        }
    }

    /**
     * @description Method to set the event listener to toggle box body.
     * @method _setToggleBoxBodyEvent
     */
    function _setToggleBoxBodyEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setToggleBoxBodyEvent' );
        }

        $( '.module__box--collapsable .module__box-title h3' ).off( 'click' ).on( 'click', function () {
            $( this ).find( '[data-toggle="box-body"]' ).toggleClass( 'closed' );
            $( this ).parents( '.module__box-title' ).next().slideToggle( 200, function() {
                _setBoxStatus();
            } );
        });
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );