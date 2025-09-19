$(document).ready(function () {
    try {
        var table = $('#audittable').DataTable({
            "paging": false,
            "searching": false,
            "ordering": false,
            "info": false,

            dom: 'Brt',

            buttons: [
                {
                    extend: 'excelHtml5',
                    text: '<i class="fa fa-file-excel-o"></i> Excel',
                    title: 'Audit Trail Report',
                    exportOptions: {
                        columns: ':visible:not(.noExport)'
                    },
                    className: 'btn btn-sm btn-outline-success'
                }
            ],
            responsive: true
        });

        table.buttons().container().appendTo('#export-buttons-container');

        console.log("DataTables initialized for #audittable with custom controls.");
    } catch(e) {
         console.error("Error initializing DataTables:", e);
    }
});