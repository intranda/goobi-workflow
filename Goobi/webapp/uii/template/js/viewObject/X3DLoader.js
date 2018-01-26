/**
 * @author Florian.Alpers@intranda.com
 */

/**
 * This loader an object from an x3d file
 */
X3DLoader = function() {
};

X3DLoader.prototype = {

    constructor : X3DLoader,

    load : function($image, url, onLoad, onProgress, onError) {

        return Q($.getJSON(url)).then(function(info) {
            console.log("Loading x3dom ", info);
            $x3d = $('<x3d width="100%" height="100%"><scene><inline url="' + info.uri + '"></inline></scene></x3d>');
            $image.append($x3d);
            onLoad();
        });
    }
}
