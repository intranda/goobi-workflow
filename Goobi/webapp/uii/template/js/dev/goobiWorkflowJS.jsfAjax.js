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
                            if (data.source.dataset.renderimage=='true'){
                                // clean up object resources
                                goobiWorkflowJS.object.freeJSResources();
                            }
                            goobiWorkflow.tinymce.renderInputFields(data);
                            break;
                        case 'complete':
                            ajaxloader.style.display = 'none';
                            break;
                        case 'success':
                            if ( _debug ){
                                console.log("handling jsf ajax success");
                            }
                            // hide button ajax loader
                            if ($('.btn').hasClass('btn--loader')) {
                                $('.btn-ajax-loader').removeClass('in');
                            }
                            // init BS features
                            goobiWorkflowJS.cleanUpBootstrapFeatures();
                            goobiWorkflowJS.initBootstrapFeatures();
                            // init layout
                            goobiWorkflowJS.layout.init();
                            if (data.source.dataset.renderimage=='true'){
                                // init object view
                                goobiWorkflowJS.object.imageLoadHandler();
                                // init thumbnails
                                goobiWorkflowJS.thumbnails.init();
                            }
                            // reload alto-editor
                            if(data.source.id != "saveAltoChanges") {
	                            var altoEditorElement = document.querySelector('alto-editor')
	                            if(altoEditorElement && altoEditorElement._tag) {
	                            	altoEditorElement._tag.unmount(true)
	                            	if(typeof riot !== "undefined") {
	                            		openAltoEditor();
	                            	}
	                            }
                            } 
                            // get box status
                            goobiWorkflowJS.box.getBoxStatus();
                            // init buttons
                            goobiWorkflowJS.buttons.init();
                            // init bookmarks
                            goobiWorkflowJS.bookmarks.init();
                            
                            // init tinyMCE if needed
                            goobiWorkflowJS.tinymce.init(_defaults);
                            
                            goobiWorkflowJS.setAutocompleteListHandler();
                            break;
                    }
                });
            }
        }
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );