var atableList = getdatatable("approveTableList", "Approved List Exported");
var rtableList = getdatatable("rejectTableList", "Rejected List Exported");

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
                title: function () {
                    return rname;
                },
                exportOptions: {
                    columns: "thead th:not(.noExport)",
                },
            },
        ],
    });
}

function getApproveProgramList() {
    var fy = $("#approvefinancialyear").val();

    if (fy) {
        $("#approvelistdiv").show();
        var fystart = fy.split("##")[0];
        var fyend = fy.split("##")[1];
        callCustomAjax(
            "/nerie/program/approved-program/list",
            "fystart=" + fystart + "&fyend=" + fyend,
            function (data) {
                atableList.clear();
                atableList.destroy();
                $("#atttablebody").empty();

                if (data.length !== 0) {
                    var count = 1;
                    data.forEach(function (item) {
                        var programname = "";
                        if (item[0].length > 20) {
                            programname =
                                "<span class='less'>" +
                                item[0].substring(0, 20) +
                                "...</span>" +
                                "<span class='more' style='display:none'>" +
                                item[0] +
                                "</span>" +
                                '</br><a href="#more" class="showmore">show more</a></br>';
                        } else {
                            programname = item[0];
                        }
                        var rowString =
                            "<tr>" +
                            "<td>" + count++ + "</td>" +
                            "<td>" + item[3] + "</td>" +
                            "<td>" + programname + "(" + getdateformate(item[4]) + " to " + getdateformate(item[5]) + ")</td>" +
                            "<td>" + item[2] + "</td>" +
                            "<td>" + item[1] + "</td>" +
                            "<td>" + item[8] + "</td>" +
                            "<td>" + item[9] + "</td>" +
                            "<td>" + item[6] + "</td>" +
                            "<td>" + item[10] + "</td>" +
                            "<td>" + item[11] + "</td>";
                        if ($("#rolecode").val() === "U") {
                            rowString =
                                rowString +
                                "<td><a href='/nerie/participant/feedback/overall-feedback/list?aid=" + item[7] + "' target='_blank' >View Overall Feedback</a> <a href='/nerie/participant/feedback/daily-feedback/list?phaseid=" + item[7] + "' target='_blank' >View Day Feedback</a></td>" +
                                '<td>Finalized/Open<br><a href="/nerie/program-details/view-approval?pdid=' + item[19] + '" target="_blank">View Letter</a><br>approved on:<br>' + getdateformate(item[15]) + "</td>" +
                                "</tr>";
                        } else {
                            rowString =
                                rowString +
                                '<td>Finalized/Open<br><a href="/nerie/program-details/view-approval?pdid=' + item[19] + '" target="_blank">View Letter</a><br>approved on:<br>' + getdateformate(item[15]) + "</td>" +
                                "</tr>";
                        }
                        $("#atttablebody").append(rowString);
                    });
                } else {
                    alert("No records found");
                }
                atableList = getdatatable("approveTableList", "Approved List");
            }
        );
    } else {
        $("#approvelistdiv").hide();
    }
}

function getRejectProgramList() {
    var fy = $("#rejectfinancialyear").val();
    if (fy) {
        $("#rejectlistdiv").show();
        var fystart = fy.split("##")[0];
        var fyend = fy.split("##")[1];
        callCustomAjax(
            "/nerie/program/rejected-program/list",
            "fystart=" + fystart + "&fyend=" + fyend,
            function (data) {
                rtableList.clear();
                rtableList.destroy();
                $("#rtttablebody").empty();

                if (data.length !== 0) {
                    var count = 1;
                    data.forEach(function (item) {
                        var programdescriptionr = "";
                        if (item[1].length > 10) {
                            programdescriptionr =
                                "<span class='less'>" +
                                item[1].substring(0, 10) +
                                "...</span>" +
                                "<span class='more' style='display:none'>" +
                                item[1] +
                                "</span>" +
                                '</br><a href="#more" class="showmore">show more</a>';
                        } else {
                            programdescriptionr = item[1];
                        }

                        var rowString =
                            "<tr>" +
                            "<td>" + count++ + "</td>" +
                            "<td>" + item[3] + "</td>" +
                            "<td>" + item[0] + "(" + getdateformate(item[4]) + " to " + getdateformate(item[5]) + ")</td>" +
                            "<td>" + item[2] + "</td>" +
                            "<td>" + programdescriptionr + "</td>" +
                            "<td>" + item[8] + "</td>" +
                            "<td>" + item[9] + "</td>" +
                            "<td>" + item[6] + "</td>" +
                            "<td>" + item[10] + "</td>" +
                            "<td>" + item[11] + "</td>" +
                            '<td>Program Rejection Details<br><a href="/nerie/program-details/view-rejection?pdid=' + item[19] + '" target="_blank">View Letter</a><br>rejected on:<br>' + getdateformate(item[22]) + "</td>" +
                            "<td>" + item[20] + "</td>" +
                            "</tr>";
                        $("#rtttablebody").append(rowString);
                    });
                } else {
                    alert("No records found");
                }
                rtableList = getdatatable("rejectTableList", "Rejected List Exported");
            }
        );
    } else {
        $("#rejectlistdiv").hide();
    }
}

