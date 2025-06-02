const scrollToLine = (lineNumber) => {
	const lines = document.querySelectorAll('.CodeMirror-linenumber');
	const activeLines = document.querySelectorAll('.cm-activeline');
	const line = lines[lineNumber];
	line.scrollIntoView({ block: 'center', inline: 'nearest' });
	activeLines.forEach(activeLine => {
		activeLine.style.backgroundColor = ''; // Reset background color
	});
	line.style.backgroundColor = 'yellow'; // Highlight the active line
}

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
			scrollToLine(targetLine);
		});
	});
}
