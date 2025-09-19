$(document).ready(function () {
    $(".select2").select2();

    $("#tstudentsid").submit(function (e) {
        e.preventDefault();
        $.confirm({
            content: function () {
                var self = this;
                return $.ajax({
                    type: "POST",
                    url: "/nerie/students/save",
                    async: false,
                    data: $("#tstudentsid").serialize(),
                })
                    .done(function (data) {
                        if (data.status === "Success") {
                            if (data.flg === 1) {
                                self.setContent("User ID: " + data.userid);
                                self.setContentAppend(
                                    "<br>Password: " + data.password
                                );
                            }
                            self.setTitle(data.msg);
                        } else {
                            self.setTitle(data.msg);
                        }
                    })
                    .fail(function () {
                        self.setContent("Something went wrong.");
                    });
            },
            onDestroy: function () {
                window.location.reload();
            },
        });
    });

    $("#departmentcode, #isshortterm_toggle").change(function () {
        if ($("#departmentcode").val() !== "-1") {
            getdepartments($("#departmentcode").val(), $("#isshortterm").val());
        } else {
            $("#coursecode").empty();
            $("#coursecode").append(
                $(
                    '<option data-duration="0" value="-1">--Select Course--</option>'
                )
            );
            $("#coursecode").val("-1").trigger("change");
        }
    });

    $("#coursecode").change(function () {
        if ($("#departmentcode").val() !== "-1") {
            if ($("#isshortterm").val() === "0") {
                getsubjects(
                    $("#departmentcode").val(),
                    $("#isshortterm").val(),
                    $("#semestercode").val(),
                    $("#coursecode").val()
                );
                getacademicyear();
            } else {
                getsubjects(
                    $("#departmentcode").val(),
                    $("#isshortterm").val(),
                    $("#sphaseid").val(),
                    $("#coursecode").val()
                );
                getacademicyear();
            }
        } else {
            alert("Something went wrong");
        }
    });

    $("#email").on("change", function () {
        var re =
            /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
        if ($("#email").val().replace(/\s/g, "").length !== 0) {
            if (re.test($("#email").val()) === false) {
                alert("Please enter valid Email ID");
                $("#email").val("");
                $("#email").focus();
            } else
                checkuserexistfuncbyemail($("#email").val())
        }
    });

    $("input.iemail").keyup(function () {
        if (this.value.match(/[^a-zA-Z0-9\-_@.]/)) {
            this.value = this.value.replace(/[^a-zA-Z\-_@.]*$/, "");
        }
    });

    $("input.name").keyup(function () {
        if (this.value.match(/[^a-zA-Z \s]/)) {
            this.value = this.value.replace(/[^a-zA-Z \s]*$/, "");
        }
    });

    $("input.number").keyup(function () {
        if (this.value.match(/[^0-9 ]/g)) {
            this.value = this.value.replace(/[^0-9 ]/g, "");
        }
    });

    $(".mobile").focusout(function () {
        var tmp = $(".mobile").val();
        if (tmp.length === 10) {
            $("#msgMobile").html("");
            return true;
        } else {
            $("#msgMobile").html("Mobile No. should be 10 digits");
            return false;
        }
    });
});

var studentid = $("#inputStudentid").val();

/**
 * Shows a generic message in the feedback modal.
 * @param {string} message - The HTML content for the modal body.
 * @param {string} [title='Message'] - The title for the modal header.
 */
function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal .modal-footer').html(
        '<button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>'
    );
    $('#feedbackModal').modal('show');
}

/**
 * Shows a confirmation dialog using the feedback modal.
 * @param {string} message - The confirmation question/message.
 * @param {function} confirmCallback - The function to execute when the user confirms.
 * @param {string} [title='Confirm Action'] - The modal title.
 * @param {string} [confirmButtonText='Confirm'] - The text for the confirm button.
 */
function showConfirmationModal(message, confirmCallback, title = 'Confirm Action', confirmButtonText = 'Confirm') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);

    const confirmBtn = $(`<button type="button" class="btn btn-primary">${confirmButtonText}</button>`);
    const cancelBtn = $('<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>');

    const footer = $('#feedbackModal .modal-footer');
    footer.empty().append(cancelBtn).append(confirmBtn);

    confirmBtn.off('click').on('click', function() {
        $('#feedbackModal').modal('hide');
        setTimeout(confirmCallback, 200);
    });

    $('#feedbackModal').modal('show');
}


