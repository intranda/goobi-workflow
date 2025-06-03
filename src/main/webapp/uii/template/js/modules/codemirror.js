/**
 * Initialize CodeMirror instances for text areas with the `data-codemirror-target` attribute.
 * Any instance needs a corresponding input field with the `data-codemirror-reference` attribute which reveives the base64 encoded content.
 * This reference input field is updated whenever the CodeMirror instance changes and used in the backend to determine whether the content has changed.
 *
 * @module codemirror
 */
import { EditorView, basicSetup } from "codemirror";
import { indentUnit } from "@codemirror/language";
import { javascript } from "@codemirror/lang-javascript";
import { xml } from "@codemirror/lang-xml";
import { json } from "@codemirror/lang-json";
import { markdown } from "@codemirror/lang-markdown";
import { yaml } from "@codemirror/lang-yaml";
import { properties } from '../util/codemirror.lang.properties.js';

export const initCodemirror = () => {
    const editorTargets = document.querySelectorAll('[data-codemirror-target]');
    editorTargets.forEach(target => {
        const targetLanguage = target.getAttribute('data-codemirror-language');
        const language = targetLanguage === 'text/x-properties' ? 'properties' : targetLanguage.toLowerCase() || 'xml';
        editorFromTextArea(target, language);
        updateReferenceInput(target.id, target.value);
    });
};

const editorFromTextArea = (textArea, language = 'xml') => {
    const languageMap = {
        javascript: javascript(),
        xml: xml(),
        json: json(),
        markdown: markdown(),
        yaml: yaml(),
        properties: properties(),
    };
    let view = new EditorView({
        doc: textArea.value,
        extensions: [
            basicSetup,
            languageMap[language],
            EditorView.updateListener.of((update) => {
                if (update.docChanged) {
                    updateReferenceInput(textArea.id, update.state.doc.toString());
                }
            }),
            indentUnit.of('    '), // Set the indentation unit to 4 spaces
        ],
        parent: textArea.parentNode
    });
    textArea.parentNode.insertBefore(view.dom, textArea);
    textArea.style.display = 'none';

    return view;
};

const updateReferenceInput = (textAreaId, value) => {
    const escapedId = CSS.escape(textAreaId);
    const referenceInput = document.querySelector('[data-codemirror-reference="' + escapedId + '"]');
    if (referenceInput) {
        referenceInput.value = base64EncodeUnicode(value);
    }
};

const base64EncodeUnicode = function base64EncodeUnicode(str) {
	// Firstly, escape the string using encodeURIComponent to get the UTF-8 encoding of the characters,
	// Secondly, we convert the percent encodings into raw bytes, and add it to btoa() function.
	const utf8Bytes = encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
		return String.fromCharCode('0x' + p1);
	});
	return btoa(utf8Bytes);
}