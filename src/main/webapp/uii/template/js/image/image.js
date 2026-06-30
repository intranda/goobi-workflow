/**
 * Image Component Initialization
 * Based on ImageQA Plugin implementation
 */

(function() {
    'use strict';

    // Global viewer instances
    if (typeof window.viewImage === 'undefined') {
        window.viewImage = null;
    }
    if (typeof window.world === 'undefined') {
        window.world = null;
    }

    // Tracks the AbortController for the currently in-flight image load.
    // Aborting it signals any pending initializeImageView call to bail out
    // after its await completes, preventing orphaned tile requests from
    // saturating the browser's connection pool during rapid navigation.
    let _currentLoadAbortController = null;

    /**
     * Initialize image viewer with configuration
     */
    const initializeImageViewer = (config) => {
        if (!config) {
            console.error('Image viewer configuration is required');
            return;
        }
        if (!config.imageView?.element) {
            return;
        }

        const mediaType = config.mediaType;

        if (mediaType === "image") {
            initializeImageView(config);
        } else if (mediaType === "object") {
            initializeObjectView(config);
        } else if (mediaType === "x3dom") {
            initializeX3DOMView(config);
        }
    };

    /**
     * Initialize image view
     */
    const initializeImageView = async (config) => {
        const targetElement = document.querySelector(config.imageView.element);
        if (!targetElement) {
            return;
        }

        // Abort any previous in-flight load so its viewer gets closed after
        // its await resolves, which stops OpenSeadragon from requesting more tiles.
        if (_currentLoadAbortController) {
            _currentLoadAbortController.abort();
        }
        const abortController = new AbortController();
        _currentLoadAbortController = abortController;

        // Close existing viewer (guards against rapid navigation when a previous
        // load already completed and set window.viewImage).
        if (window.viewImage) {
            window.viewImage.close();
            window.viewImage = null;
        }

        const viewImage = {};
        try {
            viewImage.image = new ImageView.Image(config.imageView);

            if (config.controls) {
                viewImage.zoom = new ImageView.Controls.Zoom(viewImage.image);
                if (config.controls.zoomSlider) {
                    viewImage.zoom.setSlider(config.controls.zoomSlider);
                }
                if (config.controls.zoomSliderLabel) {
                    viewImage.zoom.setInput(config.controls.zoomSliderLabel);
                }

                viewImage.rotation = new ImageView.Controls.Rotation(viewImage.image);

                if (config.controls.rotateLeftButton) {
                    rxjs.fromEvent(document.querySelector(config.controls.rotateLeftButton), "click")
                        .subscribe(e => viewImage.rotation.rotateLeft());
                }
                if (config.controls.rotateRightButton) {
                    rxjs.fromEvent(document.querySelector(config.controls.rotateRightButton), "click")
                        .subscribe(e => viewImage.rotation.rotateRight());
                }
                if (config.controls.resetViewButton) {
                    rxjs.fromEvent(document.querySelector(config.controls.resetViewButton), "click")
                        .subscribe(e => {
                            viewImage.rotation.rotateTo(0);
                            viewImage.zoom.goHome();
                        });
                }
            }

            viewImage.close = () => {
                if (viewImage.zoom) viewImage.zoom.close();
                viewImage.image.close();
            };

            await viewImage.image.load(config.tileSource);

            // A newer navigation superseded this one while we were awaiting.
            // Close the viewer to stop any tile requests it might start.
            if (abortController.signal.aborted) {
                viewImage.close();
                return;
            }

            $('#ajaxloader_image').fadeOut(800);
        } catch (error) {
            if (!abortController.signal.aborted) {
                console.error('Error opening image', error);
                $('#ajaxloader_image').fadeOut(800);
                targetElement.innerHTML = `Failed to load image: "${error}"`;
            }
            return;
        }

        window.viewImage = viewImage;
        if (_currentLoadAbortController === abortController) {
            _currentLoadAbortController = null;
        }
    };

    /**
     * Initialize 3D object view
     */
    const initializeObjectView = async (config) => {
        $('#ajaxloader_image').show();

        window.world = WorldGenerator.create(config.worldConfig);

        try {
            const object = await window.world.loadObject({
                url: config.objectUrl,
                position: { x: 0, y: 0, z: 0 },
                rotation: { x: 0, y: 0, z: 0 },
                size: 10,
                material: { color: 0x44bb33 },
                focus: true,
                onTick: function(object, time) {
                    // object.rotation.set(0, Math.PI/180 * time, 0);
                }
            });

            $('#ajaxloader_image').fadeOut(2000);
            window.world.render();
        } catch (error) {
            $('#ajaxloader_image').fadeOut(2000);
            console.error("failed to load: ", error);
        }
    };

    /**
     * Initialize X3DOM view
     */
    const initializeX3DOMView = (config) => {
        $('#ajaxloader_image').show();
        const mainImage = document.getElementById('mainImage');

        new X3DLoader().load(mainImage, config.objectUrl,
            function() {
                $('#ajaxloader_image').fadeOut(2000);
            },
            function(error) {
                $('#ajaxloader_image').fadeOut(2000);
                console.error("X3DOM error", error);
            }
        );
    };

    /**
     * Free JavaScript resources
     */
    const freeJSResources = (data) => {
        if (!data || data.status === 'begin') {
            // Abort any in-flight load so it doesn't start tile requests after we leave.
            if (_currentLoadAbortController) {
                _currentLoadAbortController.abort();
                _currentLoadAbortController = null;
            }
            if (window.viewImage) {
                window.viewImage.close();
                window.viewImage = null;
            }
            if (window.world) {
                window.world.dispose();
                window.world = null;
            }
        }
    };

    // Expose functions globally for JSF callbacks
    window.initializeImageViewer = initializeImageViewer;
    window.freeJSResources = freeJSResources;

})();
