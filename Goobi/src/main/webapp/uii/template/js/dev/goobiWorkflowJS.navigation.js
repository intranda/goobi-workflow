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
    
    function _checkNavigationBlocked() {
    	//The objects in window.imageNavBlockers look like this:
    	//{check: function, confirm: "string"}
    	//check has to be a function. If check() returns true, a confirm dialog with the
    	//string in confirm will be shown. If the confirm dialog is dismissed,
    	//navigation with shortcut keys will be blocked
    	if(!window.imageNavBlockers) {
    		return false;
    	}
    	let blocked = false;
    	for(let blocker of window.imageNavBlockers) {
    		if(blocker.check() && !confirm(blocker.confirm)) {
    			blocked = true;
    		}
    	}
    	return blocked;
    }
    
    function _setImageNavigationButtonEvents() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setImageNavigationButtonEvents' );
        }
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+right', function(e) {
        	if(_checkNavigationBlocked()) {
        		return;
        	}
            var myButton = document.getElementById( "nextImage" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+left', function() {
        	if(_checkNavigationBlocked()) {
        		return;
        	}
            var myButton = document.getElementById( "prevImage" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+up', function() {
        	if(_checkNavigationBlocked()) {
        		return;
        	}
            var myButton = document.getElementById( "imageNext20" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+down', function() {
        	if(_checkNavigationBlocked()) {
        		return;
        	}
            var myButton = document.getElementById( "imageBack20" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+home', function() {
        	if(_checkNavigationBlocked()) {
        		return;
        	}
            var myButton = document.getElementById( "imageFirst" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+end', function() {
        	if(_checkNavigationBlocked()) {
        		return;
        	}
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
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+v', function() {
            var myButton = document.getElementById( "meMenuActionsForm:startValidationButton" );
            if ( myButton != null ) {
                myButton.click();
            }
        } );
        
        $( document ).bind( 'keyup', _defaults.navigationShortcut + '+space', function() {
            var hiddenImageNo = $( "#hiddenImageNo" ).val();
            if ( hiddenImageNo == null ) {
                hiddenImageNo = $( "#hiddenImageNoThumb" ).val();
            }
            var checkbox = document.getElementById( "pagesList:" + hiddenImageNo + ":checkbox" );
            
            if ( checkbox.checked ) {
                checkbox.checked = false;
            }
            else {
                checkbox.checked = true;
            }
            checkbox.focus();
            checkbox.blur();
            
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
        
        $( 'input:checkbox' ).bind( 'keyup', _defaults.navigationShortcut + '+v', function() {
            
            var myButton = document.getElementById( "meMenuActionsForm:startValidationButton" );
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
