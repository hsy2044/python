package com.syscom.fep.web.controller.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.WebauditdescExtMapper;
import com.syscom.fep.mybatis.mapper.WebauditMapper;
import com.syscom.fep.mybatis.model.Webaudit;
import com.syscom.fep.mybatis.model.Webauditdesc;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.common.UI_080070_Form;
import com.syscom.fep.web.form.common.UI_080070_FormDetail;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;

/**
 * For Safeaa
 *
 * @author ChenYang
 */
@Controller
public class UI_080070Controller extends BaseController {

	@Autowired
	private CommonService commonService;
	@Autowired
	WebauditMapper webauditMapper;
	@Autowired
	WebauditdescExtMapper webauditdescMapper;

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_080070_Form form = new UI_080070_Form();
		try {
			form.setLogTimeBegin(
					FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
			form.setLogTimeBeginTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HH_MM));
			form.setLogTimeEnd(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
			form.setLogTimeEndTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HH_MM));
			SetQueryKeyWord(form);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}

		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}

	@PostMapping(value = "/common/UI_080070/doQuery")
	public String doInquiryMain(@ModelAttribute UI_080070_Form form, ModelMap mode) throws Exception {
		try {
			this.infoMessage(String.format("查詢條件 = [%s]", form.toString()));
			this.doKeepFormData(mode, form);
			bindGridData(form, mode);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_080070.getView();
	}

	private void bindGridData(UI_080070_Form form, ModelMap mode) throws Exception {
		// 準備資料庫查詢參數
		Webaudit audit = new Webaudit();
		audit.setAudituser(WebUtil.getUser().getLoginId());
		audit.setAuditprogramname(form.getTxtProgram_Name());
		SetQueryKeyWord(form);

		// 取得使用者軌跡資料
		PageInfo<HashMap<String, Object>> dt = commonService.queryAllAuditData( //
				audit, //
				form.getDtBegin(), //
				form.getDtEnd(), //
				StringUtils.EMPTY, // 合庫系統使用者無區分上下級
				form.getTxtProgram_ID(), //
				form.getDisplayShowAudit(), //
				form.getPageNum(), //
				form.getPageSize() //
		);

		PageData<UI_080070_Form, HashMap<String, Object>> pageData = null;
		if (dt != null) {
			List<HashMap<String, Object>> dataList = ResultGrdv_RowDataBound(dt.getList());
			if (null != dataList && dataList.size() > 0) {
				pageData = new PageData<>(dt, form);
			} else {
				pageData = new PageData<>(new PageInfo<HashMap<String, Object>>(), form);
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			}
		} else {
			pageData = new PageData<>(new PageInfo<HashMap<String, Object>>(), form);
			this.showMessage(mode, MessageType.INFO, QueryNoData);
		}
		WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
	}

	private List<HashMap<String, Object>> ResultGrdv_RowDataBound(List<HashMap<String, Object>> dataList)
			throws Exception {
		StringBuilder sb = new StringBuilder(10);

		for (int i = 0; i < dataList.size(); i++) {
			Map<String, Object> foreachmap = dataList.get(i);
			Map<String, String> map = new HashMap<String, String>();
			String auditProgramId = (String) foreachmap.get("auditProgramId");

			if (StringUtils.isNotBlank(auditProgramId)) {
				List<Webauditdesc> list = webauditdescMapper.selectByProgramid(auditProgramId);
				if (list != null) {
					for (Webauditdesc webauditdesc : list) {
						map.put(webauditdesc.getControlid(), webauditdesc.getControlname());
					}
				}
				String data = String.valueOf(foreachmap.get("webAudit_AuditData"));
				if (StringUtils.isNotBlank(data)) {
					String[] strList = data.split("&");
					for (String str : strList) {
						String[] arr = str.split("=");
						if (map.containsKey(arr[0])) {
							if (arr.length > 1) {
								sb.append(map.get(arr[0]));
								sb.append("=").append(arr[1]).append(",");
							}
						}
					}
				}
			}
			if (StringUtils.isNotBlank(sb)) {
				dataList.get(i).put("webAudit_AuditData", sb.toString());
			} else {
				dataList.get(i).put("webAudit_AuditData", "無欄位輸入資料");
			}
		}

		return dataList;
	}

	@PostMapping(value = "/common/UI_080070/inquiryDetail")
	public String doInquiryDetail(@ModelAttribute UI_080070_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);

		List<HashMap<String, String>> returnList = new ArrayList<HashMap<String, String>>();
		Map<String, String> map = new HashMap<String, String>();

		try {
			if (StringUtils.isNotBlank(form.getAuditNo())) {
				Webaudit webaudit = webauditMapper.selectByPrimaryKey(Long.valueOf(form.getAuditNo()));
				if (webaudit != null) {
					List<Webauditdesc> list = webauditdescMapper.selectByProgramid(webaudit.getProgramid());
					for (Webauditdesc webauditdesc : list) {
						map.put(webauditdesc.getControlid(), webauditdesc.getControlname());
					}
					String data = webaudit.getAuditdata();
					if (StringUtils.isNotBlank(data)) {
						String[] strList = data.split("&");
						for (String str : strList) {
							String[] arr = str.split("=");
							if (map.containsKey(arr[0])) {
								Map<String, String> returnmap = new HashMap<String, String>();
								returnmap.put("key", map.get(arr[0]));
								if (arr.length > 1) {
									returnmap.put("value", arr[1]);
								} else {
									returnmap.put("value", "");
								}
								returnList.add((HashMap<String, String>) returnmap);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			this.showMessage(mode, MessageType.DANGER, QueryFail);
		}

		WebUtil.putInAttribute(mode, AttributeName.DetailMap, returnList);
		return Router.UI_080070_Detail.getView();
	}

	private void SetQueryKeyWord(UI_080070_Form form) throws Exception {
		if (StringUtils.isNotBlank(form.getLogTimeBegin()) && StringUtils.isNotBlank(form.getLogTimeBeginTime())) {
			form.setDtBegin(form.getLogTimeBegin() + " " + form.getLogTimeBeginTime());
		}

		// 沒帶時間，日期要多一天
		if (StringUtils.isNotBlank(form.getLogTimeEnd())) {
			if (StringUtils.isBlank(form.getLogTimeEndTime())) {
				Date date = FormatUtil.parseDataTime(form.getLogTimeEnd(), FormatUtil.FORMAT_DATE_YYYY_MM_DD);
				Calendar rightNow = Calendar.getInstance();
				rightNow.setTime(date);
				rightNow.add(Calendar.DAY_OF_YEAR, 1);
				form.setLogTimeEnd(FormatUtil.dateTimeFormat(rightNow, FormatUtil.FORMAT_DATE_YYYY_MM_DD));
				form.setDtEnd(form.getLogTimeEnd() + " ");
			} else {
				form.setDtEnd(form.getLogTimeEnd() + " " + form.getLogTimeEndTime());
			}

		}
	}

}
