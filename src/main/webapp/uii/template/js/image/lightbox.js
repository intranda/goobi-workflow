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

    let viewer = container.querySelector('.lightbox-viewer');
    const index = triggerEl.getAttribute('data-index');
    const hiddenInput = container.querySelector('[id$="currentIndex"]');
    hiddenInput.value = index;
    document.body.style.overflow = 'hidden';
    document.querySelector('.scroll-top').style.display = 'none';

    faces.ajax.request(triggerEl, null, {
        execute: hiddenInput.id,
        render: viewer.id,
    });

    document.addEventListener('keydown', handleEscKey);
};

const closeLightboxViewer = () => {
    console.log('Closing lightbox viewer');
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