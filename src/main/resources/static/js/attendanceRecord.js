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

$(document).ready(function () {

    var attenTable = $('#attendancetable').DataTable({dom: 'Bfrtipl', buttons: ['pdfHtml5', 'excelHtml5'], "language": {
            "emptyTable": "Select a subject to view attendance record"
        }});

    $('#viewattendance').on('click', function () {

        var subjectcode = $("#subjectcode").val();
        if (subjectcode === '-1') {
            Notiflix.Notify.Info('Select Subject');
            $("#subjectcode").focus();
            return false;
        }

        var month = $("#month").val();

        $.ajax({
            type: "GET",
            url: "/nerie/attendance/getStudentAttendance",
            data: "subjectcode=" + subjectcode + "&month=" + month,
            success: function (data) {
                var cpresent  = 0;
                var cabsent = 0;
                attenTable.clear();
                attenTable.destroy();
                if (data.length !== 0)
                {
                    var count = 1;
                    data.forEach(function (item) {
                        var rowString = "<tr>" +
                                "<td>" + (count++) + "</td>" +
                                "<td>" + item[0] + "</td>" +
                                "<td>" + item[1] + "</td>" +
                                "<td>" + item[2] + " - " + item[3] + "</td>" +
                                "</tr>";
                        $('#atttablebody').append(rowString);

                        if (item[0] === 'P' || item[0] === 'p')
                            cpresent++;
                        else
                            cabsent++;
                    });

                    var chartname = 'attendanceChart';
                    var chartdata = [(cpresent + cabsent),cpresent, cabsent];
                    var labels = ["Present", "Absent"];
                    var sidelabelsids = ["countTotal","countPresent","countAbsent"];
                    var colorarray = ['#C6EFCA','#F0CCDC'];

                    populatePieChart(chartname, chartdata, labels, sidelabelsids, colorarray, 'pie', false);

                    $('#divChart').css('display','');


                } else
                {
                    Notiflix.Notify.Info('No Attendance Record Found');
                }
                attenTable = $('#attendancetable').DataTable({dom: 'Bfrtipl', buttons: ['pdfHtml5', 'excelHtml5'], "language": {
                        "emptyTable": "No attendance record found"
                    }});
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert("error:" + textStatus + " - exception:" + errorThrown);
            }
        });
    });
});

$(document).ready(function () {
    var date = new Date();
    var today = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    $('#assignmentdate').datepicker({
        format: 'yyyy-mm-dd',
        orientation: 'bottom'
    });

    $('#assignmentdate').datepicker('setDate', today);

});

$("#assignmentdate").datepicker({
    dateFormat: "dd-mm-yy"
});

$("#lastdate").datepicker({
    dateFormat: "dd-mm-yy",
    minDate: new Date()
});

var rowclicktddata;
$('#assignmentlist tbody').on('click', 'td button.this_is_upload_button_class', function () {
    rowclicktddata = $(this).closest('td');
});