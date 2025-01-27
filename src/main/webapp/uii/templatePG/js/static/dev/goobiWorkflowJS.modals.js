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
            
        },
    };
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );