// The decision tree box of a task (decisiontree:box): draws the lines between the answers of a family-tree layout, stacks rows that do not fit,
// and after a JSF ajax update moves the focus to the new question and announces the state. The markup is a plain ordered list of questions with
// their answers; without this module it stays usable, only the lines and the centring are missing.

const INDENT = 28; // indent of answers that stand below each other
const PAD = 6;

let liveRegion = null;
let pendingFormId = null;
let listening = false;

// The answers are moved with `left` (position: relative), because a margin beside the floated question would be swallowed by the float.
/** Positions the answers of every row and (re)draws the connecting lines of one tree. */
function layout(tree) {
    const rows = [...tree.children].filter((el) => el.matches('.dt-row'));
    const answers = rows.map((row) => row.querySelector('.dt-answers'));
    if (rows.length === 0 || answers.some((el) => !el)) {
        return;
    }
    rows.forEach((row, i) => {
        row.classList.remove('is-stack');
        answers[i].style.left = '0px';
        answers[i].style.maxWidth = '';
    });
    const origin = tree.getBoundingClientRect();
    const available = origin.width - PAD;
    const mid = (el) => {
        const b = el.getBoundingClientRect();
        return b.left + b.width / 2 - origin.left;
    };
    const chosen = (i) => rows[i].querySelector('.dt-answer.is-chosen');
    const place = (i, center) => {
        const natural = answers[i].getBoundingClientRect().left - origin.left;
        const left = Math.min(center - answers[i].offsetWidth / 2, available - answers[i].offsetWidth);
        answers[i].style.left = Math.max(0, left - natural) + 'px';
    };
    const stack = (i, parentLeft) => {
        rows[i].classList.add('is-stack');
        const left = Math.max(0, Math.min(parentLeft - PAD + INDENT, available - PAD - 140));
        answers[i].style.left = left + 'px';
        answers[i].style.maxWidth = `calc(100% - ${left}px)`;
    };
    for (let i = 1; i < rows.length; i++) {
        const parent = chosen(i - 1);
        if (rows[i].offsetWidth > available - PAD) {
            stack(i, parent ? parent.getBoundingClientRect().left - origin.left : 0);
        } else if (parent) {
            place(i, mid(parent));
        }
    }
    if (rows.length > 1) {
        if (rows[1].classList.contains('is-stack')) {
            rows[0].classList.add('is-stack');
        } else {
            place(0, mid(answers[1]));
        }
    }
    drawLinks(tree, rows, origin);
}

/** Lines from the chosen answer of a row to every answer of the next row, as an svg behind the boxes (decorative). */
function drawLinks(tree, rows, origin) {
    tree.querySelector(':scope > .dt-links')?.remove();
    let light = '';
    let dark = '';
    const f = (n) => n.toFixed(1);
    for (let i = 1; i < rows.length; i++) {
        const from = rows[i - 1].querySelector('.dt-answer.is-chosen');
        if (!from) {
            continue;
        }
        const a = from.getBoundingClientRect();
        const parentStacked = rows[i - 1].classList.contains('is-stack');
        const stacked = rows[i].classList.contains('is-stack');
        const ys = a.top + a.height / 2 - origin.top;
        const xl = a.left - origin.left;
        const xs = xl - 10;
        rows[i].querySelectorAll('.dt-answer').forEach((to) => {
            const b = to.getBoundingClientRect();
            let d;
            if (stacked) {
                const yc = b.top + b.height / 2 - origin.top;
                const x2 = b.left - origin.left;
                d = parentStacked
                    ? `M${f(xl)} ${f(ys)}H${f(xs)}V${f(yc)}H${f(x2)}`
                    : `M${f(xl + 14)} ${f(a.bottom - origin.top)}V${f(yc)}H${f(x2)}`;
            } else {
                const x2 = b.left + b.width / 2 - origin.left;
                const y2 = b.top - origin.top;
                if (parentStacked) {
                    d = `M${f(xl)} ${f(ys)}H${f(xs)}V${f(y2 - 14)}H${f(x2)}V${f(y2)}`;
                } else {
                    const x1 = a.left + a.width / 2 - origin.left;
                    const y1 = a.bottom - origin.top;
                    d = `M${f(x1)} ${f(y1)}V${f((y1 + y2) / 2)}H${f(x2)}V${f(y2)}`;
                }
            }
            const path = `<path class="${to.classList.contains('is-chosen') ? 'is-path' : ''}" d="${d}"/>`;
            if (to.classList.contains('is-chosen')) {
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
