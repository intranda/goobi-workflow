var goobiWorkflowJS = ( function() {
    'use strict';
    
    var _debug = true;
    var _defaults = {};
    
    var goobiWorkflow = {};
    
    goobiWorkflow.init = function( config ) {
        if ( _debug ) {
            console.log( 'Initializing: goobiWorkflow.init' );
            console.log( '--> config = ', config );
        }
        
        $.extend( true, _defaults, config );

        // enable BS tooltips
        $(function () {
            $('[data-toggle="tooltip"]').tooltip();
        });

        // enable BS popver
        $(function () {
            $("[data-toggle='popover']").popover({
                html: true
            });
        });

        // init menu
        goobiWorkflowJS.menu.init();

        // init layout
        goobiWorkflowJS.layout.init();
        
        // init module box
        goobiWorkflowJS.box.init();
        
        // init buttons
        goobiWorkflowJS.buttons.init();
    }

    return goobiWorkflow;
    
} )( jQuery );