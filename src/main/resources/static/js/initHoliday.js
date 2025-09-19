// --- MODAL HELPER FUNCTIONS ---
function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

function showModalAndRedirect(message, url) {
    showModalAlert(message);
    $('#feedbackModal').one('hidden.bs.modal', function () {
        window.location.href = url;
    });
}

function showModalConfirm(message, callback, title = 'Confirmation') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').text(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>' +
        '<button type="button" id="modalConfirmOkButton" class="btn btn-primary">OK</button>'
    );
    $('#feedbackModal').modal('show');

    $('#modalConfirmOkButton').one('click', function() {
        $('#feedbackModal').modal('hide');
        callback();
    });
}


// --- EXISTING SCRIPT ---
$(document).ready(function () {
    var finyear = "";
    var finyearstart = "";
    var finyearend = "";
    var d = new Date();
    var m = d.getMonth() + 1;
    var y = d.getFullYear();
    for (var i = 0; i < 5; i++) {
        if (i !== 0) {
        }
        if (m > 3) {
            finyear = (y + i) + "-" + ((y + i + 1) + "").substring(2);
            finyearstart = (y + i) + "-04";
            finyearend = (y + i + 1) + "-03";
        } else {
            finyear = (y + i - 1) + "-" + ((y + i) + "").substring(2);
            finyearstart = (y + i - 1) + "-04";
            finyearend = (y + i) + "-03";
        }
        $('#financialyear').append($("<option>").val(finyearstart + '##' + finyearend).text(finyear));
    }

    $("#financialyear").change(function () {
        $("#holidaydate").val("");
        $("#holidayreason").val("");
        var fy = $("#financialyear").val();
        if ($("#financialyear").val() !== "-1") {
            var finyearstart = fy.split("##")[0];
            var finyearend = fy.split("##")[1];
            $('#holidaydate').datepicker({
                minDate: new Date(Number(finyearstart.split("-")[0]), 3, 1)
            });
            $('#holidaydate').datepicker({
                maxDate: new Date(Number(finyearend.split("-")[0]), 2, 31)
            });
            getholidaylist(finyearstart, finyearend);
        }

    });
});

// Holidays list
function getholidaylist(finyearstart, finyearend) {
    $.ajax({
        type: "POST",
        url: "/nerie/holidays/list",
        data: "finyearstart=" + finyearstart + "&finyearend=" + finyearend,
        success: function (data) {
            console.log(data)

            $("#htable tbody tr ").empty();
            var tds;
            tds = '<table class="table table-striped table-bordered center-sans-serif" id="htable">' +
            ' <thead style="font-size: 14px; ">' +
            ' <tr>' +
            '<th>Sl No.</th>' +
            '<th>Holiday Date</th>' +
            '<th>Description</th>' +
            '<th class="noExport">Edit</th>' +
            '<th class="noExport">Delete</th>' +
            '</tr>' +
            '</thead>' +
            '<tbody style="font-size: 12px;">';

            data.forEach(([date, reason]) => {
                tds += `
                    <tr>
                        <td>*</td>
                        <td>${date}</td>
                        <td>${reason}</td>
                        <td>
                            <button class="clickme danger11 editfunc-btn btn btn-sm btn-info"
                                data-param1="${date}"
                                data-param2="${reason}"
                            >
                                <i class="fa fa-edit">  Edit</i>
                            </button>
                        </td>
                        <td>
                            <button class="clickme danger11 deletesubjectbtn btn btn-sm btn-danger" data-function="deletefunc" data-param1="${date}">
                                <span style="color: #fff"><i class="fa fa-trash">  Delete</i></span>
                            </button>
                        </td>
                    </tr>
                `
            })
            tds += '</tbody></table>';
            $("#tablediv").html(tds);
            var tname = "htable";
            var rname = "Holiday List";
            getdatatable(tname, rname);
            // Select all buttons with the class 'editfunc-btn'
            const editFuncButtons = document.querySelectorAll('.editfunc-btn');

            // Add a click event listener to each button
            editFuncButtons.forEach(function (button) {
                button.addEventListener('click', function (event) {
                    event.preventDefault(); // Prevent default behavior

                    // Retrieve data attributes
                    const param1 = button.getAttribute('data-param1');
                    const param2 = button.getAttribute('data-param2');

                    // Call the editfunc function with the parameters
                    if (typeof editfunc === 'function') {
                        editfunc(param1, param2);
                    } else {
                        console.error('editfunc is not defined');
                    }
                });
            });
        },
        error: (jqXHR, textStatus, errorThrown) => showModalAlert(`An error occurred: ${textStatus} - ${errorThrown}`)
    });
}

