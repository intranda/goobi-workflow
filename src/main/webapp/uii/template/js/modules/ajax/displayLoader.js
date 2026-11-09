/**
 * Show and hide goobi's ajaxLoader (∞) and announce the loading state to
 * assistive technology (WCAG 4.1.3 status messages).
 * @module displayLoader
 */

/** Delay before "loading" is announced, so short requests stay silent (ms). */
const ANNOUNCE_DELAY = 1000;

let statusElement = null;
let announceTimer = null;

/**
 * Returns the visually hidden live region, creating it on first use.
 * @returns {HTMLElement}
 */
const getStatusElement = () => {
    if (statusElement && statusElement.isConnected) {
        return statusElement;
    }
    statusElement = document.createElement('div');
    statusElement.className = 'visually-hidden';
    statusElement.setAttribute('role', 'status');
    document.body.appendChild(statusElement);
    return statusElement;
};

/**
 * Announces (after a short delay) or clears the loading message.
 * The text is the localized alt text of the loader image.
 * @param {boolean} show
 * @param {string} text
 */
const announce = (show, text) => {
    if (show) {
        if (announceTimer !== null || !text) return;
        // Create the live region right away so it is known before its content changes
        getStatusElement();
        announceTimer = setTimeout(() => {
            announceTimer = null;
            getStatusElement().textContent = text;
        }, ANNOUNCE_DELAY);
    } else {
        clearTimeout(announceTimer);
        announceTimer = null;
        if (statusElement) {
            statusElement.textContent = '';
        }
    }
};

export const toggleLoaders = function toggleLoaders(show) {
    const loaders = document.querySelectorAll('.ajax-loader');

    if (loaders.length === 0) return;

    const displayValue = show ? 'block' : 'none';

    loaders.forEach(loader => {
        loader.style.display = displayValue;
    });

    announce(show, loaders[0].getAttribute('alt'));
}
