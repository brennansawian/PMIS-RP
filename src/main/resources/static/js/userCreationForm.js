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

    $('#modalConfirmOkButton').one('click', function() {
        $('#feedbackModal').modal('hide');
        callback();
    });

    $('#feedbackModal').modal('show');
}

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-action='reset']").forEach(button => {
        button.addEventListener("click", function () {
            customReset();
        });
    });
});

$(document).ready(function () {
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    $("#userdescription").keyup(function () {
        $("#charactercount").html("Characters left: " + (300 - $(this).val().length));
    });

});

$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
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
    $('#usertable').DataTable({
        dom: 'Blfrtip',
        pageLength: 5,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excelHtml5',
                text: '<i class="fa fa-file-excel-o"></i> Excel',
                title: 'Users',
                exportOptions: {
                    columns: ':visible:not(.noExport)'
                },
                className: 'btn btn-sm btn-outline-success'
            }
        ]
    });

    $('#usertable tbody').on('click', 'td a.showmore', function () {
        var row = $(this).closest('tr');
        var morespan = row.find('span.more');
        var a = row.find('a.showmore');
        var l = row.find('span.less');
        if (morespan.css('display') === "none")
        {
            a.text("show less...");
            l.hide();
            morespan.show();
        } else
        {
            a.text("show more...");
            l.show();
            morespan.hide();
        }
    });
});

// User save
$(document).ready(async () => {
    $("#dlist").hide()

    $("#userloginfid").submit(async e => {
        e.preventDefault()

        // Validating if atleast a single checkbox is selected
        const checkboxes = document.querySelectorAll('input[type="checkbox"]')
        const isAnyChecked = Array.from(checkboxes).some(checkbox => checkbox.checked)

        if (!isAnyChecked) {
            showModalAlert('Please select at least one Process.');
            return false
        }

        // Validating if a leave role is selected when the corresponding process is checked
        const leaveProcessCheckbox = document.getElementById('44');
        if (leaveProcessCheckbox && leaveProcessCheckbox.checked) {
            if ($('input[name="leaveRole"]:checked').length === 0) {
                showModalAlert('Please select a role for Student Leave Applications.');
                return false;
            }
        }

        // Validating if a role type is selected for Role 'A' users
        const urole = $("#urole").val();
        if (urole === 'A') {
            const roleType = $('#roleType').val();
            if (roleType === null || roleType === "") {
                showModalAlert('Please select a Role Type.');
                return false;
            }
        }

        const formData = new FormData(document.getElementById('userloginfid'))
        await fetch("/nerie/users/save", {
            method: 'POST',
            body: formData
        })
        .then(async res => {
            const data = await res.text()

            switch (res.status) {
                case 200:
                    return data
                default:
                    handleFetchError(res.status, data)
            }
        })
        .then(data => {
            switch (data.trim()) {
                case '1':
                    showModalAlert('A user with the provided details already exists.');
                    break
                case '2':
                    showModalAndRedirect('User saved successfully.', '/nerie/users/manage');
                    break
                case '4':
                    showModalAlert('Office is a required field.');
                    break
                default:
                    showModalAlert('An unexpected error occurred. Please try again.');
            }
        })
        .catch(err => {
            showModalAlert(`Error submitting form. Please try again.<br><b>Error:</b> ${err.message}`);
        })
    })

    $('#displayuser-tab').click(function () {
        $("#officecode").val("");
        $("#usercode").val("");
        $("#username").val("");
        $("#userid").val("");
        $("#userdescription").val("");

        $("#designationcode").val("");
        $("#userrole").val("");
        $("#usermobile").val("");
        $("#emailid").val("");
        $("#userpassword").val("");
        $("#confirmpassword").val("");
        $("#pwddiv").show();
        if ($("#urole").val() === 'A') {
            $('input:checkbox').prop('checked', false);
        }
    })
});

$(document).ready(function () {
    PasswordStrength("userid", "userpassword");
    $("#usermobile").focusout(function () {
        var m = $('#usermobile').val();
        if (m.length > 0 && m.length < 10)
        {
            $('#msg1').html("Mobile no.should be 10 digit");
            $("#usermobile").focus();
            return false;
        } else {
            $('#msg1').html("");
        }
    });
    $("#usermobile").keypress(function () {
        var m = $('#usermobile').val();
        if (m.length == 9)
        {
            $('#msg1').html("");
        }
    });

    $(function () {
        $('input.alphabets').keyup(function () {
            if (this.value.match(/[^a-zA-Z. ]/g)) {
                this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
            }
        });
    });

    $(document).on("keypress", ".numbers", function (event) {
        if (event.which < 48 || event.which > 57) {
            event.preventDefault();
        }

    });
    $(document).on("keypress", ".space", function (event) {
        if (event.which == 32) {
            event.preventDefault();
        }

    });

    $("#confirmpassword").keyup(function (e) {
        if ($("#userpassword").val().replace(/\s/g, '').length == 0) {
            showModalAlert("Please enter the primary password first.");
            $("#confirmpassword").val("");
            $("#userpassword").focus();
        }
    });

    $("#confirmpassword").focusout(function (e) {
        if ($("#confirmpassword").val().replace(/\s/g, '').length != 0)
        if ($("#userpassword").val() != $("#confirmpassword").val()) {
            showModalAlert("The passwords do not match.");
            $("#confirmpassword").val("");
            $("#confirmpassword").focus();
            return false;
        }
    });

    $("#designationcode").change(function () {
        if ($("#designationcode").val() === "others") {
            $("#dlist").show();
            $("#dinput").prop("required", true);
        } else {
            $("#dlist").hide();
            $("#dinput").prop("required", false);
        }

    });
});

