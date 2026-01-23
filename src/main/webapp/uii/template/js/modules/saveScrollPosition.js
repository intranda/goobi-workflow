/**
 * This module provides functions to save and restore the scroll positions of individual elements.
 * It uses the browser's sessionStorage to persist the scroll positions across page reloads and AJAX calls.
 * It handles AJAX calls automatically. Non-AJAX requests need the button that triggers the request
 * to be set up as shown below.
 *
 * Usage for elements:
 * 1. add the attribute `data-save-scroll-position` to the element you want to track
 * 2. make sure the element has a unique id
 *
 * Usage for buttons that should save scroll positions:
 * 1. add the attribute `data-save-scroll-positions` to the button
 *
 * Usage for buttons that should reset scroll positions:
 * 1. add the attribute `data-reset-scroll-positions` to the button
 * 2. value can be: "all" or "true" to reset all, or comma-separated element IDs (e.g. "metadataform,structureform")
 *
 * @module saveScrollPosition
 */

// Cache the parsed positions
let cachedScrollPositions = null;

/**
 * Gets the cached scroll positions or loads them from sessionStorage
 * @returns {Object} The scroll positions object
 */
const getScrollPositions = () => {
    if (cachedScrollPositions === null) {
        const stored = sessionStorage.getItem('gwScrollPositions');
        cachedScrollPositions = stored ? JSON.parse(stored) : {};
    }
    return cachedScrollPositions;
};

/**
 * Saves the cached positions to sessionStorage
 */
const persistScrollPositions = () => {
    if (cachedScrollPositions !== null) {
        sessionStorage.setItem('gwScrollPositions', JSON.stringify(cachedScrollPositions));
    }
};

/**
 * Saves the scroll position of the specified element.
 * @param {HTMLElement} element
 */
export const saveScrollPosition = (element) => {
    const id = element.id;
    if (!id) {
        return;
    }

    const scrollPositions = getScrollPositions();
    scrollPositions[id] = element.scrollTop;
};

/**
 * Restores the scroll position of the specified element.
 * @param {HTMLElement} element
 */
export const restoreScrollPosition = (element) => {
    const id = element.id;
    if (!id) {
        return;
    }

    const scrollPositions = getScrollPositions();
    if (scrollPositions[id] !== undefined) {
        const scrollTop = scrollPositions[id];
        // Delay scrolling for thumbnails
        if (element.querySelector('canvas')) {
            document.addEventListener('thumbnailsLoaded', () => {
                element.scrollTo({ top: scrollTop, behavior: 'smooth' });
            }, { once: true });
        } else {
            element.scrollTo({ top: scrollTop, behavior: 'smooth' });
        }
    }
};

/**
 * Restores all saved scroll positions that can be found on the current page.
 * This function can be called on page load or after an AJAX call to restore the scroll positions.
 */
export const restoreAllScrollPositions = () => {
    // Use requestAnimationFrame to ensure DOM is ready
    requestAnimationFrame(() => {
        document.querySelectorAll('[data-save-scroll-position]').forEach(element => {
            restoreScrollPosition(element);
        });
    });
};

/**
 * Saves all scroll positions of elements that can be found on the current page.
 */
export const saveAllScrollPositions = () => {
    document.querySelectorAll('[data-save-scroll-position]').forEach(element => {
        saveScrollPosition(element);
    });
    // Batch persist all changes at once
    persistScrollPositions();
};

/**
 * Initializes  click handlers for buttons with the `data-save-scroll-positions` attribute to save positions on click.
 */
export const initSaveScrollPosition = () => {
    document.querySelectorAll('[data-save-scroll-positions]').forEach(button => {
        button.addEventListener('click', () => {
            saveAllScrollPositions();
        });
    });
};

/**
 * Resets the scroll position for a specific element.
 * @param {string} elementId
 */
export const resetScrollPosition = (elementId) => {
    const scrollPositions = getScrollPositions();
    if (scrollPositions[elementId] !== undefined) {
        delete scrollPositions[elementId];
        persistScrollPositions();
    }
};

/**
 * Resets all saved scroll positions.
 */
export const resetAllScrollPositions = () => {
    cachedScrollPositions = {};
    sessionStorage.removeItem('gwScrollPositions');
};

/**
 * Handles scroll position reset based on a button's data attribute.
 * @param {string|null} resetAttr - "all" or "true" to reset all, or comma-separated element IDs
 */
export const handleScrollPositionReset = (resetAttr) => {
    if (!resetAttr) return;

    ['all', 'true'].includes(resetAttr)
        ? resetAllScrollPositions()
        : resetAttr.split(',').forEach(id => resetScrollPosition(id.trim()));
};

