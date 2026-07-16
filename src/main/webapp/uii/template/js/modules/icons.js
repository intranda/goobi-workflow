/**
 * This modules provides a way to insert icons via JS
 * in contexts where JSF resource resolution is not possible.
 *
 * @module icons
 */

/**
 * Builds the HTML markup for an icon, mirroring the `util:icon` JSF component's output.
 *
 * @param {string} href - URL to the icon's SVG sprite entry, as returned by {@link iconHref}.
 * @param {string} [title=''] - Optional tooltip text; when set, a Bootstrap tooltip is attached.
 * @param {string} [styleClass=''] - Additional CSS class(es) for the wrapping `<span>`.
 * @param {boolean} [double=false] - Whether to render the icon twice, overlapping (double-icon variant).
 * @returns {string} HTML markup for the icon.
 */
const iconHTML = (href = '', title = '', styleClass = '', double = false) => {
    return `
        <span
            class="icon-wrapper${styleClass != '' ? ' ' + styleClass : ''}"
            aria-hidden="true"
            ${title != '' ? `title="${title}" data-bs-toggle="tooltip"` : ''}>
            <svg>
                <use href="${href}" />
            </svg>
            ${double ? `
            <svg style="margin-inline-start: -5px;">
                <use href="${href}" />
            </svg>
            ` : ''}
        </span>
    `;
}

/**
 * Builds the URL for an icon's SVG sprite entry, matching the path used by the `util:icon` JSF component.
 *
 * @param {string} iconName - Name of the icon file, without extension (e.g. "plus").
 * @param {boolean} [filled=false] - Whether to use the filled icon variant instead of outline.
 * @returns {string} URL to the icon's SVG sprite entry, e.g. "/goobi/resources/icons/outline/plus.svg#icon".
 */
export const iconHref = (iconName, filled = false) => {
    return `${faces.contextpath}/resources/icons/${filled ? 'filled' : 'outline'}/${iconName}.svg#icon`;
};

/**
 * Renders an icon as an HTML string, for use where JSF's `util:icon` component isn't available
 * (e.g. inside JS-generated markup).
 *
 * @param {string} iconName - Name of the icon file, without extension (e.g. "plus").
 * @param {Object} [options]
 * @param {string} [options.title=''] - Optional tooltip text; when set, a Bootstrap tooltip is attached.
 * @param {string} [options.styleClass=''] - Additional CSS class(es) for the wrapping `<span>`.
 * @param {boolean} [options.filled=false] - Whether to use the filled icon variant instead of outline.
 * @param {boolean} [options.double=false] - Whether to render the icon twice, overlapping.
 * @returns {string} HTML markup for the icon, or an empty string if `iconName` is falsy.
 */
export const iconEl = (iconName, { title = '', styleClass = '', filled = false, double = false } = {}) => {
    if (!iconName) return '';
    return iconHTML(iconHref(iconName, filled), title, styleClass, double);
};
