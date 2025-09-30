import {
	addLineHighlight,
} from "./highlightLine.js";

/**
 * Highlights a specific line in the editor
 * @param {number} lineNumber - The line number to highlight (1-based)
 */
export const highlightLine = (view,lineNumber) => {
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
export const scrollToLine = (view, lineNumber) => {
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

export const initScrollToLine = (view) => {
	const elements = document.querySelectorAll('[data-scroll-to-line]');
	if (elements.length === 0) {
		return;
	}
	elements.forEach(element => {
		element.addEventListener('click', () => {
			let targetLine = element.getAttribute('data-scroll-to-line');
			if (!targetLine) {
				return;
			}
			if (targetLine.startsWith('#')) {
				targetLine = targetLine.substring(1);
			}
			targetLine = parseInt(targetLine, 10);
			highlightLine(view, targetLine);
			scrollToLine(view, targetLine);
		});
	});
}
