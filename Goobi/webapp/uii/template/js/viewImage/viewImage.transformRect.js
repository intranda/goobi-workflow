var viewImage = (function(osViewer) {
	'use strict';

	var DEFAULT_CURSOR = "default";

	var _debug = false;

	var _drawingStyleClass = "transforming";

	var _active = false;
	var _drawing = false;
	var _group = null;
	var _finishHook = null;
	var _viewerInputHook = null;
	var _hbAdd = 5;
	var _sideClickPrecision = 0.004;
	var _drawArea = "";
	var _enterPoint = null;

	osViewer.transformRect = {
		init : function() {
			if (_debug) {
				console.log('##############################');
				console.log('osViewer.transformRect.init');
				console.log('##############################');
			}
			_viewerInputHook = osViewer.viewer.addViewerInputHook({
				hooks : [ {
					tracker : "viewer",
					handler : "clickHandler",
					hookHandler : _disableViewerEvent
//				}, {
//					tracker : "viewer",
//					handler : "scrollHandler",
//					hookHandler : _disableViewerEvent
				}, {
					tracker : "viewer",
					handler : "dragHandler",
					hookHandler : _onViewerDrag
				}, {
					tracker : "viewer",
					handler : "pressHandler",
					hookHandler : _onViewerPress
				}, {
					tracker : "viewer",
					handler : "dragEndHandler",
					hookHandler : _onViewerDragEnd
				}, {
					tracker : "viewer",
					handler : "releaseHandler",
					hookHandler : _onViewerRelease
				}, {
					tracker : "viewer",
					handler : "moveHandler",
					hookHandler : _onViewerMove
				} ]
			});
		},
		startDrawing : function(overlay, finishHook) {
			if (_debug)
				console.log("Start drawing");
			osViewer.overlays.setDrawingOverlay(overlay);
			_active = true;
			_group = overlay.group;
			_finishHook = finishHook;
			$(overlay.element).addClass(_drawingStyleClass);
		},
		endDrawing : function() {
			_drawing = false;
			_group = null;
			_finishHook = null;
			_active = false;
			var drawOverlay = osViewer.overlays.getDrawingOverlay();
			if(drawOverlay != null) {				
				$(drawOverlay.element).removeClass(_drawingStyleClass);
				$(drawOverlay.element).css({
					cursor : DEFAULT_CURSOR
				});
			}
		},
		isActive : function() {
			return _active;
		},
		hitAreas : {
			TOP : "t",
			BOTTOM : "b",
			RIGHT : "r",
			LEFT : "l",
			TOPLEFT : "tl",
			TOPRIGHT : "tr",
			BOTTOMLEFT : "bl",
			BOTTOMRIGHT : "br",
			CENTER : "c",
			isCorner : function(area) {
				return area === this.TOPRIGHT || area === this.TOPLEFT
						|| area === this.BOTTOMLEFT
						|| area === this.BOTTOMRIGHT;
			},
			isEdge : function(area) {
				return area === this.TOP || area === this.BOTTOM
						|| area === this.LEFT || area === this.RIGHT;
			},
			getCursor : function(area) {
			    var rotated = osViewer.viewer.viewport.getRotation() % 180 === 90;       
				if (area === this.TOPLEFT || area === this.BOTTOMRIGHT) {
					return rotated ? "nesw-resize" : "nwse-resize";
				} else if (area === this.TOPRIGHT || area === this.BOTTOMLEFT) {
					return rotated ? "nwse-resize": "nesw-resize";
				} else if (area === this.TOP || area === this.BOTTOM) {
					return rotated ? "ew-resize" : "ns-resize";
				} else if (area === this.RIGHT || area === this.LEFT) {
					return rotated ? "ns-resize" : "ew-resize";
				} else if (area === this.CENTER) {
					return "move";
				} else {
					return DEFAULT_CURSOR;
				}
			}
		}
	}

	function _onViewerMove(event) {
		if (!_drawing && _active) {
			var drawPoint = osViewer.viewer.viewport
					.viewerElementToViewportCoordinates(event.position);
			drawPoint = osViewer.overlays.getRotated(drawPoint);
			var overlayRect = osViewer.overlays.getDrawingOverlay().rect;
			var overlayElement = osViewer.overlays.getDrawingOverlay().element;
			var viewerElement = osViewer.viewer.element;
			var area = _findCorner(overlayRect, drawPoint,
					_sideClickPrecision);
			if (!area) {
				area = _findEdge(overlayRect, drawPoint,
						_sideClickPrecision);
			}
			if (!area && osViewer.overlays.contains(overlayRect, drawPoint, 0)) {
				area = osViewer.transformRect.hitAreas.CENTER;
			}
			if (area) {
				$(viewerElement).css({
					cursor : osViewer.transformRect.hitAreas.getCursor(area)
				});
			} else {
			    $(viewerElement).css({
                    cursor : DEFAULT_CURSOR
                });
			}
			event.preventDefaultAction = true;
			return true;
		}
	}

	function _onViewerPress(event) {
		if (_active) {
			if(!osViewer.overlays.getDrawingOverlay()) {
				return false;
			}
			var overlayRect = osViewer.overlays.getDrawingOverlay().rect;
			var overlayElement = osViewer.overlays.getDrawingOverlay().element;
			var drawPoint = osViewer.viewer.viewport
					.viewerElementToViewportCoordinates(event.position);
			drawPoint = osViewer.overlays.getRotated(drawPoint);
			var drawArea = _findCorner(overlayRect, drawPoint,
					_sideClickPrecision);
			if (!drawArea) {
				drawArea = _findEdge(overlayRect, drawPoint,
						_sideClickPrecision);
			}
			if (!drawArea && osViewer.overlays.contains(overlayRect, drawPoint, 0)) {
				drawArea = osViewer.transformRect.hitAreas.CENTER;
			}
			if(_debug)console.log("draw area = " + drawArea);
			if (drawArea) {
				$(overlayElement).tooltip('destroy');
				_enterPoint = drawPoint;
			}
			_drawArea = drawArea;
			event.preventDefaultAction = true;
			return true;
		}
	}

	function _onViewerDrag(event) {
		if (_drawing) {
			var newPoint = osViewer.viewer.viewport
					.viewerElementToViewportCoordinates(event.position);
			newPoint = osViewer.overlays.getRotated(newPoint);
			var rect = osViewer.overlays.getDrawingOverlay().rect;
			var topLeft; 
			var bottomRight;
//			if(_debug)console.log("Draw location = " + newPoint);
			if (_drawArea === osViewer.transformRect.hitAreas.TOPLEFT) {
				topLeft = new OpenSeadragon.Point(Math.min(newPoint.x, rect
						.getBottomRight().x), Math.min(newPoint.y, rect
						.getBottomRight().y));
				bottomRight = rect.getBottomRight();
			} else if (_drawArea === osViewer.transformRect.hitAreas.TOPRIGHT) {
				topLeft = new OpenSeadragon.Point(rect.getTopLeft().x, Math
						.min(newPoint.y, rect.getBottomRight().y));
				bottomRight = new OpenSeadragon.Point(Math.max(newPoint.x, rect
						.getTopLeft().x), rect.getBottomRight().y);
			} else if (_drawArea === osViewer.transformRect.hitAreas.BOTTOMLEFT) {
				topLeft = new OpenSeadragon.Point(Math.min(newPoint.x, rect
						.getBottomRight().x), rect.getTopLeft().y);
				bottomRight = new OpenSeadragon.Point(rect.getBottomRight().x,
						Math.max(newPoint.y, rect.getTopLeft().y));
			} else if (_drawArea === osViewer.transformRect.hitAreas.BOTTOMRIGHT) {
				topLeft = rect.getTopLeft();
				bottomRight = new OpenSeadragon.Point(Math.max(newPoint.x, rect
						.getTopLeft().x), Math.max(newPoint.y, rect
						.getTopLeft().y));
			} else if (_drawArea === osViewer.transformRect.hitAreas.LEFT) {
				topLeft = new OpenSeadragon.Point(Math.min(newPoint.x, rect
						.getBottomRight().x), rect.getTopLeft().y);
				bottomRight = rect.getBottomRight();
			} else if (_drawArea === osViewer.transformRect.hitAreas.RIGHT) {
				topLeft = rect.getTopLeft();
				bottomRight = new OpenSeadragon.Point(Math.max(newPoint.x, rect
						.getTopLeft().x), rect.getBottomRight().y);
			} else if (_drawArea === osViewer.transformRect.hitAreas.TOP) {
				topLeft = new OpenSeadragon.Point(rect.getTopLeft().x, Math
						.min(newPoint.y, rect.getBottomRight().y));
				bottomRight = rect.getBottomRight();
			} else if (_drawArea === osViewer.transformRect.hitAreas.BOTTOM) {
				topLeft = rect.getTopLeft();
				bottomRight = new OpenSeadragon.Point(rect.getBottomRight().x,
						Math.max(newPoint.y, rect.getTopLeft().y));
			} else if (_drawArea === osViewer.transformRect.hitAreas.CENTER
					&& _enterPoint) {
				var dx = _enterPoint.x - newPoint.x;
				var dy = _enterPoint.y - newPoint.y;
				rect.x -= dx;
				rect.y -= dy;
				_enterPoint = newPoint;
			}

			if (topLeft && bottomRight) {
//				if(_debug)console.log("Upper left point is " + rect.getTopLeft());
//				if(_debug)console.log("Lower right point is " + rect.getBottomRight());
//				if(_debug)console.log("Setting upper left point to " + topLeft);
//				if(_debug)console.log("Setting lower right point to " + bottomRight);
				rect.x = topLeft.x;
				rect.y = topLeft.y;
				rect.width = bottomRight.x - topLeft.x;
				rect.height = bottomRight.y - topLeft.y;
			}

			osViewer.viewer.updateOverlay(osViewer.overlays.getDrawingOverlay().element, rect, 0);
			event.preventDefaultAction = true;
			return true;
		} else if(_drawArea) {
			_drawing = true;
			event.preventDefaultAction = true;
			return true;

		}
	}

	function _onViewerRelease(event) {
		if (_active) {
			if(_drawing && _finishHook) {
				_finishHook(osViewer.overlays.getDrawingOverlay());
			}
			_drawing = false;
			if(osViewer.overlays.getDrawingOverlay()) {			    
			    $(osViewer.overlays.getDrawingOverlay().element).tooltip();
			}
			_drawArea = "";
			_enterPoint = null;
			event.preventDefaultAction = true;
			return true;
		}
	}

	function _onViewerDragEnd(event) {
		if (_drawing) {
			_drawing = false;
			event.preventDefaultAction = true;
			return true;
		}
	}

	function _disableViewerEvent(event) {
		if (_drawing) {
			event.preventDefaultAction = true;
			return true;
		}
	}
	function checkForRectHit(point) {
		var i;
		for (i = 0; i < _rects.length; i++) {
			var x = _rects[i];
			if (point.x > x.hitBox.l && point.x < x.hitBox.r
					&& point.y > x.hitBox.t && point.y < x.hitBox.b) {
				var topLeftHb = {
					l : x.x - _hbAdd,
					t : x.y - _hbAdd,
					r : x.x + _hbAdd,
					b : x.y + _hbAdd
				};
				var topRightHb = {
					l : x.x + x.width - _hbAdd,
					t : x.y - _hbAdd,
					r : x.x + x.width + _hbAdd,
					b : x.y + _hbAdd
				};
				var bottomRightHb = {
					l : x.x + x.width - _hbAdd,
					t : x.y + x.height - _hbAdd,
					r : x.x + x.width + _hbAdd,
					b : x.y + x.height + _hbAdd
				};
				var bottomLeftHb = {
					l : x.x - _hbAdd,
					t : x.y + x.height - _hbAdd,
					r : x.x + _hbAdd,
					b : x.y + x.height + _hbAdd
				};
				var topHb = {
					l : x.x + _hbAdd,
					t : x.y - _hbAdd,
					r : x.x + x.width - _hbAdd,
					b : x.y + _hbAdd
				};
				var rightHb = {
					l : x.x + x.width - _hbAdd,
					t : x.y + _hbAdd,
					r : x.x + x.width + _hbAdd,
					b : x.y + x.height - _hbAdd
				};
				var bottomHb = {
					l : x.x + _hbAdd,
					t : x.y + x.height - _hbAdd,
					r : x.x + x.width - _hbAdd,
					b : x.y + x.height + _hbAdd
				};
				var leftHb = {
					l : x.x - _hbAdd,
					t : x.y + _hbAdd,
					r : x.x + _hbAdd,
					b : x.y + x.height - _hbAdd
				};
			}
		}
	}

	/*
	 * Determine the side of the rectangle rect the point lies on or closest at
	 * <=maxDist distance
	 */
	function _findEdge(rect, point, maxDist) {
		var distanceToLeft = _distToSegment(point, rect.getTopLeft(), rect
				.getBottomLeft());
		var distanceToBottom = _distToSegment(point, rect.getBottomLeft(), rect
				.getBottomRight());
		var distanceToRight = _distToSegment(point, rect.getTopRight(), rect
				.getBottomRight());
		var distanceToTop = _distToSegment(point, rect.getTopLeft(), rect
				.getTopRight());

		var minDistance = Math.min(distanceToLeft, Math.min(distanceToRight,
				Math.min(distanceToTop, distanceToBottom)));
		if (minDistance <= maxDist) {
			if (distanceToLeft === minDistance) {
				return osViewer.transformRect.hitAreas.LEFT;
			}
			if (distanceToRight === minDistance) {
				return osViewer.transformRect.hitAreas.RIGHT;
			}
			if (distanceToTop === minDistance) {
				return osViewer.transformRect.hitAreas.TOP;
			}
			if (distanceToBottom === minDistance) {
				return osViewer.transformRect.hitAreas.BOTTOM;
			}
		}
		return "";
	}

	/*
	 * Determine the cornder of the rectangle rect the point lies on or closest
	 * at <=maxDist distance
	 */
	function _findCorner(rect, point, maxDist) {
		var distanceToTopLeft = _dist(point, rect.getTopLeft());
		var distanceToBottomLeft = _dist(point, rect.getBottomLeft());
		var distanceToTopRight = _dist(point, rect.getTopRight());
		var distanceToBottomRight = _dist(point, rect.getBottomRight());

		var minDistance = Math.min(distanceToTopLeft, Math.min(
				distanceToTopRight, Math.min(distanceToBottomLeft,
						distanceToBottomRight)));
		if (minDistance <= maxDist) {
			if (distanceToTopLeft === minDistance) {
				return osViewer.transformRect.hitAreas.TOPLEFT;
			}
			if (distanceToTopRight === minDistance) {
				return osViewer.transformRect.hitAreas.TOPRIGHT;
			}
			if (distanceToBottomLeft === minDistance) {
				return osViewer.transformRect.hitAreas.BOTTOMLEFT;
			}
			if (distanceToBottomRight === minDistance) {
				return osViewer.transformRect.hitAreas.BOTTOMRIGHT;
			}
		}
		return "";
	}

	function _sqr(x) {
		return x * x
	}
	function _dist2(v, w) {
		return _sqr(v.x - w.x) + _sqr(v.y - w.y)
	}
	function _dist(v, w) {
		return Math.sqrt(_dist2(v, w))
	}
	function _distToSegmentSquared(p, v, w) {
		var l2 = _dist2(v, w);
		if (l2 == 0)
			return _dist2(p, v);
		var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2;
		if (t < 0)
			return _dist2(p, v);
		if (t > 1)
			return _dist2(p, w);
		return _dist2(p, {
			x : v.x + t * (w.x - v.x),
			y : v.y + t * (w.y - v.y)
		});
	}
	function _distToSegment(point, lineP1, lineP2) {
		return Math.sqrt(_distToSegmentSquared(point, lineP1, lineP2));
	}
	return osViewer;

})(viewImage || {}, jQuery);