var ImageView = ( function(imageView) {
    'use strict';
    
    if(!imageView || !imageView.Overlay) {
        throw "imageView and imageView.Overlay must be initialized first";
    }

    var _hbAdd = 5;
    var _minDistanceToExistingRect = 0.01;

    
    imageView.Draw = function(viewer, style, startCondition) {
        
        this.viewer = viewer;
        this.style = style;
        this.startCondition = startCondition;
        this.active = true;
        this.drawing = false;
        this.currentRect = null;
        this.startPoint = null;
        this.finishedObservable = new Rx.Subject();
        
        _addInputHook(this);
    }
    imageView.Draw.prototype.finishedDrawing = function() {
        return this.finishedObservable;
    }
    imageView.Draw.prototype.close = function() {
        this.active = false;
        this.finishedObservable.onCompleted();
    }
    imageView.Draw.prototype.isActive = function() {
        return this.active;
    }
    imageView.Draw.prototype.isDrawing = function() {
        return this.drawing;
    }
    /*
     * Position is in viewer element coordinates
     */
    imageView.Draw.prototype.createEmptyRectAt = function(position) {
        
        this.currentRect = new OpenSeadragon.Rect(position.x, position.y, 0,0);
        this.startPoint = position;
        _drawRect(this.currentRect, this.style, this.viewer.drawer.context);
        
    }
    imageView.Draw.prototype.updateOverlay = function(position) {
            this.viewer.forceRedraw();
            var draw = this;
            this.viewer.addOnceHandler( 'update-viewport', function( event ) {
                if(draw.isDrawing()) {
                    draw.currentRect.x = Math.min(draw.startPoint.x, position.x);
                    draw.currentRect.y = Math.min(draw.startPoint.y, position.y);
                    draw.currentRect.width = Math.abs(draw.startPoint.x-position.x);
                    draw.currentRect.height = Math.abs(draw.startPoint.y-position.y);
                    _drawRect(draw.currentRect, draw.style, draw.viewer.drawer.context);
                }
            });
    }
    
    function _drawRect(rect, style, context) {
        rect = rect.times(window.devicePixelRatio);
        context.beginPath();
        context.lineWidth = style.borderWidth;
        context.strokeStyle = style.borderColor;
        context.rect(rect.x, rect.y, rect.width, rect.height);
        context.stroke();
    }
    
    function _addInputHook(draw) {
        var hook = draw.viewer.addViewerInputHook( {
            hooks: [ {
                tracker: "viewer",
                handler: "clickHandler",
                hookHandler: function(event) { _disableViewerEvent(event, draw) }
            // }, {
            // tracker: "viewer",
            // handler: "scrollHandler",
            // hookHandler: _disableViewerEvent
            }, {
                tracker: "viewer",
                handler: "dragHandler",
                hookHandler: function(event) { _onViewerDrag(event, draw) }
            }, {
                tracker: "viewer",
                handler: "pressHandler",
                hookHandler: function(event) { _onViewerPress(event, draw) }
            }, {
                tracker: "viewer",
                handler: "dragEndHandler",
                hookHandler: function(event) { _onViewerDragEnd(event, draw) }
            } ]
        } );
        return hook;
    }


    function _onViewerPress( event, draw) {
        if ( draw.isActive() && draw.startCondition(event.originalEvent)) {
            var position = new OpenSeadragon.Point(event.position.x, event.position.y);//.times(window.devicePixelRatio);
            draw.createEmptyRectAt(position);
            event.preventDefaultAction = false;
            draw.drawing = true;
            return true;
        }
    }
    
    function _onViewerDrag( event, draw) {
        if ( draw.isDrawing() ) {
            var position = new OpenSeadragon.Point(event.position.x, event.position.y);//.times(window.devicePixelRatio);
            draw.updateOverlay(position);
            event.preventDefaultAction = true;
            return true; 
        }
    }
    
    function _onViewerDragEnd( event, draw) {
        if ( draw.isDrawing() ) {
            draw.drawing = false;
            var rect = ImageView.convertCoordinatesFromCanvasToImage(draw.currentRect, draw.viewer);
            var overlay = new ImageView.Overlay(rect, draw.viewer, draw.style);
            console.log("rect ", overlay);
            draw.finishedObservable.onNext(overlay);
            event.preventDefaultAction = true;
            return true;
        }
        
    }
    
    function _disableViewerEvent( event, draw) {
        if ( draw.isActive() ) {
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function _rotatePoint(point, angle, center) {
        if(angle !== 0 && !angle) {
            angle = drawRect.viewer.viewport.getRotation();
        }
        if(!center) {
            center = drawRect.viewer.viewport.getCenter();
        }
        angle = angle*Math.PI/180.0;
        var rotated = {
                x : Math.cos(angle) * (point.x-center.x) - Math.sin(angle) * (point.y-center.y) + center.x,
                y : Math.sin(angle) * (point.x-center.x) + Math.cos(angle) * (point.y-center.y) + center.y
        }
        return rotated;
    }
    
    function _checkForRectHit( point ) {
        var i;
        for ( i = 0; i < _rects.length; i++ ) {
            var x = _rects[ i ];
            if ( point.x > x.hitBox.l && point.x < x.hitBox.r && point.y > x.hitBox.t && point.y < x.hitBox.b ) {
                var topLeftHb = {
                    l: x.x - _hbAdd,
                    t: x.y - _hbAdd,
                    r: x.x + _hbAdd,
                    b: x.y + _hbAdd
                };
                var topRightHb = {
                    l: x.x + x.width - _hbAdd,
                    t: x.y - _hbAdd,
                    r: x.x + x.width + _hbAdd,
                    b: x.y + _hbAdd
                };
                var bottomRightHb = {
                    l: x.x + x.width - _hbAdd,
                    t: x.y + x.height - _hbAdd,
                    r: x.x + x.width + _hbAdd,
                    b: x.y + x.height + _hbAdd
                };
                var bottomLeftHb = {
                    l: x.x - _hbAdd,
                    t: x.y + x.height - _hbAdd,
                    r: x.x + _hbAdd,
                    b: x.y + x.height + _hbAdd
                };
                var topHb = {
                    l: x.x + _hbAdd,
                    t: x.y - _hbAdd,
                    r: x.x + x.width - _hbAdd,
                    b: x.y + _hbAdd
                };
                var rightHb = {
                    l: x.x + x.width - _hbAdd,
                    t: x.y + _hbAdd,
                    r: x.x + x.width + _hbAdd,
                    b: x.y + x.height - _hbAdd
                };
                var bottomHb = {
                    l: x.x + _hbAdd,
                    t: x.y + x.height - _hbAdd,
                    r: x.x + x.width - _hbAdd,
                    b: x.y + x.height + _hbAdd
                };
                var leftHb = {
                    l: x.x - _hbAdd,
                    t: x.y + _hbAdd,
                    r: x.x + _hbAdd,
                    b: x.y + x.height - _hbAdd
                };
            }
        }
    }
    
    return imageView;
    
} )( ImageView );
