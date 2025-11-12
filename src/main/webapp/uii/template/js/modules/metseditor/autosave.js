let config = null;
let autosaveInterval = null;
let readOnlyMode = null;
let autosaveTimer = null;

const setupAutosave = () => {
    config = JSON.parse(document.getElementById('gwConfig').textContent);
    autosaveInterval = config.autoSaveInterval * 1000 * 60;
    readOnlyMode = config.readOnlyMode === 'true';
};

export const initAutosave = () => {
    if (config === null || autosaveInterval === null || readOnlyMode === null) {
        setupAutosave();
    }

    if (!readOnlyMode) {
        clearInterval(autosaveTimer);
        autosaveTimer = setInterval(() => {
            const saveButton = document.querySelector('[id$="meMenuActionsForm:automaticSave"]');
            if (saveButton) {
                saveButton.click();
            }
        }, autosaveInterval);
    }
};
