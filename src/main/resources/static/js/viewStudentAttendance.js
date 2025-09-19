function getDaysInMonth(month, year) {
    month--;
    var date = new Date(year, month, 1);
    var days = [];
    while (date.getMonth() === month) {
        days.push(new Date(date));
        date.setDate(date.getDate() + 1);
    }
    return days;
}


$(document).ready(function () {

    $("#exporttopdf").hide();

    function getDataTableConfig(emptyTableMessage) {
        return {
            dom: 'Blfrtip',
            pageLength: 30,
            lengthMenu: [[30, 40, 50, -1], [30, 40, 50, "All"]],
            buttons: [
                {
                    extend: 'excelHtml5',
                    text: '<i class="fa fa-file-excel-o"></i> Excel',
                    title: 'Student Attendance', // This will be the file name
                    exportOptions: {
                        columns: ':visible:not(.noExport)'
                    },
                    className: 'btn btn-sm btn-outline-success mb-3'
                }
            ],
            language: {
                emptyTable: emptyTableMessage || "No data available in table"
            },
        };
    }

    let attenTable = $('#attendancetable').DataTable(
        getDataTableConfig("Select the subject, month, and time to view attendance.")
    );

    $('#viewstudentattendance').on('click', function () {
        const subjectcode = $("#subjectcode").val();
        if (subjectcode === '-1') {
            Notiflix.Notify.Info('Please select a Subject');
            $("#subjectcode").focus();
            return;
        }
        const month = $("#month").val();
        if (!month) {
            Notiflix.Notify.Info('Please select a Month');
            $("#month").focus();
            return;
        }
        const time = $("#time").val();
        if (time === '-1') {
            Notiflix.Notify.Info('Please select a Time');
            $("#time").focus();
            return;
        }

        $("#exporttopdf").hide();
        Notiflix.Loading.Standard('Fetching Attendance...');

        $.ajax({
            type: "GET",
            url: "getStudentAttendance",
            data: {
                subjectcode: subjectcode,
                month: month,
                time: time
            },
            success: function (data) {
                Notiflix.Loading.Remove();
                const mth = $("#month option:selected").text();
                const sub = $("#subjectcode option:selected").text();
                const selectedTime = $("#time option:selected").text();

                if ($.fn.DataTable.isDataTable('#attendancetable')) {
                    attenTable.destroy();
                }
                $('#attendancetable thead').empty();
                $('#attendancetable tbody').empty();

                if (data && data.length > 0) {
                    $("#exporttopdf").show();
                    const year = new Date().getFullYear();
                    const weekday = ["S", "M", "T", "W", "T", "F", "S"];
                    const days = getDaysInMonth(parseInt(month), year);

                    var tc = "<strong>Month: " + mth + " - " + year + ", Subject: " + sub + ", Time: " + time + "</strong>";
                    $("#tabcap").html(tc);
                    let thead = '<tr>' +
                                '<th rowspan="2">Sl. No.</th>' +
                                '<th rowspan="2">Student ID</th>' +
                                '<th rowspan="2">Student Name</th>';
                    days.forEach(d => thead += `<th>${weekday[d.getDay()]}</th>`);
                    thead += '</tr><tr>';
                    days.forEach(d => thead += `<th>${d.getDate()}</th>`);
                    thead += '</tr>';
                    $('#attendancetable thead').html(thead);

                    // Build table body
                    let rowdataHtml = "";
                    data.forEach((sdata, index) => {
                        rowdataHtml += `<tr><td>${index + 1}</td>` +
                                       `<td>${sdata[0]}</td>` +
                                       `<td>${sdata[1]}</td>`;

                        const attendance = [];
                        if (sdata[2]) {
                            sdata[2].split(',').forEach(entry => {
                                const parts = entry.split('$$');
                                if (parts.length >= 2) {
                                    const dayPart = parseInt(parts[1], 10);
                                    if (!isNaN(dayPart)) {
                                        if (!attendance[dayPart]) attendance[dayPart] = [];
                                        attendance[dayPart].push({ status: parts[0], reason: parts[2] || '' });
                                    }
                                }
                            });
                        }

                        days.forEach(d => {
                            const dayid = d.getDate();
                            let ap = "";
                            if (attendance[dayid]) {
                                attendance[dayid].forEach(att => {
                                    if (att.status === "P") {
                                        ap += `<span style='color:green' title='Reason: ${att.reason}'><b>P</b></span><br/>`;
                                    } else {
                                        ap += `<span style='color:red' title='Reason: ${att.reason}'><b>A</b></span><br/>`;
                                    }
                                });
                            }
                            rowdataHtml += `<td>${ap}</td>`;
                        });
                        rowdataHtml += "</tr>";
                    });
                    $('#attendancetable tbody').html(rowdataHtml);

                    attenTable = $('#attendancetable').DataTable(
                        getDataTableConfig("No attendance data found for this selection.")
                    );
                } else {
                    Notiflix.Notify.Info('No Attendance Record Found for this selection.');
                    $('#attendancetable thead').html("<tr><th>Student Attendance</th></tr>");
                    attenTable = $('#attendancetable').DataTable(
                        getDataTableConfig('No Attendance Record Found')
                    );
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                Notiflix.Loading.Remove();
                Notiflix.Notify.Failure(`Error: ${textStatus} - ${errorThrown}`);
                 if ($.fn.DataTable.isDataTable('#attendancetable')) {
                    attenTable.destroy();
                }
                $('#attendancetable thead').html("<tr><th>Student Attendance</th></tr>");
                $('#attendancetable tbody').empty();
                attenTable = $('#attendancetable').DataTable(
                    getDataTableConfig("An error occurred while fetching attendance.")
                );
            }
        });
    });


    const exportToPdfButton = document.getElementById('exporttopdf');
    if (exportToPdfButton) {
        exportToPdfButton.addEventListener('click', function() {
            if (typeof window.jspdf === 'undefined' || typeof window.jspdf.jsPDF === 'undefined') {
                 Notiflix.Notify.Failure('PDF library (jsPDF) is not loaded or initialized correctly.');
                 console.error("window.jspdf or window.jspdf.jsPDF is undefined at click time.");
                 return;
            }
            const { jsPDF } = window.jspdf;
            const doc = new jsPDF('l', 'pt', 'a4');
            if (typeof doc.autoTable !== 'function') {
                Notiflix.Notify.Failure('PDF autoTable plugin is not loaded or attached to jsPDF instance.');
                console.error("doc.autoTable is not a function at click time.");
                return;
            }
            var year = new Date().getFullYear();
            var mth = $("#month option:selected").text();
            var sub = $("#subjectcode option:selected").text();
            var time = $("#time option:selected").text();
            var deptname = $('#departmentname').val() || 'N/A';
            var facultyname = $('#facultyname').val() || 'N/A';
            var coursename= $('#coursename').val() || 'N/A';
            doc.setFontSize(10);
            var textX = 40;
            var textY = 30;
            doc.text('Teacher: ' + facultyname, textX, textY);
            doc.text('Department: ' + deptname, textX + 200, textY);
            doc.text('Course: ' + coursename, textX + 400, textY);
            textY +=15;
            doc.text('Subject: ' + sub, textX, textY);
            doc.text('Month: ' + mth + " - " + year, textX + 200, textY);
            doc.text('Time: ' + time, textX + 400, textY);
            doc.autoTable({
                html: "#attendancetable",
                startY: textY + 10,
                theme: 'grid',
                headStyles: {fillColor: [22, 160, 133], textColor: 255, fontSize:8},
                bodyStyles: {fontSize:7, cellPadding: 2},
                alternateRowStyles: {fillColor: [240, 240, 240]},
                tableWidth: 'auto',
                margin: { top: textY + 20 },
                styles: { cellWidth: 'wrap', halign:'center', valign:'middle' },
                columnStyles: {
                    0: { cellWidth: 30 },
                    1: { cellWidth: 70 },
                    2: { cellWidth: 100, halign: 'left' }
                }
            });
            doc.save('student_attendance.pdf');
        });
    }
});