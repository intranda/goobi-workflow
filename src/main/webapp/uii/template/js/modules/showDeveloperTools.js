export const initShowDevelopertools = () => {
    const elements = document.querySelectorAll('[data-developer-tool');
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Alt' || event.key === 'Control') {
            document.addEventListener('keydown', (event) => {
                if (event.key === 'Alt' || event.key === 'Control') {
                    document.addEventListener('keyup', (e) => {
                        if (e.key === 'd') {
                            e.preventDefault();
                            e.stopPropagation();
                            elements.forEach((element) => {
                                element.classList.toggle('d-none');
                            });
                        }
                    }, { once: true });
                }
            });
        };
    }, { once: true });
};