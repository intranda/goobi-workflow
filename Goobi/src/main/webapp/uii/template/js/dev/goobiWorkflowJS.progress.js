var goobiWorkflowJS = (function (goobiWorkflow) {
    'use strict';

    var _debug = false;
    
    goobiWorkflow.progress = {
        /**
         * @description Method to initialize the progress bar module.
         * @method init
         */
        init: function (config) {
            if (_debug) {
                console.log('Initializing: goobiWorkflowJS.progress.init');
            }
        },
        /**
         * @description Method to show the progressbar panel.
         * @method displayProgressBar
         */
        displayProgressBar: function () {
            if (_debug) {
                console.log('EXECUTE: goobiWorkflowJS.progress.displayProgressBar');
            }
            
            var element = document.getElementById('progressPanel');

            if (element.style.display == 'block') {
                element.style.display = 'none';
            } 
            else {
                element.style.display = 'block';
            }
            
            document.getElementById('progressbutton').click();
        },
    };

    return goobiWorkflow;

})(goobiWorkflowJS || {}, jQuery);