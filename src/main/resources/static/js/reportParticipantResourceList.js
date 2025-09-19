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

//            $('#phaseid').change(function () {
//                if ($('#phaseid').val())
//                {
//                    $('#phaseid').val($('#phaseid').val());
//                    getlistreportfunc();
//
//                }
//            });
    $("#programs").click(function () {
        if ($("#finyear").val() === "") {
            alert("Please select Financial Year First")
            $("#finyear").focus();
            return false;
        }
    });
});


function getlistreportfunc() {
    if ($("#reporttype").val() === "-1") {
        alert("Please select report type");
        $("#reporttype").focus();
        return false;
    }
    if ($('programs').val() === "-1") {
        alert("Please select a Program");
        $("#programcode").focus();
        return false;
    }
    if ($("#phaseid").val() === "-1") {
        alert("Please select the Program");
        $("#phaseid").focus();
        return false;
    }
     var url = "/nerie/reports/ReportLA?status=" + $("#reporttype").val() + "&phaseid=" + $("#phaseid").val() ;
    window.open(url, '_blank')
}

//Added listener
document.addEventListener('DOMContentLoaded', function () {
    var submitButtons = document.querySelectorAll(".btn.btn-info.submitbtn");
    console.log("Selected submit buttons:", submitButtons);

    submitButtons.forEach(function(button) {
        button.addEventListener("click", function(event) {
            event.preventDefault();

            var functionName = button.getAttribute("data-function");


            if (typeof window[functionName] === "function") {
                window[functionName]();
            } else {
                console.error(`Function ${functionName} is not defined`);
            }
        });
    });
});