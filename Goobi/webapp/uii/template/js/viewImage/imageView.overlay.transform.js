var ImageView = ( function(imageView) {
    'use strict';
    
    if(!imageView || !imageView.Overlay) {
        throw "imageView and imageView.Overlay must be initialized first";
    }
    
    var DEFAULT_CURSOR = "default";

    var _hbAdd = 5;
    var _sideClickPrecision = 0.01;
    var _debug = true;
    
    imageView.Transform = function(viewer, style, startCondition) {

        this.viewer = viewer;
        this.style = style;
        this.startCondition = startCondition;
        this.active = true;
        this.transforming = false;
        this.currentOverlay = null;
        this.drawArea = null;
        this.startPoint = null;
        this.overlays = [];
        this.finishedObservable = new Rx.Subject();
        _addInputHook(this);
    }
    imageView.Transform.prototype.addOverlay = function(overlay) {
        if(!this.overlays.includes(overlay)) {
            this.overlays.push(overlay);
            return true;
        }
        return false;
    }
    imageView.Transform.prototype.removeOverlay = function(overlay) {
        if(this.overlays.includes(overlay)) {
            console.log("remove overlay");
            let index = this.overlays.indexOf(overlay);
            console.log("overlays ", this.overlays);
            this.overlays.splice(index, 1);
            console.log("overlays ", this.overlays);
            return true;
        }
        return false;
    }
    imageView.Transform.prototype.finishedTransforming = function() {
        return this.finishedObservable;
    }
    imageView.Transform.prototype.close = function() {
        this.active = false;
        this.finishedObservable.onCompleted();
    }
    imageView.Transform.prototype.isActive = function() {
        return this.active;
    }
    imageView.Transform.prototype.isTransforming = function() {
        return this.transforming;
    }
    imageView.Transform.prototype.getContainingOverlay = function(point) {
        for(let overlay of this.overlays) {
            if(overlay.contains(point, _sideClickPrecision)) {
                return overlay;
            } 
        }
        return null;
    }

    
    function _addInputHook(transform) {
        var hook = transform.viewer.addViewerInputHook( {
            hooks: [ {
                tracker: "viewer",
                handler: "clickHandler",
                hookHandler: function(event) { _disableViewerEvent(event, transform) }
            }, {
                tracker: "viewer",
                handler: "dragHandler",
                hookHandler: function(event) { _onViewerDrag(event, transform) }
            }, {
                tracker: "viewer",
                handler: "pressHandler",
                hookHandler: function(event) { _onViewerPress(event, transform) }
            }, {
                tracker: "viewer",
                handler: "dragEndHandler",
                hookHandler: function(event) { _onViewerDragEnd(event, transform) }
            }, {
                tracker: "viewer",
                handler: "releaseHandler",
                hookHandler: function(event) { _onViewerRelease(event, transform) }
            }, {
                tracker: "viewer",
                handler: "moveHandler",
                hookHandler: function(event) { _onViewerMove(event, transform) }
            } ]
        } );
        return hook;
    }



    
    function _createOverlay(x,y,width,height) {
        var element = document.createElement("div");
        element.classList.add(_overlayStyleClass);
        
        var location = new OpenSeadragon.Rect(x,y,width,height);
        var overlay = {
                element: element,
                location: location
            }
        draw.viewer.addOverlay(overlay);
        return overlay;
    }
    
    
    function _isInside( rect, point, extra ) {

        return point.x > rect.getTopLeft().x - extra && point.x < ( rect.x + rect.width + extra ) && point.y > rect.y - extra
                && point.y < ( rect.y + rect.height + extra );
    }

    function _onViewerMove( event, transform ) {
        if ( !transform.isTransforming() && transform.isActive() && transform.startCondition(event.originalEvent) ) {
            
            let coords = ImageView.convertPointFromCanvasToImage(event.position, transform.viewer);
            coords = ImageView.getPointInUnrotatedImage(coords, transform.viewer);
            let overlay = transform.getContainingOverlay(coords);
            var viewerElement = transform.viewer.element;
            if(overlay) {
//                console.log("point is inside overlay ", overlay);
                transform.currentOverlay = overlay;
                transform.drawArea = overlay.getHitArea(coords, _sideClickPrecision);
            } else {
                transform.currentOverlay = null;
                transform.drawArea = null;
            }
            
            if ( transform.drawArea ) {
                $( viewerElement ).css( {
                    cursor: imageView.Overlay.HitAreas.getCursor( transform.drawArea )
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
    
    function _onViewerPress( event, transform ) {
        console.log("pressed ", transform);
        if ( transform.isActive()  && transform.startCondition(event.originalEvent)) {
            if ( transform.currentOverlay && transform.drawArea ) {
                let coords = ImageView.convertPointFromCanvasToImage(event.position, transform.viewer);
                coords = ImageView.getPointInUnrotatedImage(coords, transform.viewer);
                transform.startPoint = coords;
                transform.transforming = true;
                event.preventDefaultAction = true;
                console.log("start transforming at ", transform.startPoint);
                return true;
            } else {
                transform.transforming = false;
                return false;
            }
        }
    }
    
    function _onViewerDrag( event, transform ) {
        if ( transform.isTransforming() ) {
            let newPoint = ImageView.convertPointFromCanvasToImage(event.position, transform.viewer);
            newPoint = ImageView.getPointInUnrotatedImage(newPoint, transform.viewer);
            let rect = transform.currentOverlay.rect;
            var topLeft = null;//rect.getTopLeft();
            var bottomRight = null;//rect.getBottomRight();
            // if(_debug)console.log("Draw location = " + newPoint);
            if ( transform.drawArea === imageView.Overlay.HitAreas.TOPLEFT ) {
                topLeft = new OpenSeadragon.Point( Math.min( newPoint.x, rect.getBottomRight().x ), Math.min( newPoint.y, rect.getBottomRight().y ) );
                bottomRight = rect.getBottomRight();
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.TOPRIGHT ) {
                topLeft = new OpenSeadragon.Point( rect.getTopLeft().x, Math.min( newPoint.y, rect.getBottomRight().y ) );
                bottomRight = new OpenSeadragon.Point( Math.max( newPoint.x, rect.getTopLeft().x ), rect.getBottomRight().y );
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.BOTTOMLEFT ) {
                topLeft = new OpenSeadragon.Point( Math.min( newPoint.x, rect.getBottomRight().x ), rect.getTopLeft().y );
                bottomRight = new OpenSeadragon.Point( rect.getBottomRight().x, Math.max( newPoint.y, rect.getTopLeft().y ) );
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.BOTTOMRIGHT ) {
                topLeft = rect.getTopLeft();
                bottomRight = new OpenSeadragon.Point( Math.max( newPoint.x, rect.getTopLeft().x ), Math.max( newPoint.y, rect.getTopLeft().y ) );
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.LEFT ) {
                topLeft = new OpenSeadragon.Point( Math.min( newPoint.x, rect.getBottomRight().x ), rect.getTopLeft().y );
                bottomRight = rect.getBottomRight();
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.RIGHT ) {
                topLeft = rect.getTopLeft();
                bottomRight = new OpenSeadragon.Point( Math.max( newPoint.x, rect.getTopLeft().x ), rect.getBottomRight().y );
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.TOP ) {
//                var rectOrig = new OpenSeadragon.Rect(rect.x, newPoint.y, rect.width, rect.height+(rect.y-newPoint.y));
//                var rectRotated = _createOverlay(rectOrig); 
//                rect = _normalize(rectRotated);
//                rect = rectOrig;
                topLeft = new OpenSeadragon.Point( rect.getTopLeft().x, Math.min( newPoint.y, rect.getBottomRight().y ) );
                bottomRight = rect.getBottomRight();
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.BOTTOM ) {
                topLeft = rect.getTopLeft();
                bottomRight = new OpenSeadragon.Point( rect.getBottomRight().x, Math.max( newPoint.y, rect.getTopLeft().y ) );
            }
            else if ( transform.drawArea === imageView.Overlay.HitAreas.CENTER && transform.startPoint ) {
                var dx = transform.startPoint.x - newPoint.x;
                var dy = transform.startPoint.y - newPoint.y;
                rect.x -= dx;
                rect.y -= dy;
                transform.startPoint = newPoint;
            }
            
            if(topLeft && bottomRight) {
                rect = new OpenSeadragon.Rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
            }

            transform.currentOverlay.rect = rect;
            transform.viewer.forceRedraw();
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function _createOverlay(rect) {
        var topLeft = rect.getTopLeft();
        var bottomRight = rect.getBottomRight();
//        var topLeft = {x: Math.min(p1.x, p2.x), y : Math.min(p1.y, p2.y)};
//        var bottomRight = {x: Math.max(p1.x, p2.x),y: Math.max(p1.y, p2.y)}
//        console.log("top left ", topLeft);
//        console.log("bottom right ", bottomRight);
        var topLeftRotated = _rotatePoint(topLeft);
        var bottomRightRotated = _rotatePoint(bottomRight);
        
        var x = topLeft.x;
        var y = topLeft.y;
        var width = bottomRightRotated.x-topLeftRotated.x;
        var height = bottomRightRotated.y-topLeftRotated.y;
        
        var rectRotated = new OpenSeadragon.Rect(x,y, width, height);
        return rectRotated;
    }
    
    function _normalize(rect) {
        var p1 = rect.getTopLeft();
        var p2 = rect.getBottomRight();
        var topLeft = {x: Math.min(p1.x, p2.x), y : Math.min(p1.y, p2.y)};
        var bottomRight = {x: Math.max(p1.x, p2.x),y: Math.max(p1.y, p2.y)}
        console.log("top left ", topLeft);
        console.log("bottom right ", bottomRight);
        var norm = new OpenSeadragon.Rect(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
        return norm;
    }
    
    function _onViewerRelease( event, transform ) {
        console.log("release ", transform);
        if ( transform.isActive() ) {
            if ( transform.transforming ) {
                transform.finishedObservable.onNext(transform.currentOverlay);
            }
            transform.transforming = false;
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function _onViewerDragEnd( event, transform ) {
        if ( transform.isTransforming() ) {
            transform.transforming = false;
            event.preventDefaultAction = true;
            return true;
        }
    }
    
    function _disableViewerEvent( event, transform ) {
        if ( transform.isTransforming() ) {
            event.preventDefaultAction = true;
            return true;
        }
    }

    
    return imageView;
    
} )( ImageView );
