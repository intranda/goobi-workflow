/**
 * @author Florian.Alpers@intranda.com
 */

/**
 * This loader loads a mesh from a ply file and adds a surface material of the
 * given color
 */
THREE.TDSMeshLoader = function(manager, color) {

	this.color = color;
	this.manager = (manager !== undefined) ? manager
			: THREE.DefaultLoadingManager;

};

THREE.TDSMeshLoader.prototype = {

	constructor : THREE.TDSMeshLoader,

	load : function(url, onLoad, onProgress, onError) {
		
		var geometryLoader = new THREE.TDSLoader(this.manager);
		var textureLoader = new THREE.TextureLoader();
		
		Q($.getJSON(url))
		.then(function(info) {
			console.log("loading object info = ", info);
			var objUrl = info.uri;
			var textureUrls = info.resources.filter(function(resource) {
				return resource.match(/je?pg|png$/i);
			});

		
			geometryLoader.load(objUrl, function(object) {
				console.log("LOADING 3DS OBJECT", object);
				// geometry.computeVertexNormals();
				
				if(object instanceof THREE.Mesh) {
					console.log("object is already a mesh");
				}

				var textures = textureUrls
				.map(function(url) {
					return textureLoader.load( url );
				})
				console.log("loaded textures ", textures);
				
				object.traverse( function ( child ) {
	
					if ( child instanceof THREE.Mesh ) {
						var texture = textures.shift();
						child.material.map = texture;
					}
	
				} );
				
				object.castShadow = true;
				object.receiveShadow = true;
				onLoad(object);
			}, onProgress, onError);

		});
	}
}
