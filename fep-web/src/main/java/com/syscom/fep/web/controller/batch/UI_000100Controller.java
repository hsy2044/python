package com.syscom.fep.web.controller.batch;

import static javax.swing.SortOrder.ASCENDING;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.configurer.BatchBaseConfigurationHost;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Jobs;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.form.batch.UI_000100_Detail_Form;
import com.syscom.fep.web.form.batch.UI_000100_Form;
import com.syscom.fep.web.form.batch.UI_000100_Main_Form;
import com.syscom.fep.web.form.batch.UI_000100_Task_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import com.syscom.safeaa.mybatis.vo.SyscomroleAndCulture;

/**
 * For System Monitor
 *
 * @author ZK
 */
@Controller
public class UI_000100Controller extends BaseController {
    private static final String URL_DO_QUERY = "/batch/UI_000100/queryClick";
    String[] gridDataKeyNames = {"BATCH_BATCHID"};
    String defaultSortCol = "BATCH_NAME";
    String subsys;
    Batch batch = new Batch();
    String detail = "";
    String batchName = "";

    @Autowired
    private BatchService obj;
    @Autowired
    private BatchBaseConfiguration batchBaseConfiguration;
    @Autowired
    private CommonService commonService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        subsys = WebConfiguration.getInstance().getSubsys();
        UI_000100_Form form = new UI_000100_Form();
        form.setUrl(URL_DO_QUERY);
        // 一載入就Query
        // Modify By Matt 2010/06/08
        this.queryClick(form, mode);
    }

    private void bindGrid(UI_000100_Form form, ModelMap mode, RefBase<PageInfo<HashMap<String, Object>>> dt, String sortExpression, String direction) {
        try {
            // if (dt == null) {
            dt = new RefBase<>(new PageInfo<>());
            dt.set(getResultData(form, mode));
            // }
            if (dt.get().getList().size() == 0) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            }
            WebUtil.putInAttribute(mode, AttributeName.PageData, dt.get());
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, QueryFail);
        }
    }

    private PageInfo<HashMap<String, Object>> getResultData(UI_000100_Form form, ModelMap mode) {
        PageInfo<HashMap<String, Object>> dt = null;
        try {
            String[] strings = subsys.split(",");
            dt = obj.getAllBatch(form.getBatchName(), strings, form.getPageNum(), form.getPageSize());
            return dt;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, QueryFail);
            return null;
        }
    }

    @PostMapping(value = URL_DO_QUERY, produces = "application/json;charset=utf-8")
    public String queryClick(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        batchName = form.getBatchName();
        this.bindGrid(form, mode, null, defaultSortCol, ASCENDING.name());
        return Router.UI_000100.getView();
    }

    @PostMapping(value = "/batch/UI_000100/queryDetails", produces = "application/json;charset=utf-8")
    public String queryDetails(@ModelAttribute UI_000100_Form form, ModelMap mode) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        if (StringUtils.isBlank(form.getBatchId())) {
            detail = "insert";
        } else {
            detail = "update";
            batch = obj.getBatchByID(Integer.parseInt(form.getBatchId()));
        }
        List<SelectOption<String>> options = new ArrayList<>();
        List<String> apHostNameList = batchBaseConfiguration.getHost().stream().map(BatchBaseConfigurationHost::getName).collect(Collectors.toList());
        options.add(new SelectOption<String>(" ", ""));
        for (String apHostName : apHostNameList) {
            options.add(new SelectOption<String>(apHostName, apHostName));
        }
        mode.addAttribute("options", options);
        return Router.UI_000100_Detail.getView();
    }

    @PostMapping(value = "/batch/UI_000100/delClick", produces = "application/json;charset=utf-8")
    public String delClick(String[] delChecks, ModelMap mode) {
        UI_000100_Form form = new UI_000100_Form();
        form.setBatchName(batchName);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        try {
            int iFaultCount = 0;
            StringBuilder sbFaultPK = new StringBuilder();
            int iRes = 0;
            if (delChecks != null) {
                for (String gr : delChecks) {
                    iRes = obj.deleteBatch(Integer.parseInt(gr));
                    if (iRes != 1) {
                        iFaultCount = iFaultCount + 1;
                        sbFaultPK.append(gr + ", ");
                    }
                }
            } else {
                this.bindGrid(form, mode, null, defaultSortCol, ASCENDING.name());
                this.showMessage(mode, MessageType.WARNING, "請選擇批次");
                return Router.UI_000100.getView();
            }
            // 更新畫面
            this.bindGrid(form, mode, null, defaultSortCol, ASCENDING.name());
            // 回應訊息
            if (iFaultCount == 0) {
                this.showMessage(mode, MessageType.INFO, DeleteSuccess);
            } else {
                this.showMessage(mode, MessageType.DANGER, DeleteFail);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000100.getView();
    }

	@PostMapping(value = "/batch/UI_000100/details")
	@ResponseBody
	public UI_000100_Detail_Form details() {
		UI_000100_Detail_Form form = new UI_000100_Detail_Form();
		try {
			form.setDetail(detail);
			form.setSubsys(obj.getSubsysAll());
			// 設置可啓動群組
			form.setStartGroup(this.bindGroupListBox());
			if (detail.equals("update")) {
				List<HashMap<String, String>> mList = new ArrayList<>();
				for (int i = 1; i < 13; i++) {
					HashMap<String, String> hashMap = new HashMap<>();
					hashMap.put("value", ((int) Math.pow(2, i - 1)) + "");
					hashMap.put("name", i + "月");
					mList.add(hashMap);
				}
				List<HashMap<String, String>> mdList = new ArrayList<>();
				for (int i = 1; i < 32; i++) {
					HashMap<String, String> hashMap = new HashMap<>();
					hashMap.put("value", i + "");
					hashMap.put("name", i + "日");
					mdList.add(hashMap);
				}
				if (batch.getBatchScheduleStarttime() != null) {
					SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat time = new SimpleDateFormat("HH:mm");
					form.setDate(date.format(batch.getBatchScheduleStarttime()));
					form.setTime(time.format(batch.getBatchScheduleStarttime()));
				}
				form.setTaskList(obj.getTaskAll());
				form.setTasks(bindJobGrid());
				if (StringUtils.isNotBlank(batch.getBatchScheduleType()) && batch.getBatchScheduleType().equals("O")) {
					form.setRadioType("mw");
				} else {
					form.setRadioType("m");
				}
				form.setMdList(mdList);
				form.setmList(mList);
				form.setBatch(batch);
			}
		} catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
			form.setMessage(MessageType.DANGER, QueryFail);
		}
		return form;
	}

    private List<HashMap<String, String>> bindGroupListBox() throws Exception {
        List<SyscomroleAndCulture> roleList = commonService.getAllRoles();
        List<HashMap<String, String>> list = new ArrayList<>();
        for (SyscomroleAndCulture role : roleList) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", role.getRoleno() + "-" + role.getRolename());
            hashMap.put("value", Integer.toString(role.getRoleid()));
            list.add(hashMap);
        }
        return list;
    }

    @PostMapping(value = "/batch/UI_000100/saveClick")
    @ResponseBody
    public BaseResp<?> saveClick(@RequestBody UI_000100_Main_Form form) {
        BaseResp<?> response = new BaseResp<>();
        Batch batch1 = form.getBatch();
        int iRes = 0;
        String userId = WebUtil.getUser().getUserId();
        try {
            if (checkAllBatchField(batch1, response, form)) {
                if (detail.equals("insert")) {
                    iRes = obj.insertBatch(batch1);
                    if (iRes > 0) {
                        // 新增成功後,將Gridview帶到那一頁並選取該筆
                        // LookupNewRowInGrid(iRes)
                        // 連續新增模式,清除單筆表單控制項內容
                        response.setMessage(MessageType.INFO, InsertSuccess);
                    } else {
                        response.setMessage(MessageType.DANGER, InsertFail);
                    }
                } else {
                    iRes = obj.updateBatch(batch1, userId);
                    // 更新DB成功後通知批次服務建立此Batch的Task
                    if (batch1.getBatchSchedule().intValue() == 1) {
                        // 如果hostName有異動, 則要先通知原本的主機刪除排程中的Task
                        if (StringUtils.isBlank(batch.getBatchExecuteHostName()) && StringUtils.isNotBlank(batch1.getBatchExecuteHostName())
                                || StringUtils.isNotBlank(batch.getBatchExecuteHostName()) && !batch.getBatchExecuteHostName().equals(batch1.getBatchExecuteHostName())) {
                            obj.deleteScheduleTask(batch.getBatchExecuteHostName(), batch1.getBatchBatchid().intValue(), batch1.getBatchName());
                        }
                        obj.createScheduleTask(batch1.getBatchBatchid().intValue());
                    } else {
                        obj.deleteScheduleTask(batch.getBatchExecuteHostName(), batch1.getBatchBatchid().intValue(), batch1.getBatchName());
                    }
                    if (iRes == 1) {
                        batch = batch1;
                        // 修改成功後,將Gridview帶到那一頁並選取該筆
                        // LookupNewRowInGrid()
                        response.setMessage(MessageType.INFO, UpdateSuccess);
                    } else {
                        response.setMessage(MessageType.DANGER, UpdateFail);
                    }
                }
            }
            return response;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
            return response;
        }
    }

    private boolean checkAllBatchField(Batch batch, BaseResp<?> response, UI_000100_Main_Form form) {
        try {
            if (!"update".equals(detail)) {
                batch.setBatchBatchid(null);
            }
            if (batch.getBatchSubsys().intValue() == 0) {
                response.setMessage(MessageType.DANGER, "未輸入系統別");
                return false;
            }
            if (batch.getBatchCheckbusinessdate() == 1 && StringUtils.isBlank(batch.getBatchZone())) {
                response.setMessage(MessageType.DANGER, "檢核營業日必須輸入地區別");
                return false;
            }
            batch.setBatchEditgroup("");
            batch.setBatchStartgroup(form.getGroupChk());
            if (detail.equals("update")) {
                if (form.getJobId() == 0) {
                    batch.setBatchStartjobid(0);
                } else {
                    batch.setBatchStartjobid(form.getJobId());
                }
            } else {
                batch.setBatchStartjobid(0);
            }
            if (detail.equals("update") && batch.getBatchSchedule() == 1) {
                if (batch.getBatchScheduleType().equals("M") && form.getRadioType().equals("mw")) {
                    // 選每月的第幾週
                    batch.setBatchScheduleType("O");
                }
                if (StringUtils.isBlank(form.getDateTime())) {
                    response.setMessage(MessageType.DANGER, "未輸入系統別");
                    return false;
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                batch.setBatchScheduleStarttime(format.parse(form.getDateTime()));
                switch (batch.getBatchScheduleType()) {
                    case "D": {
                        if (batch.getBatchScheduleRepetitioninterval() >= 1440) {
                            response.setMessage(MessageType.DANGER, "重覆時間必須小於1440分!");
                            return false;
                        }
                        if (batch.getBatchScheduleRepetitioninduration() >= 24) {
                            response.setMessage(MessageType.DANGER, "持續時間必須小於24小時!");
                            return false;
                        }
                        break;
                    }
                    case "W": {
                        if (form.getMwChk().length() == 1 && form.getMwChk().equals("0")) {
                            form.setMwChk("");
                        } else if (form.getMwChk().length() > 2 && form.getMwChk().substring(0, 2).equals("0,")) {
                            form.setMwChk(form.getMwChk().substring(2, form.getMwChk().length()));
                        }
                        if (StringUtils.isBlank(form.getMwChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何星期!");
                            return false;
                        }
                        batch.setBatchScheduleWeekdays(form.getMwChk());
                        break;
                    }
                    case "M": {
                        if (form.getmChk().length() == 1 && form.getmChk().equals("0")) {
                            form.setmChk("");
                        } else if (form.getmChk().length() > 2 && form.getmChk().substring(0, 2).equals("0,")) {
                            form.setmChk(form.getmChk().substring(2, form.getmChk().length()));
                        }
                        if (StringUtils.isBlank(form.getmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何月份!");
                            return false;
                        }
                        batch.setBatchScheduleMonths(form.getmChk());
                        if (form.getMdChk().length() == 1 && form.getMdChk().equals("0")) {
                            form.setMdChk("");
                        } else if (form.getMdChk().length() > 2 && form.getMdChk().substring(0, 2).equals("0,")) {
                            form.setMdChk(form.getMdChk().substring(2, form.getMdChk().length()));
                        }
                        if (StringUtils.isNotBlank(form.getMdChk())) {
                            batch.setBatchScheduleMonthdays(form.getMdChk());
                        } else {
                            response.setMessage(MessageType.DANGER, "尚未選取任何日期!");
                            return false;
                        }
                        break;
                    }
                    case "O": {
                        if (form.getmChk().length() == 1 && form.getmChk().equals("0")) {
                            form.setmChk("");
                        } else if (form.getmChk().length() > 2 && form.getmChk().substring(0, 2).equals("0,")) {
                            form.setmChk(form.getmChk().substring(2, form.getmChk().length()));
                        }
                        if (StringUtils.isBlank(form.getmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何月份!");
                            return false;
                        }
                        batch.setBatchScheduleMonths(form.getmChk());
                        if (form.getMwChk().length() == 1 && form.getMwChk().equals("0")) {
                            form.setMwChk("");
                        } else if (form.getMwChk().length() > 2 && form.getMwChk().substring(0, 2).equals("0,")) {
                            form.setMwChk(form.getMwChk().substring(2, form.getMwChk().length()));
                        }
                        if (StringUtils.isBlank(form.getMwChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取任何星期!");
                            return false;
                        }
                        batch.setBatchScheduleWeekdays(form.getMwChk());
                        if (form.getWmChk().length() == 1 && form.getWmChk().equals("0")) {
                            form.setWmChk("");
                        } else if (form.getWmChk().length() > 2 && form.getWmChk().substring(0, 2).equals("0,")) {
                            form.setWmChk(form.getWmChk().substring(2, form.getWmChk().length()));
                        }
                        if (StringUtils.isBlank(form.getWmChk())) {
                            response.setMessage(MessageType.DANGER, "尚未選取那一週!");
                            return false;
                        }
                        batch.setBatchScheduleWhickweeks(form.getWmChk());
                        break;
                    }
                    default: {
                        response.setMessage(MessageType.DANGER, "尚未選取排程方式");
                        return false;
                    }
                }
            }
            if (batch.getBatchNotifytype().intValue() != 0) {
                if (StringUtils.isBlank(batch.getBatchNotifymail())) {
                    response.setMessage(MessageType.DANGER, "通知Mail欄位必須有值");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
            return false;
        }
    }

    private List<HashMap<String, Object>> bindJobGrid() {
        return obj.getJobTaskByBatchId(batch.getBatchBatchid());
    }

    @PostMapping(value = "/batch/UI_000100/saveTaskClick")
    @ResponseBody
    public List<HashMap<String, Object>> saveTaskClick(@RequestBody UI_000100_Task_Form form) {
        Jobs job = new Jobs();
        int batchId = batch.getBatchBatchid();
        try {
            if (checkAllJobField(job, batchId, form)) {
                obj.updateJob(job, form.getTskId());
            }
            // QueryStatusBar.ShowMessage("修改成功", StatusBar.MessageType.InfoMsg); TODO
            return bindJobGrid();
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            // EditStatusBar.ShowMessage(ex.getMessage(), StatusBar.MessageType.ErrMsg); TODO
            return null;
        }
    }

    @PostMapping(value = "/batch/UI_000100/insertTaskClick")
    @ResponseBody
    public List<HashMap<String, Object>> insertTaskClick(@RequestBody UI_000100_Task_Form form) {
        Jobs job = new Jobs();
        Integer batchId = batch.getBatchBatchid();
        RefBase<Jobs> jobsRefBase = new RefBase<>(job);
        try {
            if (!checkAllJobField(job, batchId, form)) {
                return null;
            }
            job = jobsRefBase.get();
            obj.insertJobAndTask(job, form.getTskId());
            obj.createScheduleTask(batchId.intValue());
            // QueryStatusBar.ShowMessage("新增成功", StatusBar.MessageType.InfoMsg); TODO
            return bindJobGrid();
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            // EditStatusBar.ShowMessage(ex.Message, StatusBar.MessageType.ErrMsg); TODO
            return null;
        }
    }

    private boolean checkAllJobField(Jobs job, Integer batchId, UI_000100_Task_Form form) {
        try {
            Task tsk = obj.getTaskById(form.getTskId());
            job.setJobsBatchid(batchId);
            if (form.getSender() == 0) {
                // Add from EmptyDataTemplate
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                // .JOBS_DELAY = CInt(CType(sender.Controls(0).Controls(0).Controls(0).FindControl("JOBS_DELAYTxt"), NumberTextBox).Text)
                job.setJobsDelay(0);
                job.setJobsSeq(1);
                job.setJobsStarttaskid(0);
            } else if (form.getSender() > 0) {
                // Add from FooterDataTemplate
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                job.setJobsDelay(0);
                job.setJobsSeq(form.getSender() + 1);
                job.setJobsStarttaskid(0);
            } else {
                job.setJobsJobid(form.getJobId());
                job.setJobsName(tsk.getTaskName());
                job.setJobsDescription(tsk.getTaskDescription());
                // .JOBS_DELAY = CInt(CType(sender.Rows(rowIndex).Cells(4).FindControl("JOBS_DELAYTxt"), NumberTextBox).Text)
            }
            return true;
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            return false;
        }
    }

    @PostMapping(value = "/batch/UI_000100/delTaskClick")
    @ResponseBody
    public List<HashMap<String, Object>> delTaskClick(@RequestBody UI_000100_Task_Form form) {
        try {
            obj.deleteJob(form.getJobId());
            return bindJobGrid();
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            // EditStatusBar.ShowMessage(ex.Message, StatusBar.MessageType.ErrMsg); TODO
            return null;
        }
    }

    @PostMapping(value = "/batch/UI_000100/changeTaskOrder")
    @ResponseBody
    public UI_000100_Task_Form changeTaskOrder(@RequestBody UI_000100_Task_Form form) {
        form.setMessage(MessageType.INFO, "");
        String[] jobsSeqs = new String[form.getSender()];
        String[] jobsJobIDs = new String[form.getSender()];
        String[] taskIDs = new String[form.getSender()];
        String[] seqs = new String[form.getSender()];
        if (form.getSender() > 0) {
            seqs = form.getJobsSeq().split(",");
            jobsSeqs = form.getJobsSeq().split(",");
            jobsJobIDs = form.getJobsJobID().split(",");
            taskIDs = form.getTaskID().split(",");
        }

        ArrayList<String> tmp = new ArrayList<>();
        if(form.getSender() == null) {
        	return form;
        }
        for (int i = 0; i < form.getSender(); i++) {
            tmp.add((i + 1) + "");
        }
        Arrays.sort(seqs);
        String[] strArr = null;

        strArr = tmp.toArray(new String[tmp.size()]);

        if (!Arrays.equals(strArr, seqs)) {
            form.setMessage(MessageType.DANGER, "順序必須由１開始且不能跳號也不能重覆！");
            return form;
        }
        int startJob = 0;

        for (int i = 0; i < form.getSender(); i++) {
            int seq = Integer.parseInt(jobsSeqs[i]);
            Jobs job = new Jobs();
            job.setJobsJobid(Integer.parseInt(jobsJobIDs[i]));
            job.setJobsSeq(seq);
            Integer tskId = Integer.parseInt(taskIDs[i]);
            obj.updateJob(job, tskId);
            if (seq == 1) {
                startJob = job.getJobsJobid().intValue();
            }
        }

        Batch batch1 = new Batch();
        batch1.setBatchBatchid(batch.getBatchBatchid());
        batch1.setBatchZone(null); // 避免原本的欄位被更新掉
        if (form.getSender() == 0) {
            batch1.setBatchStartjobid(0);
        } else {
            batch1.setBatchStartjobid(startJob);
        }
        obj.updateBatch(batch, WebUtil.getUser().getUserId());
        form.setTasks(bindJobGrid());
        return form;
    }
}