function showModalAlert(message, title = "Message") {
    $("#feedbackModalLabel").text(title);
    $("#feedbackModalBody").html(message);
    $("#feedbackModal .modal-footer").html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $("#feedbackModal").modal("show");
}

function showModalAndReload(message, title = "Success") {
    showModalAlert(message, title);
    $("#feedbackModal").one("hidden.bs.modal", function () {
        window.location.reload();
    });
}

function handleAjaxError(jqXHR, textStatus, errorThrown) {
    let errorMessage = `An error occurred: ${textStatus} - ${errorThrown}`;
    if (jqXHR.responseText) {
        errorMessage += `<br><br>Details: ${jqXHR.responseText}`;
    }
    showModalAlert(errorMessage, "Request Failed");
}

function getdateformate(d) {
    if (!d) return "";
    var dateObj = new Date(d);
    var day = dateObj.getDate();
    var month = dateObj.getMonth() + 1;
    var year = dateObj.getFullYear();
    if (day < 10) day = "0" + day;
    if (month < 10) month = "0" + month;
    return day + "/" + month + "/" + year;
}

$(document).ready(function () {
    $(".datepicker").datepicker({
        dateFormat: "dd-mm-yy",
        orientation: "bottom",
    });

    $("#usertable").DataTable({
        dom: "Blfrtip",
        pageLength: 5,
        lengthMenu: [
            [5, 10, 20, 50, -1],
            [5, 10, 20, 50, "All"],
        ],
        buttons: [
            {
                extend: "excelHtml5",
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: "Programs",
                exportOptions: {
                    columns: ":visible:not(.noExport)",
                },
                className: "btn btn-sm btn-outline-success",
            },
        ],
    });

    var demo1 = $("[name=coordinators]").bootstrapDualListbox({
        infoText: "All {0} Coordinators",
        infoTextEmpty: "Selected Coordinators",
        filterPlaceHolder: "Search Coordinator",
    });

    var demo2 = $("[name=newcoordinators]").bootstrapDualListbox({
        infoText: "All {0} Coordinators",
        infoTextEmpty: "Selected Coordinators",
        filterPlaceHolder: "Search Coordinator",
    });

    var demo3 = $("[name=ecoordinators]").bootstrapDualListbox({
        infoText: "All {0} Coordinators",
        infoTextEmpty: "Selected Coordinators",
        filterPlaceHolder: "Search Coordinator",
        eventMoveOverride: true,
    });

    $("#focusTEST, #stageTEST, #targetTEST").select2({
        placeholder: function () {
            return $(this).data("placeholder");
        },
        allowClear: true,
        closeOnSelect: true,
    });

    $("#ongoingprograms").select2();

    function editfunc2(pname, pdesc, pid, sdate, edate, ldate, phid, phdesc, cccode, cdate, pcode, vcodes, ucodes) {
        $("#editprogramform")[0].reset();
        $("#ephasedescdiv").hide();
        $("#ephasedescription").prop("required", false);

        $("#eprogramname").val(pname);
        $("#ephaseid").val(phid);
        $("#eprogramdescription").val(pdesc);
        $("#eprogramid").val(pid);
        $("#estartdate").val(sdate);
        $("#eenddate").val(edate);
        $("#elastdate").val(ldate);
        $("#ecourseclosedate").val(cdate);
        $("#eprogramcode").val(pcode);
        $("#ecoursecategorycode").val(cccode);

        if (phdesc) {
            $("#ephasedescdiv").show();
            $("#ephasedescdiv").removeClass("display-none");
            $("#ephasedescription").val(phdesc);
            $("#ephasedescription").prop("required", true);
        }

        $('input[name="evenues"]').prop("checked", false);
        if (vcodes) {
            var venuearray = vcodes.toString().split(",");
            venuearray.forEach(function (v) {
                $("#ev_" + v).prop("checked", true);
            });
        }

        var cuserarray = ucodes ? ucodes.toString().split(",") : [];
        $("#coors3").val(cuserarray);
        demo3.bootstrapDualListbox("refresh", true);
        $("#programEditModal").modal("show");
    }

    $("#usertable").on("click", ".editfunc2-btn", function (event) {
        event.preventDefault();
        const button = $(this);
        editfunc2(
            button.data("param1"), button.data("param2"), button.data("param3"), button.data("param4"),
            button.data("param5"), button.data("param6"), button.data("param7"), button.data("param8"),
            button.data("param9"), button.data("param10"), button.data("param11"), button.data("param12"),
            button.data("param13")
        );
    });

    $("#editprogramform").on("submit", function (e) {
        e.preventDefault();
        var s_val = $("#estartdate").val(), e_val = $("#eenddate").val(), l_val = $("#elastdate").val(), c_val = $("#ecourseclosedate").val();
        var s = new Date(s_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3")), e_date = new Date(e_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3")), l = new Date(l_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3")), c = new Date(c_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3"));

        if (s > e_date) { showModalAlert("Startdate cannot be after Enddate!"); $("#estartdate").focus(); return false; }
        if (l > e_date) { showModalAlert("Lastdate cannot be after Enddate!"); $("#elastdate").focus(); return false; }
        if (e_date > c) { showModalAlert("Program closedate cannot be before Enddate!"); $("#ecourseclosedate").focus(); return false; }

        $.ajax({
            type: "POST",
            url: "/nerie/program-details/update",
            data: $("#editprogramform").serialize(),
            success: function (data) {
                if (data === "1") {
                    $("#programEditModal").modal("hide");
                    showModalAndReload("Successfully Updated!");
                } else {
                    showModalAlert("Failed to update! " + (data || ""));
                }
            },
            error: handleAjaxError,
        });
    });

    $("#phasedis").hide();
    $("#mcoursesfid").submit(function (e) {
        e.preventDefault();
        var s_val = $("#startdate").val(), e_val = $("#enddate").val(), l_val = $("#lastdate").val(), c_val = $("#courseclosedate").val();
        var s = new Date(s_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3")), e_date = new Date(e_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3")), l = new Date(l_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3")), c = new Date(c_val.replace(/(\d+)-(\d+)-(\d+)/, "$2/$1/$3"));

        if (s > e_date) { showModalAlert("Startdate cannot be after Enddate!"); $("#startdate").focus(); return false; }
        if (l > e_date) { showModalAlert("Lastdate cannot be after Enddate!"); $("#lastdate").focus(); return false; }
        if (e_date > c) { showModalAlert("Program closedate cannot be before Enddate!"); $("#courseclosedate").focus(); return false; }

        $.ajax({
            type: "POST",
            url: "/nerie/program/inst/save",
            data: $("#mcoursesfid").serialize(),
            success: function (data) {
                if (data === "2") { showModalAndReload("Successfully Saved!!!"); }
                else if (data === "1") { showModalAlert("Program Name already Exist"); }
                else if (data === "3") { showModalAlert("Program Name cannot be Empty"); }
                else { showModalAlert("Save Failed!!!"); }
            },
            error: handleAjaxError,
        });
    });

    $("#phases").change(function () {
        if ($("#phases").val() === "Yes") {
            $("#phasedis").show();
            $("#phasedescription").prop("required", true);
        } else {
            $("#phasedis").hide();
            $("#phasedescription").prop("required", false);
        }
    });

    let formCount = 1;
    function initializeBatchFormCoordinator(selectId, displayId) {
        $(selectId).select2({ placeholder: "Select Coordinators", allowClear: true, closeOnSelect: true });

        // This array is now only for the UI display logic (Head Coordinator/Co-coordinators)
        // The final submission will read directly from the form element.
        const selectedCoordinatorNames = [];
        let selectedCoordinatorIds = $(selectId).val() || []; // Pre-populate if values already exist

        // Function to update the display text
        function updateDisplay() {
            // Re-fetch names based on current IDs to ensure order
            selectedCoordinatorNames.length = 0;
            selectedCoordinatorIds.forEach(id => {
                 let name = $(selectId).find(`option[value="${id}"]`).text();
                 selectedCoordinatorNames.push(name);
            });

            $(displayId).html( selectedCoordinatorNames.length === 0 ? "Select Head Coordinator" : "Head Coordinator: " + selectedCoordinatorNames[0] + "<br><p class='text-danger'>Co-coordinators: " + selectedCoordinatorNames.slice(1).join(", ") + "</p>");
        }

        $(selectId).on("select2:select select2:unselect", function () {
             // Get the current list of selected IDs from the element
            selectedCoordinatorIds = $(this).val() || [];
            updateDisplay();
        });

        // Initial update
        updateDisplay();
    }

    function initializePhaseToggle(form) {
        const phasesSelect = form.find(".phases-select"), phasedisContainer = form.find(".phasedis-container"), phasedescriptionInput = form.find(".phasedescription-input");
        phasesSelect.change(function () {
            if ($(this).val() === "Yes") {
                phasedisContainer.show();
                phasedescriptionInput.prop("required", true);
            } else {
                phasedisContainer.hide();
                phasedescriptionInput.prop("required", false);
            }
        });
    }

    initializeBatchFormCoordinator("#coorsTEST1", "#selectionOrderDisplay1");
    initializePhaseToggle($("#formsContainer .form-container:first"));

    $("#addFormButton").click(function () {
        formCount++;
        const originalFormContainer = $("#formsContainer .form-container:first");

        originalFormContainer.find(".coordinatorSelect").select2("destroy");

        const newFormContainer = originalFormContainer.clone();

        initializeBatchFormCoordinator("#coorsTEST1", "#selectionOrderDisplay1");

        newFormContainer.find("form").attr("id", "programdetADDFORM" + formCount);
        newFormContainer.find("input, textarea, select").not('.coordinatorSelect').val(""); // Clear all fields except the coordinator select
        newFormContainer.find(".coordinatorSelect").val(null); // Specifically clear the select2
        newFormContainer.find("span.charactercount-phase").text("");
        newFormContainer.find(".phasedis-container").hide();

        const newSelectId = "coorsTEST" + formCount, newDisplayId = "selectionOrderDisplay" + formCount;
        newFormContainer.find(".coordinatorSelect").attr("id", newSelectId);
        newFormContainer.find('p[id^="selectionOrderDisplay"]').attr("id", newDisplayId).html("Select Head Coordinator");

        newFormContainer.appendTo("#formsContainer");

        initializePhaseToggle(newFormContainer);
        initializeBatchFormCoordinator("#" + newSelectId, "#" + newDisplayId); // Initialize the new form's coordinator select

        newFormContainer.find('button[type="reset"]').click(function () {
            const form = $(this).closest("form");
            form.find("input, textarea, select").val("");
            form.find("span.charactercount-phase").text("");
            $("#" + newDisplayId).html("Select Head Coordinator");
            form.find(".coordinatorSelect").val(null).trigger("change");
        });
    });

    $("#submitAllForms").click(function () {
        var forms = $("#formsContainer .form-container form"), allFormsValid = true;
        forms.each(function (index, form) {
            var $form = $(form);
            if ($form.data("submitted")) return;


            var selectedCoordinators = $form.find(".coordinatorSelect").val();

            if (!selectedCoordinators || selectedCoordinators.length === 0) {
                allFormsValid = false;
                showModalAlert("Form " + (index + 1) + ": Please select at least one Program Coordinator.");
                return false; // Exit the .each() loop
            }

            $.ajax({
                type: "POST",
                url: "/nerie/program/batch/save",
                // .val() returns an array, so we join it to create the comma-separated string for the backend.
                data: $form.serialize() + "&coordinators=" + selectedCoordinators.join(","),
                async: false,
                success: function (data) {
                    if (data !== "2") {
                        allFormsValid = false;
                        showModalAlert("Form " + (index + 1) + " failed to save. " + (data === "1" ? "Program Name already exists." : "An error occurred."));
                    } else {
                        $form.data("submitted", true);
                    }
                },
                error: (jqXHR, textStatus, errorThrown) => {
                    allFormsValid = false;
                    handleAjaxError(jqXHR, textStatus, errorThrown);
                },
            });

            if (!allFormsValid) return false; // Exit the .each() loop if an AJAX call fails
        });

        if (allFormsValid) {
            showModalAndReload("All valid forms submitted successfully!");
        }
    });

    $("#financialyear-details").change(function () {
        const fy = $(this).val(), $programsDropdown = $("#programs"), $phaseDropdown = $("#phaseno");
        $programsDropdown.empty().append($("<option></option>").attr("value", "").text("Select"));
        $phaseDropdown.empty().append($("<option></option>").attr("value", "").text("Select"));
        $("#newFormContainer").hide();
        if (fy) {
            const fystart = fy.split("##")[0], fyend = fy.split("##")[1];
            $.ajax({
                type: "POST",
                url: "/nerie/program/financial-year/accepted-list",
                data: { fystart: fystart, fyend: fyend },
                success: function (data) {
                    if (data && data.length > 0) {
                        data.forEach((program) => $programsDropdown.append($("<option></option>").attr("value", program[0]).text(program[1])));
                    } else {
                        $programsDropdown.empty().append($("<option></option>").attr("value", "").text("No programs found"));
                    }
                },
                error: handleAjaxError,
            });
        }
    });

    $("#programs").change(function () {
        const programCode = $(this).val(), $phaseDropdown = $("#phaseno");
        $phaseDropdown.empty().append($("<option></option>").attr("value", "").text("Select"));
        $("#newFormContainer").hide();
        if (programCode) {
            $.ajax({
                type: "POST",
                url: "/nerie/phases/list",
                data: { programcode: programCode },
                success: function (data) {
                    if (data && data.length > 0) {
                        data.forEach((phase) => $phaseDropdown.append($("<option></option>").attr("value", phase[0]).text(phase[1])));
                    } else {
                        $phaseDropdown.empty().append($("<option></option>").attr("value", "").text("No phases found"));
                    }
                },
                error: handleAjaxError,
            });
        }
    });

    $("#phaseno").change(function() {
        const phaseId = $(this).val();
        const programCode = $('#programs').val();

        if (phaseId) {
            $('#DetailsForm')[0].reset();
            $("#programDetailProgcode").val(programCode);
            $("#programDetailPhaseID").val(phaseId);

            $.ajax({
                type: "POST",
                url: "/nerie/program/to-populate",
                data: { programcode: programCode },
                success: function(data) {
                    if (data && data.length > 0) {
                        $("#programdescriptiondisplayDET").val(data[0][0]);
                        $("#programiddisplayDET").val(data[0][1]);
                    }

                    $.ajax({
                        type: "POST",
                        url: "/nerie/program-members/get-program-members", // Adjust URL if necessary
                        data: { programcode: programCode, phaseid: phaseId },
                        success: function(memberData) {
                            const $programMembers = $('#programMembers');
                            $programMembers.empty().append('<option value="">-- Select Member --</option>');

                            if (memberData && memberData.length > 0) {
                                memberData.forEach(function(member) {
                                    $programMembers.append('<option value="' + member.pmid + '">' + member.username + '</option>');
                                });
                            } else {
                                showModalAlert('No Member Details Available');
                            }
                        },
                        error: handleAjaxError
                    });
                },
                error: handleAjaxError
            });
            $('#newFormContainer').show();
        } else {
            $('#newFormContainer').hide();
        }
    });

    $("#DetailsForm").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: "/nerie/program-details/save",
            data: $(this).serialize(),
            success: function (data) {
                if (data === "1" || data === "2") {
                    showModalAndReload("Program details saved successfully!");
                } else {
                    showModalAlert("Failed to save program details: " + (data || ""));
                }
            },
            error: handleAjaxError,
        });
    });

    $("#activityfinancialyear").change(function () {
        var fy = $(this).val();
        $("#activityprograms, #activityphaseno").empty().append($("<option></option>").val("").text("Select"));
        $("#activityContainer").hide();
        if (fy) {
            var fystart = fy.split("##")[0], fyend = fy.split("##")[1];
            $.ajax({ url: "/nerie/program/financial-year/list", type: "POST", data: { fystart: fystart, fyend: fyend }, success: function (data) { if (data) data.forEach((x) => $("#activityprograms").append($("<option></option>").val(x[0]).text(x[1]))); }, error: handleAjaxError });
        }
    });

    $("#activityprograms").change(function () {
        var programcode = $(this).val();
        $("#activityphaseno").empty().append($("<option></option>").val("").text("Select"));
        $("#activityContainer").hide();
        if (programcode) {
            $.ajax({ url: "/nerie/phases/list", type: "POST", data: { programcode: programcode }, success: function (data) { if (data) data.forEach((x) => $("#activityphaseno").append($("<option></option>").val(x[0]).text(x[1]))); }, error: handleAjaxError });
        }
    });

    $("#activityphaseno").change(function () {
        var phaseId = $(this).val(), programcode = $("#activityprograms").val();
        if (phaseId) {
            $("#activityForm")[0].reset();
            $("#activityContainer").show();
            $("#activityProgcode").val(programcode);
            $("#activityPhaseID").val(phaseId);
            $.ajax({
                url: "/nerie/program/to-populate", type: "POST", data: { programcode: programcode },
                success: function (data) {
                    if (data.length !== 0) {
                        $("#activityprogramdescriptiondisplayDET").val(data[0][0]);
                        $("#activityprogramiddisplayDET").val(data[0][1]);
                    }
                },
                error: handleAjaxError,
            });
        } else {
            $("#activityContainer").hide();
        }
    });

    $("#activityForm").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: "POST", url: "/nerie/activities/save", data: $("#activityForm").serialize(),
            success: function (data) {
                if (data === "2") { showModalAndReload("Activity saved successfully!"); }
                else { showModalAlert("Failed to save activity!"); }
            },
            error: handleAjaxError,
        });
    });

    const userRole = $("#userrole").val();
    let requestData = userRole !== "A" ? { usercode: $("#usercode").val() } : {};
    $.ajax({
        type: "POST", url: "/nerie/program/ongoing/list", data: requestData,
        success: function (data) {
            $("#ongoingprograms").empty().append($("<option></option>").val("").text("Select"));
            if (data) { data.forEach(function (item) { $("#ongoingprograms").append($("<option></option>").val(item[0]).text(item[1]).attr("title", item[1])); }); }
        },
        error: handleAjaxError,
    });

    $("#ongoingprograms").on("change", function () {
        var programcode = $(this).val();
        if (programcode) {
            $.ajax({
                url: "/nerie/program/details", type: "POST", data: { programcode: programcode },
                success: function (data) {
                    if (data.length !== 0) {
                        $("#programdescriptiondisplay").val(data[0][0]);
                        $("#programiddisplay").val(data[0][1]);
                        $("#categorytypedisplay").val(data[0][2]);
                        $("#nextphase").val(data[0][3]);
                        $("#addphasediv").show();
                    } else {
                        showModalAlert("Kindly close the previous phase and submit phase report before creating new phase");
                        $("#addphasediv").hide();
                    }
                },
                error: handleAjaxError,
            });
        } else {
            $("#addphasediv").hide();
        }
    });

    $("#ongoingform").on("submit", function (e) {
        e.preventDefault();
        $.ajax({
            url: "/nerie/program/phases/save",
            type: "POST",
            data: $("#ongoingform").serialize() + "&ongoingprograms=" + $('#ongoingprograms').val(),
            success: function (data) {
                if (data === "1") {
                    showModalAndReload("New phase added successfully!");
                } else {
                    showModalAlert("Failed to add new phase. Please try again!");
                }
            },
            error: handleAjaxError,
        });
    });

    $("#usertable, #approveTableList, #rejectTableList").on("click", "tbody td a.showmore", function (e) {
        e.preventDefault();
        var row = $(this).closest("tr"), morespan = row.find("span.more"), lessspan = row.find("span.less");
        if (morespan.is(":hidden")) {
            $(this).text("show less");
            lessspan.hide();
            morespan.show();
        } else {
            $(this).text("show more");
            lessspan.show();
            morespan.hide();
        }
    });

    $("#programdescription, #phasedescription, #newphasedescription").keyup(function () {
        var maxLength = parseInt($(this).attr("maxlength")), currentLength = $(this).val().length, charLeft = maxLength - currentLength;
        var counterSpan = $(this).parent().next('span[id^="charactercount"]');
        if (counterSpan.length) { counterSpan.text("Characters left: " + charLeft); }
    });

    $("#programid").focusout(function () {
        var programid = $(this).val().trim().toUpperCase();
        if (programid && !["NON PAC", "NON PAC PROGRAM", "NON PAC PROGRAMME"].includes(programid)) {
            $.ajax({
                type: "POST", url: "/nerie/program/exists-by-programid", data: { programid: programid },
                success: function (data) {
                    if (data === "1") {
                        showModalAlert("Program ID already exists. To add another part to this program, please use the 'Add New Phase' tab.");
                        $("#programid").val("").focus();
                    }
                },
                error: handleAjaxError,
            });
        }
    });
});