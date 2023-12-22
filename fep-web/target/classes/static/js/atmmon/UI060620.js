var formId = "form-validator";

var refreshTime = document.getElementById("time").value > 0 && document.getElementById("time").value <= 60 ? document.getElementById("time").value : 30;

$(document).ready(function() {
	initDatePicker('dtTransactDate');
	initDateTimePicker('txTransactTimeBEG', 'HH:mm');
	initDateTimePicker('txTransactTimeEND', 'HH:mm');
	// 按下儲存按鈕
	$('#btnReserve').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});

	// Grid中第一列查詢按鈕
	$('.a-start').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060620_A/inquiryDetail', form);
	});

	// Grid中最後一列查詢按鈕
	$('.a-end').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/atmmon/UI_060620_B/inquiryDetail', form);
	});
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