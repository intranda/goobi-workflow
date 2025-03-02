var osViewer = function(divId, imageViewWidth, imageViewHeight, imageOriginalWidth, imageOriginalHeight, mimeType,
		imageLevels, tileUrl) {
	var _rects = [];
	var _hbAdd = 5;
	var _drawing = false;
	var _zoomSlider = {};
	if (tileUrl) {
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.open("GET", tileUrl + "ImageProperties.xml", false);
		tileUrl += 'TileGroup0/';
		xmlhttp.send();
		var xmlDoc = xmlhttp.responseXML;
		var properties = xmlDoc.getElementsByTagName("IMAGE_PROPERTIES")[0];
		tileSize = parseInt(properties.getAttribute("TILESIZE"));
		maxWidth = parseInt(properties.getAttribute("WIDTH"));
		maxHeight = parseInt(properties.getAttribute("HEIGHT"));
		console.log(smallImage);
		var _viewer = OpenSeadragon({
			id : divId,
			controlsEnabled : false,
			prefixUrl : "/openseadragon-bin/images/",
			zoomPerClick : 1,
			showNavigationControl : false,
			showZoomControl : false,
			showHomeControl : false,
			showFullPageControl : true,
			tileSources : {
			// ~ height: maxHeight,
			// ~ width: maxWidth,
			// ~ tileSize: tileSize,
			// ~ minlevel: 1,
			// ~ getNumTiles: function(level) {
			// ~ console.log(level);
			// ~ return Math.pow(2,level);
			// ~ },
			// ~ getTileUrl: function( level, x, y) {
			// ~ console.log("level: " + level + " x:" + x + " y:" + y);
			// ~ return tileUrl + level + "-" + x + "-" + y + ".jpg"
			// ~ }
			}
		});
	} else {
		var _viewer = OpenSeadragon({
			id : divId,
			controlsEnabled : false,
			prefixUrl : "/openseadragon-bin/images/",
			zoomPerClick : 1,
			showNavigationControl : false,
			showZoomControl : false,
			showHomeControl : false,
			showFullPageControl : true,
			tileSources : {
				type : "legacy-image-pyramid",
				levels : _convertImageLevels(imageLevels, mimeType)
			}
		});
	}
	var _viewerInputHook = _viewer.addViewerInputHook({
		hooks : [ {
			tracker : "viewer",
			handler : "clickHandler",
			hookHandler : _disableViewerEvent
		}, {
			tracker : "viewer",
			handler : "scrollHandler",
			hookHandler : _disableViewerEvent
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
		} ]
	});
	function _convertImageLevels(imageLevels, mimeType) {
		var convertedLevels = [];
		for (i = 0; i < imageLevels.length; i++) {
			convertedLevels[i] = {
				mimetype : mimeType,
				url : imageLevels[i][0],
				width : imageLevels[i][1],
				height : imageLevels[i][2]
			}
			console.log(convertedLevels[i]);
		}
		return convertedLevels;
	}

	function _onViewerPress(event) {
		if (_drawing) {
			drawElement = document.createElement("div");
			drawElement.style.border = "2px solid green";
			drawPoint = _viewer.viewport
					.viewerElementToViewportCoordinates(event.position);
			var rect = new OpenSeadragon.Rect(drawPoint.x, drawPoint.y, 0, 0);
			_viewer.addOverlay(drawElement, rect, 1);
			console.log(_viewer.viewport
					.viewerElementToImageCoordinates(event.position));
			event.preventDefaultAction = true;
			return true;
		}
	}
	function _onViewerDrag(event) {
		if (_drawing) {
			var newPoint = _viewer.viewport
					.viewerElementToViewportCoordinates(event.position);
			var rect = new OpenSeadragon.Rect(drawPoint.x, drawPoint.y,
					newPoint.x - drawPoint.x, newPoint.y - drawPoint.y);
			if (newPoint.x < drawPoint.x) {
				rect.x = newPoint.x;
				rect.width = drawPoint.x - newPoint.x;
			}
			if (newPoint.y < drawPoint.y) {
				rect.y = newPoint.y;
				rect.height = drawPoint.y - newPoint.y;
			}
			_viewer.updateOverlay(drawElement, rect, 0);
			event.preventDefaultAction = true;
			return true;
		}
	}
	function _onViewerDragEnd(event) {
		if (_drawing) {
			var newPoint = _viewer.viewport
					.viewerElementToViewportCoordinates(event.position);
			var rect = new OpenSeadragon.Rect(drawPoint.x, drawPoint.y,
					newPoint.x - drawPoint.x, newPoint.y - drawPoint.y);
			if (newPoint.x < drawPoint.x) {
				rect.x = newPoint.x;
				rect.width = drawPoint.x - newPoint.x;
			}
			if (newPoint.y < drawPoint.y) {
				rect.y = newPoint.y;
				rect.height = drawPoint.y - newPoint.y;
			}
			rect.hitBox = {
				l : rect.x - _hbAdd,
				t : rect.y - _hbAdd,
				r : rect.x + rect.width + _hbAdd,
				b : rect.y + rect.height + _hbAdd
			};
			_rects.push({
				drawElement : drawElement,
				rect : rect
			});
			_viewer.updateOverlay(drawElement, rect, 0);
			console.log(_viewer.viewport
					.viewerElementToImageCoordinates(event.position));
			event.preventDefaultAction = true;
			return true;
		}
	}
	/**
	 * coordinates are in original image space
	 * fillColor and border may be left blank for no fill/border
	 * border may contain size in pixels, style and color (eg. "2px solid red";)
	 * fillOpacity is a number betweeen 0 and 1 (1 being completely opaque)
	 */
	function drawRect(x, y, width, height, border, fillColor, fillOpacity) {
		x=_scaleToLargestLevel(x);
		y=_scaleToLargestLevel(y);
		width=_scaleToLargestLevel(width);
		height=_scaleToLargestLevel(height);
		var overlay = _viewer.viewport.imageToViewportRectangle(x, y, width, height);
		console.log("drawing rectangle " + overlay);
		rectElement = document.createElement("div");
		rectElement.style.backgroundColor = fillColor;
		rectElement.style.opacity = fillOpacity;
		rectElement.style.border = borderStyle;
		_rects.push({
			rectElement : rectElement,
			rect : overlay
		});
		_viewer.addOverlay(rectElement, overlay, 0);

	}
	function _scaleToLargestLevel(value) {
		return value*imageLevels[imageLevels.length-1][1]/imageOriginalWidth;
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
	 * begin zoomslider functions
	 */
	_viewer.addHandler('zoom', function(data) {
		_buttonToZoom(data.zoom);
	});
	var _buttonToMouse = function(mousePos) {
		var offset = _zoomSlider.$button.width() / 2;
		var newPos = mousePos - offset;
		if (newPos < 0) {
			newPos = 0;
		}
		if (newPos + 2 * offset > _zoomSlider.absoluteWidth) {
			newPos = _zoomSlider.absoluteWidth - 2 * offset;
		}
		_zoomSlider.$button.css({
			left : newPos
		});
		_zoomSlider.buttonPosition = newPos;
		var factor = (newPos / (_zoomSlider.absoluteWidth - offset * 2));
		var newScale = _viewer.viewport.getHomeZoom()
				+ (_viewer.viewport.getMaxZoom() - _viewer.viewport
						.getMinZoom()) * factor;
		console.log(newScale);
		myZoomTo(newScale);
	};
	var _buttonToZoom = function(scale) {
		console.log(scale);
		if (!_zoomSlider || !_viewer.viewport) {
			return;
		}
		var newPos = ((scale - _viewer.viewport.getHomeZoom()) / (_viewer.viewport
				.getMaxZoom() - _viewer.viewport.getMinZoom()))
				* (_zoomSlider.absoluteWidth - _zoomSlider.$button.width());
		if (Math.abs(_viewer.viewport.getMaxZoom() - scale) < 0.0000000001) {
			newPos = _zoomSlider.absoluteWidth - _zoomSlider.$button.width();
		}
		if (newPos < 0) {
			newPos = 0;
		}
		_zoomSlider.$button.css({
			left : newPos
		});
		// ~ log(newPos);
		_zoomSlider.buttonPosition = newPos;
	};
	var _zoomSliderMouseUp = function(evt) {
		_zoomSlider.mousedown = false;
	};
	var _zoomSliderMouseMove = function(evt) {
		if (!_zoomSlider.mousedown) {
			return;
		}
		var offset = $(this).offset();
		var hitX = evt.pageX - offset.left;
		_buttonToMouse(hitX);
		// ~ console.log("moving " + hitX);
	};
	var _zoomSliderMouseDown = function(evt) {
		_zoomSlider.mousedown = true;
		var offset = $(this).offset();
		var hitX = evt.pageX - offset.left;
		_buttonToMouse(hitX);
		// ~ console.log("slider mousedown");
	};
	var _buttonMouseDown = function(evt) {
		_zoomSlider.mousedown = true;
		// ~ console.log('hit button');
		return false;
	};
	var _zoomSliderClick = function(evt) {
		var offset = $(this).offset();
		var hitX = evt.pageX - offset.left;
		// ~ console.log(hitX);
	};
	var addZoomSlider = function(elementId) {
		$ = jQuery;
		// _viewer.viewport.getHomeZoom() = _viewer.viewport.getZoom(true);
		_zoomSlider.$element = $('#' + elementId);
		_zoomSlider.$button = _zoomSlider.$element
				.children('.zoomslider-handle');
		_zoomSlider.$button.css({
			position : 'relative',
			display : 'block'
		});
		_zoomSlider.buttonPosition = 0;
		// ~ console.log(_zoomSlider.$button);
		_zoomSlider.absoluteWidth = _zoomSlider.$element.innerWidth();
		_zoomSlider.mousedown = false;
		_zoomSlider.$button.on('mousedown', _buttonMouseDown);
		_zoomSlider.$element.click(_zoomSliderClick);
		_zoomSlider.$element.mousedown(_zoomSliderMouseDown);
		_zoomSlider.$element.mousemove(_zoomSliderMouseMove);
		$(document).on('mouseup', _zoomSliderMouseUp);
	};
	/*
	 * end zoomslider functions
	 */
	var toggleDrawing = function() {
		_drawing = !_drawing;
	};
	var myZoomTo = function(zoomTo) {
		var zoomBy = parseFloat(zoomTo) / _viewer.viewport.getZoom();
		console.log(zoomBy);
		_viewer.viewport
				.zoomBy(zoomBy, _viewer.viewport.getCenter(false), true);
	};
	var setFullScreen = function(enable) {
		_viewer.setFullScreen(enable);
	};
	var goHome = function(bool) {
		_viewer.viewport.goHome(bool);
		// _viewer.viewport.setRotation(0);
	};
	var zoomIn = function() {
		_viewer.viewport.zoomBy(1.5, _viewer.viewport.getCenter(false), false);
	};
	var zoomOut = function() {
		_viewer.viewport.zoomBy(.75, _viewer.viewport.getCenter(false), false);
	};
	var rotateRight = function() {
		var newRotation = _viewer.viewport.getRotation() + 90;
		_viewer.viewport.setRotation(newRotation);
		var currentZoom = _viewer.viewport.getZoom();
		var zoomDiff = _viewer.viewport.getZoom()
				- _viewer.viewport.getHomeZoom();
		if (newRotation % 180 != 0
				&& (Math.abs(zoomDiff) < 0.000000001 || zoomDiff < 0)) {
			// TODO: center image horizontally
			_viewer.viewport.fitVertically(true);
		} else if (Math.abs(zoomDiff) < 0.000000001 || zoomDiff < 0) {
			_viewer.viewport.fitHorizontally(true);
		}
	};
	var rotateLeft = function() {
		var newRotation = _viewer.viewport.getRotation() - 90;
		_viewer.viewport.setRotation(newRotation);
		var zoomDiff = _viewer.viewport.getZoom()
				- _viewer.viewport.getHomeZoom();
		if (newRotation % 180 != 0
				&& (Math.abs(zoomDiff) < 0.000000001 || zoomDiff < 0)) {
			_viewer.viewport.fitVertically(true);
		} else if (Math.abs(zoomDiff) < 0.000000001 || zoomDiff < 0) {
			_viewer.viewport.fitHorizontally(true);
		}
	};

	return {
		toggleDrawing : toggleDrawing,
		myZoomTo : myZoomTo,
		setFullScreen : setFullScreen,
		zoomIn : zoomIn,
		zoomOut : zoomOut,
		goHome : goHome,
		getRects : getRects,
		rotateRight : rotateRight,
		rotateLeft : rotateLeft,
		addZoomSlider : addZoomSlider,
		drawRect : drawRect,
		highlight : highlight
	};
};
