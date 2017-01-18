var viewImage = ( function() {
    'use strict';
    
    var osViewer = {};
    var _debug = false;
    
    osViewer.viewer = null;
    osViewer.fullImageBounds = new OpenSeadragon.Rect( 0, 0, 0, 0 );
    
    osViewer.defaults = {
        global: {
            divId: 'map',
            zoomSlider: 'slider-id',
            overlayGroups: [ {
                name: "searchHighlighting",
                styleClass: "searchHighlight",
                interactive: false
            }, {
                name: "ugc",
                styleClass: "ugcBox",
                interactive: true
                
            } ],
            zoomSpeed: 1.1,
            maxZoomLevel: 6,
            minZoomLevel: 1,
            imageControlsActive: true
        },
        image: {},
        getOverlayGroup: function( name ) {
            var allGroups = osViewer.defaults.global.overlayGroups;
            for ( var int = 0; int < allGroups.length; int++ ) {
                var group = allGroups[ int ];
                if ( group.name === name ) {
                    return group;
                }
            }
        },
        getCoordinates: function( name ) {
            var coodinatesArray = osViewer.defaults.image.highlightCoords;
            if ( coodinatesArray ) {
                for ( var int = 0; int < coodinatesArray.length; int++ ) {
                    var coords = coodinatesArray[ int ];
                    if ( coords.name === name ) {
                        return coords;
                    }
                }
            }
        },
    };
    
    osViewer.init = function( config ) {
        if ( _debug ) {
            console.log( '##############################' );
            console.log( 'osViewer.init' );
            console.log( '##############################' );
        }
        
        // consructor
        $.extend( true, osViewer.defaults, config );
        
        // init openSeadragon
        // if (osViewer.defaults.image.tileUrl) {
        // var xmlhttp = new XMLHttpRequest();
        // xmlhttp.open("GET", tileUrl + "ImageProperties.xml", false);
        // tileUrl += 'TileGroup0/';
        // xmlhttp.send();
        // var xmlDoc = xmlhttp.responseXML;
        // var properties = xmlDoc.getElementsByTagName("IMAGE_PROPERTIES")[0];
        // tileSize = parseInt(properties.getAttribute("TILESIZE"));
        // maxWidth = parseInt(properties.getAttribute("WIDTH"));
        // maxHeight = parseInt(properties.getAttribute("HEIGHT"));
        // console.log(smallImage);
        // osViewer.viewer = new OpenSeadragon({
        // id: divId,
        // controlsEnabled: false,
        // prefixUrl: "/openseadragon-bin/images/",
        // zoomPerClick: 1,
        // showNavigationControl: false,
        // showZoomControl: false,
        // showHomeControl: false,
        // showFullPageControl: true,
        // tileSources: {
        // ~ height: maxHeight,
        // ~ width: maxWidth,
        // ~ tileSize: tileSize,
        // ~ minlevel: 1,
        // ~ getNumTiles: function(level) {
        // ~ console.log(level);
        // ~ return Math.pow(2,level);
        // ~ },
        // ~ getTileUrl: function( level, x, y) {
        // ~ console.log("level: " + level + " x:" + x + " y:" + y);
        // ~ return tileUrl + level + "-" + x + "-" + y + ".jpg"
        // ~ }
        // }
        // });
        // } else {
        osViewer.viewer = new OpenSeadragon( {
            // collectionMode : true,
            // collectionRows : 1,
            // collectionTileMargin : -100,
            // collectionLayout: 'vertical',
            sequenceMode: false,
            id: osViewer.defaults.global.divId,
            controlsEnabled: false,
            prefixUrl: "/openseadragon-bin/images/",
            zoomPerClick: 1,
            maxZoomLevel: osViewer.defaults.global.maxZoomLevel,
            minZoomLevel: osViewer.defaults.global.minZoomLevel,
            zoomPerScroll: osViewer.defaults.global.zoomSpeed,
            mouseNavEnabled: osViewer.defaults.global.zoomSpeed > 1,
            showNavigationControl: false,
            showZoomControl: false,
            showHomeControl: false,
            showFullPageControl: true,
            // mouseNavEnabled: osViewer.defaults.global.imageControlsActive,
            tileSources: [ {
                type: "legacy-image-pyramid",
                levels: osViewer.convertImageLevels( osViewer.defaults.image.imageLevels, osViewer.defaults.image.mimeType )
            } ]
        } );
        
        osViewer.viewer.addHandler( 'remove-overlay', function( event ) {
            if ( event.element ) {
                $( event.element ).remove();
            }
        } );
        
        if ( osViewer.controls ) {
            osViewer.controls.init();
        }
        
        if ( osViewer.zoomSlider ) {
            osViewer.zoomSlider.init();
        }

        if ( osViewer.overlays ) {
            osViewer.overlays.init();
        }
        
        if ( osViewer.drawRect ) {
            osViewer.drawRect.init();
        }
        
        if ( osViewer.transformRect ) {
            osViewer.transformRect.init();
        }
        
        // $(document).on('webkitfullscreenchange mozfullscreenchange
        // fullscreenchange
        // MSFullscreenChange', function(event) {
        // var fullscreenElement = document.fullscreenElement ||
        // document.mozFullScreenElement || document.webkitFullscreenElement;
        // var fullscreenEnabled = document.fullscreenEnabled ||
        // document.mozFullScreenEnabled || document.webkitFullscreenEnabled;
        // });
        
    };
    
    // converts java objects for openseadragon
    osViewer.convertImageLevels = function( imageLevels, mimeType ) {
        if ( _debug ) {
            console.log( 'osViewer.convertImageLevels: imageLevels - ' + imageLevels );
            console.log( 'osViewer.convertImageLevels: mimeType - ' + mimeType );
        }
        
        var convertedLevels = [];
        for ( var i = 0; i < imageLevels.length; i++ ) {
            convertedLevels[ i ] = {
                mimetype: mimeType,
                url: imageLevels[ i ][ 0 ],
                width: imageLevels[ i ][ 1 ],
                height: imageLevels[ i ][ 2 ]
            };
            if ( convertedLevels[ i ].width > osViewer.fullImageBounds.width ) {
                osViewer.fullImageBounds.width = convertedLevels[ i ].width;
                osViewer.fullImageBounds.height = convertedLevels[ i ].height;
            }
            if ( _debug ) {
                console.log( 'osViewer.convertImageLevels: convertedLevels - ' );
                console.log( convertedLevels[ i ] );
            }
        }
        if ( _debug ) {
            console.log( "osViewer.convertImageLevels: image bounds - " + osViewer.fullImageBounds );
        }
        return convertedLevels;
    };
    
    osViewer.getViewBounds = function() {
        if ( _debug ) {
            console.log( 'osViewer.getViewBounds: imageViewWidth - ' + osViewer.defaults.image.imageViewWidth );
            console.log( 'osViewer.getViewBounds: imageViewHeight - ' + osViewer.defaults.image.imageViewHeight );
        }
        return new OpenSeadragon.Rect( 0, 0, osViewer.defaults.image.imageViewWidth, osViewer.defaults.image.imageViewHeight );
    };
    
    osViewer.addImage = function( url, width, height ) {
        if ( _debug ) {
            console.log( 'osViewer.addImage: url - ' + url );
            console.log( 'osViewer.addImage: width - ' + width );
            console.log( 'osViewer.addImage: height - ' + height );
        }
        if ( osViewer.viewer ) {
            osViewer.viewer.addTiledImage( {
                tileSource: {
                    type: "legacy-image-pyramid",
                    levels: [ {
                        url: url,
                        height: height,
                        width: width
                    } ]
                },
                x: 0,
                y: 1.6,
                width: 1
            } );
        }
        else {
            if ( _debug )
                console.log( "Viewer not initialized yet; cannot add image" );
        }
    };
    
    osViewer.renderWatermark = function() {
        var canvas = $( osViewer.viewer.canvas ).find( "canvas" );
        
        if ( canvas === null ) {
            console.log( "osViewer.renderWatermark: No canvas :(" );
        }
        else {
            var image = document.getElementById( 'footer-overlay' );
            if ( image === null ) {
                console.log( "osViewer.renderWatermark: image not found" );
            }
            else {
                canvas.get( 0 ).getContext( "2d" ).drawImage( image, 100, 200 );
            }
        }
    };
    
    osViewer.close = function() {
        if ( _debug )
            console.log( "Closing openSeadragon viewer" );
        if ( osViewer.viewer ) {
            osViewer.viewer.destroy();
        }
    };
    return osViewer;
    
} )( jQuery, OpenSeadragon );