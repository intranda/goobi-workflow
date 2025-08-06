import { EditorState } from "prosemirror-state";
import { EditorView } from "prosemirror-view";
import { DOMParser, DOMSerializer, } from "prosemirror-model";
import { history } from "prosemirror-history";
import { keymap } from "prosemirror-keymap";
import { baseKeymap } from "prosemirror-commands";
import { dropCursor } from "prosemirror-dropcursor";
import { gapCursor } from "prosemirror-gapcursor";

import { schema } from "./prosemirror/schema";
import { createMenu } from "./prosemirror/menu";

const createEditor = () => {
    const targets = document.querySelectorAll('[data-editor="prosemirror"]');
    if (!targets) {
        return;
    }
    targets.forEach((target) => {
        const baseId = target.id.replace('editor', '');
        const source = document.getElementById(`${baseId}editorSource`);
        const input = document.getElementById(`${baseId}textareaHtml`);
        if (!source) {
            return;
        }

        // Create initial state
        const state = EditorState.create({
            doc: DOMParser.fromSchema(schema).parse(source),
            plugins: [
                history(),
                keymap(baseKeymap),
                dropCursor(),
                gapCursor(),
            ]
        });

        // Create a unique editor view for this instance
        const editorView = new EditorView(target, {
            state,
            handleDOMEvents: {
                blur: () => {
                    const serializer = DOMSerializer.fromSchema(schema);
                    const fragment = serializer.serializeFragment(editorView.state.doc.content);
                    const div = document.createElement("div");
                    div.appendChild(fragment);
                    input.value = div.innerHTML;
                }
            }
        });

        // Create menu for this specific instance and add it
        const menuPlugin = createMenu(editorView);
        const newState = EditorState.create({
            doc: editorView.state.doc,
            plugins: [
                menuPlugin,
                history(),
                keymap(baseKeymap),
                dropCursor(),
                gapCursor(),
            ]
        });

        editorView.updateState(newState);

        // Store the view on the target element instead of global
        target.editorView = editorView;
    });
}

document.addEventListener("DOMContentLoaded", () => {
    createEditor();
});

faces.ajax.addOnEvent((data) => {
    switch (data.status) {
        case 'begin':
            break;
        case 'complete':
            break;
        case 'success':
            createEditor();
            break;
    }
});
