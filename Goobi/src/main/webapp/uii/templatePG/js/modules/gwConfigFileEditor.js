var configFileEditor;
var debug = false;

function initConfigFileEditor() {
	var configFileTextArea = document.getElementById("configFileEditor");
	let type = "xml";
	let typeElement = document.getElementById("currentConfigFileType");
	if (typeElement) {
		type = typeElement.innerHTML.trim();
	}
	if (configFileTextArea) {
		configFileEditor = CodeMirror.fromTextArea(configFileTextArea, {
			lineNumbers: true,
			mode: type
		});
		setTimeout(function() {
			configFileEditor.refresh();
		}, 100);
		configFileEditor.on('change', editor => {
			document.getElementById("configFileEditor").innerHTML = editor.getValue();
		});
	}
}

function loadEditorContent() {
	var configFileTextAreaBase64 = document.getElementById("configFileEditorForm:contentbox:configFileEditorBase64");
	let string = configFileEditor.getValue();
	if (debug){
	  // console.log("Load: " + string);
	}
	configFileTextAreaBase64.value = base64EncodeUnicode(string);
}

function base64EncodeUnicode(str) {
	// Firstly, escape the string using encodeURIComponent to get the UTF-8 encoding of the characters, 
	// Secondly, we convert the percent encodings into raw bytes, and add it to btoa() function.
	utf8Bytes = encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
		return String.fromCharCode('0x' + p1);
	});
	return btoa(utf8Bytes);
}

function loadEditorContentAndInit() {
	loadEditorContent();
	initConfigFileEditor();
}

function stickyBoxes() {
	var heightLeft = document.getElementById('leftarea').children[0].clientHeight;
	var heightRight = document.getElementById('rightarea').children[0].clientHeight;
	if (debug){
		console.log(heightLeft);
		console.log(heightRight);
	}
	document.getElementById('leftarea').style.height = heightLeft + 2 + "px";
	document.getElementById('rightarea').style.height = heightRight + 2 + "px";
	
	var Sticky = new hcSticky('#rightarea', {
    	stickTo: '#leftarea',
    	responsive: {
		    768: {
		      disable: true
		    }
		  }
	});

	if (debug){
		console.log("stickyBoxes was called ");
	}
}
	
document.addEventListener('DOMContentLoaded', function() {
  stickyBoxes();
});

jsf.ajax.addOnEvent( function( data ) {
        if (data.status == "success"){
		stickyBoxes();
	} 
});