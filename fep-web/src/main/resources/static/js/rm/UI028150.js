var formId = "form-validator";

var refreshTime = document.getElementById("time").value > 0 && document.getElementById("time").value <= 60 ? document.getElementById("time").value : 30;

$(document).ready(function () {
    // 按下儲存按鈕
    $('#btnReserve').click(function () {
        if (doValidateForm(formId)) {
            showLoading(true);
            showProcessingMessage(true);
            $('#' + formId).submit();
        }
    });
    // 建立表單驗證
    $('#' + formId).validate(getValidFormOptinal({
        rules: {
            time: {
                required: true,
                number: true,
            },
        },
        messages: {
            time: {
                required: "畫面更新間隔時間不能為空",
                number: "請輸入數字",
            }
        }
    }));
})

// Ajax實現定時刷新頁面
function myrefresh() {
    if (doValidateForm(formId)) {
        showLoading(true);
        showProcessingMessage(true);
        $('#' + formId).submit();
    }
}

//指定refreshTime秒刷新一次
setInterval(myrefresh, refreshTime * 1000);