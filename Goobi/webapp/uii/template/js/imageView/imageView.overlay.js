var ImageView = ( function(imageView) {
    'use strict';
    
    if(!imageView) {
        throw "Image view must exist";
    }    
    
    /**
     * rect must be in openSeadragon image coordinates (0 <= x <= 1 / 0 <= y <= height/width)
     */
    imageView.Overlay = function(rect, viewer, style) {
            
        // OpenSeadragon viewer
        this.viewer = viewer;
        // OpenSeadragon rect
        this.rect = rect;
        // object containing properties borderWidth and borderColor
        this.style = style;
            
    }
    
    imageView.Overlay.prototype.getRotation = function() {
        return this.viewer.viewport.getRotation();
    }
    
    imageView.Overlay.prototype.remove = function() {
        if(this.eventHandler) {            
            this.viewer.removeHandler( 'update-viewport', this.eventHandler, this );
            this.viewer.forceRedraw();
        }
    }

    imageView.Overlay.prototype.draw = function() {
        if(this.eventHandler) {            
            this.viewer.removeHandler( 'update-viewport', this.eventHandler, this );
        }
        _drawRect({userData: this});
        this.eventHandler = function(event) {
            _drawRect(event)
        }
        this.viewer.addHandler( 'update-viewport', this.eventHandler, this );
    }
    
    imageView.Overlay.prototype.contains = function(point, extra, pointInCanvas) {
        var rect = pointInCanvas ? imageView.convertCoordinatesFromImageToCanvas(this.rect, this.viewer) : this.rect;
        return _isInside(rect, point, extra);
    }
    
    imageView.Overlay.prototype.getHitArea = function(point, extra, pointInCanvas) {
        var rect = pointInCanvas ? imageView.convertCoordinatesFromImageToCanvas(this.rect, this.viewer) : this.rect;
        if(_isInside(rect, point, extra)) {
            var area = _findCorner(rect, point, extra);
            if(!area) {
                area = _findEdge(rect, point, extra);
            }
//            if(!area && _isInside(rect, point, 0)){
//                area = imageView.Overlay.HitAreas.CENTER;
//            }
        }
        return area;
    }
    
    imageView.Overlay.convertStringToRect = function(string) {
        var parts = string.split(",");
        if(parts && parts.length == 4) {            
            var rect = new OpenSeadragon.Rect(parseFloat(parts[0]), parseFloat(parts[1]), parseFloat(parts[2]), parseFloat(parts[3]));
            return rect;
        } else {
            throw "Cannot convert string '" + string + "' to Rectangle";
        }
    }
    
    imageView.Overlay.convertRectToString = function(rect, decimalPlaces) {
        if(!decimalPlaces) {
            decimalPlaces = 0;
        }
        return rect.x.toFixed(decimalPlaces) + "," +  rect.y.toFixed(decimalPlaces) + "," +  rect.width.toFixed(decimalPlaces) + "," +  rect.height.toFixed(decimalPlaces);
    }
    
    imageView.Overlay.drawPoint = function(point, viewer, color, radius) {
        _drawPoint({userData: [point, viewer, color, radius] });
//        var eventHandler = function(event) {
//            _drawPoint(event)
//        }
//        this.viewer.removeHandler( 'update-viewport', _drawRect, this );
//        viewer.addHandler( 'update-viewport', eventHandler, [point, viewer, color, radius] );
    }
    
    imageView.Overlay.HitAreas = {
            TOP: "t",
            BOTTOM: "b",
            RIGHT: "r",
            LEFT: "l",
            TOPLEFT: "tl",
            TOPRIGHT: "tr",
            BOTTOMLEFT: "bl",
            BOTTOMRIGHT: "br",
            CENTER: "c",
            isCorner: function( area ) {
                return area === this.TOPRIGHT || area === this.TOPLEFT || area === this.BOTTOMLEFT || area === this.BOTTOMRIGHT;
            },
            isEdge: function( area ) {
                return area === this.TOP || area === this.BOTTOM || area === this.LEFT || area === this.RIGHT;
            },
            getCursor: function( area ) {
                var rotated = false;//draw.viewer.viewport.getRotation() % 180 === 90;
                if ( area === this.TOPLEFT || area === this.BOTTOMRIGHT ) {
                    return rotated ? "nesw-resize" : "nwse-resize";
                }
                else if ( area === this.TOPRIGHT || area === this.BOTTOMLEFT ) {
                    return rotated ? "nwse-resize" : "nesw-resize";
                }
                else if ( area === this.TOP || area === this.BOTTOM ) {
                    return rotated ? "ew-resize" : "ns-resize";
                }
                else if ( area === this.RIGHT || area === this.LEFT ) {
                    return rotated ? "ns-resize" : "ew-resize";
                }
                else if ( area === this.CENTER ) {
                    return "move";
                }
                else {
                    return DEFAULT_CURSOR;
                }
            }
        }
    
    function _drawRect(event) {
        var overlay = event.userData;
        var context = overlay.viewer.drawer.context;
        var rect = ImageView.convertCoordinatesFromImageToCanvas(overlay.rect, overlay.viewer).times(window.devicePixelRatio);
        context.beginPath();        
        context.lineWidth = overlay.style.borderWidth;
        context.strokeStyle = overlay.style.borderColor;
        context.rect(rect.x, rect.y, rect.width, rect.height);
        context.stroke();
    }
    
    
    
    function _drawPoint(event) {
        var point = event.userData[0].times(window.devicePixelRatio);
        var viewer = event.userData[1];
        var color = event.userData[2];
        var radius = event.userData[3];
        var context = viewer.drawer.context;
//        console.log("draw on canvas ", viewer.drawer.context)
//        var point_canvas = ImageView.convertPointFromImageToCanvas(point, viewer);
        context.beginPath();
        if(color) {
            context.fillStyle = color;
        }
        context.arc(point.x, point.y, radius, 0, 2*Math.PI, true);
        context.fill();
    }
    
    function _isInside( rect, point, extra ) {
//        console.log("point = " + point.x.toFixed(3) + "/" + point.y.toFixed(3));
//        console.log("rect ul = " + rect.getTopLeft().x.toFixed(3) + "/" + rect.getTopLeft().y.toFixed(3));
        return point.x > (rect.getTopLeft().x - extra) && point.x < ( rect.getBottomRight().x + extra ) && 
        point.y > (rect.getTopLeft().y - extra)  && point.y < ( rect.getBottomRight().y + extra );
    }

    /*
     * Determine the side of the rectangle rect the point lies on or closest at <=maxDist
     * distance
     */
    function _findEdge( rect, point, maxDist ) {
        var distanceToLeft = _distToSegment( point, rect.getTopLeft(), rect.getBottomLeft() );
        var distanceToBottom = _distToSegment( point, rect.getBottomLeft(), rect.getBottomRight() );
        var distanceToRight = _distToSegment( point, rect.getTopRight(), rect.getBottomRight() );
        var distanceToTop = _distToSegment( point, rect.getTopLeft(), rect.getTopRight() );
        
        var minDistance = Math.min( distanceToLeft, Math.min( distanceToRight, Math.min( distanceToTop, distanceToBottom ) ) );
        if ( minDistance <= maxDist ) {
            if ( distanceToLeft === minDistance ) {
                return imageView.Overlay.HitAreas.LEFT;
            }
            if ( distanceToRight === minDistance ) {
                return imageView.Overlay.HitAreas.RIGHT;
            }
            if ( distanceToTop === minDistance ) {
                return imageView.Overlay.HitAreas.TOP;
            }
            if ( distanceToBottom === minDistance ) {
                return imageView.Overlay.HitAreas.BOTTOM;
            }
        }
        return "";
    }

    /*
     * Determine the cornder of the rectangle rect the point lies on or closest at
     * <=maxDist distance
     */
    function _findCorner( rect, point, maxDist ) {
        var distanceToTopLeft = _dist( point, rect.getTopLeft() );
        var distanceToBottomLeft = _dist( point, rect.getBottomLeft() );
        var distanceToTopRight = _dist( point, rect.getTopRight() );
        var distanceToBottomRight = _dist( point, rect.getBottomRight() );
        
        var minDistance = Math.min( distanceToTopLeft, Math.min( distanceToTopRight, Math.min( distanceToBottomLeft, distanceToBottomRight ) ) );
        if ( minDistance <= maxDist ) {
            if ( distanceToTopLeft === minDistance ) {
                return imageView.Overlay.HitAreas.TOPLEFT;
            }
            if ( distanceToTopRight === minDistance ) {
                return imageView.Overlay.HitAreas.TOPRIGHT;
            }
            if ( distanceToBottomLeft === minDistance ) {
                return imageView.Overlay.HitAreas.BOTTOMLEFT;
            }
            if ( distanceToBottomRight === minDistance ) {
                return imageView.Overlay.HitAreas.BOTTOMRIGHT;
            }
        }
        return "";
    }

    function _sqr( x ) {
        return x * x
    }
    function _dist2( v, w ) {
        return _sqr( v.x - w.x ) + _sqr( v.y - w.y )
    }
    function _dist( v, w ) {
        return Math.sqrt( _dist2( v, w ) )
    }
    function _distToSegmentSquared( p, v, w ) {
        var l2 = _dist2( v, w );
        if ( l2 == 0 )
            return _dist2( p, v );
        var t = ( ( p.x - v.x ) * ( w.x - v.x ) + ( p.y - v.y ) * ( w.y - v.y ) ) / l2;
        if ( t < 0 )
            return _dist2( p, v );
        if ( t > 1 )
            return _dist2( p, w );
        return _dist2( p, {
            x: v.x + t * ( w.x - v.x ),
            y: v.y + t * ( w.y - v.y )
        } );
    }
    function _distToSegment( point, lineP1, lineP2 ) {
        return Math.sqrt( _distToSegmentSquared( point, lineP1, lineP2 ) );
    }



    


    
return imageView;

})( ImageView );