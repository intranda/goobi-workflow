import hotkeys from 'hotkeys-js';

export const initHotkeys = () => {
    // Remove existing hotkeys to avoid duplicates
    hotkeys.deleteScope('paginator');
    hotkeys.deleteScope('metseditor');
    hotkeys.deleteScope('global');
    hotkeys.unbind();

    // Set active scope only for pages with paginator
    // This is temporary, as soon as all hotkeys are moved to this module,
    // this can be refactored to a more permanent solution
    const isMetseditor = document.querySelector('#metseditorMenuForm') !== null;
    const isPaginatorOnPage = document.querySelector('.dataTables__paginator') !== null || document.querySelector('[id$="navnext"]') !== null;

    if (isMetseditor) {
        hotkeys.setScope('metseditor');
    } else if (isPaginatorOnPage) {
        hotkeys.setScope('paginator');
    } else {
        hotkeys.setScope('global');
    }

    // Set up help modal with available hotkeys
    initHotkeysHelper();

    // Get config
    const configElement = document.getElementById('gwConfig');
    const goobiWorkflowConfig = configElement ? JSON.parse(configElement.textContent) : {};
    const hotkeysPrefix = goobiWorkflowConfig.navigationShortcut || '';

    // If developer tools are present, add a hotkey to toggle their visibility
    const developerTools = document.querySelectorAll('[data-developer-tool]');
    if (developerTools.length > 0) {
        hotkeys(`${hotkeysPrefix}+d`, (event, handler) => {
            event.preventDefault();
            event.stopPropagation();
            developerTools.forEach((element) => {
                element.classList.toggle('d-none');
            });
        });
    }

    if (hotkeys.getScope() !== 'paginator') return;

    // Define hotkeys
    hotkeys(`${hotkeysPrefix}+right`, 'paginator', () => {
        document.querySelector('[id$="navnext"]')?.click();
    });

    hotkeys(`${hotkeysPrefix}+left`, 'paginator', () => {
        document.querySelector('[id$="navprev"]')?.click();
    });

    hotkeys(`${hotkeysPrefix}+home`, 'paginator', () => {
        document.querySelector('[id$="navfirst"]')?.click();
    });

    hotkeys(`${hotkeysPrefix}+end`, 'paginator', () => {
        document.querySelector('[id$="navlast"]')?.click();
    });
};

/**
 * This function displays any hotkeys that are available in the current view.
 */
const initHotkeysHelper = () => {
    const hotkeyTermsMetseditor = [
        'shortcut_imagePrev20',
        'shortcut_imageNext20',
        'shortcut_imagePrev1',
        'shortcut_imageNext1',
        'shortcut_imageFirst',
        'shortcut_imageLast',
        'shortcut_toggleCheckboxPagination',
        'shortcut_saveMets',
        'shortcut_validate'
    ];
    const hotkeyTermsPaginator = [
        'shortcut_imagePrev1',
        'shortcut_imageNext1',
        'shortcut_imageFirst',
        'shortcut_imageLast',
    ];
    const scope = hotkeys.getScope();
    const hotkeyHelpButton = document.querySelector('#shortcutsHelpButton');
    if (hotkeyHelpButton) {
        hotkeyHelpButton.classList.remove('d-none');
    }

    let visibleHotkeys = 0;
    if (scope === 'metseditor') {
        hotkeyTermsMetseditor.forEach(term => { visibleHotkeys += showHotkeyHelpers(term); });
    } else if (scope === 'paginator') {
        hotkeyTermsPaginator.forEach(term => { visibleHotkeys += showHotkeyHelpers(term); });
    }

    // The developer tools hotkey is registered in every scope, but only on pages that offer them
    if (document.querySelectorAll('[data-developer-tool]').length > 0) {
        visibleHotkeys += showHotkeyHelpers('shortcut_toggleDeveloperTools');
    }

    // Tell the user when the current view has no hotkeys at all
    const emptyHint = document.querySelector('#shortcutsHelpEmpty');
    if (emptyHint) {
        emptyHint.classList.toggle('d-none', visibleHotkeys > 0);
    }
};

const showHotkeyHelpers = (hotkey) => {
    const hotkeyElement = document.querySelector(`[data-shortcut-helper="${hotkey}"]`);
    if (!hotkeyElement) {
        return 0;
    }
    const descriptionElement = hotkeyElement.parentElement;
    const termElement = descriptionElement.previousElementSibling;
    termElement.classList.remove('d-none');
    descriptionElement.classList.remove('d-none');
    return 1;
};