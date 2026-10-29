// The decision tree box of a task (decisiontree:box): draws the lines between the answers of a family-tree layout, switches to a compact list if it does not fit,
// and after a JSF ajax update moves the focus to the new question and announces the state. The markup is a plain ordered list of questions with
// their answers; without this module it stays usable, only the lines and the centring are missing.

const PAD = 6;

let liveRegion = null;
let pendingFormId = null;
let listening = false;

// The answers are moved with `left` (position: relative), because a margin beside the floated question would be swallowed by the float.
/**
 * Lays out one tree. If its natural width (question column plus all answers of a row next to each other) does not fit the box, the whole tree
 * switches to the compact form (`is-compact`): no lines, no centring, every question with its answers wrapped below it, like a list. Otherwise the
 * answers of every row are centred under the chosen answer above and the connecting lines are drawn.
 */
function layout(tree) {
    const rows = [...tree.children].filter((el) => el.matches('.dt-row'));
    const answers = rows.map((row) => row.querySelector('.dt-answers'));
    if (rows.length === 0 || answers.some((el) => !el)) {
        return;
    }
    tree.classList.remove('is-compact');
    tree.querySelector(':scope > .dt-links')?.remove();
    answers.forEach((el) => {
        el.style.left = '0px';
    });
    const origin = tree.getBoundingClientRect();
    const style = getComputedStyle(tree);
    const available = origin.width - parseFloat(style.paddingLeft) - parseFloat(style.paddingRight);
    // natural width of a row: the question column plus all answers next to each other (the answers are max-content wide); hints may wrap
    const natural = (row, i) => {
        const legend = row.querySelector('.dt-legend');
        const margin = legend ? parseFloat(getComputedStyle(legend).marginRight) : 0;
        return (legend ? legend.offsetWidth + margin : 0) + answers[i].offsetWidth;
    };
    if (rows.some((row, i) => natural(row, i) > available - PAD)) {
        tree.classList.add('is-compact');
        return;
    }
    const mid = (el) => {
        const b = el.getBoundingClientRect();
        return b.left + b.width / 2 - origin.left;
    };
    const place = (i, center) => {
        const natural = answers[i].getBoundingClientRect().left - origin.left;
        const left = Math.min(center - answers[i].offsetWidth / 2, origin.width - parseFloat(style.paddingRight) - answers[i].offsetWidth);
        answers[i].style.left = Math.max(0, left - natural) + 'px';
    };
    for (let i = 1; i < rows.length; i++) {
        const parent = rows[i - 1].querySelector('.dt-answer.is-chosen');
        if (parent) {
            place(i, mid(parent));
        }
    }
    if (rows.length > 1) {
        place(0, mid(answers[1]));
    }
    drawLinks(tree, rows, origin);
}

/** Lines from the chosen answer of a row to every answer of the next row, as an svg behind the boxes (decorative). */
function drawLinks(tree, rows, origin) {
    let light = '';
    let dark = '';
    const f = (n) => n.toFixed(1);
    for (let i = 1; i < rows.length; i++) {
        const from = rows[i - 1].querySelector('.dt-answer.is-chosen');
        if (!from) {
            continue;
        }
        const a = from.getBoundingClientRect();
        const x1 = a.left + a.width / 2 - origin.left;
        const y1 = a.bottom - origin.top;
        rows[i].querySelectorAll('.dt-answer').forEach((to) => {
            const b = to.getBoundingClientRect();
            const x2 = b.left + b.width / 2 - origin.left;
            const y2 = b.top - origin.top;
            const chosen = to.classList.contains('is-chosen');
            const path = `<path class="${chosen ? 'is-path' : ''}" d="M${f(x1)} ${f(y1)}V${f((y1 + y2) / 2)}H${f(x2)}V${f(y2)}"/>`;
            if (chosen) {
                dark += path;
            } else {
                light += path;
            }
        });
    }
    tree.insertAdjacentHTML('afterbegin', `<svg class="dt-links" aria-hidden="true" focusable="false" width="${origin.width}" height="${origin.height}">${light}${dark}</svg>`);
}

function announce(text) {
    if (!liveRegion) {
        liveRegion = document.createElement('div');
        liveRegion.className = 'visually-hidden';
        liveRegion.setAttribute('role', 'status');
        liveRegion.setAttribute('aria-live', 'polite');
        document.body.appendChild(liveRegion);
    }
    liveRegion.textContent = '';
    // a changed text is announced, so clear first and set in the next frame
    requestAnimationFrame(() => {
        liveRegion.textContent = text;
    });
}

/** After an ajax update of a decision tree form: focus the element the server marked, and announce the state. */
function afterUpdate(formId) {
    const form = formId ? document.getElementById(formId) : null;
    if (!form) {
        return;
    }
    const target = form.querySelector('[data-focus-target="true"]');
    const status = form.querySelector('[data-dt-status]')?.dataset.dtStatus ?? '';
    target?.focus({ preventScroll: true });
    target?.scrollIntoView({ block: 'nearest', behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' });
    const question = target?.matches('legend') ? target.textContent.trim() : '';
    announce(question ? `${status}: ${question}` : status);
}

function listenToAjax() {
    if (listening || typeof faces === 'undefined') {
        return;
    }
    listening = true;
    faces.ajax.addOnEvent((data) => {
        if (data.status === 'begin') {
            // the source is replaced by the update, so the form is remembered by its id
            const form = data.source?.closest?.('form');
            pendingFormId = form?.querySelector('[data-dt]') ? form.id : null;
        } else if (data.status === 'success' && pendingFormId) {
            const id = pendingFormId;
            pendingFormId = null;
            afterUpdate(id);
        }
    });
}

export const initDecisionTree = () => {
    listenToAjax();
    document.querySelectorAll('.dt-tree:not([data-dt-init])').forEach((tree) => {
        tree.dataset.dtInit = 'true';
        const redraw = () => layout(tree);
        new ResizeObserver(redraw).observe(tree);
        document.fonts?.ready.then(redraw);
        redraw();
    });
};
