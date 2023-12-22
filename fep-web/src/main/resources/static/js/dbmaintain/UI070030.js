var formId = "form-validator";

$(document).ready(function() {
	initDatePicker('INBKPARM_EFFECT_DATE');


	// 按下查詢按鈕
	$('#btnQuery').click(function() {
		if (doValidateForm(formId)) {
			showLoading(true);
			showProcessingMessage(true);
			$('#' + formId).submit();
		}
	});
	$('#btnDelete').click(function() {
		if (isTableColumnChecked("db", '至少勾選表格中的一筆資料')) {
			var data = getTableColumnCheckedData("db");
			doAjax(data, "/dbmaintain/UI_070030/btnDelete", false, true, function(resp) {
				if ('undefined' !== typeof resp) {
					showSuccessCmnAlert(resp.message, function() {
						// 刪除成功重新查詢主頁資料
						if (resp.result) {
							doFormSubmit('/currentPageAjax', resp, false);
						}
					});
				}
			});
		}
	});



	//按下新增按鈕
	$('#btnInsert').click(function() {
		var form = {
			btnType: "insert",
		}
		doFormSubmit('/dbmaintain/UI_070030/showDetail', form);
	});
	// Grid中第二列修改按鈕
	$('.btn-inquiry').click(function() {
		var value = $(this).attr("value");
		var form = jsonStringToObj(value);
		doFormSubmit('/dbmaintain/UI_070030/showDetail', form);
	});

})