package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleExtMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BatchService extends BaseService {

    @Autowired
    BatchExtMapper batchExtMapper;

    @Autowired
    SubsysExtMapper subsysExtMapper;

    @Autowired
    JobtaskExtMapper jobtaskExtMapper;

    @Autowired
    JobsExtMapper jobsExtMapper;

    @Autowired
    TaskExtMapper taskExtMapper;

    @Autowired
    FepuserExtMapper fepuserExtMapper;

    @Autowired
    HistoryExtMapper historyExtMapper;

    @Autowired
    TwslogExtMapper twslogExtMapper;

    @Autowired
    SyscomroleExtMapper syscomroleExtMapper;
    /**
     * 取得 Batch ALL
     *
     * @return
     * @throws Exception
     */
    public List<Batch> getBatchAll() throws Exception {
        try {
            return batchExtMapper.getBatchAll();
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Integer getRoleID(String roleId) throws Exception {
        try {
            return syscomroleExtMapper.queryRoleIdByNo(roleId);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getAllBatch(String batno, String[] subsys, int pageNum, int pageSize) throws Exception {
        try {
            // 分頁查詢
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    batchExtMapper.getAllBatch(batno, subsys);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Batch getBatchByID(Integer batchId) {
        return batchExtMapper.selectByPrimaryKey(batchId);
    }

    public List<Subsys> getSubsysAll() {
        return subsysExtMapper.queryAll();
    }

    public int insertBatch(Batch batch) {
        return batchExtMapper.insertSelective(batch);
    }

    public int updateBatch(Batch batch, String userId) {
        batch.setUpdateUser(Integer.parseInt(userId));
        return batchExtMapper.updateByPrimaryKeySelective(batch);
    }

    public void deleteScheduleTask(String hostName, int batchId, String batchName) throws Exception {
        BatchJobLibrary bcl = new BatchJobLibrary();
        bcl.deleteTask(hostName, String.valueOf(batchId), batchName);
        bcl.dispose();
    }

    public List<Map<String, Object>> getHistoryQuery(String batchName, String batchStartDate, String batchShortName, String subsys) {
        return historyExtMapper.getHistoryQuery(batchName, batchStartDate, batchShortName, subsys);
    }

    public List<Map<String, Object>> getTWSLOG(String twsTaskName, String batchStartDate) {
        return twslogExtMapper.getTwslogQuery(twsTaskName, batchStartDate);
    }

    public void createScheduleTask(int batchId) throws Exception {
        List<HashMap<String, Object>> hashMap = batchExtMapper.getBatchFirstTaskById(batchId);
        if (hashMap != null && hashMap.size() > 0) {
            if (hashMap.get(0).get("BATCH_DESCRIPTION") == null) {
                hashMap.get(0).put("BATCH_DESCRIPTION", StringUtils.EMPTY);
            }
            BatchJobLibrary bcl = new BatchJobLibrary();
            boolean bEnableSchedule = false;
            if (DbHelper.toBoolean(String.valueOf(hashMap.get(0).get("BATCH_ENABLE"))) && DbHelper.toBoolean(String.valueOf(hashMap.get(0).get("BATCH_SCHEDULE")))) {
                bEnableSchedule = true;
            } else {
                bEnableSchedule = false;
            }
            if (hashMap.get(0).get("BATCH_SCHEDULE_TYPE") != null) {
                switch (isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_TYPE"))) {
                    case "D": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        if (isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_REPETITIONINTERVAL")).equals("0")) {
                            bcl.createDailyTask(
                                    isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                    isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                    isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                    Short.parseShort(isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_DAYINTERVAL"))),
                                    bEnableSchedule);
                        } else {
                            bcl.createDailyRepetitionTask(
                                    isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                    isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                    isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                    Short.parseShort(isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_DAYINTERVAL"))),
                                    bEnableSchedule,
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_REPETITIONINTERVAL")),
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_REPETITIONINDURATION")));
                        }
                        break;
                    }
                    case "W": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        int weekdays = 0;
                        String[] wdays = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WEEKDAYS")).split("[,]", -1);
                        for (int i = 0; i < wdays.length; i++) {
                            weekdays += Integer.parseInt(wdays[i]);
                        }
                        bcl.createWeeklyTask(
                                isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                weekdays,
                                Integer.parseInt(isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WEEKINTERVAL"))),
                                bEnableSchedule);
                        break;
                    }
                    case "M": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        int months = 0;
                        String[] smonths = hashMap.get(0).get("BATCH_SCHEDULE_MONTHS").toString().split("[,]", -1);
                        for (int i = 0; i < smonths.length; i++) {
                            months += Integer.parseInt(smonths[i]);
                        }
                        bcl.createMonthlyTask(
                                isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_MONTHDAYS")),
                                months, false, bEnableSchedule);
                        break;
                    }
                    case "O": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        int months = 0;
                        String[] smonths = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_MONTHS")).split("[,]", -1);
                        for (int i = 0; i < smonths.length; i++) {
                            months += Integer.parseInt(smonths[i]);
                        }
                        int weekdays = 0;
                        String[] wdays = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WEEKDAYS")).split("[,]", -1);
                        for (int i = 0; i < wdays.length; i++) {
                            weekdays += Integer.parseInt(wdays[i]);
                        }
                        int whickweeks = 0;
                        String[] wweeks = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WHICKWEEKS")).split("[,]", -1);
                        for (int i = 0; i < wweeks.length; i++) {
                            whickweeks += Integer.parseInt(wweeks[i]);
                        }
                        boolean bRunLastWeekOfMonth = false;
                        if (isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WHICKWEEKS")).contains("16")) {
                            bRunLastWeekOfMonth = true;
                        }
                        bcl.createMonthlyDayOfWeekTask(
                                isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                weekdays,
                                months,
                                whickweeks,
                                bRunLastWeekOfMonth,
                                bEnableSchedule);
                        break;
                    }
                }
            }
        }

    }

    public List<HashMap<String, Object>> getJobTaskByBatchId(int batchid) {
        return jobtaskExtMapper.getJobTaskByBatchId(batchid);
    }

    public int deleteBatch(int batchId) {
        Batch dBatch = new Batch();
        dBatch.setBatchBatchid(batchId);
        dBatch = batchExtMapper.selectByPrimaryKey(dBatch.getBatchBatchid());
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (dBatch != null) {
                // 刪除Batch 相關的Task
                jobtaskExtMapper.deleteByBatchId(batchId);
                // 刪除Batch相關的Job
                jobsExtMapper.deleteByBatchId(batchId);
                // 最後才是刪除Batch
                Batch batch = new Batch();
                batch.setBatchBatchid(batchId);
                int iRes = batchExtMapper.deleteByPrimaryKey(batch);
                // 刪除排程中的批次
                deleteScheduleTask(dBatch.getBatchExecuteHostName(), batchId, dBatch.getBatchName());
                transactionManager.commit(txStatus);
                return iRes;
            }
            return 0;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            return 0;
        }
    }

    public List<Task> getTaskAll() {
        return taskExtMapper.queryTaskAll();
    }

    public Task getTaskById(Integer taskID) {
        return taskExtMapper.selectByPrimaryKey(taskID);
    }

    public int updateJob(Jobs job, Integer taskId) {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int iRes = jobsExtMapper.updateByPrimaryKeySelective(job);
            jobtaskExtMapper.updateTaskIdByJobId(job.getJobsJobid(), taskId);
            transactionManager.commit(txStatus);
            return iRes;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            return 0;
        }

    }

    public int insertJobAndTask(Jobs job, Integer taskId) {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 找出目前JOB的筆數
            int jobCount = jobsExtMapper.getJobsCountByBatchId(job.getJobsBatchid());
            job.setJobsSeq(jobCount + 1);
            jobsExtMapper.insertSelective(job); // 回傳的為JOBId
            int jobid = job.getJobsJobid().intValue();
            if (jobCount == 0) {
                // 如果為第一個新增的JOB則Update BATCH檔的啟動JOB欄位
                Batch oBatch = new Batch();
                oBatch.setBatchBatchid(job.getJobsBatchid());
                oBatch.setBatchStartjobid(jobid);
                oBatch.setBatchZone(null); // 避免原本的欄位被更新掉
                batchExtMapper.updateByPrimaryKeySelective(oBatch);
            }
            // 新增至JOBTASK
            Jobtask jobtask = new Jobtask();
            jobtask.setJobtaskJobid(jobid);
            jobtask.setJobtaskTaskid(taskId);
            insertJobTask(jobtask);
            transactionManager.commit(txStatus);
            return jobid;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            return 0;
        }

    }

    public int insertJobTask(Jobtask jobTask) {
        try {
            // 找出目前JOB的筆數
            Jobtask dt = jobtaskExtMapper.getMaxJobTasktByJobId(jobTask.getJobtaskJobid());
            if (dt == null) {
                jobTask.setJobtaskStepid((short) 1);
                jobTask.setJobtaskWaitfortask("0");
                // 如果為第一個新增的JOBTASK則Update JOB檔的啟動TASK欄位
                Jobs oJob = new Jobs();
                oJob.setJobsJobid(jobTask.getJobtaskJobid());
                oJob.setJobsStarttaskid(jobTask.getJobtaskTaskid());
                jobsExtMapper.updateByPrimaryKeySelective(oJob);
            } else {
                jobTask.setJobtaskStepid((short) (dt.getJobtaskStepid() + 1));
                jobTask.setJobtaskWaitfortask(String.valueOf(dt.getJobtaskTaskid()));
            }
            int iRes = jobtaskExtMapper.insertSelective(jobTask);
            // db.CommitTransaction()
            return iRes;
        } catch (Exception ex) {
            // db.RollbackTransaction()
            sendEMS(ex);
            return 0;
        }

    }

    public int deleteJob(Integer jobId) {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        int iRes = 0;
        try {
            // 先刪除JOB相關的TASK
            jobtaskExtMapper.deleteByJobId(jobId);

            Jobs dJobs = new Jobs();
            dJobs.setJobsJobid(jobId);
            List<Jobs> dt = jobsExtMapper.getDataTableByPrimaryKey(dJobs.getJobsJobid());
            if (dt.size() > 0) {
                iRes = jobsExtMapper.deleteByPrimaryKey(dJobs);
                // 刪除某筆Job,可能影響到JOBS_SEQ跳號,所以依目前SEQ順序重新Update
                List<Jobs> dtJobs = jobsExtMapper.getJobsByBatchId(dt.get(0).getJobsBatchid());
                Batch dBatch = new Batch();
                dBatch.setBatchZone(null); // 避免原本的欄位被更新掉
                if (dtJobs.size() > 0) {
                    for (int i = 0; i < dtJobs.size(); i++) {
                        if (i == 0) {// 第一個job
                            dBatch.setBatchBatchid(dtJobs.get(0).getJobsBatchid());
                            dBatch.setBatchStartjobid(dtJobs.get(0).getJobsJobid());
                        }

                        Jobs jobs = new Jobs();
                        jobs.setJobsJobid(dtJobs.get(i).getJobsJobid());
                        jobs.setJobsSeq(i + 1);
                        jobsExtMapper.updateByPrimaryKeySelective(jobs);
                    }
                } else {
                    dBatch.setBatchBatchid(dt.get(0).getJobsBatchid());
                    dBatch.setBatchStartjobid(0);
                }

                // 刪除某筆Job,可能影響到Batch檔StartJobId,所以一律以目前第一個Job更新Batch檔StartJobId
                batchExtMapper.updateByPrimaryKeySelective(dBatch);

            }

            transactionManager.commit(txStatus);
            return iRes;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            return 0;
        }
    }

    public List<Task> getTaskByName(String taskName, String direction) {
        return taskExtMapper.getTaskByName(taskName, direction);
    }

    public Integer updateSelectTask(Task defTask) {
        try {
            // 回傳的為TaskId
            return taskExtMapper.updateByPrimaryKeySelective(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    /**
     * 新增一筆 Task
     */
    public Integer insertTask(Task defTask) {
        try {
            return taskExtMapper.insertSelective(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    /**
     * 更新一筆 Task
     */
    public Integer updateTask(Task defTask) {
        try {
            // 回傳的為TaskId
            return taskExtMapper.updateByPrimaryKey(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    /**
     * 刪除一筆 Task
     */
    public Integer deleteTask(Task defTask) {
        try {
            // 回傳的為TaskId
            return taskExtMapper.deleteByPrimaryKey(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Batch getBatchQueryByPrimaryKey(Batch batch) {
        return batchExtMapper.selectByPrimaryKey(batch.getBatchBatchid());
    }

    public PageInfo<HashMap<String, Object>> getHistoryByInstanceId(Integer batchId, String instanceId, Integer pageNum, Integer pageSize) {
        try {
            // 分頁查詢
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    historyExtMapper.getHistoryById(batchId, instanceId);
                }
            });
            return pageInfo;
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return null;
        }
    }

    public List<Jobs> getJobsByBatchId(Integer batchid) {
        return jobsExtMapper.getJobsByBatchId(batchid);
    }

    public Map<String, Object> getLogByLogFile(String historyLogfile) {
        return historyExtMapper.getLogByLogFile(historyLogfile);
    }

    public PageInfo<Batch> queryScheduledBatchByNameAndSubsys(String batchName, String[] batchSubsys, Integer pageNum, Integer pageSize) throws Exception {
        try {
            PageInfo<Batch> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    batchExtMapper.queryScheduledBatchByNameAndSubsys(batchName, batchSubsys);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Batch> getAllBatchByLastRunTime(String batchName, String sqlSortExpression) throws Exception {
        try {
            return batchExtMapper.getAllBatchByLastRunTime(batchName, sqlSortExpression);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }
}
