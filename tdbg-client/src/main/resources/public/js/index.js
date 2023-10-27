$(function () {

    CodeMirror.fromTextArea($("#query").get(0), {
        mode: "application/x-cypher-query",
        theme: "default",
        lineNumbers: true,
        height: "auto"
    });

    $('#query-form')
        .on('submit', function (event) {
            event.preventDefault();
            $.ajax({
                url: '/query',
                dataType: 'json',
                data: $(this).serialize(),
                type: 'POST',
                success: handleAjax,
                error: error
            });
        })
    ;
});

$(document).ajaxStart(function () {

    $('body').addClass('wait');

}).ajaxStop(function () {

    $('body').removeClass('wait');

});

function error(responseObject) {
    showMessage(responseObject.responseJSON.title);
}

function handleAjax (response) {

    if (tableAlreadyInitialized()) {
        destroyTable();
    }

    if (response.success === true) {
        hideMessage();
        if (response.data.length === 0) {
            showMessage("No results found. " + elapsedTimeMessage(response));
        }
        else {
            showMessage(elapsedTimeMessage(response));
            initializeTable(response.data);
        }
    } else {
        showMessage(response.message);
    }
}

function showMessage(message) {
    hideTable();
    let messageDiv = $("#message");
    messageDiv.show();
    messageDiv.html(message);
}

function elapsedTimeMessage(response) {
    return "Translation time: " + response.translationTime + ". Execution time: " + response.executionTime
}

function hideMessage() {
    $("#message").hide();
}

function hideTable() {
    $("table").hide();
}

function tableAlreadyInitialized() {
    return $.fn.DataTable.isDataTable('#query-result-table');
}

function initializeTable(data) {

    if (data.length === 0) {
        return;
    }

    let table = $("#query-result-table");
    let columns = [];
    let columnNames = Object.keys(data[0]);
    for (let i in columnNames) {
        let dataColumn = columnNames[i];

        if (dataColumn.includes(".")) { // Escape dots, otherwise it treats it as objects
            dataColumn = dataColumn.replace(/\./g, "\\.")
        }

        columns.push({
            data: dataColumn,
            title: columnNames[i]
        });
    }

    let dataTable = table.DataTable({
        autoWidth: false,
        data: data,
        columns: columns,
        columnDefs: [
            {
                "render": function (data, _type, _row) {
                    return '<pre>'+ JSON.stringify(data, null, 2) +'</pre>';
                },
                "targets": "_all"
            }
        ]
    });
    table.show();
    dataTable.columns.adjust().draw();
}

function destroyTable() {
    let table = $('#query-result-table');
    table.DataTable().destroy();
    table.empty();
}
