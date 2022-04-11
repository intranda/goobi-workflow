
/*
* jQuery PageGuide Plugin
*
* Build interactive visual guides to help users get familiar your web app.
* jQuery PageGuide started as a rewrite of the Tracelytics PageGuide,
* intended to work as a true jQuery plugin. But then I started adding more
* and more functionality, and now it's taken on a life of it's own.
*
* Copyright 2012 Sprint.ly
*
* Author: Ian White <ian@sprint.ly>
* Source: http://github.com/impressiver/jquery.pageguide
*
* Free to use under the MIT license.
* http://www.opensource.org/licenses/mit-license.php
*
* -----
* Based on:
* Tracelytics PageGuide
* Copyright 2012 Tracelytics
*
* Project Home: http://tracelytics.github.com/pageguide/
*
* Free to use under the MIT license.
* http://www.opensource.org/licenses/mit-license.php
*
*/

/*
* PageGuide usage:
*
*   PageGuide guides can be defined either as markup (an `OL` in the DOM),
*   or directly in JavaScript to get more flexibility. To see how guides
*   are structured, check out the examples included in the project repo.
*
*   The simplest way to initialize PageGuide is by calling `$.pageguide()`
*   with no arguments. This will set up the default options and prepare the
*   plugin to load individual guides. For convenience, you can load a
*   default guide and set base options during initialization like so:
*   `$.pageguide(guide, options)`, where `guide` is either a CSS selector
*   that identifies the DOM element to use as the guide definition, or a JS
*   object that contains the guide definition. `options` is an optional
*   object that is used to override the default settings.
*
*   To load a guide after initialization, or load a different guide, call
*   `$.pageguide('load', guide, options)`. When loading a guide, the
*   `options` argument will only be applied to that guide.
*
*   In order to update options after initialization that will be applied to
*   all guides, use `$.pageguide('options', options)`. Keep in mind, any
*   options specified when calling `$.pageguide.load()` will still take
*   precedence.
*
*   All of these override methods perform a deep merge between the default
*   options and override options to create the base settings.
*
*
*   Guide Options:
*     defaultGuide: none (String selector, jQuery selector, Object guide)
*       - CSS selector or guide definition object to load when $.pageguide
*         is initialized without a guide as the first argument.
*
*     autoStart: true (true, false)
*       - Whether or not to focus on the first visible item immediately on
*         open.
*
*     autoStartDelay: 0 (int milliseconds)
*       - Add a delay before automatically selecting the first visible item
*         after the guide is opened.
*
*     autoAdvanceInterval: null (int seconds)
*       - Rotate through the visible steps at a regular interval while the
*         guide is open.
*
*     loadingSelector: none (String selector, jQuery selector)
*       - The CSS selector for the DOM element used as a loading indicator.
*         PageGuide will wait until this element is no longer visible
*         before starting up.
*
*     pulse: true (true, false)
*       - Show an animated effect to further highlight the target element
*         whenever a new step is selected. Requires the step shadow to be
*         set to 'true'.
*
*     events: {} (Object {init, ready, load, unload, open, close, previous,
*                next, step, resize, click} callback functions)
*       - Convenience wrapper to specify guide-level event handlers. These
*         events are bound on load, and automatically removed when the
*         guide is unloaded.
*
*   Step Options (options.step):
*     direction: 'left' ('top', 'right', 'bottom', 'left')
*       - Position of the floating step number indicator in relation to the
*         target element.
*
*     margin: {top: 100, bottom: 100} (Object {top, bottom} in px)
*       - Minimum distance the target element must be from top or bottom of
*         the viewport. If the element is outside of this margin, the
*         window will scroll to bring the element into view.
*
*     shadow: true (true, false)
*       - Render a transparent box around the current step's target
*         element.
*
*     shadowPadding: '10px' (String padding, int padding)
*       - Applied to all sides of the shadow to pad the height and width
*         around the target element.
*
*     zIndex: null (int z-index)
*       - Force the base z-index of the step, which is used when rendering
*         the floating step number indicator and the shadow. If set to
*         null, the target element's z-index is used. The shadow is
*         rendered at a z-index of base + 1, and the floating step number
*         indicator is base + 2.
*
*     arrow: {offsetX: 0, offsetY: 0} (Object {offsetX, offsetY} in px)
*       - Additional offset to apply to the floating step indicator to make
*         fine adjustments to positioning.
*
*     events: {} (Object {show, hide, select, deselect} callbacks)
*       - Convenience wrapper to specify step-level event handlers. These
*         events are bound to the individual step on load, and
*         automatically removed when the guide is unloaded.
*
*/

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(['jquery'], factory);
    } else {
        // Browser globals
        factory(jQuery);
    }
}(function($, undefined ) {
    "use strict";

    if ($.pageguide !== undefined) {
        return;
    }

    /**
     *
     * @param {String|jQuerySelector|Object} [guide]   The default guide to load as soon as initialization is complete
     * @param {Object} [options] Override settings for all guides, merged with default options.
     *
     * @class a PageGuide
     */
    var PageGuide = function (guide, options) {
        if(arguments.length == 1) {
            options = guide;
            guide = null;
        }
        options = options || {};
        this.options(options);

        // Make sure $.fn.zIndex is available
        if (!$.fn['zIndex']) $.fn['zIndex'] = this._zIndex;

        this.init();

        var defaultGuide = guide || this.settings.defaultGuide || null;

        if (defaultGuide) {
            if (this.status === 'ready') {
                this.load(defaultGuide);
            } else {
                this.$wrapper.one('ready.pageguide', $.proxy(function () {
                    this.load(defaultGuide);
                }, this));
            }
        }
    };

    $.extend(PageGuide, {
        options: {
            defaultGuide: "#pageGuide",
            autoStart: true,
            autoStartDelay: 0,
            autoAdvanceInterval: null,
            loadingSelector: null,
            pulse: true,
            events: {},
            step: {
                direction: 'left',
                margin: {top: 100, bottom: 100},
                shadow: true,
                shadowPadding: '10px',
                zIndex: null,
                arrow: {
                    offsetX: 0,
                    offsetY: 0
                },
                events: {}
            }
        },
        DIRECTION_REGEX: /pageguide[_-](top|right|bottom|left)(?:\s|$)/i,
        instances: 0,
        uid: function () {
            return PageGuide.instances++;
        },
        prototype: {
            _options: {},               // base options (with default overrides)
            settings: {},               // base options with overrides for current guide
            status: 'uninitialized',

            _guide: null,               // current guide definition
            $guide: null,               // current guide selector

            $wrapper: null,             // #pageGuideWrapper
            $message: null,             // #pageGuideMessage
            $toggle: null,              // #pageGuideToggle
            $shadow: null,              // #pageGuideShadow
            $fwd: null,                 // a.pageguide-fwd
            $back: null,                // a.pageguide-back

            /**
             * Initialize the PageGuide. Creates the navigation DOM elements
             * and triggers a 'ready.pageguide' event when done.
             *
             * @return {[type]}
             */
            init: function() {
                var wrapper = $('<div>', {
                        id: 'pageGuideWrapper'
                    }),
                    message = $('<div>', {
                        id: 'pageGuideMessage'
                    }),
                    toggle = $('<div/>', {
                        id: 'pageGuideToggle',
                        title: 'Launch Page Guide',
                        'class': 'pageguide-toggle-close'
                    }),
                    shadow = $('<div/>', {
                        id: 'pageGuideShadow',
                        'class': 'pageguide-shadow'
                    });

                toggle.append('page guide').append('<div><span class="pageguide-tourtitle"></span></div>').append('<a class="pageguide-close" title="Close Guide">close guide &raquo;</a>');
                message.append('<a class="pageguide-close" title="Close Guide">close</a>').append('<span class="pageguide-index"></span>').append('<div class="pageguide-content"></div>').append('<a class="pageguide-back" title="Previous">Previous</a>').append('<a class="pageguide-fwd" title="Next">Next</a>');
                shadow.append('<span class="pageguide-shadow-pulse"></span>');

                wrapper.append(toggle);
                wrapper.append(message);
                wrapper.append(shadow);
                $('body').append(wrapper);

                this.$wrapper = wrapper;
                this.$toggle = toggle;
                this.$message = message;
                this.$shadow = shadow;

                this.$fwd = $('a.pageguide-fwd', this.$wrapper);
                this.$back = $('a.pageguide-back', this.$wrapper);

                this.status = 'initialized';
                this.$wrapper.trigger('init.pageguide');

                if (this.settings.loadingSelector) {
                    this._wait($.proxy(function () {
                        this._registerWrapperHandlers();
                        this.status = 'ready';
                        this.$wrapper.trigger('ready.pageguide');
                    }, this));
                } else {
                    this._registerWrapperHandlers();
                    this.status = 'ready';
                    this.$wrapper.trigger('ready.pageguide');
                }
            },

            /**
             * Dismantle the UI and clean up event handlers.
             *
             * @return {PageGuide}
             */
            destroy: function () {
                if (this._guide) {
                    this.unload();
                }
                // @@@ handlers removed by jQuery.remove()
                this._removeWrapperHandlers();
                this.$wrapper.remove();

                this.status = 'destroyed';
                return this;
            },

            /**
             * Get or set overrides for base options. If the argument is
             * ommitted, an object containing the current options are returned.
             *
             * @param  {Object} [options] Settings to override for all guides
             * @return {PageGuide}
             */
            options: function(options) {
                if (options === undefined) {
                    return this._options;
                }

                this._options = $.extend(true, {}, PageGuide.options, options);
                this.settings = $.extend(true, {}, PageGuide.options, this.settings || {}, options);

                return this;
            },

            /**
             * Get or load a guide. If the argument is ommitted, the currently loaded
             * guide definition is returned.
             *
             * @param  {String|jQuerySelector|Object} [guide] Guide definition to load
             * @return {PageGuide} Guide definition
             */
            guide: function(guide) {
                if (guide === undefined) {
                    return this._guide;
                }

                this.load(guide, this.settings);

                return this;
            },

            /**
             * Load a guide. Automatically unloads the previous guide (if any).
             *
             * @param  {String|jQuerySelector|Object} guide Guide definition to load
             * @param  {Object} [options] Override options applied only to this guide
             * @return {PageGuide}
             */
            load: function(guide, options) {
                if (!guide || $.isEmptyObject(guide)) {
                  return this;
                }

                if(this._guide) {
                    this.unload();
                }

                // Override guide options
                if (options !== undefined) {
                    this.settings = $.extend(true, {}, PageGuide.options, this._options, options);
                }

                var that = this,
                    $guide = (typeof guide === 'string') ? $(guide) : null;

                if ($guide) {
                    if (!$guide.size()) return this;

                    guide = {
                        title: $guide.data('tourtitle'),
                        steps: []
                    };

                    var $allItems = $('> li', $guide);
                    $.each($allItems, function (i) {
                        var matches = PageGuide.DIRECTION_REGEX.exec($(this).attr('class'));
                        var direction = matches ? matches.pop() : that.settings.step.direction;

                        var step = {
                            target: $(this).data('tourtarget'),
                            content: $(this).html(),
                            direction: direction,
                            options: $.extend(true, {}, $(this).data('options')),
                            elem: this
                        };
                        guide.steps.push(step);
                    });
                } else {
                    $guide = ((guide.id && $('#' + guide.id).size()) ? $('#' + guide.id).empty() : $('<ol/>', {
                      id: guide.id || 'pageGuide' + PageGuide.uid(),
                      'class': 'pageguide-guide'
                    })).data('tourtitle', guide.title);

                    $.each(guide.steps, function (i) {
                        var $li = $('<li/>', {
                            id: 'pageguide-step-' + i,
                            'class': 'pageguide-step pageguide-' + this.direction
                        }).data('tourtarget', this.target);

                        $li.data('options', $.extend({}, this));

                        $('<div/>', {
                            'class': 'pageguide-content'
                        }).html(this.content).appendTo($li);

                        $guide.append($li);
                        this.elem = $li;
                    });
                }

                this._guide = guide;
                this.curIdx = 0;

                this.$wrapper.append($guide);
                this.$guide = $guide;

                this.$allItems = $('> li', this.$guide);
                this.$visibleItems = $();

                this.$toggle.removeClass('.pageguide-toggle-open').addClass('pageguide-toggle-close');
                this.$toggle.find('.pageguide-tourtitle').html(this._guide.title);

                $('body').addClass('pageguide-ready');

                this._registerGuideHandlers();
                this._registerCustomHandlers();
                this._registerCustomStepHandlers();

                this.status = 'loaded';
                this.$wrapper.trigger('load.pageguide', this._guide);

                return this;
            },

            /**
             * Close and disable the current guide. Removes guide event handlers.
             *
             * @return {PageGuide}
             */
            unload: function() {
                if(!this._guide) {
                  return this;
                }

                if(this.isOpen()) {
                    this.close();
                }

                this.$toggle.removeClass('pageguide-toggle-open pageguide-toggle-close');
                $('body').removeClass('pageguide-ready');

                // @@@ Custom handlers should be removed by jQuery.remove()
                this._removeCustomStepHandlers();
                this._removeCustomHandlers();
                this._removeGuideHandlers();

                this.$allItems = $();
                this.$visibleItems = $();

                this.$guide = null;
                this._guide = null;

                this.settings = this.options();

                this.status = 'unloaded';
                this.$wrapper.trigger('unload.pageguide');

                return this;
            },

            /**
             * Start the guide. If `options.autoStart` is true, the first step will be selected automatically.
             *
             * @return {PageGuide}
             */
            open: function() {
                if ($('body').is('.pageguide-open')) return;

                $('body').addClass('pageguide-open');
                this._onExpand();
                this.$visibleItems.toggleClass('expanded', true);

                this.$wrapper.trigger('open.pageguide');
                this.$visibleItems.trigger('show.pageguide');

                return this;
            },

            /**
             * Stop the guide.
             *
             * @return {PageGuide}
             */
            close: function() {
                if (!$('body').is('.pageguide-open')) return this;

                this.autoAdvance(false);

                this.$shadow.removeClass('pageguide-shadow-active').hide();
                this.$allItems.removeClass("pageguide-active").toggleClass('expanded', false);
                var curItem = this.$visibleItems[this.curIdx];
                if(curItem) {
                  $(curItem).trigger('deselect.pageguide');
                }

                this.$toggle.removeClass('pageguide-toggle-open').addClass('pageguide-toggle-close');

                this.$message.animate({
                    height: "0"
                }, 500, function() {
                    $(this).hide();
                });

                /* clear number tags and shading elements */
                $('ins').remove();
                $('body').removeClass('pageguide-open');

                this.$visibleItems.trigger('hide.pageguide');
                this.$wrapper.trigger('close.pageguide');

                return this;
            },

            /**
             * Show the visible step prior to the currently selected step.
             *
             * @return {PageGuide}
             */
            previous: function() {
                if (!$('body').is('.pageguide-open')) return this;
                /*
                 * If -n < x < 0, then the result of x % n will be x, which is
                 * negative. To get a positive remainder, compute (x + n) % n.
                 */
                var newIdx = (this.curIdx + this.$visibleItems.size() - 1) % this.$visibleItems.size();

                this.$wrapper.trigger('previous.pageguide');
                this.showStep(newIdx, 1);

                return this;
            },

            /**
             * Advance the guide to the next visible step.
             *
             * @return {PageGuide}
             */
            next: function() {
                if (!$('body').is('.pageguide-open')) return this;

                var newIdx = (this.curIdx + 1) % this.$visibleItems.size();

                this.$wrapper.trigger('next.pageguide');
                this.showStep(newIdx, -1);

                return this;
            },

            /**
             * Select step by index
             * @param  {[type]} newIdx    The index (0-based) of the step to select
             * @param  {int} [direction]  If negative, the step number in the message field will scroll to the left, if positive it will scroll to the right. If undefined or 0, it's calculated automatically.
             * @return {PageGuide}
             */
            showStep: function(newIdx, direction) {
                var oldIdx = this.curIdx,
                    oldItem = this.$visibleItems[oldIdx],
                    newItem = this.$visibleItems[newIdx],
                    left = (direction && direction !== 0) ? (direction > 0) ? true : false : (oldIdx > newIdx),
                    settings = $.extend(true, {}, this.settings.step, $(newItem).data('options') || {});

                this.curIdx = newIdx;

                $('div', this.$message).html($(newItem).children('div').html());
                this.$visibleItems.removeClass("pageguide-active");
                $(newItem).addClass("pageguide-active");

                if (settings.shadow) {
                    this._showShadow(newItem);
                } else {
                    this.$shadow.removeClass('pageguide-shadow-active').hide();
                }

                if (!this._isScrolledIntoView($(newItem))) {
                    this._scrollIntoView(newItem);
                }

                this.$message.not(':visible').show().animate({
                    'height': '100px'
                }, 500);

                this._rollNumber($('span', this.$message), $(newItem).children('ins').html(), left);

                this.$wrapper.trigger('step.pageguide', newItem);
                if ($(oldItem).data('idx') != $(newItem).data('idx')) $(oldItem).trigger('deselect.pageguide', newItem);
                $(newItem).trigger('select.pageguide', oldItem);

                return this;
            },

            refresh: function() {
                if (!this.isOpen()) return this;

                var that = this;

                this.$visibleItems = this.$allItems.filter(function() {
                    return $($(this).data('tourtarget')).is(':visible');
                });

                // Position the floating indicators
                this.$visibleItems.each(function() {
                    var arrow = $(this),
                        settings = $.extend(true, {}, that.settings.step, $(this).data('options') || {}),
                        target = $(arrow.data('tourtarget')),
                        setLeft = target.offset().left + parseInt(settings.arrow.offsetX, 10),
                        setTop = target.offset().top + parseInt(settings.arrow.offsetY, 10);

                    if (arrow.hasClass("pageguide-top")) {
                        setTop -= 60;
                    } else if (arrow.hasClass("pageguide-bottom")) {
                        setTop += target.outerHeight() + 15;
                    } else {
                        setTop += 5;
                    }

                    if (arrow.hasClass("pageguide-right")) {
                        setLeft += target.outerWidth(false) + 15;
                    } else if (arrow.hasClass("pageguide-left")) {
                        setLeft -= 65;
                    } else {
                        setLeft += 5;
                    }

                    arrow.css({
                        "left": setLeft + "px",
                        "top": setTop + "px"
                    });
                });

                // Position the shadow
                if (this.$shadow.is(':visible')) {
                    this._showShadow(this.$visibleItems[this.curIdx], false);
                }

                return this;
            },

            autoAdvance: function(toggle) {
                if (toggle === undefined || !!toggle) {
                  if (this.advanceTimer) return this;
                  this.advanceTimer = setInterval($.proxy(this.next, this), this.settings.autoAdvanceInterval * 1000);
                } else {
                  clearInterval(this.advanceTimer);
                  this.advanceTimer = null;
                }

                return this;
            },

            /**
             * Check if a guide is currently loaded.
             *
             * @return {Boolean}
             */
            isLoaded: function() {
              return !!this._guide;
            },

            /**
             * Check if the guide is running.
             *
             * @return {Boolean}
             */
            isOpen: function() {
                return $('body').is('.pageguide-open');
            },

            _registerWrapperHandlers: function() {
                /* interaction: open/close PG interface */
                this.$toggle.on('click', $.proxy(function(e) {
                    if ($('body').is('.pageguide-open')) {
                        this.close();
                    } else {
                        this.open();
                    }
                }, this));

                this.$message.on('click', '.pageguide-close', $.proxy(function(e) {
                    this.close();
                }, this));

                this.$message.on('click', '.pageguide-index', $.proxy(function(e) {
                    e.stopPropagation();

                    var item = this.$visibleItems[this.curIdx];
                    if (!this._isScrolledIntoView(item)) {
                        this._scrollIntoView(item);
                    }
                }, this));

                /* interaction: fwd click */
                this.$fwd.on('click', $.proxy(function(e) {
                    e.stopPropagation();

                    this.autoAdvance(false);
                    this.next();
                }, this));

                /* interaction: back click */
                this.$back.on('click', $.proxy(function(e) {
                    e.stopPropagation();

                    this.autoAdvance(false);
                    this.previous();
                }, this));

                /* shadow pulse animation end */
                this.$shadow.on('animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd', '.pageguide-shadow-pulse', function () {
                    $(this).hide();
                });

                /* register resize callback */
                $(window).resize($.proxy(function() {
                    this._onResize();
                }, this));

                /* register teardown handler */
                this.$wrapper.on("destroyed", $.proxy(this.destroy, this));
            },

            _removeWrapperHandlers: function () {
                this.$wrapper.off();
                this.$toggle.off();
                this.$message.off();
                this.$shadow.off();
                this.$fwd.off();
                this.$back.off();

                $(window).unbind('resize.pageguide');
            },

            _registerGuideHandlers: function() {
                if (!this.$guide) {
                    return false;
                }

                /* interaction: item click */
                this.$guide.on('click', 'li', $.proxy(function(e) {
                    e.stopPropagation();

                    var newIdx = $(e.currentTarget).data('idx');

                    this.$wrapper.trigger('click.pageguide', newIdx);

                    if (this.curIdx == newIdx) {
                        return;
                    }

                    this.showStep(newIdx);
                }, this));
            },

            _removeGuideHandlers: function() {
                this.$guide.off();
            },

            _registerCustomHandlers: function() {
                var that = this,
                    events = $.extend(true, {}, this.settings.events, this._guide.events);

                if (!$.isEmptyObject(events)) {
                    $.each(events, function (i) {
                        that.$wrapper.on(i + '.pageguide', this);
                    });
                }
            },

            _removeCustomHandlers: function() {
                var that = this,
                    events = $.extend(true, {}, this.settings.events, this._guide.events);

                if (!$.isEmptyObject(events)) {
                    $.each(events, function (i) {
                        that.$wrapper.off(i + '.pageguide', this);
                    });
                }
            },

            _registerCustomStepHandlers: function() {
                var that = this;
                $.each(this._guide.steps, function (i) {
                    var $step = $(this.elem),
                        settings = $.extend(true, {}, that.settings.step, this);

                    if ($.isEmptyObject(settings.events)) {
                        return;
                    }

                    for (var j in settings.events) {
                        if (!settings.events.hasOwnProperty(j)) {
                            continue;
                        }

                        $step.on(j + '.pageguide', settings.events[j]);
                    }
                });
            },

            _removeCustomStepHandlers: function() {
                $.each(this.$allItems, function (i) {
                    $(this).off('click.pageguide, show.pageguide, hide.pageguide, select.pageguide, deselect.pageguide');
                });
            },

            _wait: function(callback) {
                var that = this;
                var interval = window.setInterval(function() {
                    if (!$(that.settings.loadingSelector).is(':visible')) {
                        callback();
                        clearInterval(interval);
                    }
                }, 250);
            },

            _rollNumber: function($numWrapper, newText, left) {
                $numWrapper.animate({
                    'text-indent': (left ? '' : '-') + '50px'
                }, 'fast', function() {
                    $numWrapper.html(newText);
                    $numWrapper.css({
                        'text-indent': (left ? '-' : '') + '50px'
                    }, 'fast').animate({
                        'text-indent': "0"
                    }, 'fast');
                });

                return this;
            },

            _scrollIntoView: function(elem) {
                var $t = $(elem).data('tourtarget') ? $($(elem).data('tourtarget')) : $(elem),
                    dvh = $(window).height(),
                    msgh = this.$message.outerHeight(),
                    elh = $t.outerHeight(),
                    dvtop = $(window).scrollTop(),
                    eltop = $t.offset().top,
                    elbtm = eltop + elh,
                    mgn = $(elem).data('options') ? $.extend({}, this.settings.step.margin, $(elem).data('options').margin || {}) : this.settings.step.margin,
                    mgnb = Math.max(mgn.bottom, msgh + 15);

                var scrollTo = ((eltop <= dvtop + mgn.top) || (elh > (dvh - mgnb))) ? eltop - mgn.top : (elbtm - (dvh - mgnb));

                $('html,body').animate({
                  scrollTop: scrollTo
                }, {
                  complete: $.proxy(this._onResize, this),
                  duration: 500
                });
            },

            _isScrolledIntoView: function(elem) {
                var $t = $(elem).data('tourtarget') ? $($(elem).data('tourtarget')) : $(elem),
                    msgh = this.$message.outerHeight(),
                    dvtop = $(window).scrollTop(),
                    dvbtm = dvtop + $(window).height(),
                    eltop = $t.offset().top,
                    elbtm = eltop + $t.outerHeight(),
                    mgn = $(elem).data('options') ? $.extend({}, this.settings.step.margin, $(elem).data('options').margin || {}) : this.settings.step.margin;

                return (eltop >= dvtop + mgn.top) && (elbtm <= dvbtm - Math.max(mgn.bottom, msgh + 15));
            },

            _onExpand: function() {
                /* set up initial state */
                this.refresh();
                this.curIdx = 0;

                /* add number tags and PG shading elements */
                var that = this;
                this.$visibleItems.each(function(i) {
                    var settings = $.extend(true, {}, that.settings.step, $(this).data('options') || {}),
                        zIndex = settings.zIndex ? settings.zIndex : $($(this).data('tourtarget')).zIndex() + 2;

                    $(this).css('z-index', zIndex);
                    $(this).prepend('<ins>' + (i + 1) + '</ins>');
                    $(this).data('idx', i);
                });

                if ((this.settings.autoAdvanceInterval || this.settings.autoStart) && this.$visibleItems.size() > 0) {
                    if (this.settings.autoStartDelay) {
                        setTimeout($.proxy(function () {
                            this.showStep(0);
                        }, this), this.settings.autoStartDelay);
                    } else {
                        this.showStep(0);
                    }
                }
                if (this.settings.autoAdvanceInterval) {
                    this.autoAdvance(true);
                }
            },

            _showShadow: function(elem, pulse) {
                if (pulse === undefined) {
                    pulse = this.settings.pulse;
                }

                var $t = $(elem).data('tourtarget') ? $($(elem).data('tourtarget')) : $(elem),
                    settings = $.extend(true, {}, this.settings.step, $(elem).data('options') || {}),
                    padding = settings.shadowPadding ? parseInt(settings.shadowPadding, 10) : 0,
                    zIndex = $t.zIndex() + 1,
                    $pulse = this.$shadow.children('.pageguide-shadow-pulse');

                if (!!pulse) $pulse.hide();
                this.$shadow.css({
                    height: $t.outerHeight(),
                    width: $t.outerWidth(false),
                    padding: padding,
                    top: $t.offset().top - padding,
                    left: $t.offset().left - padding,
                    zIndex: zIndex
                }).toggleClass('pageguide-shadow-active', true).show();
                if (!!pulse) $pulse.show();

                return this;
            },

            _onResize: function() {
                if (!this.isOpen()) return this;

                this.refresh();

                if($.debounce !== undefined) {
                    $.debounce($.proxy(function() {
                        //noinspection JSPotentiallyInvalidUsageOfThis
                        this.$wrapper.trigger('resize.pageguide');
                    }, 300), this);
                } else {
                    this.$wrapper.trigger('resize.pageguide');
                }
            },

            /* Directly from jQuery UI Core
             * http://code.google.com/p/jquery-ui/source/browse/trunk/ui/jquery.ui.core.js */
            _zIndex: function(zIndex) {
                if (zIndex !== undefined) {
                    return this.css('zIndex', zIndex);
                }

                if (this.length) {
                    var elem = $(this[0]), position, value;
                    while (elem.length && elem[0] !== document) {
                        // Ignore z-index if position is set to a value where z-index is ignored by the browser
                        // This makes behavior of this function consistent across browsers
                        // WebKit always returns auto if the element is positioned
                        position = elem.css('position');
                        if (position == 'absolute' || position == 'relative' || position == 'fixed')
                        {
                            // IE returns 0 when zIndex is not specified
                            // other browsers return a string
                            // we ignore the case of nested elements with an explicit value of 0
                            // <div style="z-index: -10;"><div style="z-index: 0;"></div></div>
                            value = parseInt(elem.css('zIndex'), 10);
                            if (!isNaN(value) && value !== 0) {
                                return value;
                            }
                        }
                        elem = elem.parent();
                    }
                }

                return 0;
            }
        }
    });

    var pg = null;
    $.pageguide = function (fn, options) {
        // Return the PageGuide object, create one if necessary
        if (arguments.length == 0) {
            return pg ? pg : (pg = new PageGuide());
        }

        if (pg && typeof pg[fn] == 'function') {
            if (fn == 'destroy') {
                pg.destroy();
                pg = null;
                return;
            }

            return pg[fn].apply(pg, Array.prototype.slice.call(arguments, 1));
        }

        return pg ? pg.load(fn, options) : (pg = new PageGuide(fn, options));
    };
}));