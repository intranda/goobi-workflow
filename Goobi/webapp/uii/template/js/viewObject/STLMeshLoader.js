/**
 * @author Florian.Alpers@intranda.com
 */

/**
 * This loader loads a mesh from a ply file and adds a surface material of the
 * given color
 */
THREE.STLMeshLoader = function(manager, color) {

	this.color = color;
	this.manager = (manager !== undefined) ? manager
			: THREE.DefaultLoadingManager;

};

THREE.STLMeshLoader.prototype = {

	constructor : THREE.STLMeshLoader,

	load : function(url, onLoad, onProgress, onError) {

		var loader = new THREE.STLLoader(this.manager);
		var color = this.color;
		loader.load(url, function(geometry) {
			geometry.computeVertexNormals();
			var material = new THREE.MeshStandardMaterial({
				color : color,
				shading : THREE.FlatShading
			});
			var mesh = new THREE.Mesh(geometry, material);
			mesh.castShadow = true;
			mesh.receiveShadow = true;
			onLoad(mesh);
		}, onProgress, onError);

	}
}
