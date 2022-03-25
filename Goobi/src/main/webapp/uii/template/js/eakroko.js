// FLAT Theme v2.2
(function ($) {
	$.fn.retina = function (retina_part) {
		// Set default retina file part to '-2x'
		// Eg. some_image.jpg will become some_image-2x.jpg
		var settings = {
			'retina_part': '-2x'
		};
		if (retina_part) jQuery.extend(settings, {
			'retina_part': retina_part
		});
		if (window.devicePixelRatio >= 2) {
			this.each(function (index, element) {
				if (!$(element).attr('src')) return;

				var checkForRetina = new RegExp("(.+)(" + settings['retina_part'] + "\\.\\w{3,4})");
				if (checkForRetina.test($(element).attr('src'))) return;

				var new_image_src = $(element).attr('src').replace(/(.+)(\.\w{3,4})$/, "$1" + settings['retina_part'] + "$2");
				$.ajax({
					url    : new_image_src,
					type   : "HEAD",
					success: function () {
						$(element).attr('src', new_image_src);
					}
				});
			});
		}
		return this;
	}
})(jQuery);

function icheck() {
	if ($(".icheck-me").length > 0) {
		$(".icheck-me").each(function () {
			var $el = $(this);
			var skin = ($el.attr('data-skin') !== undefined) ? "_" + $el.attr('data-skin') : "",
				color = ($el.attr('data-color') !== undefined) ? "-" + $el.attr('data-color') : "";

			var opt = {
				checkboxClass: 'icheckbox' + skin + color,
				radioClass   : 'iradio' + skin + color,
				increaseArea : "10%"
			}

			$el.iCheck(opt);
		});
	}
}
$(document).ready(function () {
	var mobile = false,
		tooltipOnlyForDesktop = true,
		notifyActivatedSelector = 'button-active';

	if (/Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent)) {
		mobile = true;
	}

	icheck();

	if ($(".complexify-me").length > 0) {
		$(".complexify-me").complexify(function (valid, complexity) {
			if (complexity < 40) {
				$(this).parent().find(".progress .bar").removeClass("bar-green").addClass("bar-red");
			} else {
				$(this).parent().find(".progress .bar").addClass("bar-green").removeClass("bar-red");
			}

			$(this).parent().find(".progress .bar").width(Math.floor(complexity) + "%").html(Math.floor(complexity) + "%");
		});
	}

	// Round charts (easypie)
	if ($(".chart").length > 0) {
		$(".chart").each(function () {
			var color = "#881302",
				$el = $(this);
			var trackColor = $el.attr("data-trackcolor");
			if ($el.attr('data-color')) {
				color = $el.attr('data-color');
			} else {
				if (parseInt($el.attr("data-percent")) <= 25) {
					color = "#046114";
				} else if (parseInt($el.attr("data-percent")) > 25 && parseInt($el.attr("data-percent")) < 75) {
					color = "#dfc864";
				}
			}
			if (!$el.hasClass('easyPieChart')) {
				$el.addClass('easyPieChart');
			}
			$el.css('line-height', '80px');
			$el.easyPieChart({
				animate   : 1000,
				barColor  : color,
				lineWidth : 5,
				size      : 80,
				lineCap   : 'square',
				trackColor: trackColor
			});
		});
	}

	// Calendar (fullcalendar)
	if ($('.calendar').length > 0) {
		$('.calendar').fullCalendar({
			header    : {
				left  : '',
				center: 'prev,title,next',
				right : 'month,agendaWeek,agendaDay,today'
			},
			buttonText: {
				today: 'Today'
			},
			editable  : true
		});
		$(".fc-button-effect").remove();
		$(".fc-button-next .fc-button-content").html("<i class='fa fa-chevron-right'></i>");
		$(".fc-button-prev .fc-button-content").html("<i class='fa fa-chevron-left'></i>");
		$(".fc-button-today").addClass('fc-corner-right');
		$(".fc-button-prev").addClass('fc-corner-left');
	}

	// Tooltips (only for desktop) (bootstrap tooltips)
	if (tooltipOnlyForDesktop) {
		if (!mobile) {
			$('[rel=tooltip]').tooltip();
		}
	}


	// Notifications
	$(".notify").click(function () {
		var $el = $(this);
		var title = $el.attr('data-notify-title'),
			message = $el.attr('data-notify-message'),
			time = $el.attr('data-notify-time'),
			sticky = $el.attr('data-notify-sticky'),
			overlay = $el.attr('data-notify-overlay');

		$.gritter.add({
			title : (typeof title !== 'undefined') ? title : 'Message - Head',
			text  : (typeof message !== 'undefined') ? message : 'Body',
			image : (typeof image !== 'undefined') ? image : null,
			sticky: (typeof sticky !== 'undefined') ? sticky : false,
			time  : (typeof time !== 'undefined') ? time : 3000
		});
	});

	// masked input
	if ($('.mask_date').length > 0) {
		$(".mask_date").mask("9999/99/99");
	}
	if ($('.mask_phone').length > 0) {
		$(".mask_phone").mask("(999) 999-9999");
	}
	if ($('.mask_serialNumber').length > 0) {
		$(".mask_serialNumber").mask("9999-9999-99");
	}
	if ($('.mask_productNumber').length > 0) {
		$(".mask_productNumber").mask("aaa-9999-a");
	}
	// tag-input
	if ($(".tagsinput").length > 0) {
		$('.tagsinput').each(function (e) {
			$(this).tagsInput({
				width : 'auto',
				height: 'auto'
			});
		});
	}

	// datepicker
	if ($('.datepick').length > 0) {
		$('.datepick').each(function () {
			var $el = $(this);
			$el.datepicker();
			$el.on('changeDate', function () {
				$el.datepicker('hide');
			});
		});
	}

	// daterangepicker
	if ($('.daterangepick').length > 0) {
		$('.daterangepick').daterangepicker();
	}

	// timepicker
	if ($('.timepick').length > 0) {
		$('.timepick').timepicker({
			defaultTime : 'current',
			minuteStep  : 1,
			disableFocus: true,
			template    : 'dropdown'
		});
	}
	// colorpicker
	if ($('.colorpick').length > 0) {
		$('.colorpick').colorpicker();
	}
	// uniform
	if ($('.uniform-me').length > 0) {
		$('.uniform-me').uniform({
			radioClass : 'uni-radio',
			buttonClass: 'uni-button'
		});
	}
	// Chosen (chosen)
	if ($('.chosen-select').length > 0) {
		$('.chosen-select').each(function () {
			var $el = $(this);
			var search = ($el.attr("data-nosearch") === "true") ? true : false,
				opt = {};
			if (search) opt.disable_search_threshold = 9999999;
			$el.chosen(opt);
		});
	}

	if ($(".select2-me").length > 0) {
		$(".select2-me").select2();
	}

	// multi-select
	if ($('.multiselect').length > 0) {
		$(".multiselect").each(function () {
			var $el = $(this);
			var selectableHeader = $el.attr('data-selectableheader'),
				selectionHeader = $el.attr('data-selectionheader');
			if (selectableHeader != undefined) {
				selectableHeader = "<div class='multi-custom-header'>" + selectableHeader + "</div>";
			}
			if (selectionHeader != undefined) {
				selectionHeader = "<div class='multi-custom-header'>" + selectionHeader + "</div>";
			}
			$el.multiSelect({
				selectionHeader : selectionHeader,
				selectableHeader: selectableHeader
			});
		});
	}

	// spinner
	if ($('.spinner').length > 0) {
		$('.spinner').spinner();
	}

	// dynatree
	if ($(".filetree").length > 0) {
		$(".filetree").each(function () {
			var $el = $(this),
				opt = {};
			opt.debugLevel = 0;
			if ($el.hasClass("filetree-callbacks")) {
				opt.onActivate = function (node) {
					$(".activeFolder").text(node.data.title);
					$(".additionalInformation").html("<ul style='margin-bottom:0;'><li>Key: " + node.data.key + "</li><li>is folder: " + node.data.isFolder + "</li></ul>");
				};
			}
			if ($el.hasClass("filetree-checkboxes")) {
				opt.checkbox = true;

				opt.onSelect = function (select, node) {
					var selNodes = node.tree.getSelectedNodes();
					var selKeys = $.map(selNodes, function (node) {
						return "[" + node.data.key + "]: '" + node.data.title + "'";
					});
					$(".checkboxSelect").text(selKeys.join(", "));
				};
			}

			$el.dynatree(opt);
		});
	}

	if ($(".colorbox-image").length > 0) {
		$(".colorbox-image").colorbox({
			maxWidth : "90%",
			maxHeight: "90%",
			rel      : $(this).attr("rel")
		});
	}

	// PlUpload
	if ($('.plupload').length > 0) {
		$(".plupload").each(function () {
			var $el = $(this);
			$el.pluploadQueue({
				runtimes           : 'html5,flash,silverlight,html4',
				url                : 'js/plupload/upload.php',
				max_file_size      : '10mb',
				chunk_size         : '1mb',
				unique_names       : true,
				resize             : {
					width  : 320,
					height : 240,
					quality: 90
				},
				filters            : [{
					title     : "Image files",
					extensions: "jpg,gif,png"
				}, {
					title     : "Zip files",
					extensions: "zip"
				}],
				flash_swf_url      : 'js/plupload/Moxie.swf',
				silverlight_xap_url: 'js/plupload/Moxie.xap'
			});
			var upload = $el.pluploadQueue();
			$(".plupload_header").remove();
			$(".plupload_progress_container").addClass("progress").addClass('progress-striped');
			$(".plupload_progress_bar").addClass("bar");
			$(".plupload_button").each(function () {
				if ($(this).hasClass("plupload_add")) {
					$(this).attr("class", 'btn plupload_add btn-primary').html("<i class='fa fa-plus-circle'></i> " + $(this).html());
				} else {
					$(this).attr("class", 'btn plupload_start btn-success').html("<i class='fa fa-cloud-upload'></i> " + $(this).html());
				}
			});
		});
	}

	// Wizard
	if ($(".form-wizard").length > 0) {
		$(".form-wizard").formwizard({
			formPluginEnabled: true,
			validationEnabled: true,
			focusFirstInput  : false,
			disableUIStyles  : true,
			validationOptions: {
				errorElement  : 'span',
				errorClass    : 'help-block has-error',
				errorPlacement: function (error, element) {
					if (element.parents("label").length > 0) {
						element.parents("label").after(error);
					} else {
						element.after(error);
					}
				},
				highlight     : function (label) {
					$(label).closest('.form-group').removeClass('has-error has-success').addClass('has-error');
					console.log('aaa');
				},
				success       : function (label) {
					label.addClass('valid').closest('.form-group').removeClass('has-error has-success').addClass('has-success');
				}
			},
			formOptions      : {
				success  : function (data) {
					alert("Response: \n\n" + data.say);
				},
				dataType : 'json',
				resetForm: true
			}
		});
	}

	// Validation
	if ($('.form-validate').length > 0) {
		$('.form-validate').each(function () {
			var id = $(this).attr('id');
			$("#" + id).validate({
				errorElement  : 'span',
				errorClass    : 'help-block has-error',
				errorPlacement: function (error, element) {
					if (element.parents("label").length > 0) {
						element.parents("label").after(error);
					} else {
						element.after(error);
					}
				},
				highlight     : function (label) {
					$(label).closest('.form-group').removeClass('has-error has-success').addClass('has-error');
				},
				success       : function (label) {
					label.addClass('valid').closest('.form-group').removeClass('has-error has-success').addClass('has-success');
				},
				onkeyup       : function (element) {
					$(element).valid();
				},
				onfocusout    : function (element) {
					$(element).valid();
				}
			});
		});
	}

	// new dataTables
	if ($('.dataTable').length > 0) {
		$('.dataTable').each(function () {
			var $el = $(this),
				dataTable_options = {
					dom: 'lfrtip'
				},
				no_sort = $el.attr('data-nosort');
			// Skip for custom dataTable
			if ($el.hasClass('dataTable-custom')) return;

			if ($el.hasClass('dataTable-column_filter')) {
				var types = $el.attr('data-column_filter_types'),
					position = $el.attr('data-column_filter_position'),
					dateformat = $el.attr('data-column_filter_dateformat');

				if (position !== 'bottom') {
					position = 'top'
				}

				if (types !== undefined) {
					types = types.split(',');
				} else {
					types = [];
				}

				if (dateformat === undefined) {
					dateformat = 'mm/dd/yy';
				}

				dataTable_options.initComplete = function () {
					var api = this.api(),
						$filter_row = $('<tr class="dataTable-col_filter"></tr>'),
						$table = $(this);

					// Add the filter to head or foot
					if (position == 'top') {
						$filter_row.appendTo($table.find('thead'));
					} else {
						if ($table.find('tfoot').length == 0) {
							$('<tfoot></tfoot>').appendTo($table);
						}

						$filter_row.appendTo($table.find('tfoot'));
					}

					api.columns().indexes().flatten().each(function (i) {
						var column = api.column(i),
							$filter_col = $('<th></th>').appendTo($filter_row);

						if (types[i] === 'select') {
							var select = $('<select><option value=""></option></select>')
								.appendTo($filter_col)
								.on('change', function () {
									var val = $(this).val();

									column
										.search(val ? '^' + val + '$' : '', true, false)
										.draw();
								});

							column.data().unique().sort().each(function (d, j) {
								select.append('<option value="' + d + '">' + d + '</option>')
							});
						} else if (types[i] == 'daterange') {
							var $from_date = $('<input type="text" class="dataTable-datepicker-from" name="dataTable-daterpicker-from" placeholder="From...">').appendTo($filter_col),
								$to_date = $('<input type="text" class="dataTable-datepicker-to" name="dataTable-daterpicker-to" placeholder="To...">').appendTo($filter_col),
								datepicker_options = {
									dateFormat: dateformat
								};

							$from_date.datepicker(datepicker_options);
							$to_date.datepicker(datepicker_options);

							$.fn.dataTable.ext.search.push(
								function (settings, data, dataIndex) {
									var column_date = data[i],
										from_date = $from_date.val(),
										to_date = $to_date.val(),
										moment_dateformat = dateformat.toUpperCase(),
										column_moment_date = moment(column_date, moment_dateformat),
										from_moment_date = moment(from_date, moment_dateformat),
										to_moment_date = moment(to_date, moment_dateformat);

									if (column_moment_date === false || from_moment_date === false || to_moment_date === false) {
										// we had invalid date
										return true;
									}

									if (from_date == '' && to_date == '') {
										return true;
									} else {
										if (from_date == '' && to_date != '') {
											if (!column_moment_date.isBefore(to_moment_date)) {
												return false;
											}
										} else if (from_date != '' && to_date == '') {
											if (!column_moment_date.isAfter(from_moment_date)) {
												return false;
											}
										} else {
											var range = moment().range(from_moment_date, to_moment_date);
											if (!range.contains(column_moment_date)) {
												return false;
											}
										}
									}

									return true;
								}
							);

							$from_date.change(function () {
								api.draw();
							});

							$to_date.change(function () {
								api.draw();
							});
						} else if (types[i] !== 'null' || types[i] == 'text') {
							var title = '',
								input = $('<input type="text" placeholder="Search ' + title + '" />')
									.appendTo($filter_col)
									.on('keyup change', function () {
										var val = $(this).val();

										column
											.search(val)
											.draw();
									});
						}
					});
				};
			}

			if (no_sort !== undefined) {
				var cols = no_sort.split(',').map(function (col_string) {
					return parseInt(col_string.trim());
				});

				dataTable_options.columnDefs = [
					{
						'orderable': false,
						'targets'  : cols
					}
				];
				dataTable_options.order = [];
			}

			if ($el.attr("data-nosearch") !== undefined) {
				dataTable_options.filter = false;
			}
			if ($el.attr("data-nopagination") !== undefined) {
				dataTable_options.paging = false;
			}
			if ($el.attr("data-noinfo") !== undefined) {
				dataTable_options.info = false;
			}
			if ($el.attr("data-noorder") !== undefined) {
				dataTable_options.ordering = false;
			}

			if ($el.hasClass('dataTable-tools')) {
				dataTable_options.dom = 'T' + dataTable_options.dom;
				dataTable_options.tableTools = {
					"sSwfPath": "js/plugins/datatables/extensions/copy_csv_xls_pdf.swf"
				};
			}

			if ($el.hasClass('dataTable-colreorder')) {
				dataTable_options.dom = 'R' + dataTable_options.dom;
			}

			if ($el.hasClass('dataTable-colvis')) {
				dataTable_options.dom = 'C' + dataTable_options.dom;
				dataTable_options.colVis = {
					"buttonText": "Show/hide columns <i class='fa fa-angle-down'></i>",
					"iOverlayFade": 0
				};
			}

			if ($el.hasClass("dataTable-scroll-x")) {
				dataTable_options.scrollX = "100%";
				dataTable_options.scrollCollapse = true;
			}

			if ($el.hasClass("dataTable-scroll-y")) {
				dataTable_options.scrollY = "300px";
				dataTable_options.paginate = false;
				dataTable_options.scrollCollapse = true;
			}

			if ($el.hasClass("dataTable-scroller")) {
				var ajaxSource = $el.attr('data-ajax-source');

				if(ajaxSource !== '' && ajaxSource !== undefined){
					if ($el.hasClass('dataTable-tools')) {
						dataTable_options.dom = 'Tfrtip';
					}

					dataTable_options.scrollY = "300px";
					dataTable_options.deferRender = true;
					dataTable_options.dom = dataTable_options.dom + 'S';
					dataTable_options.ajax = ajaxSource;
				}
			}

			var table = $el.DataTable(dataTable_options);

			if ($el.hasClass("dataTable-fixedcolumn")) {
				new $.fn.dataTable.FixedColumns( table );
			}

			$el.find('.dataTable-checkall').change(function () {
				var $checkbox = $(this),
					col_index = $checkbox.parent().index(),
					nodes;

				if ($el.attr('data-checkall') !== 'all') {
					nodes = table.column(col_index, {page: 'current'}).nodes().to$();
				} else {
					nodes = table.column(col_index, {page: 'all'}).nodes().to$();
				}
				nodes.find('input[type="checkbox"]').prop('checked', $checkbox.prop('checked'));
			});
		});
	}

	// force correct width for chosen
	resize_chosen();

	// file_management
	if ($('.file-manager').length > 0) {
		$('.file-manager').elfinder({
			url: 'js/plugins/elfinder/php/connector.php'
		});
	}

	// slider
	if ($('.slider').length > 0) {
		$(".slider").each(function () {
			var $el = $(this);
			var min = parseInt($el.attr('data-min')),
				max = parseInt($el.attr('data-max')),
				step = parseInt($el.attr('data-step')),
				range = $el.attr('data-range'),
				rangestart = parseInt($el.attr('data-rangestart')),
				rangestop = parseInt($el.attr('data-rangestop'));

			var opt = {
				min  : min,
				max  : max,
				step : step,
				slide: function (event, ui) {
					$el.find('.amount').html(ui.value);
				}
			};

			if (range !== undefined) {
				opt.range = true;
				opt.values = [rangestart, rangestop];
				opt.slide = function (event, ui) {
					$el.find('.amount').html(ui.values[0] + " - " + ui.values[1]);
					$el.find(".amount_min").html(ui.values[0] + "$");
					$el.find(".amount_max").html(ui.values[1] + "$");
				};
			}

			$el.slider(opt);
			if (range !== undefined) {
				var val = $el.slider('values');
				$el.find('.amount').html(val[0] + ' - ' + val[1]);
				$el.find(".amount_min").html(val[0] + "$");
				$el.find(".amount_max").html(val[1] + "$");
			} else {
				$el.find('.amount').html($el.slider('value'));
			}
		});
	}

	if ($(".ckeditor").length > 0) {
		CKEDITOR.replace("ck");
	}

	$(".retina-ready").retina("@2x");

});

$(window).resize(function () {
	// chosen resize bug
	resize_chosen();
});

function resize_chosen() {
	$('.chzn-container').each(function () {
		var $el = $(this);
		$el.css('width', $el.parent().width() + 'px');
		$el.find(".chzn-drop").css('width', ($el.parent().width() - 2) + 'px');
		$el.find(".chzn-search input").css('width', ($el.parent().width() - 37) + 'px');
	});
}
