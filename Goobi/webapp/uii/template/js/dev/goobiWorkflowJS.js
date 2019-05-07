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

        // enable BS tooltips and BS popver
        $(function () {
            $( '[data-toggle="tooltip"]' ).tooltip();
            $( '[data-toggle="popover"]' ).popover({
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
        
        // init object view
        goobiWorkflowJS.object.init( _defaults );

        // listen to jsf ajax event
        if ( typeof jsf !== 'undefined' ) {
            jsf.ajax.addOnEvent( function ( data ) {
                var ajaxstatus = data.status;
                var ajaxloader = document.getElementById( 'imageLoader' );

                if ( _defaults.readOnlyMode === 'false' ) {
                    // var saveButton = document.getElementById( 'menu-form:saveMetsFileButton' );
                    // var saveMetsFileImage = document.getElementById( 'menu-form:saveMetsFileImage' );

                    // var ajaxSave = document.getElementById( 'menu-form:saveMetsFileButtonAjax' );
                    // var autoSave = document.getElementById( 'menu-form:automaticSave' );

                    // var exit = document.getElementById( 'exit' );
                    // var exitImage = document.getElementById( 'menu-form:exitImage' );

                    switch ( ajaxstatus ) {
                        case 'begin':
                            ajaxloader.style.display = 'block';
                            // saveButton.style.display = 'none';
                            // saveMetsFileImage.style.display = 'block';

                            // ajaxSave.disabled = true;
                            // autoSave.disabled = true;

                            // exit.style.display = 'none';
                            // exitImage.style.display = 'block';

                            if ( typeof renderInputFields == 'function' ) {
                                renderInputFields( data );
                            }
                            break;
                        case 'complete':
                            ajaxloader.style.display = 'none';
                            // saveButton.style.display = '';
                            // saveMetsFileImage.style.display = 'none';

                            // ajaxSave.disabled = false;
                            // autoSave.disabled = false;

                            // exit.style.display = 'block';
                            // exitImage.style.display = 'none';
                            break;
                        case 'success':
                            $( function () {
                                $( '[data-toggle="tooltip"]' ).tooltip();
                                $( '[data-toggle="popover"]' ).popover( {
                                    html: true
                                } );
                            });

                            if ( $( '.popover.fade.right.in' ).length > 0 ) {
                                $( '.popover.fade.right.in' ).remove();
                            }

                            // loadImages();
                            // loadThumbnails();
                            // addPaginationButtons();
                            // reloadAllHandler();
                            // fitResizeHandle();

                            if ( typeof renderInputFields == 'function' ) {
                                renderInputFields( data );
                            }
                            break;
                    }
                } 
                else {
                    switch ( ajaxstatus ) {
                        case "begin":
                            ajaxloader.style.display = 'block';
                            break;
                        case "complete":
                            ajaxloader.style.display = 'none';
                        case "success":
                            $( function () {
                                $( '[data-toggle="tooltip"]' ).tooltip();
                                $( '[data-toggle="popover"]' ).popover( {
                                    html: true
                                } );
                            });

                            if ( $('.popover.fade.right.in' ).length > 0 ) {
                                $( '.popover.fade.right.in' ).remove();
                            }

                    }
                }
            });
        }
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

    /**
     * @description Method to click the submit button on primefaces autocomplete.
     * @method submitEnter
     * @param {String} id The ID of the submit button.
     * @param {Event} e A JavaScript event which holds the key events.
     * */
    goobiWorkflow.submitEnter = function( id, e ) {
        if ( _debug ) {
            console.log('EXECUTE: goobiWorkflow.submitEnter');
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
            console.log('EXECUTE: goobiWorkflow.setAutocompleteListHandler');
        }

        setTimeout( function () {
            if ( $('.ui-autocomplete-panel li' ).length > 0 ) {
                $( '.ui-autocomplete-panel li' ).on( 'click', function () {
                    document.getElementById( 'goButton' ).click();
                });
            }
        }, 500 );
    }

    return goobiWorkflow;
    
} )( jQuery );