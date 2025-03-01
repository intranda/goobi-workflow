var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';

    var _debug = false;
    var _columns = {
        left: 0,
        center: 0,
        right: 0,
        handles: {
            left: 0,
            right: 0
        }
    }
    var _defaults = {};

    goobiWorkflow.layout = {
        /**
         * @description Method to initialize the layout module.
         * @method init
        */
    	init: function(config) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.layout.init' );
            }
            $.extend( true, _defaults, config );

            if ( window.matchMedia( '(min-width: 993px)' ).matches ) {
                // set resize event
                window.addEventListener( 'resize', _setResizeEvents );
                window.addEventListener( 'orientationchange', _setResizeEvents );

                // set resizable elements
                _setResizableElements();

                // get saved widths from session storage
                _getSavedWidths();
            }

            // set top margin for thumbnails
            _setThumbnailsMargin();
            // show content wrapper
            $( '#pageContentWrapper' ).show();
//            setTimeout(function() {
//            }, 500);
            // set flexible row columns
            _setFlexibleRowColumns();
            // set initial position of toc actions
            $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );

            //set tabIndex
            _setTabindex();

            //set image comments height
            this.setImageCommentHeight();
        },
        /**
         * @description Method to set the correct height of the object view column.
         * @method setObjectViewHeight
         */
        setObjectViewHeight: function() {
            if ( _debug ) {
                console.log( 'EXECUTE: goobiWorkflowJS.layout.setObjectViewHeight' );
            }

            var pageContentRightHeight = $( '#pageContentRight' ).outerHeight();
            var controlWrapperHeight = $( '#imageNavigation' ).outerHeight();
            var imageCommentHeight = $( '#imageCommentArea' ).outerHeight();
            if (!imageCommentHeight){
            	imageCommentHeight = 0;
            }

            $( '#mainImage' ).css( 'height', pageContentRightHeight - controlWrapperHeight - imageCommentHeight - 45 );
        },
	    /**
	     * @description Method to set initial image comment height
	     * @method setImageCommentHeight
	     */
	    setImageCommentHeight: function() {
			var commentArea = document.querySelector("#Comment");
			if(commentArea) {
				function setHeight() {
					var lines = Math.max(1, commentArea.value.split("\n").length);
					commentArea.style.height = (lines * 17 + 13) + "px";
				}
				setHeight();
	  			//also add event listener on input
	  			commentArea.addEventListener("input", setHeight);
			}
		}
    };


    /**
     * @description Method to add Tabindex to elements.
     * @method _setTabindex
     */
    function _setTabindex(){
    	if ( _debug ) {
    		console.log( ' EXECUTE: _setTabindex' );
    	}
    	$(".focusableChildCss input").addClass("focusable");
    	$(".focusable").attr("tabindex", "0");
     	$(".focusableChild input").attr("tabindex", "0");	//tabindex 0 not working??
     	$(".notFocusable").attr("tabindex", "-1");
     	$(".notFocusableChild").children().attr("tabindex","-1");
    }

    /**
     * @description Method to set the resizable elements.
     * @method _setResizableElements
     */
    function _setResizableElements() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setResizableElements' );
        }

        // set left column resizable
        $( '#pageContentLeft' ).resizable({
            handles: 'e',
            minWidth: 200,
            maxWidth: $(window).width() * 0.45,
            resize: function( event, ui ) {
            if (_defaults.displayImageArea) {
                        $( '#pageContentCenter' ).outerWidth( $( window ).offsetWidth - $( '#pageContentRight' ).offsetWidth - $( '#pageContentLeft' ).offsetWidth -1);
                        $( '#pageContentLeft .ui-resizable-handle' ).css( 'left', $( '#pageContentLeft' ).offsetWidth );
                        $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );
            } else {
                $( '#pageContentCenter' ).outerWidth( $( window ).offsetWidth - $( '#pageContentLeft' ).offsetWidth );
                $( '#pageContentLeft .ui-resizable-handle' ).css( 'left', $( '#pageContentLeft' ).offsetWidth );
                $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );
            }
                goobiWorkflowJS.layout.setObjectViewHeight();
                _setFlexibleRowColumns();
                _setColumnWidth();
            }
        }).on( 'resize', function( event ) {
            event.stopPropagation();
        } );
       // if (_defaults.displayImageArea) {
            // set right column resizable
        $( '#pageContentRight' ).resizable({
            handles: 'w',
            minWidth: 400,
            maxWidth: $(window).width() / 2,
            resize: function( event, ui ) {
                $( '#pageContentCenter' ).outerWidth( $( window ).offsetWidth - $( '#pageContentRight' ).offsetWidth - $( '#pageContentLeft' ).offsetWidth -1);
                $( '#pageContentRight .ui-resizable-handle' ).css( 'right', $( '#pageContentRight' ).offsetWidth - 7 );

                goobiWorkflowJS.layout.setObjectViewHeight();
                _setFlexibleRowColumns();
                _setColumnWidth();
            }
        }).on( 'resize', function( event ) {
            event.stopPropagation();
        } );

        $( '#pageContentLeft .ui-resizable-handle' ).attr('tabindex', '-1');
        $( '#pageContentRight .ui-resizable-handle' ).attr('tabindex', '-1');
    }

    /**
     * @description Method to reset the resizable elements.
     * @method _resetResizableElements
     */
    function _resetResizableElements() {
        if ( _debug ) {
            console.log( 'EXECUTE: _resetResizableElements' );
        }

        $( '#pageContentLeft' ).css( 'width', '20%' );
        if (_defaults.displayImageArea) {

        $( '#pageContentCenter, #pageContentRight' ).css( 'width', '40%' );
        } else {
            $( '#pageContentCenter').css( 'width', '80%' );
        }
        $( '#pageContentLeft .ui-resizable-handle' ).css( 'left', $( '#pageContentLeft' ).offsetWidth );
        $( '#pageContentLeft .ui-resizable-handle' ).attr('tabindex', '-1');
        $( '#pageContentRight .ui-resizable-handle' ).css( 'right', $( '#pageContentRight' ).offsetWidth - 7 );
        $( '#pageContentRight .ui-resizable-handle' ).attr('tabindex', '-1');
        $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );
        _setColumnWidth();
        _getSavedWidths();
    }

    /**
     * @description Method to set the column widths from session storage.
     * @method _setColumnWidth
     */
    function _setColumnWidth() {
        const leftColumn = document.querySelector('[id$="pageContentLeft"]');
        const centerColumn = document.querySelector('[id$="pageContentCenter"]');
        const rightColumn = document.querySelector('[id$="pageContentRight"]');
        if ( _debug ) {
            console.log( 'EXECUTE: _setColumnWidth' );
        }
        _columns.left = leftColumn.offsetWidth;
        _columns.center = centerColumn.offsetWidth;
        _columns.right = rightColumn.offsetWidth;
        sessionStorage.setItem( 'wf_columnWidths', JSON.stringify( _columns ) );
    }

    /**
     * @description Method to set the window rezise event.
     * @method _setResizeEvents
     */
    function _setResizeEvents() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setResizeEvents' );
        }

        _resetResizableElements();
        goobiWorkflowJS.layout.setObjectViewHeight();
        _setFlexibleRowColumns();
    }

    /**
     * @description Method to get the saved widths from session storage.
     * @method _getSavedWidths
     */
    function _getSavedWidths() {
        const leftColumn = document.querySelector('[id$="pageContentLeft"]');
        const centerColumn = document.querySelector('[id$="pageContentCenter"]');
        const rightColumn = document.querySelector('[id$="pageContentRight"]');
        if ( _debug ) {
            console.log( 'EXECUTE: _getSavedWidths');
        }
        if ( sessionStorage.getItem( 'wf_columnWidths' ) != null ) {
            _columns = JSON.parse( sessionStorage.getItem( 'wf_columnWidths' ) );
            if (_defaults.displayImageArea) {
                if ($( window ).offsetWidth < _columns.left +  _columns.center + _columns.right || _columns.right == null) {
                    leftColumn.style.width = Math.floor(_columns.left) + 'px';
                    centerColumn.style.width = Math.floor(($( window ).offsetWidth -  leftColumn.offsetWidth -  document.querySelector('[id$="pageContentRight"]').offsetWidth -1)) + 'px';
                    rightColumn.style.width = Math.floor(($( window ).offsetWidth -  leftColumn.offsetWidth ) /2) + 'px';
                } else {
                    leftColumn.style.width = Math.floor(_columns.left) + 'px';
                    centerColumn.style.width = Math.floor(_columns.center) + 'px';
                    rightColumn.style.width = Math.floor(_columns.right) + 'px';
                }
            } else {
                leftColumn.style.width = Math.floor(_columns.left) + 'px';
                centerColumn.style.width = Math.floor($(window).width() -  leftColumn.offsetWidth) + 'px';
            }
        } else {
            leftColumn.style.width = leftColumn.offsetWidth + 'px';
            centerColumn.style.width = centerColumn.offsetWidth + 'px';
            rightColumn.style.width = rightColumn.offsetWidth + 'px';
            _setColumnWidth();
        }
    }

    /**
     * @description Method to set top margin of the thumbnail wrapper.
     * @method _setThumbnailsMargin
     */
    function _setThumbnailsMargin() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setThumbnailsMargin' );
        }

//        var thumbnailsNavigationHeight = $( '#thumbnailsNavigation' ).height();
//
//        if (window.matchMedia('(min-width: 993px)').matches) {
//            $( '#thumbnails' ).css( 'margin-top', thumbnailsNavigationHeight + 10 );
//        }
//        else {
//            $( '#thumbnails' ).css( 'margin-top', 66 );
//        }
    }

    /**
     * @description Method to set the flexible row column width.
     * @method _setFlexibleRowColumns
     */
    function _setFlexibleRowColumns() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setFlexibleRowColumns' );
        }
        if ($( '.row-flexible' ).width() > 0 && $( '.row-flexible' ).width() < 550 ) {

            $( '.row-flexible' ).addClass( 'fullwidth' );
        }
        else {
            $( '.row-flexible' ).removeClass( 'fullwidth' );
        }
    }

    return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );