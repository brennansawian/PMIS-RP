function myFunction(x) {
    x.classList.toggle("change");
}
$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});

$(document).ready(function () {
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const viewAggregateButton = document.getElementById('view_aggregateid');

    viewAggregateButton.addEventListener('click', function () {
        getlistreportfunc('1');
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const viewDetailsButton = document.getElementById('view_detailsid');

    viewDetailsButton.addEventListener('click', function () {
        getlistreportfunc('2');
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const viewRpButton = document.getElementById('view_rpid');

    viewRpButton.addEventListener('click', function () {
        getlistreportfunc('3');
    });
});

$('.sub-menu ul').hide();
$('.sub-sub-menu ul').hide();
$(".sub-menu a").click(function () {
    $(this).parent(".sub-menu").children("ul").slideToggle("100");
    $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
});
$(".sub-sub-menu a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    }
);

$(document).ready(function () {
$("#view_aggregateid").hide();
$("#view_detailsid").hide();
$("#finyear").change(function () {
    var fy = $("#finyear").val();
    $('#programs,#phaseid').empty();
    $('#programs,#phaseid').append($('<option></option>').attr("value", "").text("Select"));
    if (fy)
    {
        var fystart = fy.split("##")[0];
        var fyend = fy.split("##")[1];

        callCustomAjax("/nerie/student-leaves/getFYCourseList", "fystart=" + fystart + "&fyend=" + fyend, function (data) {
            if (data)
            {
                data.forEach(function (x) {
                    $('#programs').append($('<option></option>').attr("value", x[0]).text(x[1]).attr("title", x[1]));
                });
                $('#programs > option').text(function (i, text) {
                    if (text.length > 100) {
                        return text.substr(0, 100) + '...';
                    }
                });
            }
        });
    }
});

$('#programs').change(function () {
    $('#phaseid').empty();
    $('#phaseid').append($('<option></option>').attr("value", "").text("Select"));
    if ($('#programs').val())
    {
        callCustomAjax("/nerie/phases/getPhasesBasedOnProgram", "programcode=" + $('#programs').val(), function (data) {
            if (data)
            {
                data.forEach(function (x) {
                    $('#phaseid').append($('<option></option>').attr("value", x[0]).text(x[1]));
                });
            }
        });
    }
});

$("#reporttype").change(function (e) {
        var rtype = $("#reporttype").val();
        if (rtype === "1") {
            $("#view_aggregateid").show();
            $("#view_detailsid").show();
            $("#view_rpid").hide();
        } else if (rtype === "2") {
            $("#view_aggregateid").hide();
            $("#view_detailsid").hide();
            $("#view_rpid").show();
        }
    });
});


function getlistreportfunc(status) {

    if ($("#reporttype").val() === "-1") {
        alert("Please select report type");
        $("#reporttype").focus();
        return false;
    }
    if ($("#programs").val() === "-1") {
        alert("Please select the Program");
        $("#programs").focus();
        return false;
    }
    if ($("#phaseid").val() === "-1") {
        alert("Please select the Phase");
        $("#phaseid").focus();
        return false;
    }

    var url = "/nerie/reports/ReportAttendance?status=" + status + "&phaseid=" + $("#phaseid").val() ;
    window.open(url, '_blank')

}