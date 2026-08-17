/**
 * @module thumbnailHoverZoom
 *
 * Adds a magnified detail preview to thumbnail canvases: hovering pans a
 * cropped view of the large image (data-image_large) across the canvas;
 * leaving restores whatever was drawn on it beforehand.
 *
 * Usage:
 * - canvas elements must have a `data-image_large` attribute pointing to the large image URL.
 * - canvas elements must have a 'data-image_small' attribute pointing to the small image URL.
 * - canvas elements must have a 'thumb-canvas' class.
 */

const SELECTOR = '.thumb-canvas[data-image_large]';

const largeImageCache = new WeakMap();

/**
 * Lazily loads and caches the large detail image for a thumbnail canvas, so
 * repeated hovers over the same canvas don't refetch it.
 *
 * @param {HTMLCanvasElement} canvas The thumbnail canvas, read for its data-image_large URL.
 * @returns {{image: HTMLImageElement|null}} A cache entry whose `image` is null until the large image has loaded.
 */
const getLargeImage = (canvas) => {
    let entry = largeImageCache.get(canvas);
    if (!entry) {
        entry = { image: null };
        const img = new Image();
        img.onload = () => { entry.image = img; };
        img.src = canvas.dataset.image_large;
        largeImageCache.set(canvas, entry);
    }
    return entry;
};

/**
 * Attaches the hover-zoom listeners to a single thumbnail canvas: mouseenter
 * snapshots the current pixels and starts loading the large image,
 * mousemove pans a crop of it sized to the canvas, and mouseleave restores
 * the snapshot.
 *
 * @param {HTMLCanvasElement} canvas The thumbnail canvas to bind hover-zoom to.
 */
const bindHoverZoom = (canvas) => {
    let snapshot = null;

    canvas.addEventListener('mouseenter', () => {
        const ctx = canvas.getContext('2d');
        snapshot = ctx.getImageData(0, 0, canvas.width, canvas.height);
        getLargeImage(canvas);
    });

    canvas.addEventListener('mousemove', (event) => {
        const { image } = getLargeImage(canvas);
        if (!image) return;

        const rect = canvas.getBoundingClientRect();
        const scaleX = image.width / rect.width;
        const scaleY = image.height / rect.height;
        let posX = (event.clientX - rect.left) * scaleX - canvas.width / 2;
        let posY = (event.clientY - rect.top) * scaleY - canvas.height / 2;
        posX = Math.max(0, Math.min(posX, image.width - canvas.width));
        posY = Math.max(0, Math.min(posY, image.height - canvas.height));

        canvas.getContext('2d').drawImage(image, -posX, -posY);
    });

    canvas.addEventListener('mouseleave', () => {
        if (snapshot) {
            canvas.getContext('2d').putImageData(snapshot, 0, 0);
            snapshot = null;
        }
    });
};

/**
 * Binds hover-zoom to every not-yet-initialized thumbnail canvas in scope.
 *
 * @param {Document|Element} [scope=document] The root to search for thumbnail canvases within.
 */
export const init = function initThumbnailHoverZoom(scope = document) {
    [...scope.querySelectorAll(SELECTOR)]
        .filter((canvas) => !canvas.hasAttribute('data-hover-zoom-init'))
        .forEach((canvas) => {
            canvas.setAttribute('data-hover-zoom-init', '');
            bindHoverZoom(canvas);
        });
};
