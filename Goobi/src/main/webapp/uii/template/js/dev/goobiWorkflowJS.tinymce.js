var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {
        selector: '.textarea-html'
    };
    
   
    
    goobiWorkflow.tinymce = {
        /**
         * @description Method to initialize tinyMCE.
         * @method init
         */
    	init: function(config) {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.tinymce.init' );
                console.log( '--> config = ', config );
            }

            $.extend( true, _defaults, config );

            this.extendedTinyMceConfig = {
                selector: _defaults.selector,
                extended_valid_elements: 'p',
                statusbar: true,
                height: 200,
                menu: {},
                plugins: [
                    'advlist autolink link image lists charmap print preview hr anchor pagebreak spellchecker',
                    'searchreplace wordcount visualblocks visualchars code insertdatetime media nonbreaking',
                    'save table contextmenu directionality emoticons template paste'],
                content_css: 'css/content.css',
                toolbar: 'undo redo | styleselect | bold italic underline strikethrough | bullist numlist | image table | fullscreen code',
                spellchecker_languages: 'English=en_US,German=de_DE_frami',
                //	spellchecker_rpc_url: 'spellchecker.php',
                spellchecker_callback: function (method, text, success, failure) {
                    tinymce.util.JSONRequest.sendRPC({
                        url: "template/js/plugins/tinymce/js/tinymce/plugins/spellchecker/spellchecker.php",
                        method: "spellcheck",
                        params: {
                            lang: this.getLanguage(),
                            words: text.match(this.getWordCharPattern())
                        },
                        success: function (result) {
                            success(result);
                        },
                        error: function (error, xhr) {
                            console.log(error, xhr);
                            failure("Spellcheck error:" + xhr.status);
                        }
                    });
                },
                style_formats: [
                    {
                        title: 'Headings', items: [
                            { title: 'Heading 1', format: 'h1' },
                            { title: 'Heading 2', format: 'h2' },
                            { title: 'Heading 3', format: 'h3' },
                            { title: 'Heading 4', format: 'h4' },
                            { title: 'Heading 5', format: 'h5' },
                            { title: 'Heading 6', format: 'h6' }
                        ]
                    },
                    {
                        title: 'Blocks', items: [
                            { title: 'Paragraph', format: 'p' },
                            { title: 'Blockquote', format: 'blockquote' },
                            { title: 'Div', format: 'div' },
                        ]
                    },
                ],
                init_instance_callback: function (editor) {
                    var readOnlyAttr = $("#" + editor.id.replace(":", "\\:")).attr(
                        "readonly");
                    if (readOnlyAttr === "readonly") {
                        editor.setMode("readonly");
                    }
                    try {
                        resizeReferenceFields();
                        $(editor.getWin()).bind('resize', function () {
                            resizeReferenceFields();
                        });
                    } catch (error) {
                    }
                },
                setup: function (editor) {
                    editor.on("blur", function (event, a, b) {
                        editor.save();
                        console.log("input id = ", editor.id.replace(/:/g, "\\:"));
                        $("#" + editor.id.replace(/:/g, "\\:")).trigger("change");
                    });
                    editor.on('change', function () {
                        tinymce.triggerSave();
                    });
                }

            };
            goobiWorkflow.tinymce.renderInputFields();
    	   },
            initTinyMce: function () {
                if(_debug)console.log("init tinymce", goobiWorkflow.tinymce.extendedTinyMceConfig);
                tinymce.init(goobiWorkflow.tinymce.extendedTinyMceConfig);
            },

            renderInputFields : function (ajaxData) {
                if (typeof tinyMCE !== 'undefined') {
                    if (ajaxData === undefined || ajaxData.status == "begin") {
                        
                        for (var edId in tinyMCE.editors) {
                            try {
                                tinyMCE.editors[edId].remove();
                                console.log("Removed editor " + edId);
                            } catch (error) {
                                console.log("Error occured during removing editors; ", error);
                            }
                        }
                    }
                    if (ajaxData === undefined || ajaxData.status == "success") {
                        goobiWorkflow.tinymce.initTinyMce(ajaxData);
                    }
                }
            }

     
    };

    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );