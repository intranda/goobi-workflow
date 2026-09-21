/**
 * Wraps WorldGenerator.create() from the vendored objectView.min.js so the
 * WebGL canvas is painted with its configured background color immediately,
 * instead of only after the first animation-loop render() call (which does
 * not happen until the 3D object has finished loading). Without this, the
 * canvas is briefly visible in its uncleared (black) state, causing a black
 * flash while a 3D object loads.
 *
 * Bundled standalone (see gulpfile.mjs objectViewBackgroundFixJs) and loaded
 * after objectView.min.js wherever it is used, so this survives updates to
 * that vendored file.
 *
 * @module objectViewBackgroundFix
 */

if (typeof WorldGenerator !== 'undefined') {
    const originalCreate = WorldGenerator.create;

    WorldGenerator.create = function(config) {
        const world = originalCreate(config);
        if (world?.renderer && world?.scene && world?.camera) {
            world.renderer.render(world.scene, world.camera);
        }
        return world;
    };
}