document.addEventListener("DOMContentLoaded", function () {
    const userInput = document.getElementById('userid');
    const emailInput = document.getElementById('emailid')
    const passwordInput = document.getElementById('userpassword');
    const confirmPasswordInput = document.getElementById('confirmpassword');
    const editLinks = document.querySelectorAll('.edit-link');

    if (userInput)
        userInput.addEventListener('focusout', () => checkuserexistfunc());

    if (emailInput)
        emailInput.addEventListener('focusout', () => checkEmailExist())

    if (passwordInput) {
        passwordInput.addEventListener('focusout', function () {
            checkuserpwd();
            checkreq();
        });
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('focusout', function () {
            checkconfirmpwd();
        });
    }

    editLinks.forEach(function (link) {
        link.addEventListener('click', function (event) {
            event.preventDefault();

            const value10 = link.getAttribute('data-value10');      // officename
            const value0 = link.getAttribute('data-value0');        // usercode
            const value1 = link.getAttribute('data-value1');        // username
            const value3 = link.getAttribute('data-value3');        // userid
            const value2 = link.getAttribute('data-value2');        // userdescription
            const value12 = link.getAttribute('data-value12');      // designationcode
            const value6 = link.getAttribute('data-value6');        // userrole (of the user being edited)
            const value7 = link.getAttribute('data-value7');        // usermobile
            const value9 = link.getAttribute('data-value9');        // emailid
            const value4 = link.getAttribute('data-value4');        // password hash

            const roleType = link.getAttribute('data-roletype');
            const leaveRole = link.getAttribute('data-leaverole');

            editfunc(value10, value0, value1, value3, value2, value12, value6, value7, value9, value4, roleType, leaveRole);
        });
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const resetLinks = document.querySelectorAll('.reset-link');

    resetLinks.forEach(function (link) {
        link.addEventListener('click', function (event) {
            event.preventDefault();

            const value0 = link.getAttribute('data-value0');
            const value1 = link.getAttribute('data-value1');
            const value3 = link.getAttribute('data-value3');

            resetpwd(value0, value1, value3);
        });
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const changeStatusLinks = document.querySelectorAll('.disablebtn, .enablebtn');

    changeStatusLinks.forEach(function (link) {
        link.addEventListener('click', function (event) {
            event.preventDefault();

            const value0 = link.getAttribute('data-value0');
            const value1 = link.getAttribute('data-value1');
            const value5 = link.getAttribute('data-value5');

            changeUserStatus(value0, value1, value5);
        });
    });
});

function checkEmailExist() {
    var re = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
    const usercode = document.getElementById('usercode')

    if ($("#emailid").val().replace(/\s/g, '').length != 0) {
        if (re.test($('#emailid').val()) == false) {
            showModalAlert("Please enter a valid Email ID.");
            $("#emailid").val("").focus();
        } else if (!usercode.value) {
            $.ajax({
                type: "POST",
                url: "/nerie/users/check-email",
                data: "emailid=" + $("#emailid").val(),
                success: function (data) {
                    if (data == "1") {
                        showModalAlert("This Email ID is already registered.");
                        $("#emailid").val("").focus();
                    }
                },
                error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
            })
        }
    }
}

function checkuserexistfunc() {
    var userid = $("#userid").val();
    const usercode = document.getElementById('usercode')

    if (userid.replace(/\s/g, '').length != 0) {
        if (!usercode.value) {
            $.ajax({
                type: "POST",
                url: "/nerie/users/check-user",
                data: "userid=" + userid + "&usercode=" + $("#usercode").val(),
                success: function (data) {
                    if (data == "1") {
                        showModalAlert("This User ID already exists. Please enter a different User ID.");
                        $("#userid").val("").focus();
                    } else {
                        var userid1 = $("#userid").val().toUpperCase();
                        var userpassword = $("#userpassword").val().toUpperCase();
                        if (userpassword.replace(/\s/g, '').length != 0) {
                            if (userpassword.indexOf(userid1) > -1)
                            {
                                showModalAlert("Password should not contain the User ID.");
                                $("#userpassword").val("").focus();
                                $("#confirmpassword").val("");
                            }
                        }
                    }
                },
                error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
            })
        }
    }
}

function checkconfirmpwd() {
    const password = document.getElementById("userpassword").value.trim();
    const confirmPassword = document.getElementById("confirmpassword").value.trim();

    if (!password) {
        showModalAlert("Please enter the primary password first.");
        document.getElementById("confirmpassword").value = "";
        document.getElementById("userpassword").focus();
        return false;
    }

    if (confirmPassword && (password !== confirmPassword)) {
        showModalAlert("The passwords do not match.");
        document.getElementById("confirmpassword").value = "";
        return false;
    }

    return true;
}

function checkuserpwd() {
    var userid = $('#userid').val().toUpperCase();
    if (userid.replace(/\s/g, '').length != 0) {
        var userpassword = $("#userpassword").val().toUpperCase();
        if (userpassword.indexOf(userid) > -1)
        {
            showModalAlert("For security, the password cannot contain the User ID.");
            $("#userpassword").val("").focus();
        }
    }
}

function checkreq() {
    var re = /(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}/;
    if ($("#userpassword").val().replace(/\s/g, '').length != 0) {
        if (re.test($('#userpassword').val()) == false) {
            showModalAlert("Password does not meet requirements.<br><br>It must contain at least one number, one lowercase letter, one uppercase letter, and be at least 8 characters long.");
            $("#userpassword").val("").focus();
        }
    }
}

function editfunc(ocode, ucode, uname, uid, udesc, udesig, urole, umno, uemail, upwd, roleType, leaveRole) {

//    console.log("ocode: " + ocode);
//    console.log("ucode: " + ucode);
//    console.log("uname: " + uname);
//    console.log("uid: " + uid);
//    console.log("udesc: " + udesc);
//    console.log("udesig: " + udesig);
//    console.log("urole: " + urole);
//    console.log("umno: " + umno);
//    console.log("uemail: " + uemail);
//    console.log("upwd: " + upwd);
//    console.log("roleType: " + roleType);
//    console.log("leaveRole: " + leaveRole);

    $('#userdetails-tab').click();
    $(window).scrollTop(0);

    $("#officecode").val(ocode).focus();
    $("#usercode").val(ucode);
    $("#username").val(uname);
    $("#userid").val(uid);
    $("#userdescription").val(udesc);
    $("#designationcode").val(udesig).trigger("change");
    $("#usermobile").val(umno);
    $("#emailid").val(uemail);
    $("#userpassword").val(upwd);
    $("#confirmpassword").val(upwd);
    $("#pwddiv").hide();

    if (roleType !== null && roleType !== 'null') {
        $('#roleType').val(roleType);
    } else {
        $('#roleType').val('');
    }

    $('input[name="processes"]').prop('checked', false);
    $('input[name="leaveRole"]').prop('checked', false);
    $('#radio-container').hide();

    if ($("#urole").val() === 'A' || $("#urole").val() === 'S' || $("#urole").val() === 'Z') {
        $.ajax({
            type: "POST",
            url: "/nerie/processes/getuprocess",
            data: "usercode=" + ucode,
            success: function (data) {
                $.each(data, function (i, v) {
                    $("#" + v[1]).prop('checked', true);
                });


                if ($('#44').is(':checked')) {
                    $('#radio-container').show(); // Show the container
                    if (leaveRole !== null && leaveRole !== 'null') {
                         // Check the correct radio button
                        $('input[name="leaveRole"][value="' + leaveRole + '"]').prop('checked', true);
                    }
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        })
    }
}


function makeid(length) {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function resetpwd(ucode, uname, userid) {
    showModalConfirm("Are you sure you want to reset the password for " + uname + "?", function() {
        var upwd = makeid(8);
        $.ajax({
            type: "POST",
            url: "/nerie/users/reset-password",
            data: "usercode=" + ucode + "&userpassword=" + upwd,
            success: function (data) {
                if (data === "1") {
                    showModalAlert(
                        'Password reset successfully. <br><br>The new temporary password is: <b>' + upwd + '</b><br><br>Please instruct the user to change this password after their first login.',
                        'Password Reset'
                    );
                } else {
                    showModalAndRedirect("An error occurred while resetting the password. Please try again.", "/nerie/users/manage");
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });
}

function changeUserStatus(ucode, uname, status) {
    var smsg = (status == '1')
        ? "Are you sure you want to disable the account for " + uname + "?"
        : "Are you sure you want to enable the account for " + uname + "?";

    showModalConfirm(smsg, function() {
        $.ajax({
            type: "POST",
            url: "/nerie/users/change-user-status",
            data: "usercode=" + ucode,
            success: function (data) {
                if (data == "1") {
                    const successMessage = (status == 1)
                        ? "Account disabled successfully."
                        : "Account enabled successfully.";
                    showModalAndRedirect(successMessage, "/nerie/users/manage");
                } else {
                    showModalAndRedirect("An error occurred while changing user status. Please try again.", "/nerie/users/manage");
                }
            },
            error: (jqXHR, textStatus, errorThrown) => handleAjaxError(jqXHR, textStatus, errorThrown)
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
  const checkbox = document.getElementById('44');
  const radioContainer = document.getElementById('radio-container');
  if(checkbox && radioContainer) {
      const radioButtons = radioContainer.querySelectorAll('input[type="radio"]');

      checkbox.addEventListener('change', () => {
        if (checkbox.checked) {
          radioContainer.style.display = 'block';
        } else {
          radioContainer.style.display = 'none';
          radioButtons.forEach(radio => {
            radio.checked = false;
          });
        }
      });
  }
});