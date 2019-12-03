var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.navigation = {
        /**
         * @description Method to initialize the buttons.
         * @method init
         */
        init: function( config ) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.navigation.init' );
            }
            
            $.extend( true, _defaults, config );
            
            _setImageNavigationButtonEvents();
        }
    };
    
    function _setImageNavigationButtonEvents() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setImageNavigationButtonEvents' );
        }
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+right', function() {
            var myButton = document.getElementById( "nextImage" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+left', function() {
            var myButton = document.getElementById( "prevImage" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+up', function() {
            var myButton = document.getElementById( "imageNext20" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+down', function() {
            var myButton = document.getElementById( "imageBack20" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+home', function() {
            var myButton = document.getElementById( "imageFirst" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+end', function() {
            var myButton = document.getElementById( "imageLast" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+return', function() {
            var myButton = document.getElementById( "meMenuActionsForm:saveMetsFileButtonAjax" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+space', function() {
            var hiddenImageNo = $( "#hiddenImageNo" ).val();
            if ( hiddenImageNo == null ) {
                hiddenImageNo = $( "#hiddenImageNoThumb" ).val();
            }
            
            console.log( document.getElementsByName( "myCheckboxes" ).length );
            
            for ( i = 0; i < document.getElementsByName( "myCheckboxes" ).length; i++ ) {
                if ( i == hiddenImageNo ) {
                    var checkbox = document.getElementsByName( "myCheckboxes" )[ i ];
                    if ( checkbox.checked ) {
                        checkbox.checked = false;
                    }
                    else {
                        checkbox.checked = true;
                    }
                    checkbox.focus();
                    checkbox.blur();
                }
            }
        } )

        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+right', function() {
            var myButton = document.getElementById( "nextImage" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+left', function() {
            var myButton = document.getElementById( "prevImage" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+up', function() {
            var myButton = document.getElementById( "imageNext20" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+down', function() {
            var myButton = document.getElementById( "imageBack20" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+home', function() {
            var myButton = document.getElementById( "imageFirst" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+end', function() {
            var myButton = document.getElementById( "imageLast" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+return', function() {
            
            var myButton = document.getElementById( "meMenuActionsForm:saveMetsFileButtonAjax" );
            console.log( myButton );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+space', function() {
            var hiddenImageNo = $( "#hiddenImageNo" ).val();
            for ( i = 0; i < document.getElementsByName( "myCheckboxes" ).length; i++ ) {
                if ( i == hiddenImageNo ) {
                    var checkbox = document.getElementsByName( "myCheckboxes" )[ i ];
                    if ( checkbox.checked ) {
                        checkbox.checked = false;
                    }
                    else {
                        checkbox.checked = true;
                    }
                }
            }
        } );
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );
