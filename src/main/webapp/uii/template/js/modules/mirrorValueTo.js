export const initMirrorValueTo = () => {
    const elements = document.querySelectorAll('[data-mirror-value-to]');
    if (elements.length === 0) {
        return;
    }
    elements.forEach(element => {
        element.addEventListener('input', () => {
            const targetSelectors = JSON.parse(element.getAttribute('data-mirror-value-to'));
            if (!targetSelectors || targetSelectors.length === 0) {
                return;
            }
            targetSelectors.forEach(targetSelector => {
                if (!targetSelector) {
                    return;
                }
                if (targetSelector.startsWith('#')) {
                    const targetElement = document.querySelector('[id$="' + targetSelector.substring(1) + '"]');
                    if (targetElement) {
                        targetElement.value = element.value;
                    }
                    return;
                }
                const targetElement = document.querySelector(targetSelector);
                if (targetElement) {
                    targetElement.value = element.value;
                }
            });
        });
    });
};