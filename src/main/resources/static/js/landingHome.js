$(document).ready(function(){
    console.log("Document ready. jQuery version: " + ($.fn.jquery || 'Not loaded'));
    if (typeof $.fn.slick === 'function') {
        console.log("Slick function IS available.");
        if ($('.hero-slider').length > 0) {
            console.log("'.hero-slider' element found. Attempting to initialize.");
            try {
                // $('.hero-slider').slick({ dots: true, arrows: true });
                // console.log("Minimal Slick initialized. Check if slides are no longer stacked.");
            } catch (e) {
                console.error("Error during Slick test initialization:", e);
            }
        } else {
            console.error("'.hero-slider' element NOT found by jQuery.");
        }
    } else {
        console.error("Slick function ($.fn.slick) is NOT available. Check slick.min.js loading and order.");
    }
});

function getmoreongoingfunc() {
    $.ajax({
        type: "POST",
        url:  "/nerie/program-details/getMoreOngoingProgramList",
        success: function (data) {
            var lst;
            lst = '<ul class="content_list tenders-list" >';
            $.each(data, function (k, v) {
                const reportUrl = '/nerie/reports/publicReport?phaseid=' + v[0];
                lst += '<li class="custommodallink mb-3">' +
                        '<i class="fa fa-arrow-right mr-2" aria-hidden="true"></i>' +
                        '<a style="color: #000000" href="' + reportUrl + '"  target="_blank">' + v[11] + ': ' + v[2] + ' started on ' + v[8] + ' at ' + v[9] +
                        '</a></li>';
            });
            lst += '</ul>';

            $("#titlespan").html("Ongoing Programs");
            $("#programlistdiv").html(lst);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching ongoing programs: " + textStatus + " - " + errorThrown);
            alert("Error fetching ongoing programs. Please try again later.");
        }
    });
}

function getmoreupcomingfunc() {
    $.ajax({
        type: "POST",
        url: "/nerie/program-details/getMoreUpcomingProgramList",
        success: function (data) {
            var lst;
            lst = '<ul class="content_list tenders-list" >';
            $.each(data, function (k, v) {
                const reportUrl = '/nerie/reports/publicReport?phaseid=' + v[0];
                lst += '<li class="custommodallink mb-3">' +
                        '<i class="fa fa-arrow-right mr-2" aria-hidden="true"></i>' +
                        '<a style="color: #000000" href="' + reportUrl + '"  target="_blank" >' + v[11] + ': ' + v[2] + ' started on ' + v[9] + ' at ' + v[10] +
                        '</a></li>';
            });
            lst += '</ul>';
            $("#titlespan").html("Upcoming Programs");
            $("#programlistdiv").html(lst);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching upcoming programs: " + textStatus + " - " + errorThrown);
            alert("Error fetching upcoming programs. Please try again later.");
        }
    });
}

function getmorecompletedfunc() {
    $.ajax({
        type: "POST",
        url: "/nerie/program-details/getMoreCompletedProgramList",
        success: function (data) {
            var lst;
            lst = '<ul class="content_list tenders-list" >';
            $.each(data, function (k, v) {
                const reportUrl = '/nerie/reports/publicReport?phaseid=' + v[0];
                lst += '<li class="mb-3 custommodallink">' +
                        '<i class="fa fa-arrow-right mr-2" aria-hidden="true"></i>' +
                        '<a style="color: #000000" href="' + reportUrl + '"  target="_blank">' + v[11] + ': ' + v[2] + ' started on ' + v[9] + ' at ' + v[10] +
                        '</a></li>';
            });
            lst += '</ul>';
            $("#titlespan").html("Completed Programs");
            $("#programlistdiv").html(lst);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.error("Error fetching completed programs: " + textStatus + " - " + errorThrown);
            alert("Error fetching completed programs. Please try again later.");
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll(".btn-view-more.moreongoingbtn").forEach(function(link) {
        link.addEventListener("click", function(event) {
            event.preventDefault();
            var functionName = link.getAttribute("data-function");
            if (functionName && typeof window[functionName] === "function") {
                window[functionName]();
            } else {
                console.error(`Function ${functionName} is not defined or data-function attribute is missing.`);
            }
        });
    });

    document.querySelectorAll(".btn-view-more.upcomingProgramsBtn").forEach(function(btn) {
        btn.addEventListener("click", function(event) {
            event.preventDefault();
            var functionName = btn.getAttribute("data-function");
            if (functionName && typeof window[functionName] === "function") {
                window[functionName]();
            } else {
                console.error(`Function ${functionName} is not defined or data-function attribute is missing.`);
            }
        });
    });

    document.querySelectorAll(".btn.btn-view-more.view-more-completed").forEach(function (link) {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            if (typeof getmorecompletedfunc === "function") {
                getmorecompletedfunc();
            } else {
                console.error("Function getmorecompletedfunc is not defined");
            }
        });
    });
});
