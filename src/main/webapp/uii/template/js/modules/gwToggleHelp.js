/**
 * Show and hide inline help.
 * @module gwToggleHelp
 */

/**
 * Show and hide inline help. This function is called when the user clicks on the help icon.
 * The toggle is implemented through an inline style change so that it may persist across AJAX requests.
 * @todo Refactor to use a class instead of inline styles.
 */
export const toggleHelp = function toggleHelp() {
    const helpButton = document.querySelector('[id$="showHelp"]');
    if (!helpButton) {
        return;
    } else {
        helpButton.addEventListener('click', function() {
            const elements = document.querySelectorAll(".help-block");
            for (let i = 0; i < elements.length; i++) {
                elements[i].style.display = elements[i].style.display == 'block' ? 'none' : 'block';
            }
        });
    };
};