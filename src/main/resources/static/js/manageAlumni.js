function showModalAlert(message, title = 'Message') {
    $('#feedbackModalLabel').text(title);
    $('#feedbackModalBody').html(message);
    $('#feedbackModal').modal('show');
}

$(document).ready(function () {
    $("#talumniid").submit(function (e) {
        e.preventDefault();

        callCustomAjax(
            "/nerie/alumni/save",
            $("#talumniid").serialize(),
            function (data) {
                if (data) {
                    const message = $("#alumniid").val()
                        ? "Alumni details were successfully updated!"
                        : "Alumni details were successfully added.";
                    
                    showModalAlert(message, "Success");

                    $('#feedbackModal').on('hidden.bs.modal', function () {
                        $(this).off('hidden.bs.modal');
                        window.location.reload();
                    });

                } else {
                    showModalAlert("An error occurred. Please check the details and try again.", "Operation Failed");
                    $("#alumniid").val("");
                }
            }
        );
    });

    $("#departmentcode").change(function () {
        if ($("#departmentcode").val() !== "-1") {
            callCustomAjaxasync(
                "/nerie/course-academics/list-by-departmentcode",
                "departmentcode=" + $("#departmentcode").val(),
                function (data) {
                    $("#coursecode").empty();
                    $("#coursecode").append(
                        $('<option data-duration="0" value="-1">--Select Course--</option>')
                    );
                    data.forEach(function (item) {
                        $("#coursecode").append(
                            $('<option data-duration="' + item[5] + '" value="' + item[0] + '">' + item[1] + "</option>")
                        );
                    });
                    $("#coursecode").val("-1").trigger("change");
                }
            );
        } else {
            $("#coursecode").empty();
            $("#coursecode").append($('<option data-duration="0" value="-1">--Select Course--</option>'));
            $("#coursecode").val("-1").trigger("change");
        }
    });

    $("#coursecode").change(function () {
        var element = document.getElementById("coursecode");
        var duration = element.options[element.selectedIndex].getAttribute("data-duration");
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
                    $("#batch").empty();
                    var options = '<option value="-1" selected="true" disabled="true">Select Batch</option>';
                    for (var i = 0; i < ay.length; i++) {
                        options += '<option value="' + ay[i] + '">' + ay[i] + "</option>";
                    }
                    $("#batch").append(options);
                    self.close();
                })
                .fail(function () {
                    self.setContent("Something went wrong.");
                    self.setTitle("");
                });
            },
        });
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
        if (tmp.length !== 10 && tmp.length > 0) {
            $("#msgMobile").html("Mobile No. should be 10 digits");
            return false;
        } else {
            $("#msgMobile").html("");
            return true;
        }
    });

    $(".email").focusout(function (e) {
        var re = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
        if ($(".email").val().replace(/\s/g, "").length !== 0) {
            if (re.test($(".email").val()) === false) {
                $("#msgEmail").html("Please enter a valid Email ID");
                $(".email").focus();
                return false;
            } else {
                $("#msgEmail").html("");
                return true;
            }
        }
    });

    $("#rollno").on("change", function () {
        if ($("#rollno").val()) {
            callCustomAjax(
                "/nerie/alumni/check-exist",
                "rollno=" + $("#rollno").val(),
                function (data) {
                    if (data == "1") {
                        $("#msgRollNo").html("Student with this Roll No. already exists");
                        $("#rollno").focus();
                    } else {
                        $('#msgRollNo').html('');
                    }
                }
            );
        }
    });

    $('#usertable').on('click', '.editalum', function () {
        const alumniId = $(this).data("id");
        if (alumniId) {
            editalumni(alumniId);
        }
    });
    
    $("#usertable").DataTable({
        dom: "Blfrtip",
        retrieve: true,
        sPaginationType: "full_numbers",
        bJQueryUI: true,
        bDestroy: true,
        pageLength: 5,
        lengthMenu: [
            [5, 10, 20, 50, -1],
            [5, 10, 20, 50, "All"],
        ],
        buttons: [
            {
                extend: "excel",
                text: '<em><i class="fa fa-file-excel-o">Excel</i></em>',
                title: function () {
                    return "Alumni List";
                },
                exportOptions: {
                    columns: "thead th:not(.noExport)",
                },
                className: 'btn btn-sm btn-outline-success'
            },
        ],
    });
});


function editalumni(alumniid) {
    $.ajax({
        type: "GET",
        async: false,
        url: `/nerie/alumni/detail/${alumniid}`,
        success: function (data) {
            if (data.length > 0) {
                $("#alumniid").val(data[0][0]);
                $("#rollno").val(data[0][1]);
                $("#fname").val(data[0][2]);
                $("#mname").val(data[0][3]);
                $("#lname").val(data[0][4]);
                $("#gender").val(data[0][5]);
                $("#email").val(data[0][8]);
                $("#mobileno").val(data[0][7]);
                $("#currentoccupation").val(data[0][9]);
                $("#batch").val(data[0][6]);
                $(window).scrollTop(0);
            } else {
                Notiflix.Notify.Failure(
                    "No alumni details found for this ID."
                );
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            showModalAlert("Error: " + textStatus + " - " + errorThrown, "Request Failed");
        },
    });
}