/**
 * A CodeMirror language declaration for properties files.
 *
 * @module
 */
import {
    LanguageSupport,
    StreamLanguage,
} from '@codemirror/language';

const propertiesLanguage = StreamLanguage.define({
    name: 'properties',
    startState: () => ({}),
    token: (stream, state) => {
        console.log(state)
        if (stream.match(/^#/)) {
            stream.skipToEnd();
            return 'comment';
        }
        if (stream.match(/^[^#].+=/)) {
            return 'keyword';
        }
        if (stream.match(/(?<=^[^#]+=).+/)) {
            return 'string';
        }
        stream.next();
    },
    languageData: {
        commentTokens: { line: "#" },
    },
});

export const properties = () => {
    return new LanguageSupport(propertiesLanguage, []);
};