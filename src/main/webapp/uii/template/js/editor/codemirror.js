/**
 * Initialize CodeMirror instances for text areas with the `data-codemirror-target` attribute.
 * Any instance needs a corresponding input field with the `data-codemirror-reference` attribute which receives the base64 encoded content.
 * This reference input field is updated whenever the CodeMirror instance changes and used in the backend to determine whether the content has changed.
 *
 * @module codemirror
 */
import { basicSetup } from "codemirror";
import {
    EditorState,
    StateField,
    StateEffect,
} from "@codemirror/state";
import {
    EditorView,
    Decoration,
} from "@codemirror/view";
import { indentUnit } from "@codemirror/language";
import { javascript } from "@codemirror/lang-javascript";
import { xml } from "@codemirror/lang-xml";
import { json } from "@codemirror/lang-json";
import { markdown } from "@codemirror/lang-markdown";
import { yaml } from "@codemirror/lang-yaml";
import { properties } from './codemirror/properties.js';
import { initScrollToLine } from "./codemirror/scrollToLine.js";

// Global view reference for line highlighting and scrolling
let view;

// Track existing CodeMirror instances to prevent duplicates
const editorInstances = new Map();

// Language mapping configuration
const LANGUAGE_MAP = {
    javascript: javascript(),
    xml: xml(),
    json: json(),
    markdown: markdown(),
    yaml: yaml(),
    properties: properties(),
};

// Line highlighting state management
const addLineHighlight = StateEffect.define();
const lineHighlight = Decoration.line({
    attributes: { style: 'background-color: yellow;' },
});

const highlightStateField = StateField.define({
    create() {
        return Decoration.none;
    },
    update(lines, tr) {
        lines = lines.map(tr.changes);
        for (let effect of tr.effects) {
            if (effect.is(addLineHighlight)) {
                // Reset previous highlights and add new one
                lines = Decoration.none;
                lines = lines.update({ add: [lineHighlight.range(effect.value)] });
            }
        }
        return lines;
    },
    provide: (f) => EditorView.decorations.from(f),
});

/**
 * Encodes a string to base64 with proper Unicode handling
 * @param {string} str - The string to encode
 * @returns {string} Base64 encoded string
 */
const base64EncodeUnicode = (str) => {
    // First, escape the string using encodeURIComponent to get the UTF-8 encoding of the characters
    // Then, convert the percent encodings into raw bytes for btoa() function
    const utf8Bytes = encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (match, p1) => {
        return String.fromCharCode('0x' + p1);
    });
    return btoa(utf8Bytes);
};

/**
 * Updates the reference input field with base64 encoded content
 * @param {string} textAreaId - The ID of the original textarea
 * @param {string} value - The current content value
 */
const updateReferenceInput = (textAreaId, value) => {
    const escapedId = CSS.escape(textAreaId);
    const referenceInput = document.querySelector(`[data-codemirror-reference="${escapedId}"]`);
    if (referenceInput) {
        referenceInput.value = base64EncodeUnicode(value);
    }
};

/**
 * Gets the appropriate language extension based on the language string
 * @param {string} language - The language identifier
 * @returns {Extension} CodeMirror language extension
 */
const getLanguageExtension = (language) => {
    return LANGUAGE_MAP[language] || LANGUAGE_MAP.xml; // Default to XML
};

/**
 * Normalizes the language string from data attribute
 * @param {string} targetLanguage - The raw language string from data attribute
 * @returns {string} Normalized language string
 */
const normalizeLanguage = (targetLanguage) => {
    if (targetLanguage === 'text/x-properties') {
        return 'properties';
    }
    return targetLanguage?.toLowerCase() || 'xml';
};

/**
 * Creates a CodeMirror editor from a textarea element
 * @param {HTMLTextAreaElement} textArea - The textarea to replace with CodeMirror
 * @param {string} language - The language mode to use
 * @returns {EditorView} The created CodeMirror editor view
 */
const createEditorFromTextArea = (textArea, language = 'xml') => {
    const shouldUpdateSelf = textArea.getAttribute('data-codemirror-update-self') === 'true';

    const editorView = new EditorView({
        state: EditorState.create({
            doc: textArea.value,
            extensions: [
                basicSetup,
                getLanguageExtension(language),
                EditorView.updateListener.of((update) => {
                    if (update.docChanged) {
                        updateReferenceInput(textArea.id, update.state.doc.toString());
                    }
                }),
                indentUnit.of('    '), // Set indentation to 4 spaces
                highlightStateField,
                // Add blur event listener if update-self is enabled
                ...(shouldUpdateSelf ? [EditorView.domEventHandlers({
                    blur: (event, view) => {
                        // Update the original textarea with current editor content
                        textArea.value = view.state.doc.toString();

                        // Trigger change event on textarea for any listeners
                        const changeEvent = new Event('change', { bubbles: true });
                        textArea.dispatchEvent(changeEvent);

                        return false; // Allow default blur behavior
                    }
                })] : []),
            ],
        }),
        parent: textArea.parentNode
    });

    // Insert editor before textarea and hide the original
    textArea.parentNode.insertBefore(editorView.dom, textArea);
    textArea.style.display = 'none';

    // Store reference for global functions and track the instance
    view = editorView;
    editorInstances.set(textArea.id, editorView);

    return editorView;
};