document.addEventListener("DOMContentLoaded", function () {
    document.addEventListener("click", function (event) {
        let target = event.target.closest(".editsubjectbtn");
        if (target) {
            event.preventDefault();
            let functionName = target.getAttribute("data-function");

            // Collect parameters dynamically
            let params = [];
            for (let i = 1; i <= 2; i++) {
                let paramValue = target.getAttribute(`data-param${i}`);
                params.push(paramValue);
            }

            // Call the function dynamically if it exists
            if (typeof window[functionName] === "function") {
                window[functionName](...params);
            } else {
                console.error(`Function ${functionName} is not defined`);
            }
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    document.addEventListener("click", function (event) {
        let target = event.target.closest(".deletesubjectbtn");
        if (target) {
            event.preventDefault();
            let functionName = target.getAttribute("data-function");

            // Get the single parameter
            let param1 = target.getAttribute("data-param1");

            // Call the function dynamically if it exists
            if (typeof window[functionName] === "function") {
                window[functionName](param1);
            } else {
                console.error(`Function ${functionName} is not defined`);
            }
        }
    });
});

// Holidays save
$(document).ready(function () {
    $("#mholidayfid").submit(function (e) {
        e.preventDefault();
        if ($("#holidaydate").val()) {
            $.ajax({
                type: "POST",
                url: "/nerie/holidays/save",
                data: $("#mholidayfid").serialize(),
                success: function (data) {
                    if (data == "2") {
                        showModalAlert("Successfully Saved!");
                        $("#holidaydate").val("");
                        $("#oldholidaydate").val("");
                        $("#holidayreason").val("");
                        $("#holidaydate").prop("disabled", false);
                        var fy = $("#financialyear").val();
                        var finyearstart = fy.split("##")[0];
                        var finyearend = fy.split("##")[1];
                        getholidaylist(finyearstart, finyearend)
                    } else {
                        showModalAlert("Save Failed!");
                    }
                },
                error: (jqXHR, textStatus, errorThrown) => showModalAlert(`An error occurred: ${textStatus} - ${errorThrown}`)
            })
        } else {
            showModalAlert("Please select the holiday date");
            $("#holidaydate").focus();
        }
    })
});

function editfunc(hdate, hreason) {
    const date = hdate.split('-')
    $("#holidaydate").val(`${date[1]}/${date[2]}/${date[0]}`);
    $("#holidayreason").val(hreason);
    $("#holidayreason").focus();
}

function customReset() {
    $("#financialyear").val("");
    $("#holidaydate").val("");
    $("#oldholidaydate").val("");
    $("#holidayreason").val("");
}

// Holiday delete
function deletefunc(hdate) {
    showModalConfirm("Are you sure you want to delete?", function() {
        $.ajax({
            type: "POST",
            url: "/nerie/holidays/remove",
            data: "holidaydate=" + hdate,
            success: function (data) {
                if (data == "1") {
                    showModalAlert("Deleted successfully");
                    var fy = $("#financialyear").val();
                    var finyearstart = fy.split("##")[0];
                    var finyearend = fy.split("##")[1];
                    getholidaylist(finyearstart, finyearend)
                } else {
                    showModalAndRedirect("Error Occurred! Please try again", "/nerie/holidays/init");
                }
            },
            error: (jqXHR, textStatus, errorThrown) => showModalAlert(`An error occurred: ${textStatus} - ${errorThrown}`)
        })
    });
}

function getdatatable(tname, rname) {
    $('#' + tname).DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Holidays',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });
}

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-action='reset']").forEach(button => {
        button.addEventListener("click", function () {
            customReset();
        });
    });
});

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