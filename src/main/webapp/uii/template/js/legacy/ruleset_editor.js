var rulesetEditor;
var debug = false;

function initRulesetEditor() {
	var rulesetTextArea = document.getElementById("rulesetEditorForm:contentbox:rulesetEditor");
	if (rulesetTextArea) {
		rulesetEditor = CodeMirror.fromTextArea(rulesetTextArea, {
			lineNumbers: true,
			viewportMargin: Infinity,
			mode: 'xml'
		});
		setTimeout(function() {
			rulesetEditor.refresh();
		}, 100);
		rulesetEditor.on('change', editor => {
			document.getElementById("rulesetEditor").innerHTML = editor.getValue();
		});
	}
}

function loadRulesetEditorContent() {
	var rulesetTextAreaBase64 = document.getElementById("rulesetEditorForm:contentbox:rulesetEditorBase64");
	let string = rulesetEditor.getValue();
	if (debug){
	  // console.log("Load: " + string);
	}
	rulesetTextAreaBase64.value = base64EncodeUnicodeRuleset(string);
}

function base64EncodeUnicodeRuleset(str) {
	// Firstly, escape the string using encodeURIComponent to get the UTF-8 encoding of the characters,
	// Secondly, we convert the percent encodings into raw bytes, and add it to btoa() function.
	utf8Bytes = encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
		return String.fromCharCode('0x' + p1);
	});
	return btoa(utf8Bytes);
}

function loadRulesetEditorContentAndInit() {
	loadRulesetEditorContent();
	initRulesetEditor();
}

function stickyRulesetBoxes() {
	var heightLeft = document.getElementById('leftarea').children[0].clientHeight;
	var heightRight = document.getElementById('rightarea').children[0].clientHeight;
	if (debug){
		console.log(heightLeft);
		console.log(heightRight);
	}
	document.getElementById('leftarea').style.height = heightLeft + 2 + "px";
	document.getElementById('rightarea').style.height = heightRight + 2 + "px";

	var Sticky = new hcSticky('#leftarea', {
    	stickTo: '#rightarea',
    	responsive: {
		    768: {
		      disable: true
		    }
		  }
	});

	if (debug){
		console.log("stickyRulesetBoxes was called ");
	}
}

document.addEventListener('DOMContentLoaded', function() {
  //stickyRulesetBoxes();
});

faces.ajax.addOnEvent( function( data ) {
    if (data.status == "success"){
		//stickyRulesetBoxes();
	}
});