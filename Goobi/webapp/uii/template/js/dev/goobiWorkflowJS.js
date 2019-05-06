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

    /**
     * @description Method to print parts of the page.
     * @method printPage
     */
    goobiWorkflow.printPage = function () {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflow.printPage');
        }

        var printReport = document.getElementById( 'left' ).innerHTML;
        var link = document.createElement( 'link' );
        link.rel = 'stylesheet';
        link.type = 'text/css';
        link.href = '#{HelperForm.servletPathWithHostAsUrl}/uii/template/css/intranda.css?version=1';

        var win = window.open("", "", "");
        var head = win.document.getElementsByTagName('head')[0];
        head.appendChild(link);
        win.document.body.innerHTML = printReport;

        win.window.print();
        win.close();
    }

    return goobiWorkflow;
    
} )( jQuery );