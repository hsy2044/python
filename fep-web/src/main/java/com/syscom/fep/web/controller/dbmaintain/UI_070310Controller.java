package com.syscom.fep.web.controller.dbmaintain;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.model.Bin;
import com.syscom.fep.mybatis.model.Inbkparm;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.dbmaintain.UI_070310_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 跨行系統參數維護
 *
 * @author Joseph
 * @create 2022/05/23
 */
@Controller
public class UI_070310Controller extends BaseController {

	@Autowired
	private InbkService inbkService;
	@SuppressWarnings("unused")
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_070310_Form form = new UI_070310_Form();
		form.setUrl("/dbmaintain/UI_070310/queryClick");
		this.queryClick(form, mode);
	}

	@PostMapping(value = "/dbmaintain/UI_070310/queryClick")
	private String queryClick(@ModelAttribute UI_070310_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		BindGridData(form, mode);
		return Router.UI_070310.getView();
	}

	@PostMapping(value = "/dbmaintain/UI_070310/btnDelete")
	@ResponseBody
	public BaseResp<UI_070310_Form> btnDelete(@RequestBody List<UI_070310_Form> list) {
		this.infoMessage("執行刪除動作, 條件 = [", list.toString(), "]");
		BaseResp<UI_070310_Form> response = new BaseResp<>();
		try {
			for (UI_070310_Form form : list) {
				BinExt bin = new BinExt();
				bin.setBinNo(form.getBinNo());
				bin.setBinBkno(form.getBinBkno());

				inbkService.deleteBin(bin.getBinNo(),bin.getBinBkno(),WebUtil.getFepuser().getFepuserUserid());
			}
			response.setMessage(MessageType.INFO, DeleteSuccess);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}
	@PostMapping(value = "/dbmaintain/UI_070310/showDetail")
	private String showDetail(@ModelAttribute UI_070310_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		form.setBtnType(nullToEmptyStr(form.getBtnType()));
		form.setBinNo(nullToEmptyStr(form.getBinNo()));
		form.setBinBkno(nullToEmptyStr(form.getBinBkno()));
		form.setBinNet(nullToEmptyStr(form.getBinNet()));
		form.setBinZone(nullToEmptyStr(form.getBinZone()));
		form.setBinOrg(nullToEmptyStr(form.getBinOrg()));
		form.setBinProd(nullToEmptyStr(form.getBinProd()));
		if (!"insert".equals(form.getBtnType())) {
			Bin bin = bindFormViewData(form.getBinNo(),form.getBinBkno());
			form.setBinNo(nullToEmptyStr(bin.getBinNo()));
			form.setBinBkno(nullToEmptyStr(bin.getBinBkno()));
			form.setBinNet(nullToEmptyStr(bin.getBinNet()).trim());
			form.setBinZone(nullToEmptyStr(bin.getBinZone()).trim());
			form.setBinOrg(nullToEmptyStr(bin.getBinOrg()).trim());
			form.setBinProd(nullToEmptyStr(bin.getBinProd()).trim());
		}
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		return Router.UI_070310_Detail.getView();
	}

	private Bin bindFormViewData(String binno, String binbkno) {
		return inbkService.getBinDataByPK(binno,binbkno);
	}
	@PostMapping(value = "/dbmaintain/UI_070310/saveClick")
	private String saveClick(@ModelAttribute UI_070310_Form dform, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		this.infoMessage("參數檔案維護, 條件= [", dform.toString(), "]");
		BinExt bin = new BinExt();
		Integer iRes;
		if (this.checkAllField(bin, dform, redirectAttributes)) {
			if ("insert".equals(dform.getBtnType())) {
				iRes = inbkService.insertBIN(bin);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, InsertFail);
				}
			} else {
				iRes = inbkService.updateBIN(bin);
				if (iRes == 1) {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateSuccess);
					return this.doRedirectForPrevPage(redirectAttributes, request);
				} else {
					this.showMessage(redirectAttributes, MessageType.INFO, UpdateFail);
				}
			}
		}
		return this.doRedirectForCurrentPage(redirectAttributes, request);
	}
	private boolean checkAllField(Bin bin, UI_070310_Form dform, RedirectAttributes redirectAttributes) {
		try {
			bin.setBinNo(dform.getBinNo());
			if(dform.getBinNo().length() < 6){
				this.showMessage(redirectAttributes, MessageType.WARNING, "欄位「CREDIT CARD BIN」少於6碼");
				return false;
			}
			bin.setBinBkno(dform.getBinBkno());
			bin.setBinZone(dform.getBinZone());
			bin.setBinNet(dform.getBinNet());
			bin.setBinOrg(dform.getBinOrg());
			bin.setBinProd(dform.getBinProd());
			bin.setUpdateUserid(WebUtil.getFepuser().getFepuserUserid());

			return true;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(redirectAttributes, MessageType.WARNING, programError);
			return false;
		}
	}

	public void BindGridData(UI_070310_Form form, ModelMap mode) {
		PageInfo<Bin> pageInfo = null;
		try {
			String BINNO = form.getBinNo();
			String BINBKNO= form.getBinBkno();
		   
			if (StringUtils.isBlank(form.getBinNo())
					&& StringUtils.isBlank(form.getBinBkno())) {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
						form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
									inbkService.getBinAll();
							}
						});
			} else {
				pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
						form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
							@Override
							public void doSelect() {
									inbkService.getBinByPK(BINNO,BINBKNO);
							}
						});
			}
			if (pageInfo.getList() == null || pageInfo.getList().size() == 0) {
				this.showMessage(mode, MessageType.WARNING, QueryNoData);
				return;
			} else {
				this.clearMessage(mode);
				PageData<UI_070310_Form, Bin> pageData = new PageData<>(pageInfo, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}

		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
}
