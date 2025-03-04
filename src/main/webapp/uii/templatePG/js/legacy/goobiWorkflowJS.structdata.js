var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _tabStatusDefault = {
        metseditorPagesOverview: true,
        metseditorPagesAllPages: false,
        metseditorPagesQuickassign: false
    };
    var _tabStatus = {};
    
    goobiWorkflow.structdata = {
        /**
         * @description Method to initialize the structdata module.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflowJS.structdata.init' );
            }

            // check session storage for tab status
            if (sessionStorage.getItem('wf_structdataTabStatus') == null || sessionStorage.getItem('wf_structdataTabStatus') == undefined) {
                sessionStorage.setItem('wf_structdataTabStatus', JSON.stringify(_tabStatusDefault));
                _tabStatus = sessionStorage.getItem('wf_structdataTabStatus');
                
                // check page assignment tab status
                _getPageAssignmentTabStatus();
                _setPageAssignmentTabStatus();
            }
            else {
                _tabStatus = sessionStorage.getItem('wf_structdataTabStatus');
                
                // check page assignment tab status
                _getPageAssignmentTabStatus();
                _setPageAssignmentTabStatus();
            }
        },
    };

    /**
     * @description Method to check and get the page assignment tab status.
     * @method _getPageAssignmentTabStatus
     */
    function _getPageAssignmentTabStatus() {
        if (_debug) {
            console.log('EXECUTE: _getPageAssignmentTabStatus');
        }

        var status = JSON.parse(_tabStatus);
        
        $.each(status, function(index, value) {
            if (value) {
                $('#' + index).addClass('show');
            }
        });
    }

    /**
     * @description Method to check and set the page assignment tab status.
     * @method _setPageAssignmentTabStatus
     */
    function _setPageAssignmentTabStatus() {
        if (_debug) {
            console.log('EXECUTE: _setPageAssignmentTabStatus');
        }

        var status = JSON.parse(_tabStatus);
        
        $('body').on('click', '[data-target="#metseditorPagesOverview"], [data-target="#metseditorPagesAllPages"], [data-target="#metseditorPagesQuickassign"]', function() {
            var currentTarget = $(this).attr('aria-controls');
            
            $.each(status, function (index) {
                if (index == currentTarget) {
                    status[index] = true;
                }
                else {
                    status[index] = false;
                }
                sessionStorage.setItem('wf_structdataTabStatus', JSON.stringify(status));
            });
        });
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );