$(document).ready(function () {
  $('#approveprogram').DataTable({
    dom: 'Blfrtip',
    pageLength: 5,
    lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
    buttons: [
      {
        extend: 'excelHtml5',
        text: '<i class="fa fa-file-excel-o"></i> Excel',
        title: 'Overall Feedback List',
        exportOptions: {
          columns: ':visible:not(.noExport)'
        },
        className: 'btn btn-sm btn-outline-success'
      }
    ]
  });
});