/**
 * This module handles submitting specific action when the Enter key is pressed.
 *
 * Usage:
 * - Add the attribute `data-submit-on-enter` to the input element
 * - Specify targets either with a direct CSS selector or a JSON object for more complex behavior
 * - Simple Usage: specify the target button using a valid CSS selector for an ID or class, such as `#targetBtn`
 * - Complex Usage: provide a JSON object with specific keys for different actions, such as:
 *   {
 *       "default": "#targetBtn",
 *       "ctrl": "#otherBtn"
 *   }
 * - Keep in mind that valid JSON in JSF xhtml requires escaping double quotes:
 *   data-submit-on-enter="{&quot;default&quot;: &quot;#targetBtn&quot;, &quot;ctrl&quot;: &quot;#otherBtn&quot;}"
 *
 * @module submitOnEnter
 */

const findTargetElement = (target) => {
    const selectorType = target.substring(0, 1);
    const targetId = target.substring(1);
    if (selectorType === '.') {
        return document.querySelector('.' + targetId);
    }
    return document.querySelector('[id$="' + targetId + '"]');
};

const preventDefaultEnter = (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        event.stopPropagation();
    }
};

const handleSimpleKeyDown = (event) => {
    preventDefaultEnter(event);
};

const handleSimpleKeyUp = (event, targetSelector) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        event.stopPropagation();
        const targetElement = findTargetElement(targetSelector);
        if (targetElement) {
            targetElement.click();
        }
    }
};

const parseTargetsObject = (targetSelector) => {
    try {
        const parsedTargets = JSON.parse(targetSelector);
        if (Object.keys(parsedTargets).length === 0) {
            return null;
        }
        return parsedTargets;
    } catch (e) {
        console.error('Error parsing data-submit-on-enter JSON:', e);
        return null;
    }
};

const handleComplexKeyDown = (event, targetSelector, state) => {
    event.preventDefault();
    event.stopPropagation();
    const targetsObject = parseTargetsObject(targetSelector);
    if (!targetsObject) {
        return;
    }

    if (event.ctrlKey && targetsObject.ctrl) {
        state.combinatoryKeyPressed = true;
        state.combinatoryTargetEl = findTargetElement(targetsObject.ctrl);
        state.combinatoryTargetEl?.classList.add('btn-highlight');
    }
};

const handleCombinatoryRelease = (state) => {
    state.combinatoryKeyPressed = false;
    state.combinatoryTargetEl?.classList.remove('btn-highlight');
    state.combinatoryTargetEl = null;
};

const handleCombinatoryEnter = (event, targetsObject, state) => {
    event.preventDefault();
    event.stopPropagation();
    if (state.combinatoryTargetEl) {
        state.combinatoryTargetEl.click();
        state.combinatoryTargetEl.classList.remove('btn-highlight');
    }
    handleCombinatoryRelease(state);
};

const handleDefaultEnterSubmit = (event, targetsObject) => {
    event.preventDefault();
    event.stopPropagation();
    const targetElement = findTargetElement(targetsObject.default);
    if (targetElement) {
        targetElement.click();
    }
};

const handleComplexKeyUp = (event, targetSelector, state) => {
    const targetsObject = parseTargetsObject(targetSelector);
    if (!targetsObject) {
        return;
    }

    if (event.key === 'Control' || event.key === 'Meta') {
        handleCombinatoryRelease(state);
    } else if (event.key === 'Enter' && state.combinatoryKeyPressed && targetsObject.ctrl) {
        handleCombinatoryEnter(event, targetsObject, state);
    } else if (event.key === 'Enter' && !state.combinatoryKeyPressed && targetsObject.default) {
        handleDefaultEnterSubmit(event, targetsObject);
    }
};

const attachEventListeners = (element, targetSelector) => {
    const state = {
        combinatoryKeyPressed: false,
        combinatoryTargetEl: null
    };

    const idSelector = targetSelector.startsWith('#');

    element.addEventListener('keydown', (event) => {
        if (idSelector) {
            handleSimpleKeyDown(event);
        } else {
            handleComplexKeyDown(event, targetSelector, state);
        }
    });

    element.addEventListener('keyup', (event) => {
        if (idSelector) {
            handleSimpleKeyUp(event, targetSelector);
        } else {
            handleComplexKeyUp(event, targetSelector, state);
        }
    });
};

export const initSubmitOnEnter = () => {
    const elements = document.querySelectorAll('[data-submit-on-enter]');
    if (elements.length === 0) {
        return;
    }

    elements.forEach(element => {
        const targetSelector = element.getAttribute('data-submit-on-enter');
        attachEventListeners(element, targetSelector);
    });
};