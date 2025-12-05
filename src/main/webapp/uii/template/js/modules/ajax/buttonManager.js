/**
 * Manages AJAX button disable states
 * @class AjaxButtonManager
 *
 * Usage:
 * - Add data-disable-on-ajax="self|form|group" to buttons
 * - When using "group", also add data-ajax-group="groupName"
 *
 * Values for data-disable-on-ajax:
 * - 'self': Disable only the clicked button
 * - 'form': Disable all buttons in the same form with data-disable-on-ajax="form"
 * - 'group': Disable all buttons with the same data-ajax-group value
 * - No attribute: No automatic disabling
 */
class AjaxButtonManager {
    static ATTR_DISABLE_ON_AJAX = 'data-disable-on-ajax';
    static ATTR_AJAX_GROUP = 'data-ajax-group';
    static ATTR_PROCESSING = 'data-ajax-processing';

    constructor() {
        this.initialized = false;
        this.init();
    }

    /**
     * Initialize AJAX button management
     */
    init() {
        if (this.initialized) return;

        if (typeof faces !== 'undefined' && faces.ajax) {
            faces.ajax.addOnEvent(this.handleAjaxEvent.bind(this));
            this.initialized = true;
        }
    }

    /**
     * Handle Jakarta Faces AJAX events
     *
     * @param {*} data
     */
    handleAjaxEvent(data) {
        const status = data.status;
        const source = data.source;

        if (!source) return;

        if (status === 'begin') {
            this.handleAjaxBegin(source);
        } else if (status === 'complete') {
            this.handleAjaxComplete(source);
        }
    }

    /**
     * Handle the AJAX begin event
     *
     * @param {HTMLElement} sourceElement
     */
    handleAjaxBegin(sourceElement) {
        const disableStrategy = sourceElement.getAttribute(AjaxButtonManager.ATTR_DISABLE_ON_AJAX);

        if (!disableStrategy) return;

        const buttonsToDisable = this.getRelevantButtons(sourceElement, disableStrategy);

        this.disableButtons(buttonsToDisable);
    }

    /**
     * Handle the AJAX complete event
     *
     * @param {HTMLElement} sourceElement
     */
    handleAjaxComplete(sourceElement) {
        const disableStrategy = sourceElement.getAttribute(AjaxButtonManager.ATTR_DISABLE_ON_AJAX);

        if (!disableStrategy) return;

        const buttonsToEnable = this.getRelevantButtons(sourceElement, disableStrategy);
        this.enableButtons(buttonsToEnable);
    }

    /**
     * Get relevant buttons based on strategy
     *
     * @param {HTMLElement} sourceElement
     * @param {string} strategy
     * @returns {HTMLElement[]} Array of relevant buttons
     */
    getRelevantButtons(sourceElement, strategy) {
        switch (strategy) {
            case 'self':
                return [sourceElement];
            case 'form':
                return this.getFormButtons(sourceElement);
            case 'group':
                return this.getGroupButtons(sourceElement);
            default:
                return [];
        }
    }

    /**
     * Get all buttons in the same form as the source element
     *
     * @param {HTMLElement} sourceElement
     * @returns {HTMLElement[]} Array of buttons in the form
     */
    getFormButtons(sourceElement) {
        const form = sourceElement.closest('form');

        if (!form) return [];

        const buttons = form.querySelectorAll('button, input[type="button"], input[type="submit"]');
        return Array.from(buttons);
    }

    /**
     * Get all buttons with the same data-ajax-group attribute as the source element
     *
     * @param {HTMLElement} sourceElement
     * @returns {HTMLElement[]} Array of buttons in the group
     */
    getGroupButtons(sourceElement) {
        const groupName = sourceElement.getAttribute(AjaxButtonManager.ATTR_AJAX_GROUP);

        if (!groupName) return [];

        const buttons = document.querySelectorAll(`[${AjaxButtonManager.ATTR_AJAX_GROUP}="${groupName}"]`);
        return Array.from(buttons);
    }

    /**
     * Disable buttons
     *
     * @param {HTMLElement[]} buttons
     */
    disableButtons(buttons) {
        buttons.forEach(btn => {
            if (!btn.disabled) {
                btn.disabled = true;
                btn.setAttribute(AjaxButtonManager.ATTR_PROCESSING, 'true');
            }
        });
    }

    /**
     * Enable buttons
     *
     * @param {HTMLElement[]} buttons
     */
    enableButtons(buttons) {
        console.log('Enabling buttons:', buttons);
        buttons.forEach(btn => {
            if (btn.getAttribute(AjaxButtonManager.ATTR_PROCESSING) === 'true') {
                btn.disabled = false;
                btn.removeAttribute(AjaxButtonManager.ATTR_PROCESSING)
            }
        });
    }
};

export { AjaxButtonManager };