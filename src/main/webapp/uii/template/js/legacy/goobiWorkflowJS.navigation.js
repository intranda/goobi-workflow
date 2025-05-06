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

    const findImageOrThumbButton = ( imageSelector, thumbSelector ) => {
        if (document.querySelector( '#thumbnailsNavigation')) {
            clickButton( thumbSelector );
        }
        if (document.querySelector( '#imageNavigation')) {
            clickButton( imageSelector );
        }
    }

    const clickButton = ( selector ) => {
        const button = document.querySelector( selector );
        if ( button != null ) {
            button.click();
        }
    };

    const nextImage = () => {
        if(_checkNavigationBlocked()) {
            return;
        }
        findImageOrThumbButton( '#nextImage', '#thumbNext' );
    };

    const prevImage = () => {
        if(_checkNavigationBlocked()) {
            return;
        }
        findImageOrThumbButton( '#prevImage', '#thumbPrev' );
    };

    const imageNext20 = () => {
        if(_checkNavigationBlocked()) {
            return;
        }
        clickButton( '#imageNext20' );
    };

    const imageBack20 = () => {
        if(_checkNavigationBlocked()) {
            return;
        }
        clickButton( '#imageBack20' );
    };

    const imageFirst = () => {
        if(_checkNavigationBlocked()) {
            return;
        }
        findImageOrThumbButton( '#imageFirst', '#thumbFirst' );
    };

    const imageLast = () => {
        if(_checkNavigationBlocked()) {
            return;
        }
        findImageOrThumbButton( '#imageLast', '#thumbLast' );
    };

    const save = () => {
        clickButton( '[id$="saveMetsFileButtonAjax"]' );
    };

    const validate = () => {
        clickButton( '[id$="startValidationButton"]' );
    };

    const selectImage = () => {
        const hiddenImageNo = document.querySelector( '[id$="hiddenImageNo"]' ).value;
        if ( hiddenImageNo == null ) {
            hiddenImageNo = document.querySelector( '[id$="hiddenImageNoThumb"]' ).value;
        }
        const checkbox = document.querySelector('[id$="pagesList:' + hiddenImageNo + ':checkbox"]');
        if ( checkbox.checked ) {
            checkbox.checked = false;
        }
        else {
            checkbox.checked = true;
        }
        checkbox.focus();
        checkbox.blur();
        $( document ).off( 'keyup', null, _defaults.navigationShortcut + '+space' );
    };

    function _setImageNavigationButtonEvents() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setImageNavigationButtonEvents' );
        }

        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+right', nextImage );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+left', prevImage );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+up', imageNext20 );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+down', imageBack20 );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+home', imageFirst );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+end', imageLast );

        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+return', save );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+v', validate );
        $( document ).on( 'keyup', null, _defaults.navigationShortcut + '+space', selectImage );

    }

    return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );
