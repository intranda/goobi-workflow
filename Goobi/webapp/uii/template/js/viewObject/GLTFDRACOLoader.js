/**
 * @author Florian.Alpers@intranda.com
 */

/**
 * This loader loads a mesh from a .gltf file which may point to a .bin file and a number of texture image files
 */
THREE.GLTFDRACOLoader = function(manager) {

	this.manager = (manager !== undefined) ? manager
			: THREE.DefaultLoadingManager;

};

THREE.GLTFDRACOLoader.prototype = {

	constructor : THREE.GLTFDRACOLoader,

	load : function(url, onLoad, onProgress, onError) {

		var gltfLoader = new THREE.GLTFLoader(this.manager);
		THREE.DRACOLoader.setDecoderPath( '/../three/dependencies/draco' );
		gltfLoader.setDRACOLoader( new THREE.DRACOLoader() );
		
		Q($.getJSON(url)).then(function(info) {
			console.log("loading object info = ", info);
			var baseResourceUrl = info.uri.substring(0, info.uri.lastIndexOf("/"));
			var objUrl = info.uri;

			gltfLoader.load(objUrl, function(gltf) {
				console.log("loaded gltf ", gltf);
//		        scene.add( gltf.scene );
				onLoad(gltf.scene);
			}, function(status) {
				console.log("loading ", status);
			}, function(error) {
				console.log("error ", error)
			});
		});
	}
}