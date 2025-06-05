export const initAccessibilityModeButton = () => {
    const accessibilityButton = document.querySelector('[data-toggle-accessibility-mode]');
    if (!accessibilityButton) {
        return;
    }

    accessibilityButton.addEventListener('click', (event) => {
        // Toggle the accessibility mode class on the body
        document.body.classList.toggle('accessibility-mode');
    });
};