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
    const submitButton = document.getElementById('submit');
    if (submitButton) {
        submitButton.addEventListener('click', function () {
            getlistreportfunc();
        });
    }
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

$("#programs").click(function () {
        if ($("#finyear").val() == "") {
            alert("Please select Financial Year First")
            $("#finyear").focus();
            return false;
        }
    });
});

function getlistreportfunc() {
    if ($("#programs").val() == "-1") {
        alert("Please select a program");
        $("#programs").focus();
        return false;
    }
    if ($("#phaseid").val() == "-1") {
        alert("Please select the Phase of the program");
        $("#phaseid").focus();
        return false;
    }

    var url = "/nerie/reports/scheduleReport?phaseid=" + $("#phaseid").val() ;
    window.open(url, '_blank')
}