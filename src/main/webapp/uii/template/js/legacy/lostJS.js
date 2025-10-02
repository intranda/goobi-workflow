// template_metseditor.html
function reloadAllHandler() {
    $('a[id*="geonamesIndexTrigger-"]').on('click', function () {
        var currIndex = $(this).attr('data-row');
        var currType = $(this).attr('data-datatype');
        $('#rowIndex').val(currIndex);
        $('#rowType').val(currType);
    });

    $('a[id*="gndIndexTrigger-"]').on('click', function () {
        var currIndex = $(this).attr('data-row');
        var currType = $(this).attr('data-datatype');
        $('#rowIndex').val(currIndex);
        $('#rowType').val(currType);
    });

    $('a[id*="gndPersonIndexTrigger-"]').on('click', function () {
        var currIndex = $(this).attr('data-row');
        var currType = $(this).attr('data-datatype');
        $('#rowIndex').val(currIndex);
        $('#rowType').val(currType);
    });

    $( 'body' ).on( 'click', 'a[id*="danteIndexTrigger-"]', function() {
        var currIndex = $( this ).attr( 'data-row' );
        var currType = $( this ).attr( 'data-datatype' );
        var currGroup = $( this ).attr( 'data-groupindex')
        $( '#rowIndex' ).val( currIndex );
        $( '#groupIndex' ).val( currGroup );
        $( '#rowType' ).val( currType );

        $('#resultList').empty();
        $('#danteInput').val('');
        $('#updatePluginButton').click();
    } );

    $( 'body' ).on( 'click', 'a[id*="processIndexTrigger-"]', function() {
        var currIndex = $( this ).attr( 'data-row' );
        var currType = $( this ).attr( 'data-datatype' );
        var currGroup = $( this ).attr( 'data-groupindex')
        $( '#rowIndex' ).val( currIndex );
        $( '#groupIndex' ).val( currGroup );
        $( '#rowType' ).val( currType );
        $('#updatePluginButton').click();
    } );

    $( 'body' ).on( 'click', 'a[id*="viafIndexTrigger-"]', function() {
        var currIndex = $( this ).attr( 'data-row' );
        var currType = $( this ).attr( 'data-datatype' );
        var currGroup = $( this ).attr( 'data-groupindex');
        $( '#rowIndex' ).val( currIndex );
        $( '#groupIndex' ).val( currGroup );
        $( '#rowType' ).val( currType );
        $('#updatePluginButton').click()
    } );
    $( 'body' ).on( 'click', 'a[id*="viafPersonIndexTrigger-"]', function() {
        var currIndex = $( this ).attr( 'data-row' );
        var currType =  'viafperson';
        var currGroup = $( this ).attr( 'data-groupindex');
        $( '#rowIndex' ).val( currIndex );
        $( '#groupIndex' ).val( currGroup );
        $( '#rowType' ).val( currType );
        $('#updatePluginButton').click()
    } );
}

function addPaginationButtons() {
    if ($('.pagination-thumb')) {
        $('.pagination-thumb').remove();
    }
    var escapeClientId = function (a) {
        return a.replace(/:/g, "\\:")
    };
}

function paginierungWertAnzeigen() {
    var paginationInputFields = document.getElementById("paginationInputFields");
    var fictitiousField = document.getElementById("fictitious");
    var inputBoxElement = document.getElementById("paginierungWert");
    paginationInputFields.style.display = ($("#paginationType").val() == 3 ? 'none' : '');
    fictitiousField.style.display = ($("#paginationType").val() == 3 ? 'none' : '');
    if ($("#paginationType").val() == 2 || $("#paginationType").val() == 5) {
        inputBoxElement.value = 'I';
    }
    if ($("#paginationType").val() == 1 || $("#paginationType").val() == 4) {
        inputBoxElement.value = '1';
    }
}

$(document).ready(function () {
    target = parseInt($("#contentArea").width()) - parseInt($("#pagLeft").width()) - 10;
    addPaginationButtons();

    if ($('.popover.fade.right.in').length != 0) {
        $('.popover.fade.right.in').remove();
    }
});

// metseditor_image.xhtml
window.onload = function () {
    loadImages();
}
function selectClickedThumbnail(element) {
    var galleryLinks;
    galleryLinks = document.getElementsByClassName('goobi-thumbnail');
    for (var i = 0; i < galleryLinks.length; i++) {
        galleryLinks[i].className = "goobi-thumbnail font-light";
        element.parentElement.parentElement.className = "goobi-thumbnail img-active";
        document.getElementById('menu-form:scrollToThumb').value = "false";
        return true;
    }
}
function loadThumbnails() {
    var height = parseInt($('.thumbnails').val());
    if (height) {
        $('.goobi-thumbnail').css('height', (height + 25) + 'px');
        $('.goobi-thumbnail .thumb').css('max-height', height + 'px');
        $('.goobi-thumbnail .thumb canvas').css('max-height', height + 'px');
        $('.goobi-thumbnail').css('max-width', (height) + 'px');

        scrollToThumbnail();
    }
}
loadThumbnails();
goobiWorkflowJS.object.freeJSResources({ status: "success" });

// inc_me_image-thumbnails.xhtml
function scrollToThumbnail() {
    var thumb = $("#thumb_#{Metadaten.image.order}");
    var useScroll = document.getElementById('menu-form:scrollToThumb').value;
    var shallScroll = '#{NavigationForm.uiStatus.mets_scrollToThumb}';

    if (shallScroll == 'true' && useScroll == "true") {
        if (thumb != null && thumb.position() != null) {
            var thumbScroll = thumb.position().top;
            $(document).scrollTop(thumbScroll - 22);
        } else {
            $(document).scrollTop(0);
        }
    }
}

// metseditor_menu.xhtml
function setScrollPosition() {
    var scrollPosition = $('#left').scrollTop();
    document.getElementById('menu-form:scrollPosition').value = scrollPosition;

    var structdata = $('#structdata');
    if (structdata != null) {
        var pos = structdata.scrollTop();
        document.getElementById('menu-form:scrollPositionStructData').value = pos;
    }
}

$(document).ready(function () {
    var position = '#{NavigationForm.uiStatus.mets_scrollPosition}';
    var pos2 = '#{NavigationForm.uiStatus.mets_scrollPositionStructData}';
    $('#left').scrollTop(position);
    var structdata = $('#structdata');

    if (structdata != null) {
        structdata.scrollTop(pos2);
    }
});
