package com.syscom.fep.web.controller.batch;

import static javax.swing.SortOrder.ASCENDING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.form.batch.UI_000110_Form;
import com.syscom.fep.web.form.batch.UI_000120_Form;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.mybatis.vo.SyscomGroupVo;

/**
 * UI000110 批次啟動作業
 *
 * @author xingyun_yang
 * @create 2022/1/13
 */
@Controller
public class UI_000110Controller extends BaseController {

    @Autowired
    private BatchService batchService;

    private final static String defaultSortCol = "BATCH_NAME";
    private static final String URL_DO_QUERY = "/batch/UI_000110/doQuery";

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_000110_Form form = new UI_000110_Form();
        form.setUrl(URL_DO_QUERY);
        // 一載入就Query
        // Modify By Matt 2010/06/08
        doQuery(form, mode);
    }

    @PostMapping(value = URL_DO_QUERY)
    private String doQuery(@ModelAttribute UI_000110_Form form, ModelMap mode) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        this.bindGrid(form, mode, defaultSortCol, ASCENDING.name());
        return Router.UI_000110.getView();
    }

    /*
     * 取得批次列表
     */
    private void bindGrid(UI_000110_Form form, ModelMap mode, String sortExpression, String direction) {
        try {
            // 取得當前使用者群組
            List<SyscomGroupVo> syscomGroupVoList = WebUtil.getFromSession(SessionKey.Group);
            // 取得所有batch資料
            PageInfo<HashMap<String, Object>> resultData = this.getResultData(form, mode);

            // 查無資料
            if (resultData.getList().size() == 0) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
                WebUtil.putInAttribute(mode, AttributeName.PageData, resultData);
                return;
            }

            List<HashMap<String, Object>> showResultList = new ArrayList<>();
            for (HashMap<String, Object> result : resultData.getList()) {
                // 批次是否啟用
                boolean startBtn = DbHelper.toBoolean(((Integer) result.get("BATCH_ENABLE")).shortValue());
                // 最近執行時間
                Date batchLastruntime = (Date) result.get("BATCH_LASTRUNTIME");
                // 每天是否只能做一次
                boolean batchSingletime = DbHelper.toBoolean(((Integer) result.get("BATCH_SINGLETIME")).shortValue());
                // 執行結果
                String batchResult = (String) result.get("BATCH_RESULT");
                // 可啓動群組
                String batchStartgroup = (String) result.get("BATCH_STARTGROUP");
                boolean batchCheckbusinessdate = //
                        DbHelper.toBoolean(((Integer) result.get("BATCH_CHECKBUSINESSDATE")).shortValue());
                String batchZone = (String) result.get("BATCH_ZONE");

                // 無可執行群組
                if (StringUtils.isBlank(batchStartgroup)) {
                    continue;
                }

                // 可執行群組無自己所在的群組，則無法執行
                boolean isStartGroup = false;
                // 可啟用群組轉為列表
                List<String> batchStartgroupList = Arrays.asList(batchStartgroup.split(","));
                for (SyscomGroupVo vo : syscomGroupVoList) { // 比對多群組
                    if (batchStartgroupList.contains(vo.getRoleId())) {
                        isStartGroup = true;
                        break;
                    }
                }
                if (!isStartGroup) {
                    continue;
                }

                // 每天是否只能做一次的批次若今日有執行過，則無法執行
                if (batchLastruntime != null && CalendarUtil.equals(batchLastruntime, Calendar.getInstance().getTime(),
                        FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN) && batchSingletime && "1".equals(batchResult)) {
                    startBtn = false;
                }

                // 非營業日 且 無排程，則無法執行
                if (batchCheckbusinessdate && StringUtils.isNotBlank(batchZone)) {
                    startBtn = new BatchJobLibrary().isBsDay(batchZone);
                }

                result.put("startBtn", startBtn);
                if (!startBtn) {
                    result.put("color", "red");
                }
                showResultList.add(result);
            }
            resultData.setList(showResultList);
            WebUtil.putInAttribute(mode, AttributeName.PageData, resultData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, QueryFail);
        }
    }

	private PageInfo<HashMap<String, Object>> getResultData(UI_000110_Form form, ModelMap mode) throws Exception {
		return batchService.getAllBatch(form.getBatchName(), WebConfiguration.getInstance().getSubsys().split(","),
				form.getPageNum(), form.getPageSize());
	}

    @PostMapping(value = "/batch/UI_000110/btnExecute")
    private String btnExecute(UI_000110_Form form, ModelMap mode, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        this.infoMessage("執行批次, 條件 = [", form.toString(), "]");
        Batch def = new Batch();
        def.setBatchBatchid(Integer.parseInt(form.getBatchId()));
        def = batchService.getBatchQueryByPrimaryKey(def);
        if ("0".equals(def.getBatchResult())) {
            this.showMessage(redirectAttributes, MessageType.WARNING, "批次執行中！");
            // 停留在當前頁
            return this.doRedirectForCurrentPage(redirectAttributes, request);
        } else {
            String currentBatchId = form.getBatchId();
            String jobId = form.getBatchStartJobId();
            try {
                BatchJobLibrary batchLib = new BatchJobLibrary();
                batchLib.startBatch(
                        def.getBatchExecuteHostName(),
                        currentBatchId,
                        jobId,
                        1000); // 等待1秒鐘讓批次收到動作更新最近的BATCH_CURRENTID
                this.showMessage(mode, MessageType.INFO, "批次啟動成功");
                // 跳到120畫面
                return showHistory(form, mode);
            } catch (Exception e) {
                this.errorMessage(e, "批次啟動失敗, ", e.getMessage());
                this.showMessage(redirectAttributes, MessageType.DANGER, "批次啟動失敗");
                // 停留在當前頁
                return this.doRedirectForCurrentPage(redirectAttributes, request);
            }
        }
    }

    private String showHistory(UI_000110_Form form, ModelMap mode) {
        UI_000120_Form form120 = new UI_000120_Form();
        form120.setAutoRefresh(form.isAutoRefresh());
        form120.setBatchCurrentId(form.getBatchCurrentId());
        form120.setBatchId(form.getBatchId());
        form120.setBatchStartJobId(form.getBatchStartJobId());
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        // 重新查詢一次批次的資料
        Batch def = new Batch();
        def.setBatchBatchid(Integer.valueOf(form.getBatchId()));
        def = batchService.getBatchQueryByPrimaryKey(def);
        if (def != null) {
            form.setBatchCurrentId(def.getBatchCurrentid());
            mode.addAttribute("BATCH_NAME", def.getBatchName());
            mode.addAttribute("BATCH_DESCRIPTION", def.getBatchDescription());
            mode.addAttribute("BATCH_CURRENTID", def.getBatchCurrentid());
            String result = " ";
            if ("0".equals(def.getBatchResult())) {
                result = "執行中";
            } else if ("1".equals(def.getBatchResult())) {
                result = "成功";
            } else if ("2".equals(def.getBatchResult())) {
                result = "失敗";
            }
            mode.addAttribute("BATCH_RESULT", result);
        }
        mode.addAttribute("testTimeLbl", new Date());
        // 跳到120畫面
        return Router.UI_000120.getView();
    }
}
