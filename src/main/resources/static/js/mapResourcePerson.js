function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function showModalAndReload(message, title = 'Success') {
    showModalAlert(message, title);
    $('#feedbackModal').one('hidden.bs.modal', function () {
        window.location.reload();
    });
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    let errorMessage = `An error occurred: ${textStatus} - ${errorThrown}`;
    if (jqXHR.responseText) {
        errorMessage += `<br><br>Details: ${jqXHR.responseText}`;
    }
    showModalAlert(errorMessage, 'Request Failed');
}

function customReset() {
    window.location.reload();
}

$(document).ready(function () {
    var rptable = initdatatable("resoursepersontable", "Resource Persons List Exported");

    $("#resetButton").on("click", customReset);

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('.sub-menu ul, .sub-sub-menu ul').hide();

    $(".sub-menu a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $(".sub-sub-menu a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $("#financialyear").change(function () {
        var fy = $("#financialyear").val();
        $('#programs, #phaseno').empty().append($('<option></option>').attr("value", "").text("Select"));
        if (fy) {
            var fystart = fy.split("##")[0];
            var fyend = fy.split("##")[1];

            $.ajax({
                type: "POST",
                url: "/nerie/program/financial-year/list",
                data: { fystart: fystart, fyend: fyend },
                success: function (data) {
                    if (data) {
                        data.forEach(function (x) {
                            $('#programs').append($('<option></option>').attr("value", x[0]).text(x[1]));
                        });
                    }
                },
                error: handleAjaxError
            });
        }
    });

    $('#programs').change(function () {
        $('#phaseno').empty().append($('<option></option>').attr("value", "").text("Select"));
        if ($('#programs').val()) {
            $.ajax({
                type: "POST",
                url: "/nerie/phases/list",
                data: { programcode: $('#programs').val() },
                success: function (data) {
                    if (data) {
                        data.forEach(function (x) {
                            $('#phaseno').append($('<option></option>').attr("value", x[0]).text(x[1]));
                        });
                    }
                },
                error: handleAjaxError
            });
        }
    });

    $('#phaseno').change(function () {
        if ($('#phaseno').val()) {
            $.ajax({
                type: "POST",
                url: "/nerie/resource-persons/list/phase",
                data: { phaseid: $('#phaseno').val() },
                success: function (data) {
                    rptable.clear().destroy();
                    if (data && data.length > 0) {
                        let tableBody = $('#resoursepersontable tbody');
                        tableBody.empty();
                        data.forEach(function (x) {
                            var rd = "<tr>" +
                                "<td><input type='checkbox' value='" + x[0] + "' name='resourceperson'" + (x[8] ? " checked" : "") + " /></td>" +
                                "<td>" + x[2] + "</td>" +
                                "<td>" + x[1] + "</td>" +
                                "<td>" + x[3] + "</td>" +
                                "<td>" + x[4] + '<br>(' + x[5] + ')' + "</td>" +
                                "<td>" + x[6] + "</td>" +
                                "<td>" + x[7] + "</td>" +
                                "</tr>";
                            tableBody.append(rd);
                        });
                        $('#tablediv').show();
                    } else {
                        $('#tablediv').hide();
                        showModalAlert('No data found');
                    }
                    rptable = initdatatable("resoursepersontable", "Resource Persons List Exported");
                },
                error: handleAjaxError
            });
        }
    });

    $("#mprogramfid").submit(function (e) {
        e.preventDefault();
        var isChecked = $('input:checkbox[name=resourceperson]:checked').length > 0;

        if (!isChecked) {
            showModalAlert("Please select at least one resource person");
            return false;
        }

        $.ajax({
            type: "POST",
            url: "/nerie/resource-persons/map/save",
            data: $("#mprogramfid").serialize(),
            success: function (data) {
                if (data === "2") {
                    showModalAndReload("Successfully Saved!!!");
                } else {
                    showModalAlert("Save Failed!!!");
                }
            },
            error: handleAjaxError
        });
    });
});