export const initFocusOnClick = () => {
    const elements = document.querySelectorAll('[data-focus-on-click]');
    if (elements.length === 0) {
        return;
    }
    elements.forEach(element => {
        element.addEventListener('click', () => {
            const targetSelector = element.getAttribute('data-focus-on-click');
            if (!targetSelector) {
                return;
            }
            if (targetSelector.startsWith('#')) {
                const targetElement = document.querySelector('[id$="' + targetSelector.substring(1) + '"]');
                targetElement?.focus();
                return;
            }
            const targetElement = document.querySelector(targetSelector);
            if (targetElement) {
                targetElement.focus();
            }
        });
    });
};