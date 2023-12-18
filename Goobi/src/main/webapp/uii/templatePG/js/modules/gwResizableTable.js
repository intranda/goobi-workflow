/**
 * This script allows users to resize the column widths of tables.
 *
 * REQUIREMENTS:
 * 1. the table MUST have the class 'table-resizable' for the script to identify it.
 * 2. the table MUST have an id for resizes to be persistent. Changes are saved to localStorage and
 *    looked up by table id.
 * 3. columns MUST have an id if their widths are being saved.
 * 4. the table MUST NOT have a fixed layout. If you can shrink columns so far you can't read the
 *    contents, most likely its styling is set to `table-layout: fixed`. Remove this style or
 *    the corresponding class.
 */

/**
 * Ensures that an element's width does not exceed min-width or max-width, if these are defined.
 * @param {HTMLTableCellElement} col A column of the resizable table
 * @param {number} newWidth The new width for the column
 * @returns {number}
 */
const constrainWidth = function constrainWidth(col, newWidth) {
    let constrainedWidth;
    const maxWidth = getComputedStyle(col).getPropertyValue('max-width');
    let minWidth = getComputedStyle(col).getPropertyValue('min-width');

    minWidth = minWidth === "0px" ? "60px" : minWidth;
    constrainedWidth = Math.max(parseInt(minWidth, 10), newWidth)

    if (maxWidth !== 'none') {
        constrainedWidth = Math.min(parseInt(maxWidth, 10), newWidth)
    }

    return constrainedWidth;
};

/**
 * Adapts the height of the resize handle so it stays the same as the height of the relevant table.
 * @param {HTMLDivElement} handle The resize handle
 */
const resizeHandles = function resizeHandles(col) {
    const table = col.closest('.table-resizable');
    const tableHeight = table.offsetHeight;
    const handles = col.parentElement.querySelectorAll('.resize-handle');
    [...handles].forEach((handle) => {
        handle.style.setProperty('--handle-height', `${tableHeight}px`);
    });
};

/**
 * Get a column's relative width in the table.
 * @param {HTMLTableCellElement} col A column of the resizable table
 * @returns The relative width
 */
const getRelWidth = function getRelativeWidth(col) {
    const table = col.closest('.table-resizable');
    const tableWidth = table.offsetWidth;
    const relWidth = (col.offsetWidth / tableWidth) * 100;
    return relWidth.toFixed(2);
};

/**
 * Save the current column widths to localStorage.
 * @param {HTMLTableCellElement} col A column of the resizable table
 */
const saveColWidths = function saveColWidths(col) {
    const table = col.closest('.table-resizable');
    if (!table.id) return;
    const firstRow = table.getElementsByTagName('tr')[0];
    const colWidths = [];
    const cols = firstRow ? firstRow.children : undefined;
    if (!cols) return;
    [...cols].forEach((c) => {
        if (c.id) {
            const colInfo = {
                id: c.id,
                width: `${getRelWidth(c)}%`,
            };
            colWidths.push(colInfo);
        }
    });
    localStorage.setItem(table.id, JSON.stringify(colWidths));
};

/**
 * Load and implement column widths from localStorage, if any are present for the current table.
 * @param {HTMLTableElement} table A resizable table
 */
const loadColWidths = function loadColWidths(table) {
    const storage = JSON.parse(localStorage.getItem(table.id));
    if (storage) {
        storage.forEach((col) => {
            try {
                document.getElementById(col.id).style.width = col.width;
                return 0;
            } catch (error) {
                return 1;
            }
        });
    }
};

/**
 * Set a column's width as a percentage.
 * @param {HTMLTableCellElement} col A column of the resizable table
 */
const setRelWidth = function setRelativeWidth(col) {
    const column = col;
    column.style.width = `${getRelWidth(col)}%`;
};

/**
 * Create a resize handle for a table column
 * @param {number} height The height of the table
 * @returns {HTMLDivElement}
 */
const createHandle = function createHandle() {
    const div = document.createElement('div');
    div.classList.add('resize-handle');
    return div;
};

/**
 * Attaches the necessary events to the resize handle.
 * @param {number} tableWidth The width of the table
 * @param {HTMLDivElement} handle The resize handle
 */
const setListeners = function setListeners(tableWidth, handle) {
    let pageX;
    let col;
    let colWidth;
    let newWidth;
    let nextCol;
    let nextColWidth;
    let nextColNewWidth;
    let diffX;
    let totalWidth;
    handle.addEventListener('mousedown', (e) => {
        col = e.target.parentElement;
        pageX = e.pageX;
        colWidth = col.offsetWidth;
        nextCol = col.nextElementSibling;
        nextColWidth = nextCol.offsetWidth;
        totalWidth = colWidth + nextColWidth;
    });

    document.addEventListener('mousemove', (e) => {
        if (col) {
            diffX = e.pageX - pageX;
            if (diffX === 0) {
                return;
            }

            handle.classList.add('active');
            resizeHandles(col);
            // make the cursor style global so that it stays as resize handle regardless of position
            document.getElementsByTagName('body')[0].classList.add('table-resizing');

            nextColNewWidth = nextColWidth - diffX;
            nextColNewWidth = constrainWidth(nextCol, nextColNewWidth);

            newWidth = colWidth + diffX;
            newWidth = constrainWidth(col, newWidth);

            if (totalWidth === (newWidth + nextColNewWidth)) {
                newWidth = (newWidth / tableWidth) * 100;
                nextColNewWidth = (nextColNewWidth / tableWidth) * 100;

                col.style.width = `${newWidth.toFixed(2)}%`;
                nextCol.style.width = `${nextColNewWidth.toFixed(2)}%`;
            }
        }
    });

    document.addEventListener('mouseup', () => {
        if (col) {
            handle.classList.remove('active');
            saveColWidths(col);
        }
        document.getElementsByTagName('body')[0].classList.remove('table-resizing');
        pageX = undefined;
        col = undefined;
        colWidth = undefined;
        newWidth = undefined;
        nextCol = undefined;
        nextColWidth = undefined;
        nextColNewWidth = undefined;
        diffX = undefined;
        totalWidth = undefined;
    });
};

/**
 * Prepares a table to make it resizable and calls all necessary functions
 * @param {HTMLTableElement} table A resizable table
 */
const initialize = function initializeResizeTable(table) {
    const firstRow = table.getElementsByTagName('tr')[0];
    const cols = firstRow ? firstRow.children : undefined;
    if (!cols) return;
    [...cols].forEach((col) => {
        const column = col;
        const tableWidth = table.offsetWidth;
        const handle = createHandle();
        resizeHandles(col);
        setRelWidth(col);
        column.appendChild(handle);
        column.classList.add('resize-col');
        setListeners(tableWidth, handle);
    });
};

/**
 * Initialize resizable tables if there are any.
 */
const setup = function setupResizableTables() {
    const tables = document.getElementsByClassName('table-resizable');
    if (tables) {
        [...tables].forEach((table) => {
            loadColWidths(table);
            initialize(table);
        });
    }
};

const gwResizableTable = (() => {
    function init() {
        setup();
    }
    function reload(data = 'none') {
        if (data.status === 'success') {
            setup();
        }
    }

    return {
        init,
        reload,
    };
})();
