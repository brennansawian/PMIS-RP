function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function showModalConfirm(message, title, callback) {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    const footer = `
        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
        <button type="button" id="modalConfirmOkButton" class="btn btn-danger">OK</button>
    `;
    $('#feedbackModal .modal-footer').html(footer);
    $('#feedbackModal').modal('show');

    $('#modalConfirmOkButton').off('click').on('click', function() {
        $('#feedbackModal').modal('hide');
        if (typeof callback === 'function') {
            callback();
        }
    });
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    let errorMessage = `An error occurred: ${textStatus} - ${errorThrown}`;
    if (jqXHR.responseText) {
        errorMessage += `<br><br>Details: ${jqXHR.responseText}`;
    }
    showModalAlert(errorMessage, 'Request Failed');
}

var mutable;
var programcode;

document.addEventListener('DOMContentLoaded', () => {
    mutable = initdatatable("programmaterialtable", "");

    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $('#backtotop').click(function() {
        $("html, body").animate({ scrollTop: 0 }, 600);
        return false;
    });

    $('.sub-menu ul').hide();
    $('.sub-sub-menu ul').hide();
    $(".sub-menu a").click(function() {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });
    $(".sub-sub-menu a").click(function() {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    document.querySelectorAll('.resetformbtn').forEach(button => {
        button.addEventListener('click', resetform);
    });

    document.getElementById('addft').addEventListener('click', setcoursecodefunc);

    $("#financialyear").change(function() {
        var fy = $("#financialyear").val();
        $('#programs, #phaseno').empty().append($('<option></option>').attr("value", "").text("Select"));
        if (fy) {
            var fystart = fy.split("##")[0];
            var fyend = fy.split("##")[1];
            callCustomAjax("/nerie/program/financial-year/list", "fystart=" + fystart + "&fyend=" + fyend, function(data) {
                if (data) {
                    data.forEach(x => {
                        $('#programs').append($('<option></option>').attr("value", x[0]).text(x[1]).attr('title', x[1]));
                    });
                    $('#programs > option').text((i, text) => (text.length > 100) ? text.substr(0, 100) + '...' : text);
                }
            });
        }
    });

    $('#programs').change(function() {
        $('#phaseno').empty().append($('<option></option>').attr("value", "").text("Select"));
        if ($('#programs').val()) {
            callCustomAjax("/nerie/phases/list", "programcode=" + $('#programs').val(), function(data) {
                if (data) {
                    data.forEach(x => $('#phaseno').append($('<option></option>').attr("value", x[0]).text(x[1])));
                }
            });
        }
    });

    $('#phaseno').change(function() {
        if ($('#phaseno').val()) {
            $('#phaseid').val($('#phaseno').val());
            getmaterialuploaddatafunc();
        } else {
             $("#uploadbtn").hide();
        }
    });

    $("#tprogrammaterialform").submit(function(e) {
        e.preventDefault();
        if (!checkmaterialdoctype()) return;

        var formData = new FormData($(this)[0]);
        $.ajax({
            type: "POST",
            url: "/nerie/program-materials/save",
            data: formData,
            processData: false,
            contentType: false,
            success: function(data) {
                $('#modalsubj-modal').modal('hide');
                if (data == "2") {
                    showModalAlert("Successfully Saved!!!", "Success");
                    $('#feedbackModal').one('hidden.bs.modal', function() {
                        getmaterialuploaddatafunc();
                        resetform();
                    });
                } else if (data == "1") {
                    showModalAlert("Please Upload Program Material.", "Warning");
                } else {
                    showModalAlert("Save Failed!!!", "Error");
                }
            },
            error: handleAjaxError
        });
    });
});

function resetform() {
    $("#tprogrammaterialform")[0].reset();
}

function checkmaterialdoctype() {
    if (document.getElementById("file1").files.length !== 0) {
        var mext = $("#file1").val().split('.').pop().toLowerCase();
        if (mext !== "jpg" && mext !== "jpeg" && mext !== "pdf") {
            showModalAlert("File must be of type PDF, JPG, or JPEG.", "Invalid File Type");
            $("#file1").val("");
            return false;
        }
    }
    return true;
}

function getmaterialuploaddatafunc() {
    callCustomAjax("/nerie/program-materials/list", "phaseid=" + $('#phaseno').val(), function(data) {
        if(mutable) {
            mutable.clear().destroy();
        }
        $('#programmaterialtable tbody').empty();

        if (data && data.length > 0) {
            let count = 1;
            data.forEach(x => {
                var udate = getdateformate(x[2]);
                var rd = "<tr>" +
                        "<td>" + (count++) + "</td>" +
                        "<td>" + x[1] + "</td>" +
                        "<td style='white-space: nowrap'>" + udate + "</td>" +
                        "<td><a href='/nerie/program-materials/view-file?programmaterialid=" + x[0] + "' target='_blank'>View File</a> </td>" +
                        "<td><a href='#' class='clickme enablebtn delete-material-btn' data-programmaterialid='" + x[0] + "'><span style='color: #fff'><i class='fa fa-trash'> &nbsp;Delete</i></span></a></td>" +
                        "</tr>";
                $('#programmaterialtable tbody').append(rd);
            });
            $('#tablediv').show();
            $("#uploadbtn").show();
            
            // Add event listener for delete material buttons
            var deleteButtons = document.querySelectorAll('.delete-material-btn');

            deleteButtons.forEach(function(button) {
                button.addEventListener('click', function(event) {
                    event.preventDefault();
                    var programMaterialId = this.getAttribute('data-programmaterialid');
                    deletematerialfunc(programMaterialId);
                });
            });

            $('#tablediv').show();
            $("#uploadbtn").show();
        } else {
            $('#tablediv').hide();
            $("#uploadbtn").show();
            showModalAlert('No data found for the selected phase.', 'Information');
        }
        mutable = initdatatable("programmaterialtable", "");
    });
}

function deletematerialfunc(cmid) {
    if (confirm("Are you sure you want to delete this program material?")) {
        $.ajax({
            type: "POST",
            url: "/nerie/program-materials/delete",
            data: "programmaterialid=" + cmid,
            success: function (data) {
                if (data == "1") {
                    alert("Program Material deleted successfully");
                    getmaterialuploaddatafunc();
                } else {
                    alert("Error Occured!!! Please try again");
                    window.location.reload()
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
       })
    }
}

function setcoursecodefunc() {
    $("#programcode").val($("#programs").val());
}

function getdateformate(d) {
    if (!d) return '';
    var dateObj = new Date(d);
    var day = String(dateObj.getDate()).padStart(2, '0');
    var month = String(dateObj.getMonth() + 1).padStart(2, '0');
    var year = dateObj.getFullYear();
    return `${day}-${month}-${year}`;
}

function myFunction(x) {
    x.classList.toggle("change");
}