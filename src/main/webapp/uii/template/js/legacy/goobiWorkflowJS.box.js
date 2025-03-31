var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';

    var _debug = false;
    var _defaults = {};
    var _status = {};

    const toggleBox = (id, isOpen) => {
        const element = document.querySelector( '#' + id );
        const toggle = document.querySelector('[aria-controls="' + id + '"]');
        if (isOpen) {
            element.classList.remove('show');
            toggle.setAttribute('aria-expanded', 'false');
        } else {
            element.classList.add('show');
            toggle.setAttribute('aria-expanded', 'true');
        }
    };

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

            const status = JSON.parse( sessionStorage.getItem( 'wf_boxStatus' ) );

            if ( status != null ) {
                for (const [id, open] of Object.entries(status)) {
                    const element = document.querySelector( '#' + id );
                    if (element) {
                        const currentToggleState = element?.classList.contains('show');
                        const targetToggleState = open;
                        if (currentToggleState !== targetToggleState) {
                            toggleBox(id, currentToggleState);
                        }
                    }
                }
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

        const status = JSON.parse( sessionStorage.getItem( 'wf_boxStatus' ) );

        $( '.box .collapse' ).each( function() {
            const currId = $( this ).attr( 'id' );
            const isOpen = $( this ).hasClass( 'show' );

            if ( !isOpen ) {
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

        const toggleBoxes = document.querySelectorAll('.box .collapse');
        toggleBoxes.forEach( box => {
            box.addEventListener('hidden.bs.collapse', function () {
                _setBoxStatus();
            });
            box.addEventListener('shown.bs.collapse', function () {
                _setBoxStatus();
            });
        });
    }

    return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );