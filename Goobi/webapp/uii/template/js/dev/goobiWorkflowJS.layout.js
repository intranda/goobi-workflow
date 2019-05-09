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
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.layout.init' );
            }

            if ( window.matchMedia( '(min-width: 993px)' ).matches ) {
                // set resize event
                window.addEventListener( 'resize', _setResizeEvents );
                window.addEventListener( 'orientationchange', _setResizeEvents );

                // set resizable elements
                _setResizableElements();

                // get saved widths from session storage
                _getSavedWidths();
            }

            // show content wrapper
            $( '#pageContentWrapper' ).fadeIn( {
                duration: 500,
                start: function() {
                    // set flexible row columns            
                    _setFlexibleRowColumns();
                    // set initial position of toc actions
                    $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );
                    // set top margin for thumbnails
                    _setThumbnailsMargin();
                }
            } );
        },
        /**
         * @description Method to set the correct height of the object view column.
         * @method setObjectViewHeight
         */
        setObjectViewHeight: function() {
            if ( _debug ) {
                console.log( 'EXECUTE: goobiWorkflow.layout.setObjectViewHeight' );
            }
                        
            var pageContentRightHeight = $( '#pageContentRight' ).outerHeight();
            var controlWrapperHeight = $( '#objectControlWrapper' ).outerHeight();

            $( '#mainImage' ).css( 'height', pageContentRightHeight - controlWrapperHeight - 45 );
            
        }
    };
    
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
            maxWidth: 400,
            resize: function( event, ui ) {
                $( '#pageContentCenter' ).outerWidth( $( window ).outerWidth() - $( '#pageContentRight' ).outerWidth() - $( '#pageContentLeft' ).outerWidth() );
                $( '#pageContentRight' ).outerWidth( $( window ).outerWidth() - $( '#pageContentLeft' ).outerWidth() - $( '#pageContentCenter' ).outerWidth() );
                $( '#pageContentLeft .ui-resizable-handle' ).css( 'left', $( '#pageContentLeft' ).outerWidth() );
                $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );
                
                goobiWorkflowJS.layout.setObjectViewHeight();
                _setFlexibleRowColumns();
                _setColumnWidth();
            }
        }).on( 'resize', function( event ) {
            event.stopPropagation();
        } );
        
        // set right column resizable
        $( '#pageContentRight' ).resizable({
            handles: 'w',
            minWidth: 400,
            maxWidth: 800,
            resize: function( event, ui ) {
                $( '#pageContentCenter' ).outerWidth( $( window ).outerWidth() - $( '#pageContentRight' ).outerWidth() - $( '#pageContentLeft' ).outerWidth() );
                $( '#pageContentLeft' ).outerWidth( $( window ).outerWidth() - $( '#pageContentRight' ).outerWidth() - $( '#pageContentCenter' ).outerWidth() );
                $( '#pageContentRight .ui-resizable-handle' ).css( 'right', $( '#pageContentRight' ).outerWidth() - 7 );
                
                goobiWorkflowJS.layout.setObjectViewHeight();
                _setFlexibleRowColumns();
                _setColumnWidth();
            }
        }).on( 'resize', function( event ) {
            event.stopPropagation();
        } );
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
        $( '#pageContentCenter, #pageContentRight' ).css( 'width', '40%' );
        $( '#pageContentLeft .ui-resizable-handle' ).css( 'left', $( '#pageContentLeft' ).outerWidth() );
        $( '#pageContentRight .ui-resizable-handle' ).css( 'right', $( '#pageContentRight' ).outerWidth() - 7 );
        $( '#structureActions' ).css( 'left', $( '#pageContentLeft' ).width() - 45 );
        _setColumnWidth();
        _getSavedWidths();
    }

    /**
     * @description Method to set the column widths from session storage.
     * @method _setColumnWidth
     */
    function _setColumnWidth() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setColumnWidth' );
        }
            
        _columns.left = $( '#pageContentLeft' ).outerWidth();
        _columns.center = $( '#pageContentCenter' ).outerWidth();
        _columns.right = $( '#pageContentRight' ).outerWidth();
        _columns.handles.left = parseInt( $( '#pageContentLeft .ui-resizable-handle' ).css( 'left' ) );
        _columns.handles.right = parseInt( $( '#pageContentRight .ui-resizable-handle' ).css( 'right' ) );

        sessionStorage.setItem( 'columnWidths', JSON.stringify( _columns ) );
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
        if ( _debug ) {
            console.log( 'EXECUTE: _getSavedWidths' );
        }
            
        if ( sessionStorage.getItem( 'columnWidths' ) != undefined ) {
            _columns = JSON.parse( sessionStorage.getItem( 'columnWidths' ) );

            $( '#pageContentLeft' ).outerWidth( _columns.left );
            $( '#pageContentCenter' ).outerWidth( _columns.center );
            $( '#pageContentRight' ).outerWidth( _columns.right );
            $( '#pageContentLeft .ui-resizable-handle' ).css( 'left', _columns.handles.left );
            $( '#pageContentRight .ui-resizable-handle' ).css( 'right', _columns.handles.right );
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
            
        var thumbnailsNavigationHeight = $( '#thumbnailsNavigation' ).height();
        
        $( '#thumbnails' ).css( 'margin-top', thumbnailsNavigationHeight + 10 );
    }
    
    /**
     * @description Method to set the flexible row column width.
     * @method _setFlexibleRowColumns
     */
    function _setFlexibleRowColumns() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setFlexibleRowColumns' );
        }
            
        if ( $( '.row-flexible' ).width() < 550 ) {
            $( '.row-flexible' ).addClass( 'fullwidth' );
        }
        else {
            $( '.row-flexible' ).removeClass( 'fullwidth' );
        }
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );