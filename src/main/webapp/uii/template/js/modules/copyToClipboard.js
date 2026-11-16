/**
 * Copies text to the clipboard for buttons marked with `data-copy-text`.
 *
 * Markup contract:
 * - `data-copy-text`: the text to copy
 * - `data-copied-label`: message announced after a successful copy
 * - `data-copy-failed-label`: message announced when copying is not possible
 * - `data-feedback-target`: id of an `aria-live` element that receives the message
 *
 * @module copyToClipboard
 */

/**
 * Copies text with the legacy `execCommand`, the only way in contexts without the async clipboard API
 * (the API is limited to secure contexts, but Goobi is often served over plain http).
 *
 * @param {string} text - The text to copy.
 * @returns {boolean} True if the browser reported success.
 */
const copyWithExecCommand = (text) => {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    try {
        return document.execCommand('copy');
    } catch (e) {
        return false;
    } finally {
        textarea.remove();
    }
};

/**
 * Copies text to the clipboard, using the async clipboard API where available.
 *
 * @param {string} text - The text to copy.
 * @returns {Promise<boolean>} Resolves to true if the text was copied.
 */
export const copyToClipboard = async (text) => {
    if (navigator.clipboard?.writeText) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (e) {
            // permission denied or document not focused: try the fallback
        }
    }
    return copyWithExecCommand(text);
};

/**
 * Announces a message in an `aria-live` element. The element is emptied first so that
 * repeating the same message is announced again.
 *
 * @param {HTMLElement|null} target - The live region.
 * @param {string} message - The message to announce.
 */
const announce = (target, message) => {
    if (!target) return;
    target.textContent = '';
    window.setTimeout(() => {
        target.textContent = message;
    }, 50);
};

/**
 * Registers the click handler for `[data-copy-text]` buttons.
 * Uses event delegation so it works after AJAX DOM replacements; call once.
 */
export const initCopyToClipboard = () => {
    document.addEventListener('click', async (event) => {
        const button = event.target.closest('[data-copy-text]');
        if (!button) return;

        const copied = await copyToClipboard(button.dataset.copyText);
        const target = document.getElementById(button.dataset.feedbackTarget);
        announce(target, copied ? button.dataset.copiedLabel : button.dataset.copyFailedLabel);
    });
};
