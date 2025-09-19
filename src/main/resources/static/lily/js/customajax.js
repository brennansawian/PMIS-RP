function callCustomAjax(url, data, success)
{
    $.ajax({
        type: "POST",
        url: url,
        data: data,
        success: function (data) {
            success(data);
        },
        error: (jqXHR, textStatus, errorThrown) => handleErrorResponse(jqXHR, textStatus, errorThrown)
    });

}

function callCustomAjaxasync(url, data, success)
{
    $.ajax({
        type: "POST",
        async: false,
        url: url,
        data: data,
        success: function (data) {
            success(data);
        },
        error: (jqXHR, textStatus, errorThrown) => handleErrorResponse(jqXHR, textStatus, errorThrown)
    });
}

function handleErrorResponse(jqXHR, textStatus, errorThrown) {
    switch (jqXHR.status) {
        case 400: // Bad Request
            alert(`[400] BAD REQUEST: ${jqXHR.responseText}`)
            break
        case 401: // Unauthorized
            window.location.href = '/nerie/error/401'
            break
        case 404:   // Not Found
            window.location.href = '/nerie/error/404'
            break
        case 500: // Server Error
            if (jqXHR.responseText)
                window.location.href = `/nerie/error/500?message=${jqXHR.responseText}`
            break
        default:
            alert("error:" + textStatus + " - exception:" + errorThrown);
    }
}

function initdatatable(tname, rname) {
    return $('#' + tname).DataTable({
        dom: 'Blrftip',
        retrieve: true,
        pageLength: 10,
        lengthMenu: [[5, 10, 20, 50, -1], [5, 10, 20, 50, "All"]],
        buttons: [
            {
                extend: 'excel',
                text: '<em><i class="fa fa-file-excel-o">Excel</i></em>',
                title: function () {
                    return rname;
                },
                exportOptions: {
                    columns: "thead th:not(.noExport)"
                }
            },
            {
                extend: 'pdf',
                title: function () {
                    return rname;
                },
                orientation: 'portrait',
                pageSize: 'A4',
                text: '<i class="fa fa-file-pdf-o"> PDF</i>',
                titleAttr: 'PDF',
                exportOptions: {
                    columns: "thead th:not(.noExport)"
                }
            }
        ]
    });
}



