var goobiWorkflowJS = ( function() {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    var goobiWorkflow = {};
    
    goobiWorkflow.init = function( config ) {
        if ( _debug ) {
            console.log( 'Initializing: goobiWorkflowJS.init' );
            console.log( '--> config = ', config );
        }
        
        $.extend( true, _defaults, config );

        // throw some console infos
        console.info( 'Current View: ', _defaults.currentView );

        // init BS features --> needs ajax reload
        goobiWorkflowJS.initBootstrapFeatures();

        // init layout --> needs ajax reload
        goobiWorkflowJS.layout.init(_defaults);

        // init menu
        goobiWorkflowJS.menu.init();
        
        // init module box --> needs partial ajax reload
        goobiWorkflowJS.box.init();
        
        // init buttons --> needs ajax reload
        goobiWorkflowJS.buttons.init(_defaults);
        
        // TODO: init tinyMCE if needed
        // goobiWorkflowJS.tinymce.init(...);
        
        // init object view --> needs ajax reload
        goobiWorkflowJS.object.init();
        
        // init bookmarks --> needs ajax reload
        goobiWorkflowJS.bookmarks.init();

        // init jump to page
        goobiWorkflowJS.jumpToPage.init();

        // init scroll positions
        goobiWorkflowJS.scrollPositions.init();

        // init progressbar
        // goobiWorkflowJS..init();
        
        // init shortcuts for navigation
        goobiWorkflowJS.navigation.init(_defaults);
        
        // init buttons --> needs ajax reload
        goobiWorkflowJS.buttons.init(_defaults);
        
        // execute autosave interval
        if (_defaults.readOnlyMode === 'false') {
            goobiWorkflowJS.autoSave(_defaults.autoSaveInterval);
        }

        // init structdata module
        goobiWorkflowJS.structdata.init();
        
        // init modals
        goobiWorkflowJS.modals.init();
        
        // init thumbnails --> needs ajax reload
        goobiWorkflowJS.thumbnails.init();
        
        // init jsf ajax listener
        goobiWorkflowJS.jsfAjax.init( _defaults );
        

    }
    
    /**
     * @description Method to initialize Bootstrap features.
     * @method initBootstrapFeatures
     */
    goobiWorkflow.initBootstrapFeatures = function () {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflowJS.initBootstrapFeatures');
        }
        
        $( '[data-toggle="tooltip"]' ).tooltip( {
            trigger: 'hover'
        } );
        $( '[data-toggle="popover"]' ).popover({
            html: true
        });
    }

    /**
     * @description Method to clean up Bootstrap features.
     * @method cleanUpBootstrapFeatures
     */
    goobiWorkflow.cleanUpBootstrapFeatures = function () {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflowJS.cleanUpBootstrapFeatures');
        }

        if ( $( '.popover' ).length > 0 ) {
            $( '.popover' ).remove();
        }
        else if ( $( '.tooltip' ).length > 0 ) {
            $( '.tooltip' ).remove();
        }
    }

    /**
     * @description Method to print parts of the page.
     * @method printPage
     */
    goobiWorkflow.printPage = function () {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflowJS.printPage');
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

    /**
     * @description Method to click the submit button on primefaces autocomplete.
     * @method submitEnter
     * @param {String} id The ID of the submit button.
     * @param {Event} e A JavaScript event which holds the key events.
     * */
    goobiWorkflow.submitEnter = function( id, e ) {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflowJS.submitEnter');
        }

        var keycode;

        if ( window.event ) {
            keycode = window.event.keyCode;
        }
        else if ( e ) {
            keycode = e.which;
        }
        else {
            return true;
        }
        if ( keycode == 13 ) {
            document.getElementById( id ).click();

            return false;
        }
        else {
            return true;
        }
    }

    /**
     * @description Method to set on click handler to primefaces autocomplete items.
     * @method setAutocompleteListHandler
     * */
    goobiWorkflow.setAutocompleteListHandler = function() {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflowJS.setAutocompleteListHandler');
        }

        setTimeout( function () {
            if ( $('.ui-autocomplete-panel li' ).length > 0 ) {
                $( '.ui-autocomplete-panel li' ).on( 'click', function () {
                    document.getElementById( 'goButton' ).click();
                });
            }
        }, 500 );
    }
    
    /**
     * @description Method to set on click handler to primefaces autocomplete items.
     * @method displayProgressBar
     * */
    goobiWorkflow.displayProgressBar = function() {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflowJS.displayProgressBar');
        }
        
        var element = document.getElementById( 'progressPanel' );

        if ( element.style.display == 'block' ) {
            element.style.display = 'none';
        } 
        else {
            element.style.display = 'block';
        }

        document.getElementById( 'progressbutton' ).click();
    }
    
    /**
     * @description Method to trigger autosave.
     * @method autoSave
     * @param {Number} interval The interval when autosave starts.
     * */
    goobiWorkflow.autoSave = function(interval) {            
        var intervalValue = parseInt(interval);
        
        if (intervalValue > 0) {
            setInterval( function() {
                var myButton = document.getElementById("meMenuActionsForm:automaticSave");
                if (myButton!=null) {
                    myButton.click();
                }
            }, intervalValue * 1000 * 60);
        }
    };
    
    /**
     * @description Method to trigger a button on enter.
     * @method fireOnEnter
     * @param {String} object The object selector to trigger enter.
     * @param {String} selector The selector of the button to click.
     * */
    goobiWorkflow.fireOnEnter = function (object, selector) {
        if (_debug) {
            console.log('EXECUTE: goobiWorkflowJS.fireOnEnter');
            console.log('--> object = ', object);
            console.log('--> selector = ', selector);
        }

        $('#' + object).on('keyup', function (event) {
            event.preventDefault();

            if (event.keyCode == 13) {
                $('#' + selector).click();
            }
        });
    };

    return goobiWorkflow;
    
} )( jQuery );