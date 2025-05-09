function scrollToTestLine(lineNumber) {
	const lines = document.querySelectorAll('.CodeMirror-linenumber');
	const line = lines[parseInt(lineNumber)];
	line.scrollIntoView({ block: 'center', inline: 'nearest' });
	lines.forEach(line => {
	        line.style.backgroundColor = '';
	    });
	line.style.backgroundColor = "yellow";  
}
