var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {
        navigationSelector: '#navigation',
        navigationActionSelector: '.navigationActions'
    };
    
    goobiWorkflow.menu = {
        /**
         * @description Method to initialize the menu module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.menu.init' );
            }

            _setSubmenuToggleEvent();

            // toggle mobile navigation
            $('body').on('click', '[data-toggle="mobile-menu"]', function () {
                $('#navigation').slideToggle(200);
            });
            // toggle mobile image
            $('body').on('click', '[data-toggle="mobile-image"]', function () {
                $(this).toggleClass('in');
                $('#pageContentRight').toggleClass('in');
            });
        }
    };
    
    /**
     * @description Method to set the event listener to toggle box body.
     * @method _setSubmenuToggleEvent
     */
    function _setSubmenuToggleEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setSubmenuToggleEvent' );
        }

        $( 'body' ).on( 'click', '[data-show="submenu"]', function ( event ) {
            if ( $( this ).next().is( ':visible' ) ) {
                $( '.submenu' ).hide();
                $( this ).next().hide();
            }
            else {
                $( '.submenu' ).hide();
                $( this ).next().show();
            }
        } );

        // reset all menus by clicking on body
        $( 'body' ).on( 'click', function( event ) {
            if ( 
                event.target.id == 'navigation' || 
                $( event.target ).closest( _defaults.navigationSelector ).length || 
                event.target.id == 'navigationActions' || 
                $( event.target ).closest( _defaults.navigationActionSelector ).length 
            ) {
                return;
            }
            else {
                $( '.submenu' ).hide();
            }
        } );
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );