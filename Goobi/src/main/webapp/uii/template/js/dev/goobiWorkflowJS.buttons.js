var goobiWorkflowJS = (function (goobiWorkflow) {
    'use strict';

    var _debug = false;
    var _defaults = {};

    goobiWorkflow.buttons = {
        /**
         * @description Method to initialize the buttons.
         * @method init
         */
        init: function (config) {
            if (_debug) {
                console.log('Initializing: goobiWorkflowJS.buttons.init');
            }
            
            $.extend( true, _defaults, config );
            
            if ($('.btn').hasClass('btn--toggle')) {
                _setButtonToggleEvent();
            }
        }
    };

    /**
     * @description Method to set the event listener to button toggle.
     * @method _setButtonToggleEvent
     */
    function _setButtonToggleEvent() {
        if (_debug) {
            console.log('EXECUTE: _setButtonToggleEvent');
        }

        $('.btn--toggle').off().on('click', function () {
            $(this).next('div').slideToggle(300);
        });
    }

   

                
    return goobiWorkflow;

})(goobiWorkflowJS || {}, jQuery);