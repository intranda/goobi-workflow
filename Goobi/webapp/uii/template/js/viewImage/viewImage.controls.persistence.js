var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    
    osViewer.controls.persistance = {
            
            init: function(config) {
                if ( typeof ( Storage ) !== 'undefined' ) {
                    
                    /**
                     * Set Location from local storage
                     */
                    var location = null;
                    var currentPersistenceId = osViewer.getConfig().global.persistenceId;
                    if(config.global.persistZoom || config.global.persistRotation) {     
                    try {                        
                        var location = JSON.parse(localStorage.imageLocation);
                    } catch(err) {
                        if(_debug) {
                            console.error("No readable image location in local storage");
                        }
                    }
                    if(location && _isValid(location) && location.persistenceId === currentPersistenceId) {
                        if(_debug) {
                            console.log("Reading location from local storage", location);
                        }
                        config.image.location = {};
                        if(config.global.persistZoom) {
                            if(_debug) {
                                console.log("setting zoom from local storage");
                            }
                            config.image.location.zoom = location.zoom;
                            config.image.location.x = location.x;
                            config.image.location.y = location.y;
                        }
                        if(config.global.persistRotation) {
                            if(_debug) {
                                console.log("setting rotation from local storage");
                            }
                            config.image.location.rotation = location.rotation;
                        } else {
                            config.image.location.rotation = 0;
                        }

                    }
                    
                    /**
                     * save current location to local storage before navigating away
                     */
                        window.onbeforeunload = function() {
                            var loc = osViewer.controls.getLocation();
                            loc.persistenceId = osViewer.getConfig().global.persistenceId;
                            localStorage.imageLocation = JSON.stringify(loc);
                            if(_debug) {                                
                                console.log("storing zoom " + localStorage.imageLocation);
                            }
                        }
                    }
                }
            }
    }
    
    function _isValid(location) {
        return _isNumber(location.x) && _isNumber(location.y) && _isNumber(location.zoom) && _isNumber(location.rotation);
    }
    
    function _isNumber(x) {
        return typeof x === "number" && !Number.isNaN(x);
    }
    
    return osViewer;
    
} )( viewImage || {}, jQuery );