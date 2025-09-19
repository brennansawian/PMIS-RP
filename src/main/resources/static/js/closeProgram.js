var utable;
var ptable;
var programcode;

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
        <button type="button" id="modalConfirmOkButton" class="btn btn-danger">Confirm</button>
    `;
    $('#feedbackModal .modal-footer').html(footer);
    $('#feedbackModal').modal('show');

    $('#modalConfirmOkButton').off('click').on('click', function() {
        $('#feedbackModal').modal('hide');
        $('#feedbackModal').one('hidden.bs.modal', function () {
            if (typeof callback === 'function') {
                callback();
            }
        });
    });
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    let errorMessage = `An error occurred: ${textStatus} - ${errorThrown}`;
    if (jqXHR.responseText) {
        errorMessage += `<br><br>Details: ${jqXHR.responseText}`;
    }
    showModalAlert(errorMessage, 'Request Failed');
}

document.addEventListener("DOMContentLoaded", () => {
    utable = getdatatable("unclosetable", "Unclosed Programs Exported");
    ptable = getdatatable("phasestable", "Unclosed Phases Exported");

    $('.sub-menu ul, .sub-sub-menu ul').hide();

    $("#menu-toggle").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    $(".sub-menu a").click(function () {
        $(this).parent(".sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $(".sub-sub-menu a").click(function () {
        $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
        $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
    });

    $("#fyunclose").change(getClosedPrograms);
    
    document.addEventListener("click", function (event) {
        const viewBtn = event.target.closest(".viewphasesbtn");
        const closeProgramBtn = event.target.closest(".closeprogrambtn");
        const closePhaseBtn = event.target.closest(".closephasebtn");

        if (viewBtn) {
            viewphasesofprogram(viewBtn.getAttribute("data-pcode"));
        }
        if (closeProgramBtn) {
            closeprogram(closeProgramBtn.getAttribute("data-pcode"));
        }
        if (closePhaseBtn) {
            closephase(closePhaseBtn.getAttribute("data-phaseid"));
        }
    });

    $('#closephaseform').submit(function (e) {
        e.preventDefault();
        showModalConfirm(
            "Are you sure you want to close this phase? This action cannot be undone.", 
            "Confirm Phase Closure", 
            function() {
                callCustomAjax(
                    "/nerie/program-details/phase/close", 
                    "phaseid=" + $('#phaseid').val() + "&closingreport=" + $('#phaseclosingreport').val(), 
                    function (data) {
                        $('#phasesModalCloseReport').modal('hide');
                        if (data === '1') {
                            showModalAlert('Phase closed successfully.', 'Success');
                            $('#phaseclosingreport').val('');
                            $('#feedbackModal').one('hidden.bs.modal', function() {
                                viewphasesofprogram(programcode);
                            });
                        } else {
                            showModalAlert('Failed to close the phase. Please try again.', 'Error');
                        }
                    },
                    handleAjaxError
                );
            }
        );
    });

    $('#closeprogramform').submit(function (e) {
        e.preventDefault();
        showModalConfirm(
            "Are you sure you want to close this program and all its phases? This action cannot be undone.",
            "Confirm Program Closure",
            function () {
                callCustomAjax(
                    "/nerie/program/close", 
                    "pcode=" + $('#programcode').val() + "&closingreport=" + $('#programclosingreport').val(), 
                    function (data) {
                        $('#programModalCloseReport').modal('hide');
                        if (data === '1') {
                            showModalAlert('Program closed successfully.', 'Success');
                            $('#programclosingreport').val('');
                            $('#feedbackModal').one('hidden.bs.modal', getClosedPrograms);
                        } else {
                            showModalAlert('Failed to close the program. Please try again.', 'Error');
                        }
                    },
                    handleAjaxError
                );
            }
        );
    });

    $('#backtotop').click(function () {
        $("html, body").animate({ scrollTop: 0 }, 600);
        return false;
    });

    $('#unclosetable tbody').on('click', 'td a.showmore', function () {
        var row = $(this).closest('tr');
        var morespan = row.find('span.more');
        var l = row.find('span.less');
        if (morespan.is(':hidden')) {
            $(this).text("show less...");
            l.hide();
            morespan.show();
        } else {
            $(this).text("show more...");
            l.show();
            morespan.hide();
        }
    });
});

function myFunction(x) {
    x.classList.toggle("change");
}

function getdatatable(tname, rname) {
    return $('#' + tname).DataTable({
        dom: 'Blfrtip',
        retrieve: true,
        pageLength: 10,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excel',
                text: '<em><i class="fa fa-file-excel-o">Excel</i></em>',
                title: () => rname,
                exportOptions: {
                    columns: "thead th:not(.noExport)"
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });
}

function getdateformate(d) {
    if (!d) return '';
    var date = new Date(d);
    var day = String(date.getDate()).padStart(2, '0');
    var month = String(date.getMonth() + 1).padStart(2, '0');
    var year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

function getClosedPrograms() {
    var fy = $("#fyunclose").val();
    if (!fy) return;
    var fystart = fy.split("##")[0];
    var fyend = fy.split("##")[1];

    callCustomAjax("/nerie/program/open-course/list", "fystart=" + fystart + "&fyend=" + fyend,
        function (returndata) {
            utable.clear().destroy();
            $('#unclosetable tbody').empty();
            if (returndata && returndata.length > 0) {
                let count = 1;
                returndata.forEach(function (x) {
                    var programdescriptionr = x[3].length > 10
                        ? `<span class='less'>${x[3].substring(0, 10)}...</span><span class='more' style='display:none'>${x[3]}</span></br><a href="#" class="showmore">show more</a>`
                        : x[3];

                    var rd = `<tr>
                        <td>${count++}</td>
                        <td>${x[1]}</td>
                        <td>${x[2]}</td>
                        <td>${programdescriptionr}</td>
                        <td>${x[4]}</td>
                        <td><button class='btn btn-info btn-sm viewphasesbtn' data-pcode='${x[0]}'><i class='far fa-eye'> View Phases</i></button></td>
                        <td><button class='btn btn-danger btn-sm closeprogrambtn' data-pcode='${x[0]}'><i class='fas fa-times'></i> Close Program</button></td>
                        </tr>`;
                    $('#unclosetable tbody').append(rd);
                });
                $('#tablediv').show();
            } else {
                $('#tablediv').hide();
                showModalAlert('No open programs found for the selected financial year.', 'Information');
            }
            utable = getdatatable("unclosetable", "Unclosed Programs Exported");
        },
        handleAjaxError
    );
}

function viewphasesofprogram(pcode) {
    programcode = pcode;
    callCustomAjax("/nerie/phases/unclose/list", "pcode=" + pcode,
        function (returndata) {
            ptable.clear().destroy();
            $('#phasestable tbody').empty();
            if (returndata && returndata.length > 0) {
                let count = 1;
                returndata.forEach(function (x) {
                    var closebtn = (x[6] === 'N')
                        ? `<button class='btn btn-danger btn-sm closephasebtn' data-phaseid='${x[0]}'><i class='fas fa-times'> Close Phase</i></button>`
                        : "<span class='btn btn-info btn-sm'>CLOSED</span>";

                    var rd = `<tr>
                        <td>${count++}</td>
                        <td>${x[1]}</td>
                        <td>${x[2]}</td>
                        <td>${x[3]}</td>
                        <td>${x[4]}</td>
                        <td>${x[5]}</td>
                        <td>${closebtn}</td>
                        </tr>`;
                    $('#phasestable tbody').append(rd);
                });
                ptable = getdatatable("phasestable", "Unclosed Phases Exported");
                $('#phasesModal').modal('show');
            } else {
                showModalAlert('No unclosed phases found for this program.', 'Information');
            }
        },
        handleAjaxError
    );
}

function closephase(pid) {
    $('#phasesModal').modal('hide');
    $('#phasesModalCloseReport').modal('show');
    $('#phaseid').val(pid);
}

function closeprogram(pcode) {
    $('#programcode').val(pcode);
    $('#programModalCloseReport').modal('show');
}