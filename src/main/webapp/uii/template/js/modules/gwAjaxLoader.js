/**
 * Show and hide goobi's ajaxLoader (∞).
 * @module gwAjaxLoader
 */
export const toggleLoaders = function toggleLoaders(show) {
    const loaders = document.querySelectorAll('.ajax-loader');

    if (loaders.length === 0) return;

    const displayValue = show ? 'block' : 'none';

    loaders.forEach(loader => {
        loader.style.display = displayValue;
    });
}