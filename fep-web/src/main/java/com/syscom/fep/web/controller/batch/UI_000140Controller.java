package com.syscom.fep.web.controller.batch;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.configurer.BatchBaseConfigurationHost;
import com.syscom.fep.batch.base.util.BatchRestfulClient;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.base.vo.restful.request.ListSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.request.OperateSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.response.ListSchedulerResponse;
import com.syscom.fep.batch.base.vo.restful.response.OperateSchedulerResponse;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Fepuser;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.batch.ScheduledBatch;
import com.syscom.fep.web.form.batch.UI_000140_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.utils.ExceptionUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Trigger.TriggerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Richard
 */
@Controller
public class UI_000140Controller extends BaseController {
    private static final String URL_DO_QUERY = "/batch/UI_000140/doQuery";
    @Autowired
    private BatchService batchService;
    @Autowired
    private BatchRestfulClient client;
    @Autowired
    private BatchBaseConfiguration batchBaseConfiguration;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_000140_Form form = new UI_000140_Form();
        form.setUrl(URL_DO_QUERY);
        doQuery(form, mode);
    }

    @PostMapping(value = URL_DO_QUERY)
    private String doQuery(@ModelAttribute UI_000140_Form form, ModelMap mode) {
        this.infoMessage("查詢資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            String subsys = WebConfiguration.getInstance().getSubsys();
            String[] batchSubsys = null;
            if (StringUtils.isNotBlank(subsys)) {
                batchSubsys = subsys.split(",");
            }
            PageInfo<Batch> pageInfoBatch = batchService.queryScheduledBatchByNameAndSubsys(form.getBatchName(), batchSubsys, 0, 0);
            if (pageInfoBatch.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            List<Batch> batchList = pageInfoBatch.getList();
            List<BatchScheduler> batchSchedulerList = listScheduler(batchList);
            List<ScheduledBatch> scheduledBatchList = new ArrayList<>(batchList.size());
            if (CollectionUtils.isNotEmpty(batchList)) {
                Fepuser fepuser = WebUtil.getFepuser();
                for (Batch batch : batchList) {
                    if (CollectionUtils.isNotEmpty(batchSchedulerList)) {
                        List<BatchScheduler> list = batchSchedulerList.stream().filter(t -> t.getBatchId().equals(batch.getBatchBatchid().toString())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(list)) {
                            for (BatchScheduler batchScheduler : list) {
                                ScheduledBatch scheduledBatch = new ScheduledBatch(batch);
                                scheduledBatch.setTriggerState(batchScheduler.getTriggerState());
                                scheduledBatch.setBatchNextruntime(batchScheduler.getNextExecutedDateTime());
                                if (StringUtils.isNotBlank(batch.getBatchStartgroup()) && batch.getBatchStartgroup().indexOf(fepuser.getFepuserGroup()) < 0) {
                                    // 不可以操作
                                    scheduledBatch.setOperability(false);
                                } else if (scheduledBatch.getTriggerState() != TriggerState.NORMAL && scheduledBatch.getTriggerState() != TriggerState.PAUSED) {
                                    // 不可以操作
                                    scheduledBatch.setOperability(false);
                                }
                                // 塞入執行主機
                                scheduledBatch.setBatchExecuteHostName(batchScheduler.getExecutedHostName());
                                scheduledBatchList.add(scheduledBatch);
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isEmpty(scheduledBatchList)) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageInfo<ScheduledBatch> pageInfoScheduledBatch = clientPaged(scheduledBatchList, form.getPageNum(), form.getPageSize());
            PageData<UI_000140_Form, ScheduledBatch> pageData = new PageData<>(pageInfoScheduledBatch, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            mode.put("operabilityColumnVisible", scheduledBatchList.stream().filter(t -> t.isOperability()).findFirst().orElse(null) != null);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_000140.getView();
    }

    /**
     * 從Batch Control Service取排程的訊息
     *
     * @param batchList
     * @return
     * @throws Exception
     */
    private List<BatchScheduler> listScheduler(List<Batch> batchList) throws Exception {
        if (CollectionUtils.isEmpty(batchList)) {
            return null;
        }
        List<BatchScheduler> batchSchedulerList = new ArrayList<>(batchList.size());
        for (Batch batch : batchList) {
            batchSchedulerList.add(new BatchScheduler(batch.getBatchBatchid().toString(), batch.getBatchExecuteHostName()));
        }
        ListSchedulerRequest request = new ListSchedulerRequest();
        request.setBatchSchedulerList(batchSchedulerList);
        request.setOperator(WebUtil.getUser().getUserId());
        ListSchedulerResponse response = client.listScheduler(request);
        if (response != null) {
            if (!response.isResult()) {
                throw ExceptionUtil.createException("排程訊息查詢失敗, ", response.getMessage());
            }
            List<BatchScheduler> list = response.getBatchSchedulerList();
            return list;
        } else {
            throw ExceptionUtil.createException("排程訊息查詢失敗!!!");
        }
    }

    @PostMapping(value = "/batch/UI_000140/doOperate")
    @ResponseBody
    public BaseResp<?> doOperate(@RequestBody UI_000140_Form form) {
        this.infoMessage("執行動作, 條件 = [", form.toString(), "]");
        BaseResp<BatchScheduler> resp = new BaseResp<>();
        try {
            // 目前只考慮一次處理一筆
            List<BatchScheduler> batchSchedulerList = new ArrayList<>();
            batchSchedulerList.add(new BatchScheduler(form.getBatchId(), form.getBatchExecuteHostName()));
            OperateSchedulerRequest request = new OperateSchedulerRequest();
            request.setAction(form.getAction());
            request.setBatchSchedulerList(batchSchedulerList);
            request.setOperator(WebUtil.getUser().getUserId());
            OperateSchedulerResponse response = client.operateScheduler(request);
            if (response != null) {
                if (!response.isResult()) {
                    resp.setMessage(MessageType.DANGER, StringUtils.join("排程操控失敗, ", response.getMessage()));
                } else {
                    List<BatchScheduler> list = response.getBatchSchedulerList();
                    resp.setData(list.get(0)); // 目前只考慮一次處理一筆
                    resp.setMessage(MessageType.SUCCESS, "排程操控成功");
                }
            } else {
                resp.setMessage(MessageType.DANGER, "排程操控失敗!!!");
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            resp.setMessage(MessageType.DANGER, "排程操控失敗!!!");
        }
        return resp;
    }
}
