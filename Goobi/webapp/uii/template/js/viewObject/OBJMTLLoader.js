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

		Q($.getJSON(url)).then(function(info) {
			console.log("loading object info = ", info);
			var baseResourceUrl = info.uri.substring(0, info.uri.lastIndexOf("/"));
			var objUrl = info.uri;
			var mtlUrl = info.resources.filter(function(resource) {
				return resource.endsWith(".mtl");
			}).shift();
			var textureLoader = new THREE.MTLLoader(this.manager);
			var objectLoader = new THREE.OBJLoader(this.manager);

			textureLoader.setTexturePath(mtlUrl.substring(0, mtlUrl.lastIndexOf("/")) + "/");

			var texture = textureLoader.load(mtlUrl, function(materials) {
				materials.preload();
				objectLoader.setMaterials(materials);
				objectLoader.load(objUrl, onLoad, onProgress, onError);

			}, onProgress, onError);
		});
	}
}
