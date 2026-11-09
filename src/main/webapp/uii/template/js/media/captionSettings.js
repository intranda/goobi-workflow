/**
 * Caption appearance settings for the video player (EN 301 549, 7.1.4).
 *
 * Options and labels come from the hidden selects with `[data-caption-setting]`
 * in video.xhtml. They are turned into custom panels in the Plyr settings menu.
 * Chosen values are stored as CSS variables on the player container and in
 * localStorage.
 *
 * Plyr has no public API for custom menu items: the markup and IDs
 * (`plyr-settings-<id>-<panel>`) replicate the Plyr 3.8 menu. Re-check on
 * Plyr updates.
 */

const STORAGE_PREFIX = 'goobi.captions.';

const CSS_VARS = {
    color: '--gw-caption-color',
    background: '--gw-caption-bg',
    size: '--gw-caption-size-factor',
};

const readStored = (name) => {
    try {
        return window.localStorage.getItem(STORAGE_PREFIX + name);
    } catch (e) {
        return null;
    }
};

const writeStored = (name, value) => {
    try {
        window.localStorage.setItem(STORAGE_PREFIX + name, value);
    } catch (e) {
        // setting will only persist for this session
    }
};

const el = (tag, attrs = {}, text = null) => {
    const node = document.createElement(tag);
    Object.entries(attrs).forEach(([key, value]) => node.setAttribute(key, value));
    if (text !== null) {
        node.textContent = text;
    }
    return node;
};

const selectedText = (select) => select.options[select.selectedIndex]?.textContent.trim() ?? '';

const labelOf = (select) => select.closest('label')?.querySelector('span')?.textContent.trim() ?? select.dataset.captionSetting;

/** Switches the Plyr menu to the panel `name`. */
const showPanel = (player, name, focusVisible = false) => {
    const target = player.elements.container.querySelector(`#plyr-settings-${player.id}-${name}`);
    if (!target) {
        return;
    }
    Array.from(target.parentNode.children).forEach((panel) => {
        panel.hidden = panel !== target;
    });
    const first = target.querySelector('[role^="menuitem"]');
    if (first) {
        first.focus({ focusVisible });
    }
};

/** Keeps Plyr from treating arrow keys inside the menu as seek/volume. */
const swallowArrowKeys = (node) => {
    node.addEventListener('keydown', (event) => {
        if ([' ', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(event.key)) {
            event.stopPropagation();
        }
    });
};

const buildPanel = (player, select, homeItem, valueLabel) => {
    const name = `caption-${select.dataset.captionSetting}`;
    const title = labelOf(select);
    const classes = player.config.classNames;

    const pane = el('div', { id: `plyr-settings-${player.id}-${name}`, hidden: '' });

    const back = el('button', { type: 'button', class: `${classes.control} ${classes.control}--back` });
    back.append(el('span', { 'aria-hidden': 'true' }, title), el('span', { class: classes.hidden }, player.config.i18n.menuBack));
    back.addEventListener('click', () => showPanel(player, 'home'));
    pane.append(back);

    const list = el('div', { role: 'menu' });
    pane.append(list);

    const items = Array.from(select.options).map((option) => {
        const item = el(
            'button',
            {
                'type': 'button',
                'role': 'menuitemradio',
                'class': classes.control,
                'aria-checked': String(option.value === select.value),
                'value': option.value,
            },
        );
        item.append(el('span', {}, option.textContent.trim()));
        item.addEventListener('click', (event) => {
            event.preventDefault();
            event.stopPropagation();
            select.value = option.value;
            select.dispatchEvent(new Event('change', { bubbles: true }));
            items.forEach((other) => other.setAttribute('aria-checked', String(other === item)));
            valueLabel.textContent = selectedText(select);
            showPanel(player, 'home', event.detail === 0);
        });
        item.addEventListener('keydown', (event) => {
            // Up/down moves between entries, left arrow goes back
            if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                const index = items.indexOf(item) + (event.key === 'ArrowDown' ? 1 : -1);
                items[(index + items.length) % items.length].focus();
            } else if (event.key === 'ArrowLeft') {
                showPanel(player, 'home', true);
            }
        });
        swallowArrowKeys(item);
        list.append(item);
        return item;
    });

    pane.addEventListener('keydown', (event) => {
        if (event.key === 'ArrowLeft') {
            event.preventDefault();
            event.stopPropagation();
            showPanel(player, 'home', true);
        }
    });

    homeItem.addEventListener('click', () => showPanel(player, name));
    homeItem.addEventListener('keydown', (event) => {
        if (event.key === 'ArrowRight') {
            event.preventDefault();
            showPanel(player, name, true);
        }
    });
    swallowArrowKeys(homeItem);

    return pane;
};

/**
 * Adds the caption settings to the Plyr menu.
 * Idempotent: runs once per player.
 */
const buildPlyrMenu = (player, selects) => {
    const settings = player.elements?.settings;
    const home = settings?.panels?.home;
    if (!home || home.dataset.captionMenu === 'true') {
        return;
    }
    home.dataset.captionMenu = 'true';

    const homeMenu = home.querySelector('[role="menu"]');
    const inner = home.parentNode;
    const classes = player.config.classNames;
    const homeItems = [];

    selects.forEach((select) => {
        const item = el('button', {
            'type': 'button',
            'class': `${classes.control} ${classes.control}--forward`,
            'role': 'menuitem',
            'aria-haspopup': 'true',
            'hidden': '',
        });
        const flex = el('span', {}, labelOf(select));
        const valueLabel = el('span', { class: classes.menu.value }, selectedText(select));
        flex.append(valueLabel);
        item.append(flex);
        homeMenu.append(item);
        homeItems.push(item);
        inner.append(buildPanel(player, select, item, valueLabel));
    });

    // Only show the entries if captions exist (Plyr shows its own captions entry then)
    const syncVisibility = () => {
        const hasCaptions = settings.buttons.captions && !settings.buttons.captions.hidden;
        homeItems.forEach((item) => {
            item.hidden = !hasCaptions;
        });
    };
    player.elements.buttons.settings?.addEventListener('click', syncVisibility);
    syncVisibility();
};

/**
 * Initializes the caption settings inside a container.
 * Safe to call repeatedly (e.g. after AJAX updates).
 * @param {HTMLElement} container Element wrapping the selects and the player
 * @param {import('plyr')} [player] Plyr instance for the menu integration
 */
export const initCaptionSettings = (container, player = null) => {
    if (!container) {
        return;
    }
    const selects = Array.from(container.querySelectorAll('select[data-caption-setting]')).filter(
        (select) => CSS_VARS[select.dataset.captionSetting],
    );

    selects.forEach((select) => {
        const name = select.dataset.captionSetting;
        const stored = readStored(name);
        if (stored !== null && Array.from(select.options).some((o) => o.value === stored)) {
            select.value = stored;
        }
        container.style.setProperty(CSS_VARS[name], select.value);

        if (select.dataset.captionBound !== 'true') {
            select.dataset.captionBound = 'true';
            select.addEventListener('change', () => {
                container.style.setProperty(CSS_VARS[name], select.value);
                writeStored(name, select.value);
            });
        }
    });

    if (player) {
        buildPlyrMenu(player, selects);
    }
};
