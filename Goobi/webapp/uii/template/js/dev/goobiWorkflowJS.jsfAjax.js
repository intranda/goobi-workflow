var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.jsfAjax = {
        /**
         * @description Method to initialize the jsf ajax listener.
         * @method init
         */
    	init: function( config ) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.jsfAjax.init' );
                console.log( '--> config = ', config );
            }

            $.extend( true, _defaults, config );

            // listen to jsf ajax event
            if ( typeof jsf !== 'undefined' ) {
                jsf.ajax.addOnEvent( function ( data ) {
                    if ( _debug ) {
                        console.log( 'JSF AJAX - data: ', data );
                    }

                    switch ( data.status ) {
                        case 'begin':
                            // clean up object resources
                            goobiWorkflowJS.object.freeJSResources();
                        case 'complete':
                        case 'success':
                            // init BS features
                            goobiWorkflowJS.cleanUpBootstrapFeatures();
                            goobiWorkflowJS.initBootstrapFeatures();
                            // init layout
                            goobiWorkflowJS.layout.init();
                            // init object view
                            goobiWorkflowJS.object.imageLoadHandler();
                            // get box status
                            goobiWorkflowJS.box.getBoxStatus();
                            // init buttons
                            goobiWorkflowJS.buttons.init();
                            // init bookmarks
                            goobiWorkflowJS.bookmarks.init();
                            // init thumbnails
                            goobiWorkflowJS.thumbnails.init();
                            break;
                    }
                });
            }
        }
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );