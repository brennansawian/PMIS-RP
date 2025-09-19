var updateProfileUrl = '/nerie/participant/update-profile';
var getDistrictsUrl = '/nerie/districts/get-districts';
var getQualificationSubjectsUrl = '/nerie/qualification-subjects/get-subjects';
var checkUserEmailUrl = '/nerie/participant/check-existing-email';
var redirectAfterUpdateUrl = '/nerie/participant/edit-profile';

$(document).ready(function () {
    if ($.fn.selectpicker) {
        $('.selectpicker').selectpicker();
    }

    function toggleOtherOfficeType() {
        if ($("#participantofficetypecode").val() == "5") {
            $(".otherparticipantofficetypediv").removeClass("display-none").show();
            $("#otherparticipantofficetype").prop("required", true);
        } else {
            $(".otherparticipantofficetypediv").addClass("display-none").hide();
            $("#otherparticipantofficetype").val("").prop("required", false);
        }
    }
    toggleOtherOfficeType();
    $("#participantofficetypecode").change(toggleOtherOfficeType);

    function toggleMinorityFields() {
        if ($("#isminority").val() == "Y") {
            $("#ifminoritydiv").removeClass("display-none").show();
            $("#minoritycode").prop("required", true).prop("disabled", false);
            if ($("#minoritycode").val() == "6") {
                 $("#ifotherminority").removeClass("display-none").show();
                 $("#others").prop("required", true);
            } else {
                 $("#ifotherminority").addClass("display-none").hide();
                 $("#others").val("").prop("required", false);
            }
        } else {
            $("#ifminoritydiv").addClass("display-none").hide();
            $("#minoritycode").val('').prop("required", false).prop("disabled", true);
            $("#ifotherminority").addClass("display-none").hide();
            $("#others").val("").prop("required", false);
        }
        if ($('#minoritycode').data('selectpicker')) {
            $('#minoritycode').selectpicker('refresh');
        }
    }
    toggleMinorityFields();
    $("#isminority, #minoritycode").change(toggleMinorityFields);

    $(document).on("keypress", ".numbers", function (event) {
        if (event.which < 48 || event.which > 57) event.preventDefault();
    });
    $(document).on("keyup", ".alphabets", function () {
        this.value = this.value.replace(/[^a-zA-Z. ]/g, '');
    });
    $(document).on("focusout", ".defaultval", function () {
        if ($(this).val().trim().length === 0) $(this).val('0');
    });
    $(document).on("keyup", ".numberspl", function () {
        this.value = this.value.replace(/[^0-9\ -]/g, '');
    });

    $("#usermobile").focusout(function () {
        var m = $('#usermobile').val();
        if (m.length > 0 && m.length < 10) {
            $('#msg1').html("Mobile no. should be 10 digits");
            showModalAlert("Mobile no. should be 10 digits");
        } else {
            $('#msg1').html("");
        }
    });
    $("#usermobile").keypress(function () {
        if ($('#usermobile').val().length === 9) $('#msg1').html("");
    });

    $("#emailid").focusout(function () {
        var emailVal = $(this).val().trim();
        var re = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
        if (emailVal.length > 0 && !re.test(emailVal)) {
            showModalAlert("Please enter valid Email ID");
            $(this).focus();
        } else if (emailVal.length > 0) {
            checkuserexistfunc();
        }
    });

    $("#tparticipantfid").submit(function (e) {
        e.preventDefault();
        if ($("#participantofficestatecode").val() === "") {
            showModalAlert("Please Select the Office State"); $("#participantofficestatecode").focus(); return false;
        }
        if ($("#statecode").val() === "") {
            showModalAlert("Please Select the Residential State"); $("#statecode").focus(); return false;
        }
        if ($("#districtcode").val() === "" || $("#districtcode").val() === null) {
            showModalAlert("Please Select the Residential District"); $("#districtcode").focus(); return false;
        }
        if ($("#designationcode").val() === "others" && $("#dinput").val().trim() === "") {
            showModalAlert("Please enter designation name when 'Others' is selected."); $("#dinput").focus(); return false;
        }

        $.ajax({
            type: "POST", url: updateProfileUrl, data: $("#tparticipantfid").serialize(),
            success: function (data) {
                if (data == "1") { showModalAlert("Email Id cannot be empty"); $("#emailid").focus(); }
                else if (data == "3") { showModalAlert("Email id should be 1-50 characters long"); $("#emailid").focus(); }
                else if (data == "4") { showModalAlert("Email id Already exist"); $("#emailid").focus(); }
                else if (data == "2") {
                    showModalAlert("Successfully Updated!!!", "Success");
                } else { showModalAlert("Save Failed!!! " + (data || "Please try again."), "Error"); }
            }, error: function (jqXHR, textStatus, errorThrown) {
                showModalAlert("Error: " + textStatus + " - " + errorThrown, "Error");
            }
        });
    });

    $("#statecode").change(function () {
        var stateCodeVal = $(this).val();
        var $districtSelect = $('#districtcode');
        $districtSelect.empty().append($("<option>").val("").text("Select"));

        if ($districtSelect.data('selectpicker')) {
            $districtSelect.selectpicker('refresh');
        }

        if(stateCodeVal) {
            $.ajax({
                type: "POST", url: getDistrictsUrl, data: { statecode: stateCodeVal },
                success: function (data) {
                    if(data && data.length > 0) {
                        $.each(data, function(i, d) { $districtSelect.append($("<option>").val(d.districtcode).text(d.districtname)); });
                    } else {
                        showModalAlert('No districts for selected state.');
                    }
                    if ($districtSelect.data('selectpicker')) {
                        $districtSelect.selectpicker('refresh');
                    }
                }, error: function () {
                    showModalAlert("Error fetching districts.", "Error");
                    if ($districtSelect.data('selectpicker')) {
                        $districtSelect.selectpicker('refresh');
                    }
                }
            });
        }
    });

    $("#qualificationcode").change(function () {
        var qualCodeVal = $(this).val();
        var $subjectSelect = $('#qualificationsubjectcode');
        $subjectSelect.empty().append($("<option>").val('0').text("Not Applicable"));

        if ($subjectSelect.data('selectpicker')) {
            $subjectSelect.selectpicker('refresh');
        }

        if(qualCodeVal){
            $.ajax({
                type: "GET",
                url: getQualificationSubjectsUrl,
                data: { qualificationcode: qualCodeVal },
                success: function (data) {
                    if(data && data.length > 0) {
                        $.each(data, function(i, s) { $subjectSelect.append($("<option>").val(s[0]).text(s[1])); });
                    }
                    if ($subjectSelect.data('selectpicker')) {
                        $subjectSelect.selectpicker('refresh');
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    showModalAlert("Error fetching subjects.", "Error");
                    if ($subjectSelect.data('selectpicker')) {
                        $subjectSelect.selectpicker('refresh');
                    }
                }
            });
        }
    });

    function toggleDlist() {
        if ($("#designationcode").val() === "others") {
            $("#dlist").removeClass("display-none").show();
            $("#dinput").prop("required", true);
        } else {
            $("#dlist").addClass("display-none").hide();
            $("#dinput").prop("required", false).val('');
        }
    }
    toggleDlist();
    $("#designationcode").change(toggleDlist);

    $('#districtcode').on('focus', function() {
        checkstate();
    });
});

function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal').modal('show');
}

function checkuserexistfunc() {
    var useridVal = $("#emailid").val().trim();
    if (useridVal.length > 0) {
        $.ajax({
            type: "POST", url: checkUserEmailUrl, data: { userid: useridVal },
            success: function (data) {
                if (data == "1") {
                    showModalAlert("Email Id already exist!", "Message");
                    $("#emailid").focus();
                }
            }, error: function () {
                showModalAlert("Error checking email.", "Error");
            }
        });
    }
}

function checkstate() {
    if ($("#statecode").val() === "" || $("#statecode").val() === null) {
        showModalAlert("Select Residential State first.");
        if ($("#statecode").data('selectpicker')) {
            $("#statecode").selectpicker('toggle');
        } else {
            $("#statecode").focus();
        }
    }
}