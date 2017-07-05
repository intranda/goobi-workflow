/**
 * 
 */

var WorldGenerator = (function() {
	
	var _defaultConfig = {
    		camera: {
    			fieldOfView: 35,
    			nearPlane: 0.1,
    			farPlane: 10000,
    			offset:  { x:0, y:0, z:5 },
    		},
    		container: {
    			id: "container"
    		},
    		light: {
    			background: {
    				color: 0xffffff
    			},
    			ambient: {
    				color: 0x909090,
    				intensity: 0.5
    			},
    			directional: {
    				color: 0xaaaaaa,
    				intensity: 1,
    				position: { x:100, y:100, z:100 },
    				castShadow: true
    			}
    		}
    };
	
	var _getObjectLoader = function(config, manager) {
		var suffix = config.url.substring(config.url.lastIndexOf(".")+1);
		switch(suffix.toLowerCase()) {
		case "obj":
			return new THREE.OBJMTLLoader(manager);
		case "ply":
			return new THREE.PLYMeshLoader(manager, config.material.color);
		case "stl":
			return new THREE.STLMeshLoader(manager, config.material.color);
		case "fbx":
			return new THREE.FBXLoader(manager, config.material.color);
		default:
			console.log("not loader defined for " + suffix);
		}
	}
	
	var _getDistance = function(vector) {
		return Math.sqrt(vector.x*vector.x + vector.y*vector.y + vector.z*vector.z);
	}
	
	var Generator = {
			
			create: function(config) {
				
				var localConfig = {};
				$.extend(true, localConfig, _defaultConfig);
				$.extend(true, localConfig, config);
				
				return new World(localConfig);
			}
			
	}
	
    class World {
		constructor(config) {
			console.log("Constructing world with config ", config);
			this.config = config;
			this.time = 0;
			this.container = document.getElementById(config.container.id);
			this.scene = new THREE.Scene();
			// CAMERA//
			this.camera = new THREE.PerspectiveCamera(
					config.camera.fieldOfView,
					this.container.clientWidth / this.container.clientHeight,
					config.camera.nearPlane,
					config.camera.farPlane);
			this.camera.position.set(config.camera.position.x, config.camera.position.y, config.camera.position.z)
			// RENDERER//
			this.renderer = new THREE.WebGLRenderer();
			this.renderer.setSize(this.container.clientWidth, this.container.clientHeight);
			this.renderer.setClearColor( config.light.background.color );
			this.renderer.shadowMap.enabled = true;
			this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
			this.container.appendChild(this.renderer.domElement);
			// CONTROLS//
			this.controls = new THREE.OrbitControls( this.camera, this.container );
			// LOADING MANAGER//
			this.loadingManager = new THREE.LoadingManager();
			this.loadingManager.onProgress = function(item, loaded, total) {
				console.log(item, loaded, total);
			};
			
			// LIGHTS
			if(config.light.ambient) {
				var light = new THREE.AmbientLight( config.light.ambient.color, config.light.ambient.intensity );
				this.scene.add(light);
			}
			if(config.light.directional) {
				var lights = config.light.directional
				if(!$.isArray(lights)) {
					lights = [lights];
				}
				for(var i=0; i < lights.length; i++) {		
					var lightConfig = lights[i];
					var light = this.createShadowedLight(
							lightConfig.position,
							lightConfig.color,
							lightConfig.intensity,
							_getDistance(lightConfig.position),
							lightConfig.castShadow,
							lightConfig.showHelper);
					this.scene.add(light);
				}			
			}
			
			this.tick = new Rx.Subject();
		}
		/**
		 * config.size: diameter of sphere config.material.color: color of the
		 * sphere config.material.opacity: if defined, opacity of sphere
		 * config.offset: Vector3D defining the offset of the sphere's center
		 * from point 0
		 */
		addSphere(config) {
			var sphereGeometry = new THREE.SphereGeometry(config.size/2, 64,64);
			var sphereMaterial = new THREE.MeshLambertMaterial({
				color: config.material.color,
				transparent: config.material.opacity ? true : false,
				opacity: config.material.opacity
			});
			var sphere = new THREE.Mesh(sphereGeometry, sphereMaterial);

			this.center(sphere, config.position);
			if(config.focus) {							
				this.zoomToObject(sphere, this.config.camera.viewPadding, this.config.camera.fieldOfView);
			}
			if(config.onTick) {
				this.tick.subscribe(function(time) {
					config.onTick(sphere, time);
				})
			}
			this.scene.add(sphere);
		}
		addBlock(config) {
			var geometry = new THREE.BoxGeometry(
					config.box.max.x-config.box.min.x, 
					config.box.max.y-config.box.min.y, 
					config.box.max.z-config.box.min.z);
			var material = new THREE.MeshLambertMaterial({
				color: config.material.color,
				transparent: config.material.opacity ? true : false,
				opacity: config.material.opacity
			});
			var box = new THREE.Mesh(geometry, material);

			this.center(box, config.position);
			if(config.focus) {							
				this.zoomToObject(box, this.config.camera.viewPadding, this.config.camera.fieldOfView);
			}
			if(config.onTick) {
				this.tick.subscribe(function(time) {
					config.onTick(box, time);
				})
			}
			this.scene.add(box);
		}
		addPlane(config) {
			var geometry = new THREE.PlaneGeometry(config.size, config.size);
			var material = new THREE.MeshLambertMaterial({
				color: config.material.color,
				transparent: config.material.opacity ? true : false,
				opacity: config.material.opacity
			});
			var plane = new THREE.Mesh(geometry, material);
			plane.receiveShadow = true;
			plane.position.set(config.offset.x, config.offset.y, config.offset.z);
			plane.rotation.set(config.rotation.x * Math.PI / 180, config.rotation.y * Math.PI / 180, config.rotation.z * Math.PI / 180);
			if(config.onTick) {
				this.tick.subscribe(function(time) {
					config.onTick(plane, time);
				})
			}
			this.scene.add(plane);
		}
		loadObject(config) {
			var loader = _getObjectLoader(config, this.loadingManager);
			var world = this;
			var deferred = Q.defer();
			if(loader) {
				loader.load(config.url, function(object) {
					world.addObject(object, config);
					deferred.resolve(object);
				}, function(){}, function(error) {
					deferred.reject(error);
				});
			}
			return deferred.promise;
		}
		addObject(object, config) {
			
			this.setSize(object, config.size);
			this.center(object, config.position);
			this.rotate(object, config.rotation);
			if(config.focus) {				
				this.zoomToObject(object, this.config.camera.viewPadding, this.config.camera.fieldOfView);
			}
			if(config.onTick) {
				this.tick.subscribe(function(time) {
					config.onTick(object, time);
				})
			}
			this.scene.add(object);
			this.object = object;
			return object;
		}
		rotate(object, rotation) {
			object.rotation.set(rotation.x * Math.PI / 180, rotation.y * Math.PI / 180, rotation.z * Math.PI / 180);
		}
		center(object, position) {
			var sphere = this.getBoundingSphere(object);
			var offset = sphere.center;
			object.position.set(position.x-offset.x, position.y-offset.y, position.z-offset.z);
		}
		setSize(object, size) {
			var r = this.getBoundingSphere(object).radius;
			var scale = size/r;
			object.scale.set(scale, scale, scale);
		}
		getBoundingSphere(object) {
			var sphere;
			if(object.geometry) {				
				if(!object.geometry.boundingSphere) {				
					object.geometry.computeBoundingSphere();
				}
				sphere = object.geometry.boundingSphere;
				sphere.radius *= object.scale.x;
			} else {
				var box = new THREE.Box3().setFromObject( object );
				sphere = box.getBoundingSphere();
			}
			return {
				center: sphere.center,
				radius: sphere.radius
			}
		}
		zoomToObject(object, padding, fieldOfView) {
			var sphere = this.getBoundingSphere(object);
			this.zoomToPosition(sphere.center, 2*sphere.radius+padding, fieldOfView);
		}
		zoomToPosition(position, size, fieldOfView) {
			var d = size/(2*Math.sin(Math.PI / 180 * fieldOfView/2));
			this.camera.position.set(position.x, position.y, position.z+d);
//			this.camera.lookAt(position);
		}
		createShadowedLight( position, color, intensity, d, castShadow, showHelper) {
			var directionalLight = new THREE.DirectionalLight( color, intensity );
			directionalLight.position.set( position.x, position.y, position.z );
			this.scene.add( directionalLight );
			directionalLight.castShadow = castShadow;
			directionalLight.shadow.camera.left = -d;
			directionalLight.shadow.camera.right = d;
			directionalLight.shadow.camera.top = d;
			directionalLight.shadow.camera.bottom = -d;
			directionalLight.shadow.camera.near = d/10;
			directionalLight.shadow.camera.far = d*10;
			directionalLight.shadow.mapSize.width = 1024;
			directionalLight.shadow.mapSize.height = 1024;
			directionalLight.shadow.bias = -0.005;
			
			if(showHelper) {				
				var helper = new THREE.DirectionalLightHelper( directionalLight, d/10 );
				this.scene.add( helper );
			}
			
			return directionalLight;
		}
		render() {
			this.time +=1;
			this.renderer.render(this.scene, this.camera);
			this.tick.onNext(this.time);
			var world = this;
			window.requestAnimationFrame(function() {
				world.render();
			});
		}
	}
	
	return Generator;
	
})();