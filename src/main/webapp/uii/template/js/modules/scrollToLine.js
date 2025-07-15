import {
	highlightLine,
	scrollToLine,
} from "./codemirror";


export const init = () => {
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
			highlightLine(targetLine);
			scrollToLine(targetLine);
		});
	});
}
