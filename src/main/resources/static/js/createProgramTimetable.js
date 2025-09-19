var pTable = initdatatable("programtable", "Program TT Export");

function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    let errorMessage = `An error occurred: ${textStatus} - ${errorThrown}`;
    if (jqXHR.responseText) {
        errorMessage += `<br><br>Details: ${jqXHR.responseText}`;
    }
    showModalAlert(errorMessage, 'Request Failed');
}

document.addEventListener('DOMContentLoaded', () => {
    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    $('input[type=radio][name=breakclass]').change(function () {
        if (this.value == 'option1') {
            $('#subinput').hide();
            $('#rpinput').hide();
            $('#rpslno').removeAttr('required');
            $('#subject').removeAttr('required');
            $('#rpslno').val('');
            $('.multiselectDropdown').selectpicker('refresh');
            $('#subject').val("BREAK");

        } else if (this.value == 'option2') {
            $('#subinput').show();
            $('#rpinput').show();
            $('#rpslno').attr('required', 'true');
            $('#subject').attr('required', 'true');
            $('#rpslno').val('');
            $('.multiselectDropdown').selectpicker('refresh');
            $('#subject').val("");
        }
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

    $("#starttime,#endtime").datetimepicker({
        format: 'LT'
    });

    $('#programs').change(function () {
        $('#phaseno').empty().append($('<option></option>').attr("value", "").text("Select"));
        if ($('#programs').val()) {
            callCustomAjax("/nerie/phases/list", "programcode=" + $('#programs').val(), function (data) {
                if (data) {
                    data.forEach(function (x) {
                        $('#phaseno').append($('<option></option>').attr("value", x[0]).text(x[1]));
                    });
                }
            });
        }
    });

    $('#phaseno').change(function () {
        $('#venuecode, #programday, #rpslno').empty();
        $('#venuecode, #programday').append($("<option>").val("").text("Select"));
        if ($('#phaseno').val()) {
            callCustomAjax("/nerie/venues/list", "phaseid=" + $("#phaseno").val(), function (data) {
                if (data) {
                    data.forEach(item => $('#venuecode').append($("<option>").val(item[0]).text(item[1])));
                }
            });

            callCustomAjax("/nerie/program-details/list", "phaseid=" + $("#phaseno").val(), function (data) {
                if (data) {
                    data.forEach(item => $('#programday').append($("<option>").val(item[2]).text(item[1])));
                }
            });

            callCustomAjax("/nerie/resource-persons/list/course-phase", "phaseid=" + $("#phaseno").val(), function (data) {
                if (data) {
                    $('.multiselectDropdown').selectpicker('destroy');
                    data.forEach(item => $('#rpslno').append($("<option>").val(item[0]).text(item[1])));
                    $('.multiselectDropdown').selectpicker();
                }
            });
        }
    });

    $("#venuecode").change(function () {
        $('#roomcode').empty().append($("<option>").val("").text("Select"));
        callCustomAjax("/nerie/venue-rooms/list", "venuecode=" + $("#venuecode").val(), function (data) {
            if (data) {
                data.forEach(item => $('#roomcode').append($("<option>").val(item[0]).text(item[1])));
            }
        });
    });

    $("#programday").change(function () {
        if ($("#phaseno").val() && $("#programday").val()) {
            loadTimeTable();
        }
    });

    $('#tprogramttform').submit(function (e) {
        e.preventDefault();

        const originalStartTime = $("#starttime").val();
        const originalEndTime = $("#endtime").val();

        let stime = originalStartTime.trim();
        let etime = originalEndTime.trim();

        let shh = stime.split(":")[0];
        let ehh = etime.split(":")[0];
        let smm = (stime.split(":")[1]).split(" ")[0];
        let emm = (etime.split(":")[1]).split(" ")[0];
        let sam = stime.split(" ")[1];
        let eam = etime.split(" ")[1];

        if (sam == "PM") {
            if (Number(shh) !== 12) stime = (Number(shh) + 12) + ":" + smm + ":00";
            else stime = "12:" + smm + ":00";
        } else {
            if (Number(shh) === 12) stime = "00:" + smm + ":00";
            else stime = shh + ":" + smm + ":00";
        }

        if (eam === "PM") {
            if (Number(ehh) !== 12) etime = (Number(ehh) + 12) + ":" + emm + ":00";
            else etime = "12:" + emm + ":00";
        } else {
            if (Number(ehh) === 12) etime = "00:" + emm + ":00";
            else etime = ehh + ":" + emm + ":00";
        }

        let cdate = standardToRawdate($("#programday :selected").text().split("-")[0].trim());

        $("#starttime").val(stime);
        $("#endtime").val(etime);
        $("#programdate").val(cdate);

        callCustomAjax("/nerie/timetable/program-timetable/save", $('#tprogramttform').serialize(), function (data) {
            $("#starttime").val(originalStartTime);
            $("#endtime").val(originalEndTime);

            const response = data.trim();
            switch (response) {
                case "2":
                    showModalAlert("Successfully Added!!!", "Success");
                    $('#feedbackModal').one('hidden.bs.modal', function () {
                        $('#tprogramttform')[0].reset();
                        $('.multiselectDropdown').selectpicker('refresh');
                        $('input[name=breakclass][value=option2]').prop('checked', true).trigger('change');
                        loadTimeTable();
                    });
                    break;
                case "1":
                    showModalAlert("Time Duration cannot be empty.", "Validation Error");
                    break;
                case "3":
                    showModalAlert("Please check the time duration format.", "Validation Error");
                    break;
                case "4":
                    showModalAlert("Subject cannot be empty.", "Validation Error");
                    break;
                case "5":
                    showModalAlert("Subject should be 1-70 characters long.", "Validation Error");
                    break;
                case "6":
                    showModalAlert("There is a clash in the Time Table for the selected slot.", "Scheduling Conflict");
                    break;
                case "7":
                    showModalAlert("The selected Resource Person is engaged with another session.", "Scheduling Conflict");
                    break;
                case "8":
                    showModalAlert("The selected Room/Hall is not available at this time.", "Scheduling Conflict");
                    break;
                default:
                    showModalAlert("Save Failed! Please try again.", "Error");
                    break;
            }
        });
    });
});

function myFunction(x) {
    x.classList.toggle("change");
}

function loadTimeTable() {
    callCustomAjax("/nerie/program-details/list/timetable", "phaseid=" + $("#phaseno").val() + "&programday=" + $("#programday").val(), function (data) {
        if(pTable) pTable.clear().destroy();
        $('#programtable tbody').empty();

        if (data && data.length !== 0) {
            data.forEach(function (x) {
                var rowData = `<tr>
                        <td style="font-size: smaller">${x[13]}<br/>(${x[16]})</td>
                        <td style="font-size: smaller">${x[6]}</td>
                        <td style="font-size: smaller">${x[7]}</td>
                        <td style="font-size: smaller">${x[8]}</td>
                        <td style="font-size: smaller">${x[10]}</td>
                        <td style="font-size: smaller">
                        <a href="#" class="clickme danger11 edit-program-btn"
                           data-sttcode="${x[3]}" data-phaseid="${x[0]}" data-venuecode="${x[14]}"
                           data-roomcode="${x[15]}" data-courseday="${x[5]}" data-starttime="${x[6]}"
                           data-endtime="${x[7]}" data-subject="${x[8]}" data-rpslno="${x[9]}">
                           <i class="fa fa-edit">  Edit</i>
                        </a>
                        </td>
                        </tr>`;
                $('#programtable tbody').append(rowData);
            });

            document.querySelectorAll(".edit-program-btn").forEach(button => {
                button.addEventListener("click", function (event) {
                    event.preventDefault();
                    const ds = this.dataset;
                    editfunc(ds.sttcode, ds.phaseid, ds.venuecode, ds.roomcode, ds.courseday, ds.starttime, ds.endtime, ds.subject, ds.rpslno);
                });
            });

            $('#tablediv').show();
            $('#errordiv').hide();
        } else {
            $('#tablediv').hide();
            $('#errordiv').show();
            $('#errorspan').html("Schedule not defined for the selected day.");
        }
        pTable = initdatatable("programtable", "Program TT Export");
    });
}

function editfunc(sttcode, phaseid, venuecode, roomcode, courseday, starttime, endtime, subject, rpslno) {
    $("#programtimetablecode").val(sttcode);
    $("#venuecode").val(venuecode).trigger('change');
    $("#courseday").val(courseday);
    $("#starttime").val(starttime);
    $("#endtime").val(endtime);
    $("#subject").val(subject);

    if(subject.toUpperCase() === 'BREAK'){
         $('input[name=breakclass][value=option1]').prop('checked', true).trigger('change');
    } else {
         $('input[name=breakclass][value=option2]').prop('checked', true).trigger('change');
    }

    setTimeout(() => { $("#roomcode").val(roomcode); }, 500);

    var rpslnos = rpslno ? rpslno.split(',') : [];
    $("#rpslno").val(rpslnos);
    $('.multiselectDropdown').selectpicker('refresh');
}