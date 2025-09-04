var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';

    var _debug = false;

    goobiWorkflow.meScrollPos = {
        /**
         * Initialize this module: Update the current view,
         * save and restore scroll positions.
         * @param {Object} data -- jsf data obj, available in `<f:ajax>`
         * @param {Object.<string>} [opts] -- options to be passed
         */
      init: function(data, {view = goobiWorkflowConfig.currentView, saveScrollPos = true} = {}) {
        if ( _debug ) console.log( 'Init: goobiWorkflowJS.meScrollPos.init' );
        if ( _debug ) console.log( 'view:', view,'|||', 'saveScrollPos:', saveScrollPos);

        // Set goobiWorkflowConfig.currentView
        this.updateView(data, view)
        // Save and restore scroll position
        this.restoreScrollPosCenter(data, saveScrollPos)
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
          'thumbnails': 'meThumbnails'
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

      /** Store and restore the scroll position of a view.
       * This function is used in ajax calls.
       * @param {Object} data -- jsf data object, available in `<f:ajax>`
       * @param {boolean} saveScrollPos -- determines if the current scroll position is stored in session storage
       */
      restoreScrollPosCenter: function(data, saveScrollPos) {
        if(_debug) console.log({saveScrollPos})

        // Save scroll position
        if(saveScrollPos) {
          // Ajax call starts
          if (data.status === 'begin') {
            this.storeScrollPos();
          }
        }

        // Ajax call is done
        if (data.status === 'success') {
          this.restoreScrollPos()
        }
      },

    /** Get the current scroll position, and store it in session storage */
    storeScrollPos: function() {
        if(_debug) console.log('RESTORE SCROLL POS')

        const view = goobiWorkflowConfig.currentView;
        const key =  this.getScrollPosKey(view);
        const contentLeft = document.querySelector('#pageContentLeft');
        const contentCenter = view === 'Paginierung' ? document.querySelector('#paginationList') : document.querySelector('#pageContentCenter');
        const contentRight = document.querySelector('#pageContentRight #thumbnailsContainer');

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

        // Init or update current scroll position of the left column
        scrollPosAll.meLeft = contentLeft.scrollTop;

        // Init or update current scroll position of the right column
        scrollPosAll.meThumbnails = contentRight?.scrollTop;

        // Init or update current scroll position of the center column
        scrollPosAll[key] = curScrollPos;

        // Write updated scroll positions to session storage
        sessionStorage.setItem('gw_me_scrollPos', JSON.stringify(scrollPosAll))

        // Debugging
        if(_debug) console.log({curScrollPos})
        if(_debug) console.log('%cscrollPos saved', 'background: lime; color: #fff')
        if(_debug) console.table({scrollPosAll})
      },

      /** Get previous scroll positions from session storage, and
       * calculate + restore current scroll position based on these values.
       */
      restoreScrollPos: function() {
        // Get scroll position from session storage
        const view = goobiWorkflowConfig.currentView;
        const key = this.getScrollPosKey(view)
        const restoredScrollPosAll = JSON.parse(sessionStorage.getItem('gw_me_scrollPos'));

        const contentLeft = document.querySelector('#pageContentLeft');
        const contentCenter = view === 'Paginierung' ? document.querySelector('#paginationList') : document.querySelector('#pageContentCenter');
        const contentRight = document.querySelector('#pageContentRight #thumbnailsContainer');

        // Abort if session storage is empty
        if(!restoredScrollPosAll) return;

        const oldPos = restoredScrollPosAll[key];

        // Set new scroll positions
        contentLeft.scrollTop = restoredScrollPosAll.meLeft;
        contentCenter.scrollTop = oldPos + this.getErrorMsgHeight();
        if (contentRight) {
          setTimeout(() => {
            contentRight.scrollTop = restoredScrollPosAll.meThumbnails;
          }, 300);
        }

        [contentLeft, contentCenter, contentRight].forEach((el) => {
          if (!el) return;
          el.addEventListener('scroll', function() {
            goobiWorkflowJS.meScrollPos.storeScrollPos();
          });
        });

        // Debugging
        if (_debug) {
          console.log({oldPos})
          console.log('contentCenter.scrollTop:', contentCenter.scrollTop)
        }

      },

      /** Delete scroll positions from local storage. */
      destroyScrollPos: function() {
        sessionStorage.removeItem('gw_me_scrollPos');
      }

    }

  return goobiWorkflow;

} )( goobiWorkflowJS || {}, jQuery );
