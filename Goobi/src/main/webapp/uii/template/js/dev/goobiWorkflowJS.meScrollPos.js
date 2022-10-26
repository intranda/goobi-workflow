var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = true;
    
    goobiWorkflow.meScrollPos = {
        /**
         * Initialize this module: Update the current view, 
         * save and restore scroll positions.
         * @param {Object} data -- jsf data obj, available in `<f:ajax>`
         * @param {String} [view] -- name of a view, used to update goobiWorkflowConfig.currentView
         */
    	init: function(data, view) {
        if ( _debug ) console.log( 'Init: goobiWorkflowJS.meScrollPos.init' );

        // If view is undefined, use the current view name
        this.view = view || goobiWorkflowConfig.currentView;

        // Update state => set goobiWorkflowConfig.currentView.
        this.updateView(data, this.view)
        // Restore scroll position
        this.restoreScrollPosCenter(data)
      },

      /**
       * Update state => set goobiWorkflowConfig.currentView.
       * This is done, because this variable is not updated in AJAX calls.
         * @param {Object} data -- jsf data obj, available in `<f:ajax>`
         * @param {String} [view] -- name of the current view
       */
      updateView: function(data, view) {
        if(data.status === 'success') {
          if(_debug) console.log({view})
          goobiWorkflowConfig.currentView = view;
        }
      },


      /** Map view names to keys used in session storage
       * @param {string} view -- name of a view
       * @returns {string} key to access the scroll position of a view
       */
      getScrollPosKey: function(view) {
        const keys = {
          'Paginierung': 'mePagination',
          'Strukturdaten': 'meStructData',
          'Metadaten': 'meMetadata',
          'File': 'meFile',
          'default': 'default',
        }
        return keys[view] || keys['default'];
      },

      /** Calculate the height of error messages 
       * displayed at the top of the message editors center column.
       * When rerendering a view the number of message (and thus their height) may change.
       * Accounting for these changes, keeps the scroll position at the same level.
       * @returns {number} messageHeight -- height of the aggregated error messages
       */
      getErrorMsgHeight: function() {
        // Get height and margin of error messages at the top of the center col
        const messages = document.querySelector('#messages');
        const messagesStyles = getComputedStyle(messages);
        let messageHeight = messages.offsetHeight;
        messageHeight += parseInt(messagesStyles.marginTop)
        messageHeight += parseInt(messagesStyles.marginBottom)
        return messageHeight;
      },

      /** Restore the previous scroll position of a view.
       * Calculates, restores scroll positions, 
       * writes and recovers them from session storage.
       * @param {Object} data -- jsf data object, available in `<f:ajax>`
       */
      restoreScrollPosCenter: function(data) {
        const contentCenter = document.querySelector('#pageContentCenter');

        // Ajax call starts
        if (data.status === 'begin') {
          const view = goobiWorkflowConfig.currentView;
          const key =  this.getScrollPosKey(view);

          // Restore previous scroll positions of all views
          const restoredScrollPosAll = sessionStorage.getItem('gw_me_scrollPos');

          // Current view: get scroll position of the center div
          const centerScrollPos = contentCenter.scrollTop; // absolute position
          const curScrollPos = centerScrollPos - this.getErrorMsgHeight();

          let scrollPosAll = {};
          if(restoredScrollPosAll) {
            scrollPosAll = JSON.parse(restoredScrollPosAll);
          }
          // Assign a default scroll position
          scrollPosAll.default = '0';

          // Init or update current scroll position
          scrollPosAll[key] = curScrollPos;

          // Write updated scroll positions to session storage
          sessionStorage.setItem('gw_me_scrollPos', JSON.stringify(scrollPosAll))

          // Debugging
          if(_debug) console.log({scrollPosAll})
        }

        // Ajax call is done
        if (data.status === 'success') {

          // Get scroll pos from session storage
          const view = goobiWorkflowConfig.currentView;
          const key = this.getScrollPosKey(view)
          const restoredScrollPosAll = JSON.parse(sessionStorage.getItem('gw_me_scrollPos'));
          const oldPos = restoredScrollPosAll[key]

          // Set box status (collapsed / open)
          goobiWorkflowJS.box.getBoxStatus();

          // Set new scroll pos
          contentCenter.scrollTop = oldPos + this.getErrorMsgHeight();

          // Debugging
          if (_debug) {
            console.log({oldPos})
            console.log('contentCenter.scrollTop:', contentCenter.scrollTop)
          }

        }
      },

    }

  return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );
