var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.printpage = {
        /**
         * @description Method to initialize a print stylesheet.
         * @method init
         */
    	init: function( config ) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.printpage.init' );
            }
        },
        /**
         * @description Method to open the structure tree in a new window for print.
         * @method print
         */
        print: function(url) {
            if ( _debug ) {
                console.log('EXECUTE: goobiWorkflowJS.printpage.print');
                console.log( '--> url = ', url );
            }
            
            var printReport = document.getElementById('pageContentLeft').innerHTML;
            var link = document.createElement('link');
            link.rel = 'stylesheet';
            link.type = 'text/css';
            link.href = url + '/uii/template/css/intranda.css?version=1';
            
            var win = window.open("", "", "");
            var head = win.document.getElementsByTagName('head')[0];
            head.appendChild(link);
            win.document.body.innerHTML = printReport;
            
            win.window.print();
            win.close();
        }
    };
    

    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );