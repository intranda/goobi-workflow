var viewImage = ( function( osViewer ) {
    'use strict';
    
    var _debug = false;
    
    osViewer.tileSourceResolver = {
            
            resolveAsJsonOrURI: function(imageInfo) {
                var deferred = Q.defer();
                if(this.isJson(imageInfo)) {
                    deferred.resolve(imageInfo);
                } else if(this.isStringifiedJson(imageInfo)) {
                    deferred.resolve(JSON.parse(imageInfo));
                } else {
                    deferred.resolve(imageInfo);
                }
                return deferred.promise;
                    
            },
            
            resolveAsJson: function(imageInfo)  {  
                var deferred = Q.defer();
                    if(this.isURI(imageInfo)) {
                        if(this.isJsonURI(imageInfo)) {
                            return this.loadJsonFromURL(imageInfo);
                        } else {
                            deferred.reject("Url does not lead to a json object");
                        }
                    } else if(typeof imageInfo === "string"){
                            try {                        
                                var json = JSON.parse(imageInfo);
                                deferred.resolve(json);
                            } catch(error) {
                                deferred.reject("String does not contain valid json: " + error);
                            }
                    } else if(typeof imageInfo === "object"){
                        deferred.resolve(imageInfo);
                    } else {
                        deferred.reject("Neither a url nor a json object");
                    }
                    return deferred.promise;
            },
            
            loadJsonFromURL: function(imageInfo) {
                var deferred = Q.defer();
                if(this.isJsonURI(imageInfo)) {
                    OpenSeadragon.makeAjaxRequest(imageInfo, 
                            //success
                            function(request) {
                                try {                                    
                                    deferred.resolve(JSON.parse(request.responseText));
                                } catch(error) {
                                    deferred.reject(error)
                                }
                            },
                            //error
                            function(error) {
                                deferred.reject(error);
                            }
                    )
                } else {
                    deferred.reject("Not a json uri: " + imageInfo);
                }
                return deferred.promise;
            },
           

            loadIfJsonURL: function( imageInfo ) {
                return Q.promise( function( resolve, reject ) {
                    if ( osViewer.tileSourceResolver.isURI( imageInfo ) ) {
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
                        Q( $.ajax( ajaxParams ) )
                        .then( function( data ) {
                            resolve( data );
                        } )
                        .fail( function( error ) {
                            reject( "Failed to retrieve json from " + imageInfo );
                        } )
                    } else {                        
                        reject( "Not a uri: " + imageInfo );
                    }
                } );
            },
            
            isJsonURI: function(imageInfo) {
                if(this.isURI(imageInfo)) {
                   var shortened = imageInfo.replace(/\?.*/, "");
                   if(shortened.endsWith("/")) {
                       shortened = shortened.substring(0, shortened.length-1);
                   }
                   return shortened.toLowerCase().endsWith(".json");
                }
                return false;
            },
            isURI: function(imageInfo) {
                if(imageInfo && typeof imageInfo === "string") {
                    if(imageInfo.startsWith("http://") || imageInfo.startsWith("https://") || imageInfo.startsWith("file:/")) {
                        return true;
                    }
                }
                return false;
            },
            isStringifiedJson: function(imageInfo) {
                if(imageInfo && typeof imageInfo === "string") {
                    try {                        
                        var json = JSON.parse(imageInfo);
                        return this.isJson(json);
                    } catch(error) {
                        //no json
                        return false;
                    }
                }
                return false;
                
            },
            isJson: function(imageInfo) {
                return imageInfo && typeof imageInfo === "object";
            },
            
    }
    
    return osViewer;
        
} )( viewImage || {}, jQuery );



