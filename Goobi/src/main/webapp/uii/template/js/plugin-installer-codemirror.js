function initXmlCodeMirror() {
	var xmlTextAreaSource = document.querySelectorAll(".textarea_edit_file_source");
	var xmlTextAreaDestination = document.querySelectorAll(".textarea_edit_file_destination");

	if ((typeof xmlTextAreaSource == "undefined") || (typeof xmlTextAreaDestination == "undefined")) {
		return;
	}

	var xmlCodeMirror = new Array(xmlTextAreaDestination.length);

	for (var index = 0; index < xmlTextAreaSource.length; index++) {
		if ((typeof xmlTextAreaSource[index] == "undefined") || (typeof xmlTextAreaDestination[index] == "undefined")) {
			continue;
		}

		xmlTextAreaDestination[index].value = xmlTextAreaSource[index].value;
		xmlCodeMirror[index] = CodeMirror.fromTextArea(xmlTextAreaSource[index], {
			lineNumbers: true,
			mode: 'xml'
		});
		xmlCodeMirror[index].setSize("100%", "100%");
		let xmlTextAreaDestinationInstance = xmlTextAreaDestination[index];
		xmlCodeMirror[index].on('change', editor => {
			xmlTextAreaDestinationInstance.value = editor.getValue();
		});
	}
}
