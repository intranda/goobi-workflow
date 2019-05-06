var goobiWorkflowJS = ( function() {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    var goobiWorkflow = {
        currentView: ''
    };
    
    goobiWorkflow.init = function( config ) {
        if ( _debug ) {
            console.log( 'Initializing: goobiWorkflow.init' );
            console.log( '--> config = ', config );
        }
        
        $.extend( true, _defaults, config );

        // throw some console infos
        console.info( 'Current View: ', _defaults.currentView );

        // enable BS tooltips
        $(function () {
            $('[data-toggle="tooltip"]').tooltip();
        });

        // enable BS popver
        $(function () {
            $( "[data-toggle='popover']" ).popover({
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
        
        // TODO: init tinyMCE if needed
        // goobiWorkflowJS.tinymce.init(...);
    }

    return goobiWorkflow;
    
} )( jQuery );