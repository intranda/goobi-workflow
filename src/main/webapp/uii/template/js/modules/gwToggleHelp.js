/**
 * Show and hide inline help.
 * @module gwToggleHelp
 */

/**
 * Sync help elements with server state after AJAX updates
 */
export const syncHelpState = function syncHelpState() {
    const helpButton = document.querySelector('[id$="showHelp"]');
    if (!helpButton) return;

    // Check if button has the server-side state class
    const shouldShow = helpButton.dataset.showHelp === 'true';
    const elements = document.querySelectorAll(".help-block");

    elements.forEach((el) => {
        el.style.display = shouldShow ? 'block' : 'none';
    });
};

/**
 * Show and hide inline help. This function is called when the user clicks on the help icon.
 * The toggle is implemented through an inline style change so that it may persist across AJAX requests.
 * @todo Refactor to use a class instead of inline styles.
 */
export const toggleHelp = function toggleHelp() {
    const helpButton = document.querySelector('[id$="showHelp"]');
    if (!helpButton) return;

    helpButton.addEventListener('click', function() {
        // Let JSF handle the state change, then sync after AJAX completes
        setTimeout(() => {
            syncHelpState();
        }, 100); // Small delay to let AJAX complete
    });

    // Sync on page load
    syncHelpState();
};