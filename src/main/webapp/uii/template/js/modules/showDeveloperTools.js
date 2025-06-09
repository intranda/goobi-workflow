export const initShowDevelopertools = () => {
    const toggle = document.querySelector('[data-developer-tool-toggle]');
    const elements = document.querySelectorAll('[data-developer-tool');
    if (toggle && elements.length > 0) {
        toggle.addEventListener('click', (event) => {
            event.preventDefault();
            event.stopPropagation();
            console.debug('Toggling developer tools visibility');
            elements.forEach((element) => {
                element.classList.toggle('d-none');
            });
        });
    };
};