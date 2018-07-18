var ImageView = ( function(imageView) {
    'use strict';
    
    if(!imageView || !imageView.Overlay) {
        throw "imageView and imageView.Overlay must be initialized first";
    }
    
    var DEFAULT_CURSOR = "default";
    var REMOVE_CURSOR = "not-allowed";

    var _hbAdd = 5;
    var _sideClickPrecision = 4;
    var _debug = true;
    
    imageView.Remove = function(viewer, startCondition) {
        this.viewer = viewer;
        this.startCondition = startCondition;
        this.active = true;
        this.currentOverlay = null;
        this.overlays = [];
        this.finishedObservable = new Rx.Subject();
        _addInputHook(this);
    }
    imageView.Remove.prototype.addOverlay = function(overlay) {
        if(!this.overlays.includes(overlay)) {
            this.overlays.push(overlay);
            return true;
        }
        return false;
    }
    imageView.Remove.prototype.removeOverlay = function(overlay) {
        if(this.overlays.includes(overlay)) {
            var index = this.overlays.indexOf(overlay);
            this.overlays.splice(index, 1);
            return true;
        }
        return false;
    }
    imageView.Remove.prototype.finishedRemoving = function() {
        return this.finishedObservable;
    }
    imageView.Remove.prototype.close = function() {
        this.active = false;
        this.finishedObservable.onCompleted();
    }
    imageView.Remove.prototype.isActive = function() {
        return this.active;
    }
    imageView.Remove.prototype.getContainingOverlay = function(point) {
        for(var index in this.overlays) {
            var overlay = this.overlays[index];
            if(overlay.contains(point, _sideClickPrecision, true)) {
                return overlay;
            } 
        }
        return null;
    }

    
    function _addInputHook(remove) {
        var hook = remove.viewer.addViewerInputHook( {
            hooks: [ {
                tracker: "viewer",
                handler: "clickHandler",
                hookHandler: function(event) { _onViewerPress(event, remove) }
            }, {
                tracker: "viewer",
                handler: "moveHandler",
                hookHandler: function(event) { _onViewerMove(event, remove) }
            } ]
        } );
        return hook;
    }

    
    
    function _isInside( rect, point, extra ) {

        return point.x > rect.getTopLeft().x - extra && point.x < ( rect.x + rect.width + extra ) && point.y > rect.y - extra
                && point.y < ( rect.y + rect.height + extra );
    }

    function _onViewerMove( event, remove ) {
        if (remove.isActive() && remove.startCondition(event.originalEvent) ) {
            
            var coords = event.position;
            var overlay = remove.getContainingOverlay(coords);
            var viewerElement = remove.viewer.element;
            if(overlay) {
                remove.currentOverlay = overlay;
            } else {
                remove.currentOverlay = null;
            }
            
            if ( remove.currentOverlay ) {
                $( viewerElement ).css( {
                    cursor: REMOVE_CURSOR
                } );
            } else {
                $( viewerElement ).css( {
                    cursor: DEFAULT_CURSOR
                } );
            }
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function _onViewerPress( event, remove ) {
        if ( remove.isActive()  && remove.startCondition(event.originalEvent)) {
            if ( remove.currentOverlay) {
                remove.currentOverlay.remove();
                remove.finishedObservable.onNext(remove.currentOverlay);
                remove.currentOverlay = null;
                return true;
            } else {
                return false;
            }
        }
    }
    function _disableViewerEvent( event, remove ) {
        if ( remove.isTransforming() ) {
            event.preventDefaultAction = true;
            return true;
        }
    }

    
    return imageView;
    
} )( ImageView );
