function myFunction(x) {
    x.classList.toggle("change");
}

$("#menu-toggle").click(function (e) {
    e.preventDefault();
    $("#wrapper").toggleClass("toggled");
});

$(document).ready(function () {
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
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
    });

    $("#selectAllBtn").click(function (e) {
        e.preventDefault();
        var checkboxes = $("#studentlistbody input[type='checkbox']");
        if ($(this).text() === "De-Select All") {
            checkboxes.prop("checked", false);
            $(this).text("Select All");
        } else {
            checkboxes.prop("checked", true);
            $(this).text("De-Select All");
        }
    });

    var date = new Date();
    var today = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    $('#dateselect').datepicker({
        dateFormat: 'dd-mm-yy', // Correct for jQuery UI Datepicker
        maxDate: new Date()     // jQuery UI Datepicker option
    });
    $('#dateselect').datepicker('setDate', today);

    $("#stusel").change(function (e) {
        var rcount = $("#stusel").val();
        $("#dlist").html(""); // This overwrites the student table structure.
        $("#dlist").addClass("mt-4");
        var temp = "<div class='col-sm-12 col-md-12 mt-4'><h5>Enter Student IDs of the absentees:</h5></div>";
        for (var i = 1; i <= rcount; i++) {
            temp += "<div class='col-sm-12 col-md-6 mt-4'>";
            temp += "<label for='input-" + i + "'>Enter Student ID #" + i + " :</label>";
            temp += "<input type='text' class='form-control' name='sturollno' id='input-" + i + "' required='required' />";
            temp += "</div>";
        }
        $("#dlist").html(temp);
    });

    var studentlist;
    var stdtable = $('#studentlist').DataTable({
        dom: 'f',
        paging: false,
        info: false
    });

    $("#subject").change(function () {
        var subjectcode = $("#subject").val();
        $('#dlist').addClass('d-none');
        stdtable.clear().draw();
        $('#studentlistbody').empty();

        if (subjectcode) {
            Notiflix.Loading.Standard('Loading student list...');
            $.ajax({
                type: "GET",
                url: "getStudentsListBasedOnSubjectCode",
                data: { subjectcode: subjectcode },
                success: function (data) {
                    Notiflix.Loading.Remove();
                    if (!data) {
                        Notiflix.Notify.Failure("Unable to retrieve student list. Please try again.");
                        $('#studentlistbody').html('<tr><td colspan="3">Unable to retrieve student list.</td></tr>');
                    } else {
                        studentlist = data;
                        if (studentlist.length > 0) {
                            stdtable.destroy();
                            $('#studentlistbody').empty();

                            studentlist.forEach(function (x) {
                                var sname = [x[1], x[2], x[3]].filter(Boolean).join(" ");
                                var rowData = "<tr>" +
                                    "<td>" + x[0] + "</td>" +
                                    "<td>" + sname + "</td>" +
                                    "<td><input type='checkbox' checked='checked' /></td>" +
                                    "</tr>";
                                $('#studentlistbody').append(rowData);
                            });

                            stdtable = $('#studentlist').DataTable({
                                dom: 'lf',
                                lengthMenu: [[-1], ['Show all']]
                            });
                            $('#dlist').removeClass('d-none');
                        } else {
                            Notiflix.Notify.Warning("No students found for this subject.");
                            $('#studentlistbody').html('<tr><td colspan="3">No Students For This Subject Code!</td></tr>');
                            $('#dlist').removeClass('d-none');
                        }
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    Notiflix.Loading.Remove();
                    Notiflix.Notify.Failure("Error: " + textStatus + " - " + errorThrown);
                    $('#studentlistbody').html('<tr><td colspan="3">Error loading student list.</td></tr>');
                    $('#dlist').removeClass('d-none');
                }
            });
        }
    });

    $("#attendanceform").submit(function (e) {
        e.preventDefault();
        var attedancearray = [];

        stdtable.rows().every(function () {
            var rowNode = this.node();
            var cells = $(rowNode).find('td');
            var studentid = $(cells[0]).text();
            var isChecked = $(cells[2]).find('input[type="checkbox"]').prop('checked');
            var presentorabsent = isChecked ? "P" : "A";

            attedancearray.push({
                studentid: studentid,
                pora: presentorabsent
            });
        });

        var formData = new FormData($(this)[0]);
        formData.append('attendancejsonstring', JSON.stringify(attedancearray));

        Notiflix.Loading.Standard('Saving attendance...');
        $.ajax({
            type: "POST",
            url: "upload-attendance",
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                Notiflix.Loading.Remove();
                if (data === "0") {
                    Notiflix.Notify.Failure("Unable to save. Please try again.");
                } else if (data === "-1") {
                    Notiflix.Notify.Failure("Error processing request on server. Please check data/formats.");
                } else {
                    Notiflix.Notify.Success("Successfully saved.");
                    //window.location.reload();
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Loading.Remove();
                Notiflix.Notify.Failure("Error: " + textStatus + " - " + errorThrown);
            }
        });
    });
});