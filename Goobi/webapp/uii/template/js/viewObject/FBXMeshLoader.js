/**
 * @author Florian.Alpers@intranda.com
 */

/**
 * This loader loads a mesh from a ply file and adds a surface material of the
 * given color
 */
THREE.FBXMeshLoader = function(manager, color) {

	this.color = color;
	this.manager = (manager !== undefined) ? manager
			: THREE.DefaultLoadingManager;

};

THREE.FBXMeshLoader.prototype = {

	constructor : THREE.FBXMeshLoader,

	load : function(url, onLoad, onProgress, onError) {

		var loader = new THREE.FBXLoader(this.manager);
		var color = this.color;
		
		Q($.getJSON(url)).then(function(info) {
			console.log("loading object info = ", info);
			var baseResourceUrl = info.uri.substring(0, info.uri.lastIndexOf("/"));
			var objUrl = info.uri;

			loader.load(objUrl, function(geometry) {
				console.log("loaded mesh ", geometry);
//				geometry.computeVertexNormals();
//				var material = new THREE.MeshStandardMaterial({
//					color : color,
//					shading : THREE.FlatShading
//				});
//				var mesh = new THREE.Mesh(geometry, material);
//				mesh.castShadow = true;
//				mesh.receiveShadow = true;
				onLoad(geometry);
			}, function(status) {
				console.log("loading ", status);
			}, function(error) {
				console.log("error ", error)
			});
		});
	}
}
