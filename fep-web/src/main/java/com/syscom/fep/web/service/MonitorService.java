package com.syscom.fep.web.service;

import com.google.gson.reflect.TypeToken;
import com.syscom.fep.base.configurer.FEPMonitorConfig;
import com.syscom.fep.frmcommon.parse.GsonDateParser;
import com.syscom.fep.frmcommon.parse.GsonParser;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.JsonUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SmsExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Sms;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.vo.monitor.*;
import com.syscom.fep.web.configurer.WebConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MonitorService extends BaseService implements MonitorConstant {
    @Autowired
    private WebConfiguration webConfiguration;
    @Autowired
    private SmsExtMapper smsExtMapper;
    @Autowired
    private SysconfExtMapper sysconfExtMapper;

    /**
     * 取得監控數據
     *
     * @return
     */
    public ServiceMonitoring getData() {
        ServiceMonitoring monitor = initData();
        fetchSystemData(monitor);
        fetchDBData(monitor);
        fetchMQData(monitor);
        fetchServiceData(monitor);
        fetchNetworkServerData(monitor);
        fetchNetworkClientData(monitor);
        convertServiceNameToAlias(monitor);
        return monitor;
    }

    /**
     * 有一些服務的名字需要用另外的名字顯示, 所以這裡要判斷轉換一下
     *
     * @param monitor
     */
    private void convertServiceNameToAlias(ServiceMonitoring monitor) {
        FEPMonitorConfig fepMonitorConfig = SpringBeanFactoryUtil.registerBean(FEPMonitorConfig.class);
        List<ServiceStatus> serviceStatusList = monitor.getServiceStatusList();
        if (CollectionUtils.isNotEmpty(serviceStatusList)) {
            for (ServiceStatus status : serviceStatusList) {
                status.setServiceName(fepMonitorConfig.getAliasByServiceName(status.getServiceName()));
            }
        }
        List<ClientNetworkStatus> clientNetworkStatusList = monitor.getClientNetworkStatusList();
        if (CollectionUtils.isNotEmpty(clientNetworkStatusList)) {
            for (ClientNetworkStatus status : clientNetworkStatusList) {
                status.setServiceName(fepMonitorConfig.getAliasByServiceName(status.getServiceName()));
            }
        }
        List<ServerNetworkStatus> serverNetworkStatusList = monitor.getServerNetworkStatusList();
        if (CollectionUtils.isNotEmpty(serverNetworkStatusList)) {
            for (ServerNetworkStatus status : serverNetworkStatusList) {
                status.setServiceName(fepMonitorConfig.getAliasByServiceName(status.getServiceName()));
            }
        }
    }

    /*
     * 初始化
     */
    private ServiceMonitoring initData() {
        ServiceMonitoring monitor = new ServiceMonitoring();
        //是否停止提醒 true -停止提醒 / false -提醒
        Sysconf sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, SYSCONF_NAME_STOPNOTIFICATION);
        if (sysconf != null) {
            monitor.setStopNotification(Boolean.parseBoolean(sysconf.getSysconfValue()));
        }
        // 是否啟用自動重啟
        sysconf = sysconfExtMapper.selectByPrimaryKey(SYSCONF_VALUE_CMN, SYSCONF_NAME_ENABLEAUTORESTART);
        if (sysconf != null) {
            monitor.setEnableAutoRestart(Boolean.parseBoolean(sysconf.getSysconfValue()));
        }
        return monitor;
    }

    /**
     * 取得監控服務的基本數據
     *
     * @param monitor
     */
    private void fetchSystemData(ServiceMonitoring monitor) {
        List<SystemStatus> systemStatusList = new ArrayList<>();
        List<DiskSpace> diskSpaceList = new ArrayList<>();
        List<Sms> smsSystemList = smsExtMapper.selectByServiceName(SERVICE_NAME_SYSTEM);
        if (CollectionUtils.isNotEmpty(smsSystemList)) {
            for (Sms smsSystem : smsSystemList) {
                // SYSTEM
                SystemStatus systemStatus = this.createSystemStatus();
                systemStatus.setSysHostname(smsSystem.getSmsHostname());
                systemStatus.setSysServiceIP(smsSystem.getSmsServiceip());
                systemStatus.setSysHostname(smsSystem.getSmsHostname());
                if (this.isStarted(smsSystem)) {
                    systemStatus.setSysCpu(this.getCpu(smsSystem.getSmsCpu()));
                    systemStatus.setSysRam(this.getRam(smsSystem.getSmsRam()));
                } else {
                    systemStatus.setSysCpu("0");
                    systemStatus.setSysRam("0");
                }
                systemStatusList.add(systemStatus);
                // DISK
                if (StringUtils.isNotBlank(smsSystem.getSmsOthers())) {
                    String jsonStr = smsSystem.getSmsOthers();
                    JSONArray disArray = new JSONArray(jsonStr);
                    if (!disArray.isEmpty()) {
                        for (int i = 0; i < disArray.length(); i++) {
                            JSONObject disk = disArray.getJSONObject(i);
                            try {
                                DiskSpace diskSpace = new DiskSpace();
                                diskSpace.setServiceHostName(JsonUtil.getString(disk, JSON_FIELD_HOSTNAME, StringUtils.EMPTY));
                                diskSpace.setServiceName(JsonUtil.getString(disk, JSON_FIELD_NAME, StringUtils.EMPTY));
                                diskSpace.setServiceIP(JsonUtil.getString(disk, JSON_FIELD_IP, StringUtils.EMPTY));
                                diskSpace.setUseDisk(JsonUtil.getString(disk, JSON_FIELD_USED, StringUtils.EMPTY));
                                diskSpace.setTotalDisk(JsonUtil.getString(disk, JSON_FIELD_TOTAL, StringUtils.EMPTY));
                                diskSpace.setDisk(JsonUtil.getString(disk, JSON_FIELD_DISK, StringUtils.EMPTY));
                                diskSpaceList.add(diskSpace);
                            } catch (Exception e) {
                                this.errorMessage(e, "fetchSystemData with exception occur!!!");
                            }
                        }
                    }
                }
            }
        }
        monitor.setDiskSpaceList(diskSpaceList);
        monitor.setSystemStatusList(systemStatusList);
    }

    /**
     * 獲取DB監控數據
     *
     * @param monitor
     */
    private void fetchDBData(ServiceMonitoring monitor) {
        List<SystemStatus> systemStatusList = monitor.getSystemStatusList();
        List<Sms> smsDbList = smsExtMapper.selectByServiceName(SERVICE_NAME_DB);
        // 如果查不到DB的監控數據, 則全部設定狀態為false
        if (CollectionUtils.isNotEmpty(smsDbList)) {
            for (Sms smsDb : smsDbList) {
                // 根據IP取出對應的SystemStatus
                SystemStatus systemStatus = systemStatusList.stream().filter(t -> t.getSysServiceIP().equals(smsDb.getSmsServiceip())).findFirst().orElse(null);
                // 如果沒有取到, 則這裡就new一個
                if (systemStatus == null) {
                    systemStatus = this.createSystemStatus();
                    systemStatus.setSysServiceIP(smsDb.getSmsServiceip());
                    systemStatus.setSysHostname(smsDb.getSmsHostname());
                    systemStatusList.add(systemStatus);
                }
                // 解讀DB的json數據
                String jsonStr = smsDb.getSmsOthers();
                if (StringUtils.isNotBlank(jsonStr)) {
                    JSONObject rootObject = new JSONObject(jsonStr);
                    // 如果狀態為未知, 則狀態顯示為空白
                    if ("2".equals(smsDb.getSmsServicestate())) {
                        continue;
                    } else {
                        try {
                            systemStatus.setSysFEPDB(JsonUtil.getString(rootObject, DB_NAME_FEP, Boolean.FALSE.toString()));
                            systemStatus.setSysEMSDB(JsonUtil.getString(rootObject, DB_NAME_EMS, Boolean.FALSE.toString()));
                            systemStatus.setSysENCDB(JsonUtil.getString(rootObject, DB_NAME_ENC, Boolean.FALSE.toString()));
                            systemStatus.setSysENCLOGDB(JsonUtil.getString(rootObject, DB_NAME_ENCLOG, Boolean.FALSE.toString()));
                            systemStatus.setSysFEPHIS(JsonUtil.getString(rootObject, DB_NAME_FEPHIS, Boolean.FALSE.toString()));
                        } catch (Exception e) {
                            this.errorMessage(e, "fetchDBData with exception occur!!!");
                        }
                    }
                }
            }
        }
    }

    private SystemStatus createSystemStatus() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setSysServiceIP(StringUtils.EMPTY);
        systemStatus.setSysUserport(StringUtils.EMPTY);
        systemStatus.setSysENCDB(StringUtils.EMPTY);
        systemStatus.setSysEMSDB(StringUtils.EMPTY);
        systemStatus.setSysFEPDB(StringUtils.EMPTY);
        systemStatus.setSysENCLOGDB(StringUtils.EMPTY);
        systemStatus.setSysFEPHIS(StringUtils.EMPTY);
        systemStatus.setSysCpu("0");
        return systemStatus;
    }

    /**
     * 獲取MQ監控數據
     *
     * @param monitor
     */
    private void fetchMQData(ServiceMonitoring monitor) {
        List<Sms> smsMqList = smsExtMapper.selectByServiceName(SERVICE_NAME_MQ);
        if (CollectionUtils.isEmpty(smsMqList)) {
            monitor.setIbmMQStatusList(null);
        } else {
            // 解讀json字串
            List<IBMMQStatus> ibmmqStatusList = new ArrayList<>();
            GsonParser<List<IBMMQStatus>> gsonParser = new GsonParser<List<IBMMQStatus>>(new TypeToken<List<IBMMQStatus>>() {}.getType());
            for (Sms smsMq : smsMqList) {
                String jsonStr = smsMq.getSmsOthers();
                if (StringUtils.isNotBlank(jsonStr)) {
                    try {
                        List<IBMMQStatus> list = gsonParser.readIn(jsonStr);
                        if (CollectionUtils.isNotEmpty(list)) {
                            for (IBMMQStatus ibmMq : list) {
                                // 再根據status編號, 替換成相應的中文
                                if ("2".equals(smsMq.getSmsServicestate())) {
                                    ibmMq.setStatus(STATUS_UNKNOWN);
                                    ibmMq.setQueueCount("0");
                                } else {
                                    if ("1".equals(ibmMq.getStatus())) {
                                        ibmMq.setStatus(STATUS_NORMAL);
                                    } else {
                                        ibmMq.setStatus(STATUS_STOPPED);
                                        ibmMq.setQueueCount("0");
                                    }
                                }
                            }
                            ibmmqStatusList.addAll(list);
                        }
                    } catch (Exception e) {
                        this.errorMessage(e, "fetchMQData with exception occur!!!");
                    }
                }
            }
            // 這裡按照QueueName, 主機名稱, IP進行排序
            ibmmqStatusList.sort((c1, c2) -> {
                int result = c1.getName().compareTo(c2.getName());
                if (result == 0) {
                    result = c1.getServiceHostName().compareTo(c2.getServiceHostName());
                    if (result == 0) {
                        return c1.getServiceIP().compareTo(c2.getServiceIP());
                    }
                    return result;
                }
                return result;
            });
            monitor.setIbmMQStatusList(ibmmqStatusList);
        }
    }

    /**
     * 獲取每個服務的監控數據
     *
     * @param monitor
     */
    private void fetchServiceData(ServiceMonitoring monitor) {
        // 先取FEP各個服務
        List<Sms> smsService = smsExtMapper.queryExcludeServiceName(
                Arrays.asList(
                        SERVICE_NAME_SYSTEM,
                        SERVICE_NAME_DB,
                        SERVICE_NAME_MQ,
                        SERVICE_NAME_NET_CLIENT,
                        SERVICE_NAME_NET_SERVER,
                        SERVICE_NAME_PROCESS
                ));
        List<ServiceStatus> serviceStatusList = new ArrayList<>();
        if (smsService != null) {
            for (Sms sms : smsService) {
                ServiceStatus serviceStatus = new ServiceStatus();
                serviceStatus.setServiceHostName(sms.getSmsHostname());
                serviceStatus.setServiceName(sms.getSmsServicename());
                serviceStatus.setServiceIP(sms.getSmsServiceip());
                if ("1".equals(sms.getSmsServicestate())) {
                    if (sms.getSmsStarttime() != null) {
                        serviceStatus.setStartTime(FormatUtil.dateTimeFormat(sms.getSmsStarttime()));
                    }
                    serviceStatus.setServiceState(STATUS_NORMAL);
                    serviceStatus.setServiceThreads(sms.getSmsThreads().toString());
                    serviceStatus.setServiceCpu(this.getCpu(sms.getSmsCpu()));
                    serviceStatus.setServiceRam(this.getRam(sms.getSmsRam()));
                } else if ("0".equals(sms.getSmsServicestate())) {
                    if (sms.getSmsStoptime() != null) {
                        serviceStatus.setStopTime(FormatUtil.dateTimeFormat(sms.getSmsStoptime()));
                    }
                    serviceStatus.setServiceState(STATUS_STOPPED);
                    serviceStatus.setServiceThreads(StringUtils.EMPTY);
                    serviceStatus.setServiceRam(StringUtils.EMPTY);
                    serviceStatus.setServiceCpu(StringUtils.EMPTY);
                } else {
                    serviceStatus.setServiceState(STATUS_UNKNOWN);
                    serviceStatus.setServiceThreads(StringUtils.EMPTY);
                    serviceStatus.setServiceRam(StringUtils.EMPTY);
                    serviceStatus.setServiceCpu(StringUtils.EMPTY);
                }
                serviceStatusList.add(serviceStatus);
            }
        }
        // 獲取每個Process的監控數據
        fetchProcessData(serviceStatusList);
        if (CollectionUtils.isNotEmpty(serviceStatusList)) {
            serviceStatusList.sort((c1, c2) -> {
                int result = c1.getServiceName().compareTo(c2.getServiceName());
                if (result == 0) {
                    return c1.getServiceIP().compareTo(c2.getServiceIP());
                }
                return result;
            });
        }
        monitor.setServiceStatusList(serviceStatusList);
    }

    /**
     * 獲取每個Process的監控數據
     *
     * @param serviceStatusList
     */
    private void fetchProcessData(List<ServiceStatus> serviceStatusList) {
        List<Sms> smsProcessList = smsExtMapper.selectByServiceName(SERVICE_NAME_PROCESS);
        if (CollectionUtils.isNotEmpty(smsProcessList)) {
            // 注意這裡要用GsonDateParser產出json字串, 因為AIX下解讀Date字串有問題, 所以GsonDateParser中會特別處理Date類型的欄位, 轉為字串處理
            GsonDateParser<List<Sms>> gsonDateParser = new GsonDateParser<List<Sms>>(new TypeToken<List<Sms>>() {}.getType());
            for (Sms smsProcess : smsProcessList) {
                if (StringUtils.isNotBlank(smsProcess.getSmsOthers())) {
                    try {
                        List<Sms> smsProcesses = gsonDateParser.readIn(smsProcess.getSmsOthers());
                        if (CollectionUtils.isNotEmpty(smsProcesses)) {
                            for (Sms sms : smsProcesses) {
                                ServiceStatus serviceStatus = new ServiceStatus();
                                serviceStatus.setServiceHostName(sms.getSmsHostname());
                                serviceStatus.setServiceName(sms.getSmsServicename());
                                serviceStatus.setServiceIP(sms.getSmsServiceip());
                                if ("1".equals(sms.getSmsServicestate())) {
                                    if (sms.getSmsStarttime() != null) {
                                        serviceStatus.setStartTime(FormatUtil.dateTimeFormat(sms.getSmsStarttime()));
                                    }
                                    serviceStatus.setServiceState(STATUS_NORMAL);
                                    serviceStatus.setServiceThreads(sms.getSmsThreads().toString());
                                    serviceStatus.setServiceCpu(this.getCpu(sms.getSmsCpu()));
                                    serviceStatus.setServiceRam(this.getRam(sms.getSmsRam()));
                                } else if ("0".equals(sms.getSmsServicestate())) {
                                    if (sms.getSmsStoptime() != null) {
                                        serviceStatus.setStopTime(FormatUtil.dateTimeFormat(sms.getSmsStoptime()));
                                    }
                                    serviceStatus.setServiceState(STATUS_STOPPED);
                                    serviceStatus.setServiceThreads(StringUtils.EMPTY);
                                    serviceStatus.setServiceRam(StringUtils.EMPTY);
                                    serviceStatus.setServiceCpu(StringUtils.EMPTY);
                                } else {
                                    serviceStatus.setServiceState(STATUS_UNKNOWN);
                                    serviceStatus.setStartTime(StringUtils.EMPTY);
                                    serviceStatus.setStopTime(StringUtils.EMPTY);
                                    serviceStatus.setServiceThreads(StringUtils.EMPTY);
                                    serviceStatus.setServiceRam(StringUtils.EMPTY);
                                    serviceStatus.setServiceCpu(StringUtils.EMPTY);
                                }
                                // 如果是APPMon Service主動關閉, 則會更新SmsServicestate為2
                                // 所以所有的process全部更新為2
                                if ("2".equals(smsProcess.getSmsServicestate())) {
                                    serviceStatus.setServiceState(STATUS_UNKNOWN);
                                    serviceStatus.setStartTime(StringUtils.EMPTY);
                                    serviceStatus.setStopTime(StringUtils.EMPTY);
                                    serviceStatus.setServiceThreads(StringUtils.EMPTY);
                                    serviceStatus.setServiceRam(StringUtils.EMPTY);
                                    serviceStatus.setServiceCpu(StringUtils.EMPTY);
                                }
                                serviceStatusList.add(serviceStatus);
                            }
                        }
                    } catch (Exception e) {
                        this.errorMessage(e, "fetchProcessData with exception occur!!!");
                    }
                }
            }
        }
    }

    /**
     * 獲取網絡服務端監控數據
     *
     * @param monitor
     */
    private void fetchNetworkServerData(ServiceMonitoring monitor) {
        List<Sms> smsNetworkServerList = smsExtMapper.selectByServiceName(SERVICE_NAME_NET_SERVER);
        if (CollectionUtils.isEmpty(smsNetworkServerList)) {
            monitor.setServerNetworkStatusList(null);
        } else {
            List<ServerNetworkStatus> serverNetworkStatusList = new ArrayList<>();
            GsonParser<List<ServerNetworkStatus>> gsonParser = new GsonParser<List<ServerNetworkStatus>>(new TypeToken<List<ServerNetworkStatus>>() {}.getType());
            for (Sms smsNetworkServer : smsNetworkServerList) {
                String jsonStr = smsNetworkServer.getSmsOthers();
                if (StringUtils.isNotBlank(jsonStr)) {
                    try {
                        List<ServerNetworkStatus> list = gsonParser.readIn(jsonStr);
                        if (CollectionUtils.isNotEmpty(list)) {
                            for (ServerNetworkStatus serverNetworkStatus : list) {
                                // 再根據status編號, 替換成相應的中文
                                if ("2".equals(smsNetworkServer.getSmsServicestate())) {
                                    serverNetworkStatus.setSocketCount("0");
                                    serverNetworkStatus.setServiceState("2");
                                }
                            }
                        }
                        serverNetworkStatusList.addAll(list);
                    } catch (Exception e) {
                        this.errorMessage(e, "fetchNetworkServerData with exception occur!!!");
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(serverNetworkStatusList)) {
                serverNetworkStatusList.sort((c1, c2) -> {
                    int result = c1.getServiceName().compareTo(c2.getServiceName());
                    if (result == 0) {
                        return c1.getServiceIP().compareTo(c2.getServiceIP());
                    }
                    return result;
                });
                monitor.setServerNetworkStatusList(serverNetworkStatusList);
            }
        }
    }


    /**
     * 獲取網絡客戶端監控數據
     *
     * @param monitor
     */
    private void fetchNetworkClientData(ServiceMonitoring monitor) {
        List<Sms> smsNetworkClientList = smsExtMapper.selectByServiceName(SERVICE_NAME_NET_CLIENT);
        if (CollectionUtils.isEmpty(smsNetworkClientList)) {
            monitor.setClientNetworkStatusList(null);
        } else {
            List<ClientNetworkStatus> clientNetworkStartList = new ArrayList<>();
            GsonParser<List<ClientNetworkStatus>> gsonParser = new GsonParser<List<ClientNetworkStatus>>(new TypeToken<List<ClientNetworkStatus>>() {}.getType());
            for (Sms smsNetworkClient : smsNetworkClientList) {
                String jsonStr = smsNetworkClient.getSmsOthers();
                if (StringUtils.isNotBlank(jsonStr)) {
                    try {
                        List<ClientNetworkStatus> list = gsonParser.readIn(jsonStr);
                        if (CollectionUtils.isNotEmpty(list)) {
                            for (ClientNetworkStatus clientNetworkStatus : list) {
                                // 再根據status編號, 替換成相應的中文
                                if ("2".equals(smsNetworkClient.getSmsServicestate())) {
                                    clientNetworkStatus.setState(NET_CLIENT_STATE_UNKNOWN);
                                    clientNetworkStatus.setLocalEndPoint(StringUtils.EMPTY);
                                    clientNetworkStatus.setSocketCount("0");
                                }
                            }
                            clientNetworkStartList.addAll(list);
                        }
                    } catch (Exception e) {
                        this.errorMessage(e, "fetchNetworkClientData with exception occur!!!");
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(clientNetworkStartList)) {
                clientNetworkStartList.sort((c1, c2) -> {
                    int result = c1.getServiceName().compareTo(c2.getServiceName());
                    if (result == 0) {
                        return c1.getLocalEndPoint().compareTo(c2.getLocalEndPoint());
                    }
                    return result;
                });
                monitor.setClientNetworkStatusList(clientNetworkStartList);
            }
        }
    }

    /**
     * 判斷服務是否有啟動
     *
     * @param sms
     * @return
     */
    private boolean isStarted(Sms sms) {
        boolean start = false;
        if ("1".equals(sms.getSmsServicestate())) {
            Date uDate = sms.getSmsUpdatetime();
            Calendar newCal = Calendar.getInstance();
            newCal.setTime(uDate);
            newCal.add(Calendar.MINUTE, 5);
            uDate = newCal.getTime();
            Date nowDate = new Date();
            if (uDate.getTime() > nowDate.getTime()) {
                start = true;
            }
        }
        return start;
    }

    /**
     * 計算CPU數據
     *
     * @param cpu
     * @return
     */
    private String getCpu(Integer cpu) {
        String rst = StringUtils.EMPTY;
        if (cpu != null) {
            rst = StringUtils.join(cpu / 100.00, "%");
        }
        return rst;
    }

    /**
     * 計算記憶體數據
     *
     * @param ram
     * @return
     */
    private String getRam(Integer ram) {
        String rst = StringUtils.EMPTY;
        if (ram != null) {
            rst = FormatUtil.longFormat(ram / 1024);
        }
        return rst;
    }
}
