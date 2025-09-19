$(document).ready(function () {
    $("#calendar").MEC();
    $('#backtotop').click(function () {
        $("html, body").animate({scrollTop: 0}, 600);
        return false;
    });

    $('#completedTable').DataTable({ responsive: false });
    $('#closedTable').DataTable({ responsive: false });
    $('#upcomingTable').DataTable({ responsive: false });
    $('#ongoingTable').DataTable({ responsive: false });

    $.ajax({
        url: '/nerie/students/getmyhomepageattendance',
        type: 'GET',
        success: function (data) {
            var labels = [];
            var barData = [];
            var attpercent = [];
            var totalClassesArray = [];
            var totalPresentArray = [];
            var totalAbsentArray = [];
            if (data) {
                data.forEach(function (row) {
                    labels.push(row[0]);
                    barData.push(row[5]);
                    totalClassesArray.push(row[4]);
                    totalPresentArray.push(row[2]);
                    totalAbsentArray.push(row[3]);
                    attpercent.push(row[5]);
                });
            }
            var colorarray = ['#C6EFCA', '#F0CCDC', '#84D2C5', '#E4C988', '#C27664', '#B05A7A', '#567189', '#AD8E70', '#C7BCA1', '#C69749', '#B4CDE6', '#829460', '#A084CF', '#F0CCDC', '#F0CCDC', '#F0CCDC', '#F0CCDC', '#F0CCDC', '#F0CCDC'];

            var barData = {
                labels: labels,
                datasets: [{
                    label: 'Attendance Percentage',
                    data: barData,
                    backgroundColor: colorarray.slice(0, labels.length),
                    borderWidth: 1
                }]
            };
            var ctx = document.getElementById("attendanceChart").getContext("2d");
            var pieOptions = {
                legend: {
                    display: false
                },
                tooltips: {
                    callbacks: {
                        label: function (tooltipItem, data) {
                            var label = data.labels[tooltipItem.index] || '';
                            if (label) {
                                label += ': ';
                            }
                            label += 'Percent: ' + attpercent[tooltipItem.index] + '% ';
                            label += 'Present: ' + totalPresentArray[tooltipItem.index] + ', ';
                            label += 'Absent: ' + totalAbsentArray[tooltipItem.index] + ', ';
                            label += 'Total Classes: ' + totalClassesArray[tooltipItem.index];
                            return label;
                        }
                    }
                }
            };
            var pieChart = new Chart(ctx, {
                type: "bar",
                data: barData,
                options: pieOptions
            });
        }
    });

    $('#notificationsModal').on('show.bs.modal', function (event) {
        const modalBody = $('#all-notifications-list');
        modalBody.empty();

        if (typeof allNotifications !== 'undefined' && allNotifications.length > 0) {
            let listHtml = '<ul class="list-unstyled mb-0">';

            allNotifications.forEach(function(n) {
                const date = new Date(n.entrydate);
                const day = String(date.getDate()).padStart(2, '0');
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const year = date.getFullYear();
                const formattedDate = `${day}-${month}-${year}`;

                listHtml += `
                    <li class="mb-3 border-bottom pb-3">
                        <p class="mb-1">${n.notification}</p>
                        <small class="text-muted">Date: ${formattedDate}</small>
                    </li>
                `;
            });

            listHtml += '</ul>';
            modalBody.html(listHtml);
        } else {
            modalBody.html('<p class="text-muted">No notifications found.</p>');
        }
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const cards = document.querySelectorAll('.navigate-to-submit-assignment');
    cards.forEach(function(card) {
        const url = card.getAttribute('data-url');
        card.addEventListener('click', function() {
            if (url) {
                window.location.href = url;
            }
        });
    });
});