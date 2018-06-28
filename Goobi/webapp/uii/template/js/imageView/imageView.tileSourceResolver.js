/**
 * This file is part of the Goobi viewer - a content presentation and management
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
 * Module which interprets the image information.
 * 
 * @version 3.2.0
 * @module viewImage.tileSourceResolver
 * @requires jQuery
 */
ImageView = ( function( imageView ) {
    'use strict';
    
    var _debug = false;
    
    imageView.TileSourceResolver = {
        
        resolveAsJsonOrURI: function( imageInfo ) {
            var deferred = Q.defer();
            if ( this.isJson( imageInfo ) ) {
                deferred.resolve( imageInfo );
            }
            else if ( this.isStringifiedJson( imageInfo ) ) {
                deferred.resolve( JSON.parse( imageInfo ) );
            }
            else {
                deferred.resolve( imageInfo );
            }
            return deferred.promise;
            
        },
        
        resolveAsJson: function( imageInfo ) {
            var deferred = Q.defer();
            if ( this.isURI( imageInfo ) ) {
                if ( this.isJsonURI( imageInfo ) ) {
                    return this.loadJsonFromURL( imageInfo );
                }
                else {
                    deferred.reject( "Url does not lead to a json object" );
                }
            }
            else if ( typeof imageInfo === "string" ) {
                try {
                    var json = JSON.parse( imageInfo );
                    deferred.resolve( json );
                }
                catch ( error ) {
                    deferred.reject( "String does not contain valid json: " + error );
                }
            }
            else if ( typeof imageInfo === "object" ) {
                deferred.resolve( imageInfo );
            }
            else {
                deferred.reject( "Neither a url nor a json object" );
            }
            return deferred.promise;
        },
        
        loadJsonFromURL: function( imageInfo ) {
            var deferred = Q.defer();
            if ( this.isJsonURI( imageInfo ) ) {
                OpenSeadragon.makeAjaxRequest( imageInfo,
                // success
                function( request ) {
                    try {
                        deferred.resolve( JSON.parse( request.responseText ) );
                    }
                    catch ( error ) {
                        deferred.reject( error )
                    }
                },
                // error
                function( error ) {
                    deferred.reject( error );
                } )
            }
            else {
                deferred.reject( "Not a json uri: " + imageInfo );
            }
            return deferred.promise;
        },
        
        loadIfJsonURL: function( imageInfo ) {
            return Q.promise( function( resolve, reject ) {
                if ( imageView.TileSourceResolver.isURI( imageInfo ) ) {
                    var ajaxParams = {
                        url: decodeURI( imageInfo ),
                        type: "GET",
                        dataType: "JSON",
                        async: true,
                        crossDomain: true,
                        accepts: {
                            application_json: "application/json",
                            application_jsonLd: "application/ld+json",
                            text_json: "text/json",
                            text_jsonLd: "text/ld+json",
                        }
                    }
                    Q( $.ajax( ajaxParams ) ).then( function( data ) {
                        resolve( data );
                    } ).fail( function( error ) {
                        reject( "Failed to retrieve json from " + imageInfo );
                    } );
                    setTimeout( function() {
                        reject( "Timeout after 10s" );
                    }, 10000 )
                }
                else {
                    reject( "Not a uri: " + imageInfo );
                }
            } );
        },
        
        isJsonURI: function( imageInfo ) {
            if ( this.isURI( imageInfo ) ) {
                var shortened = imageInfo.replace( /\?.*/, "" );
                if ( shortened.endsWith( "/" ) ) {
                    shortened = shortened.substring( 0, shortened.length - 1 );
                }
                return shortened.toLowerCase().endsWith( ".json" );
            }
            return false;
        },
        isURI: function( imageInfo ) {
            if ( imageInfo && typeof imageInfo === "string" ) {
                if ( imageInfo.startsWith( "http://" ) || imageInfo.startsWith( "https://" ) || imageInfo.startsWith( "file:/" ) ) {
                    return true;
                }
            }
            return false;
        },
        isStringifiedJson: function( imageInfo ) {
            if ( imageInfo && typeof imageInfo === "string" ) {
                try {
                    var json = JSON.parse( imageInfo );
                    return this.isJson( json );
                }
                catch ( error ) {
                    // no json
                    return false;
                }
            }
            return false;
            
        },
        isJson: function( imageInfo ) {
            return imageInfo && typeof imageInfo === "object";
        },
    
    }

    return imageView;
    
} )( ImageView );
