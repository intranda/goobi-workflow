function altoSaveButtonCallback(data) {
	if(data.status == "success") {
		if(!document.querySelector(".alto-message-error")) {
			document.querySelector('alto-editor')._tag.saved();
		}

		let altoEditor = document.querySelector('alto-editor')._tag;
		let newJson = { lines: altoEditor.lines };
		let updatedJsonAlto = JSON.stringify(newJson);
		document.querySelector(altoEditor.opts.altoDivSelector).innerText = updatedJsonAlto;
	}
}
function checkAltoEditorDirty() {
	let modal = document.querySelector('#altoEditBox');
	let altoEditorVisible = modal && modal.offsetWidth > 0 && modal.offsetHeight > 0;
	let editorEl = document.querySelector('alto-editor');
	let isDirty = editorEl && editorEl._tag && editorEl._tag.isDirty();
	return altoEditorVisible && isDirty;
}
function altoEditorShowAutocomplete() {
	$('#altoEditorImageNumberInfo').hide();
	$('#altoEditorAutocomplete').show();
	document.querySelector('#altoEditorAutocomplete autocomplete')._tag.clear();
	$('#altoEditorAutocomplete input').focus();
	$('#altoEditorAutocomplete input').on('blur', function()  {
    	$('#altoEditorAutocomplete').hide();
		$('#altoEditorImageNumberInfo').show();
	})
}

function saveAltoResults() {
	let changeJson = document.querySelector('alto-editor')._tag.getChanges();
	document.querySelector('#altoChanges').value = changeJson;
	document.querySelector('#saveAltoChanges').click();
}