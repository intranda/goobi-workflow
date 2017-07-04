/**
 * @author Florian.Alpers@intranda.com
 */

/**
 * This loader loads a mesh and surface from an obj file and a mtl file. The
 * given url should be of the obj file and it is assumed the mtl url is the same
 * with .obj replaced by .mtl
 */
THREE.OBJMTLLoader = function(manager) {

	this.manager = (manager !== undefined) ? manager
			: THREE.DefaultLoadingManager;

};

THREE.OBJMTLLoader.prototype = {

	constructor : THREE.OBJMTLLoader,

	load : function(url, onLoad, onProgress, onError) {

		var objUrl = url;
		var mtlUrl = url.replace(".obj", ".mtl");
		var baseResourceUrl = url.substring(0, url.lastIndexOf("/"));
		var textureLoader = new THREE.MTLLoader(this.manager);
		var objectLoader = new THREE.OBJLoader(this.manager);

		textureLoader.setTexturePath(baseResourceUrl);

		var texture = textureLoader.load(mtlUrl, function(materials) {
			materials.preload();
			objectLoader.setMaterials(materials);
			objectLoader.load(objUrl, onLoad, onProgress, onError);

		}, onProgress, onError);
	}
}
