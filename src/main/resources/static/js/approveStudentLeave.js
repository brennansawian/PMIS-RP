function myFunction(x) {
    x.classList.toggle("change");
}

$(document).ready(function () {
    $('#leavelist').DataTable({
        dom: 'lfrtip',
        pageLength: 10,
        lengthMenu: [[10, 25, 50, -1], [10, 25, 50, "All"]],
        language: {
            emptyTable: "No student leave records."
        }
    });
});