import {
    StateField,
    StateEffect,
} from "@codemirror/state";
import {
    EditorView,
    Decoration,
} from "@codemirror/view";

// Line highlighting state management
export const addLineHighlight = StateEffect.define();

export const lineHighlight = Decoration.line({
    attributes: { style: 'background-color: yellow;' },
});

export const highlightStateField = StateField.define({
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