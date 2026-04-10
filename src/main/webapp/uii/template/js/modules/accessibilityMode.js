export const initAccessibilityModeButton = () => {
    const accessibilityButton = document.querySelector('[data-toggle-accessibility-mode]');
    if (!accessibilityButton || accessibilityButton.hasAttribute('data-accessibility-init')) return;
    accessibilityButton.setAttribute('data-accessibility-init', '');
    accessibilityButton.addEventListener('click', (event) => {
        document.body.classList.toggle('accessibility-mode');
    });
};
