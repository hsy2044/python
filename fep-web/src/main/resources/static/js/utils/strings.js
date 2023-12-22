/**
 * 判斷一個字串是否是空白字串, 包括空格、製表符、換頁符
 *
 * @param str
 */
function stringIsBlank(str) {
	return (str === null) || ('undefined' === typeof str) || (str.toString().replace(/(^s*)|(s*$)/g, "").length == 0);
}

function stringIsNotBlank(str) {
	return $.trim(str) != '';
}