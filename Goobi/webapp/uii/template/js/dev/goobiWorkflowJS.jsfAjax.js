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
            if (typeof jsf !== 'undefined') {
                jsf.ajax.addOnEvent(function (data) {
                    if (_debug) {
                        console.log('JSF AJAX - data: ', data);
                    }
                    var ajaxloader = document.getElementById("ajaxloader");
                    
                    switch (data.status) {
                        case 'begin':
                            // show button ajax loader
                            if ($('.btn').hasClass('btn--loader')) {
                                $('.btn-ajax-loader').addClass('in');
                            }
                            ajaxloader.style.display = 'block';
                            // clean up object resources
                            goobiWorkflowJS.object.freeJSResources();
                            break;
                        case 'complete':
                            ajaxloader.style.display = 'none';
                            break;
                        case 'success':
                            // hide button ajax loader
                            if ($('.btn').hasClass('btn--loader')) {
                                $('.btn-ajax-loader').removeClass('in');
                            }
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