function loadStudent(studentid) {
    $.ajax({
        type: "GET",
        url: `/nerie/students/student/studentid=${studentid}`,
        // data: "studentid=" + studentid,
        success: function (data) {
            if (data) {
                var depcode = "";
                var isstc = "";
                var spcode = "";
                depcode = data.departmentcode.departmentcode;
                isstc = data.isshortterm;
                if (isstc === "0") {
                    spcode = data.semestercode.semestercode;
                } else {
                    spcode = data.sphaseid.sphaseid;
                }
                $.ajax({
                    type: "GET",
                    url: "/nerie/students/student/promotion",
                    data:
                        "depcode=" +
                        depcode +
                        "&spcode=" +
                        spcode +
                        "&isstc=" +
                        isstc,
                    success: function (promo) {
                        if (promo) {
                            $("#inputYearid").val(data.academicyear);
                            $("#inputStudentid").val(data.studentid);
                            $("#inputFname").val(data.fname);
                            $("#inputMname").val(data.mname);
                            $("#inputLname").val(data.lname);
                            $("#inputEmail").val(data.email);
                            $("#inputMobile").val(data.mobileno);
                            $("#inputDepartment").val(
                                data.departmentcode.departmentname
                            );
                            $("#inputCourse").val(data.coursecode.coursename);
                            if (data.isshortterm === "0") {
                                $("#inputSPLabel").html("Current Semester:");
                                $("#inputSP").val(
                                    data.semestercode.semestername
                                );
                            } else {
                                $("#inputSPLabel").html("Current Phase:");
                                $("#inputSP").val(data.sphaseid.sphasename);
                            }
                            $("#studentdetailsModal").modal("show");
                        } else {
                            showModalAlert("Error Occurred! Please try again.", "Error");
                        }
                    },
                    error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
                });
                $("#studentdetailsModal").modal("show");
            } else {
                showModalAlert("Error Occurred! Please try again.", "Error");
            }
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
}

function getdepartments(departmentcode, isshortterm) {
    console.log('getdepartments')

    callCustomAjaxasync(
        "/nerie/course-academics/coursesbasedondepartment",
        "departmentcode=" + departmentcode + "&isshortterm=" + isshortterm,
        function (data) {
            $("#coursecode").empty();
            $("#coursecode").append(
                $(
                    '<option data-duration="0" value="-1">--Select Course--</option>'
                )
            );

            data.forEach(function (item) {
                console.log(item)

                $("#coursecode").append(
                    $(
                        '<option data-duration="' +
                            item.duration +
                            '" value="' +
                            item.coursecode +
                            '">' +
                            item.coursename +
                            "</option>"
                    )
                );
            });
        }
    );
}

// TODO @Abanggi: Refactor multi-dimensional response array
function getsubjects(departmentcode, isshortterm, spcode, coursecode) {
    $.ajax({
        type: "POST",
        async: false,
        url: "/nerie/subjects/student-subjects",
        data:
            "departmentcode=" +
            departmentcode +
            "&isshortterm=" +
            isshortterm +
            "&spcode=" +
            spcode +
            "&coursecode=" +
            coursecode,
        success: function (data) {
            $("#compulsorylist").html("");
            var com = "<ul>";
            var opt = "";
            $(".multiselectDropdown").selectpicker("destroy");
for (var i = 0; i < data[0].length; i++) {
                if (data[0][i].isopt === "1") {
                    opt +=
                        ' <option value="' +
                        data[0][i].subjectcode +
                        '">' +
                        data[0][i].subjectname +
                        "</option>";
                } else {
                    com += "<li>" + data[0][i].subjectname + "</li>";
                }
            }
            com += "</ul>";
            $("#compulsorylist").html(com);
            $("#optionalsubjects").html(opt);
            $(".multiselectDropdown").selectpicker();
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
}

function getacademicyear() {
    var element = document.getElementById("coursecode");
    var duration =
        element.options[element.selectedIndex].getAttribute("data-duration");
    $.confirm({
        content: function () {
            var self = this;
            return $.ajax({
                type: "POST",
                url: "/nerie/course-academics/generateacademicyearbyduration",
                data: "duration=" + duration,
            })
                .done(function (data) {
                    var ay = data;
                    $("#academicyear").empty();
                    var options =
                        '<option value="-1" selected="true">Select Academic Year</option>';
                    for (var i = 0; i < ay.length; i++) {
                        options +=
                            '<option value="' +
                            ay[i] +
                            '">' +
                            ay[i] +
                            "</option>";
                    }
                    $("#academicyear").append(options);
                    self.close();
                })
                .fail(function () {
                    self.setContent("Something went wrong.");
                    self.setTitle("");
                });
        },
    });
}

function toggleViz() {
    if ($("#isshortterm_toggle").is(":checked")) {
        $("#isshortterm_label").html("Yes");
        $("#isshortterm").val("1");
        $("#semesterdiv").css("display", "none");
        $("#phasediv").css("display", "");
        $("#semestercode").attr("required", false);
        $("#sphaseid").attr("required", true);
        $("#semestercode").val("").trigger("change");
    } else {
        $("#isshortterm_label").html("No");
        $("#isshortterm").val("0");
        $("#semesterdiv").css("display", "");
        $("#phasediv").css("display", "none");
        $("#semestercode").attr("required", true);
        $("#sphaseid").attr("required", false);
        $("#sphaseid").val("").trigger("change");
    }
}

document.addEventListener('DOMContentLoaded', () => {
    $("#username").on("change", function () {
        if ($("#username").val()) {
            callCustomAjax(
                "/nerie/users/check-user",
                "userid=" + $("#username").val(),
                function (data) {
                    if (data === "1") {
                        Notiflix.Notify.Failure("User Id/ Username already exists");
                        $("#username").val("");
                        $("#username").focus();
                    }
                }
            );
        }
    });
})

function checkuserexistfuncbyemail(email) {
    $.ajax({
        type: "POST",
        url: "/nerie/users/check-user",
        data: "emailid=" + email,
        success: function (data) {
            if (data === "1") {
                alert("Email ID already exist");
                $("#email").val("");
                $("#email").focus();
            }
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
}

function editstudent(usercode) {
    $("#usercode").val(usercode);
    $.ajax({
        type: "GET",
        url: `/nerie/students/student/usercode=${usercode}`,
        success: function (data) {
            if (data.studentid.length > 0) {
                var spcode = "";

                $("#rollno").val(data.rollno);
                $("#academicyear").val(data.academicyear);
                $("#username").val(data.usercode.username);
                $("#studentid").val(data.studentid);
                $("#fname").val(data.fname);
                $("#mname").val(data.mname);
                $("#lname").val(data.lname);
                $.when(
                    $("#departmentcode")
                        .val(data.departmentcode.departmentcode)
                        .change()
                ).then(function () {
                    $("#coursecode")
                        .val(data.coursecode.coursecode)
                        .trigger("change");
                });
                $("#email").val(data.email).trigger("change");
                $("#mobileno").val(data.mobileno);
                $("#gender").val(data.gender).trigger("change");
                if (data.isshortterm === "1") {
                    $("#isshortterm").val("1");
                    $("#isshortterm_toggle")
                        .attr("checked", true)
                        .trigger("change");
                    $("#isshortterm_label").html("Yes");
                    spcode = data.sphaseid.sphaseid;
                    $("#sphaseid")
                        .val(
                            data.sphaseid.sphaseid ? data.sphaseid.sphaseid : ""
                        )
                        .trigger("change");
                } else {
                    $("#isshortterm").val("0");
                    $("#isshortterm_toggle")
                        .attr("checked", false)
                        .trigger("change");
                    $("#isshortterm_label").html("No");
                    spcode = data.semestercode.semestercode;
                    $("#semestercode")
                        .val(
                            data.semestercode.semestercode
                                ? data.semestercode.semestercode
                                : ""
                        )
                        .trigger("change");
                }
                getdepartments(
                    data.departmentcode.departmentcode,
                    data.isshortterm
                );
                getsubjects(
                    data.departmentcode.departmentcode,
                    data.isshortterm,
                    spcode,
                    data.coursecode.coursecode
                );
                getstudentsubject(data.usercode.usercode);
                $(window).scrollTop(0);
            } else {
                Notiflix.Notify.Success(
                    "No student details found. Kindly enter student details!"
                );
            }
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
}

function getstudentsubject(usercode) {
    $.ajax({
        type: "GET",
        url: `/nerie/student-subject/${usercode}`,
        success: function (data) {
            var subarray = new Array();
            subarray.push(data.subjectcode.subjectcode);
            $("#optionalsubjects").val(subarray).trigger("change");
        },
        error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
    });
}

function resetpwd(ucode, uname, userid) {
    const confirmMsg = `Are you sure you want to reset the password of <b>${uname}</b>?`;

    showConfirmationModal(confirmMsg, function() {
        var upwd = makeid(5);

        $.ajax({
            type: "POST",
            url: "/nerie/users/reset-password",
            data: "usercode=" + ucode + "&userpassword=" + upwd,
            success: function (data) {
                if (data === "1") {
                    const successMsg = `Password has been reset to: <br><b>${upwd}</b><br><br>Please change the password after first Login.`;
                    showModalAlert(successMsg, "Password Reset Successful");
                } else {
                    showModalAlert("An error occurred while resetting the password. Please try again.", "Error");
                     $('#feedbackModal').one('hidden.bs.modal', function () {
                        window.location.reload();
                     });
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    }, "Reset Password Confirmation", "Reset Password");
}

function makeid(length) {
    var result = "";
    var characters =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(
            Math.floor(Math.random() * charactersLength)
        );
    }
    return result;
}

function changeUserStatus(ucode, uname, status) {
    let smsg = "";
    let title = "";
    const numericStatus = Number(status);

    if (numericStatus === 1) {
        smsg = `Are you sure you want to <b>Disable</b> the account of <b>${uname}</b>?`;
        title = "Confirm Account Disabling";
    } else {
        smsg = `Are you sure you want to <b>Enable</b> the account of <b>${uname}</b>?`;
        title = "Confirm Account Enabling";
    }

    showConfirmationModal(smsg, function() {
        $.ajax({
            type: "POST",
            url: "/nerie/users/change-user-status",
            data: "usercode=" + ucode,
            success: function (data) {
                let successMsg = "";
                if (data === "1") {
                    successMsg = (numericStatus === 1) ? "Account disabled successfully." : "Account enabled successfully.";
                    showModalAlert(successMsg, "Success");
                } else {
                    successMsg = "An error occurred while changing user status. Please try again.";
                    showModalAlert(successMsg, "Error");
                }
                $('#feedbackModal').one('hidden.bs.modal', function () {
                    window.location.href = "/nerie/students/manage";
                });
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    }, title, "Confirm");
}

$(document).ready(function () {
    $(".datepicker").datepicker({
        dateFormat: "dd-mm-yy",
        orientation: "bottom",
    });

    var currentYear = new Date().getFullYear();
    $("#dateofbirth").datepicker({
        maxDate: new Date(currentYear, 11, 31),
        yearRange: "-100:+0",
    });

    $(function () {
        $("#dateofbirth").datepicker();
    });

    $("#usertable").DataTable({
        dom: "Blfrtip",
        retrieve: true,
        sPaginationType: "full_numbers",
        //"bPaginate": false,
        bJQueryUI: true,
        bDestroy: true,
        pageLength: 50,
        lengthMenu: [
            [5, 10, 20, 50, -1],
            [5, 10, 20, 50, "All"],
        ],
        buttons: [
            {
                extend: "excel",
                text: '<em><i class="fa fa-file-excel-o">Excel</i></em>',
                title: function () {
                    return "Students";
                },
                exportOptions: {
                    columns: "thead th:not(.noExport)",
                },
                className: 'btn btn-sm btn-outline-success'
            },
        ],
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const studentLinks = document.querySelectorAll(".load-student-link");

    studentLinks.forEach(function (link) {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            const studentId = link.getAttribute("data-student-id");
            if (studentId) {
                loadStudent(studentId);
            }
        });
    });

    const editLinks = document.querySelectorAll(".edit-student");

    editLinks.forEach(function (link) {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            const studentId = link.getAttribute("data-student-id");
            if (studentId) {
                editstudent(studentId);
            }
        });
    });

    const resetButtons = document.querySelectorAll(".resetbtn");

    resetButtons.forEach(function (button) {
        button.addEventListener("click", function (event) {
            event.preventDefault(); // Prevent default behavior
            const userId = button.getAttribute("data-user-id");
            const email = button.getAttribute("data-email");
            const phone = button.getAttribute("data-phone");

            if (userId && email && phone) {
                resetpwd(userId, email, phone);
            }
        });
    });

    const disableButtons = document.querySelectorAll(".disablebtn");

    disableButtons.forEach(function (button) {
        button.addEventListener("click", function (event) {
            event.preventDefault();

            const userId = button.getAttribute("data-user-id");
            const email = button.getAttribute("data-email");
            const status = button.getAttribute("data-status");

            if (userId && email && status) {
                changeUserStatus(userId, email, status);
            }
        });
    });
});