/**
 * Initialize all CodeMirror instances on the page
 */
export const initCodemirror = () => {
    // Clean up instances for textareas that no longer exist in the DOM
    cleanupDestroyedInstances();

    const editorTargets = document.querySelectorAll('[data-codemirror-target]');

    editorTargets.forEach(target => {
        // Check if this textarea already has a CodeMirror instance
        if (editorInstances.has(target.id)) {
            // Instance already exists, skip creation
            return;
        }

        // Check if the textarea is still visible (not already replaced)
        if (target.style.display === 'none') {
            // Textarea is hidden, likely already has an editor, skip
            return;
        }

        const targetLanguage = target.getAttribute('data-codemirror-language');
        const language = normalizeLanguage(targetLanguage);

        createEditorFromTextArea(target, language);
        updateReferenceInput(target.id, target.value);
    });

    // Set up content setters after initializing editors (only once)
    setupContentSetters();
    initScrollToLine();
};

/**
 * Cleans up CodeMirror instances for textareas that no longer exist in the DOM
 */
const cleanupDestroyedInstances = () => {
    for (const [textareaId, editorView] of editorInstances.entries()) {
        const textarea = document.getElementById(textareaId);
        if (!textarea) {
            // Textarea no longer exists, destroy the editor and remove from tracking
            editorView.destroy();
            editorInstances.delete(textareaId);
        }
    }
};

/**
 * Highlights a specific line in the editor
 * @param {number} lineNumber - The line number to highlight (1-based)
 */
export const highlightLine = (lineNumber) => {
    if (!view) {
        console.warn('CodeMirror view not initialized');
        return;
    }

    const line = view.state.doc.line(lineNumber).from;
    view.dispatch({
        effects: addLineHighlight.of(line)
    });
    console.log(view.lineBlockAt(line));
};

/**
 * Scrolls to a specific line in the editor
 * @param {number} lineNumber - The line number to scroll to (1-based)
 */
export const scrollToLine = (lineNumber) => {
    if (!view) {
        console.warn('CodeMirror view not initialized');
        return;
    }

    const line = view.state.doc.line(lineNumber).from;
    const editorElement = document.querySelector('.cm-editor');

    if (!editorElement) {
        console.warn('CodeMirror editor element not found');
        return;
    }

    const editorOffsetTop = editorElement.offsetTop;
    window.scrollTo({
        top: view.lineBlockAt(line).top - editorOffsetTop,
        left: 0,
        behavior: 'smooth',
    });
};

/**
 * Sets the content of the CodeMirror editor
 * @param {string} content - The content to set in the editor
 */
export const setContent = (content) => {
    if (!view) {
        console.warn('CodeMirror view not initialized');
        return;
    }

    // Convert escaped newlines to actual line breaks
    const processedContent = content.replace(/\\n/g, '\n');

    view.dispatch({
        changes: {
            from: 0,
            to: view.state.doc.length,
            insert: processedContent
        }
    });
};

/**
 * Sets up event listeners for elements with data-set-codemirror attribute
 * These elements will set the CodeMirror content to their attribute value when activated
 */
const setupContentSetters = () => {
    const contentSetters = document.querySelectorAll('[data-codemirror-set]:not([data-codemirror-initialized])');

    contentSetters.forEach(element => {
        const content = element.getAttribute('data-codemirror-set');

        // Handle click events
        const handleActivation = (event) => {
            event.preventDefault();
            setContent(content);

            // Announce to screen readers
            const announcement = document.createElement('div');
            announcement.setAttribute('aria-live', 'polite');
            announcement.setAttribute('aria-atomic', 'true');
            announcement.className = 'visually-hidden';
            announcement.textContent = 'Editor content has been updated';
            document.body.appendChild(announcement);

            // Remove announcement after it's been read
            setTimeout(() => {
                if (announcement.parentNode) {
                    announcement.parentNode.removeChild(announcement);
                }
            }, 1000);

            // Focus the editor for immediate editing
            if (view) {
                view.focus();
            }
        };

        // Add click listener
        element.addEventListener('click', handleActivation);

        // Add keyboard support for accessibility
        element.addEventListener('keydown', (event) => {
            // Activate on Enter or Space
            if (event.key === 'Enter' || event.key === ' ') {
                handleActivation(event);
            }
        });

        // Mark as initialized to prevent duplicate event listeners
        element.setAttribute('data-codemirror-initialized', 'true');
    });
};

// Initialize on DOM content loaded
document.addEventListener("DOMContentLoaded", () => {
    initCodemirror();
});

// Re-initialize after JSF AJAX updates
faces.ajax.addOnEvent((data) => {
    if (data.status === 'success') {
        initCodemirror();
    }
});
