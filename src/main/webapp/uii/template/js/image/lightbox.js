let container = null;

const handleEscKey = (event) => {
    if (event.key === 'Escape' && container) {
        const viewer = container.querySelector('.lightbox-viewer');
        const closeButton = viewer.querySelector('[id$="closeLightbox"]');
        closeButton.click();
    }
};

const openLightboxViewer = (triggerEl) => {
    container = triggerEl.closest('.lightbox');

    document.body.style.overflow = 'hidden';
    document.querySelector('.scroll-top').style.display = 'none';

    document.addEventListener('keydown', handleEscKey);
};

const closeLightboxViewer = () => {
    document.body.style.overflow = '';
    document.querySelector('.scroll-top').style.display = 'block';

    document.removeEventListener('keydown', handleEscKey);
};

document.body.addEventListener('click', (event) => {
    if (event.target.classList.contains('lightbox-toggle')) {
        openLightboxViewer(event.target);
    }
    if (event.target.classList.contains('lightbox-close')) {
        closeLightboxViewer();
    }
});