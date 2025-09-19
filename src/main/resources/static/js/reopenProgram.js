var utable;

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

function myFunction(x) {
  x.classList.toggle("change");
}

document.addEventListener("DOMContentLoaded", () => {
  utable = getdatatable("closetable", "Un-Closed Programs Exported");

  $("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
  });

  $("#backtotop").click(function () {
    $("html, body").animate({ scrollTop: 0 }, 600);
    return false;
  });

  $(".sub-menu ul, .sub-sub-menu ul").hide();
  $(".sub-menu a").click(function () {
    $(this).parent(".sub-menu").children("ul").slideToggle("100");
    $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
  });
  $(".sub-sub-menu a").click(function () {
    $(this).parent(".sub-sub-menu").children("ul").slideToggle("100");
    $(this).find(".right").toggleClass("fa-caret-up fa-caret-down");
  });

  $("#fyclose").change(getUnClosedPrograms);

  document.addEventListener("click", function (event) {
    let target = event.target.closest(".unclosephasebtn");
    if (target) {
      event.preventDefault();
      let functionName = target.getAttribute("data-function");
      let param1 = target.getAttribute("data-param1");

      if (typeof window[functionName] === "function") {
        window[functionName](param1);
      } else {
        console.error(`Function ${functionName} is not defined`);
      }
    }
  });
});

function getUnClosedPrograms() {
  var fy = $("#fyclose").val();
  if(!fy) return;
  var fystart = fy.split("##")[0];
  var fyend = fy.split("##")[1];

  callCustomAjax(
    "/nerie/program/close-course/list",
    "fystart=" + fystart + "&fyend=" + fyend,
    function (returndata) {
      utable.clear().destroy();
      $('#closetable tbody').empty();
      if (returndata && returndata.length > 0) {
        let count = 1;
        returndata.forEach(function (x) {
          var rd =
            `<tr>
              <td>${count++}</td>
              <td>${x[2]}</td>
              <td>${x[4]}</td>
              <td>${x[3]}</td>
              <td>${x[5]}</td>
              <td>${x[6]}</td>
              <td><button class='btn btn-danger btn-sm unclosephasebtn'
                data-function='uncloseprogram'
                data-param1='${x[0]}'>
                <i class='fas fa-undo'></i> Reopen Phase</button></td>
            </tr>`;
          $("#closetable tbody").append(rd);
        });
        $("#tablediv").show();
      } else {
        $("#tablediv").hide();
        showModalAlert("No closed programs found for the selected financial year.", "Information");
      }
      utable = getdatatable("closetable", "Un-Closed Programs Exported");
    },
    handleAjaxError
  );
}

function uncloseprogram(phid) {
    showModalConfirm(
        "Are you sure you want to reopen this program phase? This action cannot be undone.",
        "Confirm Phase Reopening",
        function() {
            callCustomAjax("/nerie/program/reopen-phase", "phid=" + phid, function (data) {
                if (data === "1") {
                    showModalAlert("Program phase reopened successfully.", "Success");
                    $('#feedbackModal').one('hidden.bs.modal', getUnClosedPrograms);
                } else {
                    showModalAlert("Failed to reopen the phase. Please try again.", "Error");
                }
            }, handleAjaxError);
        }
    );
}

function getdatatable(tname, rname) {
  return $("#" + tname).DataTable({
    dom: "Blfrtip",
    retrieve: true,
    pageLength: 10,
    lengthMenu: [
      [5, 10, 20, 50, -1],
      [5, 10, 20, 50, "All"],
    ],
    buttons: [
      {
        extend: "excel",
        text: '<em><i class="fa fa-file-excel-o">Excel</i></em>',
        title: () => rname,
        exportOptions: {
          columns: "thead th:not(.noExport)",
        },
        className: 'btn btn-sm btn-outline-success'
      },
    ],
  });
}

function getdateformate(d) {
    if(!d) return '';
    var date = new Date(d);
    var day = String(date.getDate()).padStart(2, '0');
    var month = String(date.getMonth() + 1).padStart(2, '0');
    var year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

function unclosecoursefunc(ccode) {
    showModalConfirm(
        "Are you sure you want to reopen this program?",
        "Confirm Program Reopening",
        function() {
            $.ajax({
                type: "POST",
                url: "/nerie/reports/course/reopen",
                data: "coursecode=" + ccode,
                success: function (data) {
                    if (data == "1") {
                        showModalAlert("Program has been reopened successfully.", "Success");
                        if (typeof getfycloseprogramfunc === 'function') {
                            $('#feedbackModal').one('hidden.bs.modal', getfycloseprogramfunc);
                        }
                    } else {
                        showModalAlert("An error occurred. Please try again.", "Error");
                    }
                },
                error: handleAjaxError
            });
        }
    );
}