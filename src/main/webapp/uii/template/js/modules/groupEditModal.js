/*
 * This module persists the state of the group edit modal across ajax requests
 *
 * @module groupEditModal
 */
let modalShouldStayOpen = false;
let listenersRegistered = false;

const openModal = () => {
    modalShouldStayOpen = true;
    const dialogEl = document.getElementById('meGroupEditModal');
    if (dialogEl && !dialogEl.open) {
        dialogEl.showModal();
    }
};

export const initGroupEditModal = () => {
    window.meGroupModalOpen = openModal;

    if (listenersRegistered) {
        return;
    }
    listenersRegistered = true;

    document.addEventListener('close', (event) => {
        if (event.target && event.target.id === 'meGroupEditModal') {
            modalShouldStayOpen = false;
            document.getElementById('meGroupModalCloseSync')?.click();
        }
    }, true);

    if (typeof faces === 'undefined') {
        return;
    }

    faces.ajax.addOnEvent((data) => {
        if (data.status === 'success' && modalShouldStayOpen) {
            openModal();
        }
    });

    faces.ajax.addOnError((data) => {
        if (modalShouldStayOpen) {
            console.error('[group-edit-modal] ajax error while modal was open:', data.errorName, data.errorMessage, data.responseText);
        }
    });
};
