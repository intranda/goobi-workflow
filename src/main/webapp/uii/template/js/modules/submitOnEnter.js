export const initSubmitOnEnter = () => {
    const elements = document.querySelectorAll('[data-submit-on-enter]');
    if (elements.length === 0) {
        return;
    }
    elements.forEach(element => {
        const targetSelector = element.getAttribute('data-submit-on-enter');
        if (JSON.parse(targetSelector)) {
            element.addEventListener('keydown', () => {
                const targetsObject = JSON.parse(targetSelector);
                if (Object.keys(targetsObject).length === 0) {
                    return;
                }
                Object.keys(targetsObject).forEach((combinatoryKey) => {
                    if (!combinatoryKey || combinatoryKey === 'default') {
                        return;
                    }
                    if (combinatoryKey === 'ctrl') {
                        if (event.ctrlKey) {
                            const targetElement = document.querySelector('[id$="' + targetsObject[combinatoryKey].substring(1) + '"]');
                            targetElement?.classList.add('btn-highlight');
                            document.addEventListener('keydown', (event) => {
                                if (event.key === 'Enter') {
                                    event.preventDefault();
                                    event.stopPropagation();
                                    targetElement?.click();
                                    targetElement?.classList.remove('btn-highlight');
                                }
                            });
                        }
                        return;
                    }
                });
            });
        };
        element.addEventListener('keyup', (event) => {
            if (JSON.parse(targetSelector)) {
                const targetsObject = JSON.parse(targetSelector);
                if (Object.keys(targetsObject).length === 0) {
                    return;
                }
                Object.keys(targetsObject).forEach((combinatoryKey) => {
                    if (!combinatoryKey) {
                        return;
                    }
                    if (combinatoryKey === 'ctrl') {
                        const targetElement = document.querySelector('[id$="' + targetsObject[combinatoryKey].substring(1) + '"]');
                        targetElement?.classList.remove('btn-highlight');
                        return;
                    }

                });
            };
            if (event.key === 'Enter') {
                event.preventDefault();
                event.stopPropagation();
                const targetSelector = element.getAttribute('data-submit-on-enter');
                if (!targetSelector) {
                    return;
                }
                if (targetSelector.startsWith('#')) {
                    const targetElement = document.querySelector('[id$="' + targetSelector.substring(1) + '"]');
                    targetElement?.click();
                    return;
                }
                const targetElement = document.querySelector(targetSelector);
                if (targetElement) {
                    targetElement.click();
                }
            }
        });
    });
};