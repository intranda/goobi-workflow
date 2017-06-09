/**
 * This file is part of the Goobi Viewer - a content presentation and management
 * application for digitized objects.
 * 
 * Visit these websites for more information. - http://www.intranda.com -
 * http://digiverso.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Module which handles the persistence of zoom and rotation levels.
 * 
 * @version 3.2.0
 * @module viewImage.controls.persistence
 * @requires jQuery
 */
var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = true;
    
    osViewer.controls.persistence = {
            
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
                    }
                    catch ( err ) {
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
                        }
                        else {
                            config.image.location.rotation = 0;
                        }

                    }
                    
                    /**
                     * save current location to local storage before navigating away
                     */
                        window.onbeforeunload = function() {
                        	viewImage.controls.persistence.storeLocation();
                        }

                    }
                }
            },
            storeLocation: function() {
                var loc = osViewer.controls.getLocation();
                loc.persistenceId = osViewer.getConfig().global.persistenceId;
                localStorage.imageLocation = JSON.stringify(loc);
                if(_debug) {                                
                    console.log("storing zoom " + localStorage.imageLocation);
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