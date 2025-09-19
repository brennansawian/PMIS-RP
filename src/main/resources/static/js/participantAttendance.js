document.addEventListener("DOMContentLoaded", () => {
    $("#backtotop").click(function () {
        $("html, body").animate({ scrollTop: 0 }, 600);
        return false;
    });
    
    $(".sub-menu ul").hide();
    $(".sub-sub-menu ul").hide();
    $(".sub-menu a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
    $(".sub-sub-menu a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
});

$(document).ready(function () {
    $("#submit").hide();
    $("#cancel").hide();
    $(document).on("keypress", ".numbers", function (event) {
        if (event.which < 48 || event.which > 57) {
            event.preventDefault();
        }
    });

    $(function () {
        $("input.alphabets").keyup(function () {
            if (this.value.match(/[^a-zA-Z. ]/g)) {
                this.value = this.value.replace(/[^a-zA-Z. ]/g, "");
            }
        });
    });

    $("#programs > option").text(function (i, text) {
        if (text.length > 100) {
            return text.substr(0, 100) + "...";
        }
    });
});

var pTable = initdatatable("participantatt", "Participant Attendance ");

$(document).ready(() => {
    $("#programs").change(function () {
        $("#phaseno").empty();
        $("#phaseno").append(
            $("<option></option>").attr("value", "").text("Select")
        );
        $("#phaseno").val("").trigger("change");
        if ($("#programs").val()) {
            callCustomAjax(
                "/nerie/phases/list",
                "programcode=" + $("#programs").val(),
                function (data) {
                    if (data) {
                        data.forEach(function (x) {
                            $("#phaseno").append(
                                $("<option></option>")
                                    .attr("value", x[0])
                                    .text(x[1])
                            );
                        });
                    }
                }
            );
        }
    });

    $("#phaseno").change(function () {
        $("#programday").empty();
        $("#programday").append(
            $("<option></option>").attr("value", "").text("Select")
        );
        $("#programday").val("").trigger("change");
        if ($("#phaseno").val()) {
            callCustomAjax(
                "/nerie/program-details/list",
                "phaseid=" + $("#phaseno").val(),
                function (data) {
                    if (data) {
                        for (var i = 0; i < data.length; i++) {
                            $("#programday").append(
                                $("<option>").val(data[i][2]).text(data[i][1])
                            );
                        }
                    }
                }
            );
        }
    });

    $("#programday").change(function () {
        $("#timings").empty();
        $("#timings").append(
            $("<option></option>").attr("value", "").text("Select")
        );
        $("#timings").val("").trigger("change");
        if ($("#programday").val()) {
            var datatosend =
                "phaseid=" +
                $("#phaseno").val() +
                "&programday=" +
                $("#programday").val();

            callCustomAjax(
                "/nerie/timetable/program-timetable",
                datatosend,
                function (data) {
                    if (data) {
                        for (var i = 0; i < data.length; i++) {
                            $("#timings").append(
                                $("<option>").val(data[i][0]).text(data[i][1])
                            );
                        }
                    }
                }
            );
        }
    });

    $("#timings").change(function () {
        if ($("#timings").val()) {
            getParticipantsList();
        } else {
            $("#tablediv").hide();
            $("#submit").hide();
            $("#cancel").hide();
        }
    });

    $("#tparticipantattendanceid").submit(function (e) {
            e.preventDefault(); // Prevent the form from submitting the old way

            // 1. Get the values from the form controls.
            const phaseId = $("#phaseno").val();
            const timetableCode = $("#timings").val();

            // 2. Collect all *checked* application codes into an array.
            const applicationCodes = [];
            $('input[name="p_applicationcode"]:checked').each(function() {
                applicationCodes.push($(this).val());
            });

            $.ajax({
                type: "POST",
                url: "/nerie/participant/attendance/save",
                data: {
                    phaseid: phaseId,
                    programtimetablecode: timetableCode,
                    p_applicationcode: applicationCodes // Pass the array of checked values
                },
                success: function (response) {
                    if (response === "2") {
                        alert("Attendance saved successfully!");
                        // Refresh the participant list to show the updated status
                        getParticipantsList();
                    } else {
                        alert("Save Failed! Please try again.");
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.error("Error saving attendance:", textStatus, errorThrown);
                    alert("An error occurred: " + (jqXHR.responseText || "Please check the console for details."));
                }
            });
        });
    });

function getParticipantsList() {
    callCustomAjax(
        "/nerie/applications/session-participants/list",
        "phaseid=" +
            $("#phaseno").val() +
            "&programtimetablecode=" +
            $("#timings").val(),
        function (data) {
            pTable.clear();
            pTable.destroy();
            if (data) {
                count = 1;
                data.forEach(function (x) {
                    var checkbox = "";
                    if (x[3] === "P") {
                        checkbox =
                            '<input type="checkbox" name="p_applicationcode" checked value=' +
                            x[2] +
                            "></input>";
                    } else {
                        checkbox =
                            '<input type="checkbox" name="p_applicationcode" value=' +
                            x[2] +
                            "></input>";
                    }
                    var rowData =
                        "<tr>" +
                        "<td>" +
                        count++ +
                        "</td>" +
                        "<td>" +
                        x[2] +
                        "</td>" +
                        "<td>" +
                        x[1] +
                        "</td>" +
                        "<td>" +
                        checkbox +
                        "</td>" +
                        "</tr>";
                    $("#participantatt tbody").append(rowData);
                });
                $("#tablediv").show();
                $("#submit").show();
                $("#cancel").show();
            }
        }
    );
    pTable = initdatatable("participantatt", "Participant Attendance ");
}

function myFunction(x) {
    x.classList.toggle("change");
}
