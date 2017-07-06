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
		
		var baseResourceUrl = url.substring(0, url.lastIndexOf("/"));
		var filename = "/jabiru/Jabiru_160C_paint.png";

		var textureLoader = new THREE.TextureLoader();
		
		var geometryLoader = new THREE.TDSLoader(this.manager);
		var color = this.color;
		geometryLoader.load(url, function(object) {
			console.log("LOADING 3DS OBJECT");
			var texture = textureLoader.load( baseResourceUrl + filename );
			//geometry.computeVertexNormals();
			
			if(object instanceof THREE.Mesh) {
				console.log("object is already a mesh");
			}
			
			object.traverse( function ( child ) {

				if ( child instanceof THREE.Mesh ) {
					console.log("Found mesh inside object");
					child.material.map = texture;
				}

			} );
			
			object.castShadow = true;
			object.receiveShadow = true;
			onLoad(object);
		}, onProgress, onError);

	}
}
