package com.syscom.fep.web.controller.inbk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SQLSortExpression;
import com.syscom.fep.web.form.inbk.UI_019270_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 查詢請求傳送滯留訊息-2280
 *
 * @author Joseph
 * @create 2022/12/14
 */
@Controller
public class UI_019270Controller extends BaseController {
    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019270_Form form = new UI_019270_Form();
        // 營業日期
        form.setInbkpendOriTbsdyFisc(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
        try {
            String sysstatHbkno= SysStatus.getPropertyValue().getSysstatHbkno();
            form.setBkno1(sysstatHbkno);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, "原交易STAN", DATA_INQUIRY_EXCEPTION_OCCUR);
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019270/queryClick")
    public String doInquiryDetail(@ModelAttribute UI_019270_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        BindGridData(form, mode);
        return Router.UI_019270.getView();
    }

    public void BindGridData(UI_019270_Form form, ModelMap mode) {
        try {
            PageInfo<Inbkpend> pageInfo;
            String datetime = form.getDatetime().replace("-", "");
            String datetimo = form.getDatetimeo().replace("-", "");
            String bkno = form.getBkno();
            String stan = form.getStan();
            String stan1 = form.getStan1();
            String trad = form.getTrad();
            String sysstatHbkno= SysStatus.getPropertyValue().getSysstatHbkno();

            // 首次按下查詢時預設的排序
            if (form.getSqlSortExpressionCount() == 0) {
                form.addSqlSortExpression("INBKPEND_ORI_BKNO_STAN", SQLSortExpression.SQLSortOrder.ASC);
            }
            if (StringUtils.isBlank(datetimo)) {
                pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
                        form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                            inbkService.getINBKPend2270(datetime,"",bkno,stan,trad, stan1,sysstatHbkno,form.getSqlSortExpression());
                    }
                });
            } else {
                pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(),
                        form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                            inbkService.getINBKPend2270(datetime,datetimo,bkno,stan, trad, stan1,sysstatHbkno,form.getSqlSortExpression());
                    }
                });
            }
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<UI_019270_Form, Inbkpend> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
    @PostMapping(value = "/inbk/UI_019270/doDownload")
    public ResponseEntity<?> doDownload(@RequestBody UI_019270_Form form, @ModelAttribute ModelMap mode) throws Exception {
        String fileName = "";
        List<Map<String, Object>> dt = new ArrayList<>();
        try {
            String datetime = form.getDatetime().replace("-", "");
            String datetimo = form.getDatetimeo().replace("-", "");
            String bkno = form.getBkno();
            String stan = form.getStan();
            String stan1 = form.getStan1();
            String trad = form.getTrad();
            String sysstatHbkno= SysStatus.getPropertyValue().getSysstatHbkno();
            if (StringUtils.isBlank(datetimo)) {
                dt = inbkService.getINBKPend2270csv(datetime,"",bkno,stan,trad, stan1,sysstatHbkno,form.getSqlSortExpression());
            }else{
                dt=  inbkService.getINBKPend2270csv(datetime,datetimo,bkno,stan, trad, stan1,sysstatHbkno,form.getSqlSortExpression());
            }
            if (null != dt && dt.size() > 0) {
                fileName =MakeCSV(dt);
                return download(fileName);
            }else{
                return this.handleDownloadExcption(new Exception("查無資料"));
            }
        }catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER,programError);
        }
        this.showMessage(mode, MessageType.INFO,"下載成功");
        return null;
    }
    private ResponseEntity<?> handleDownloadExcption(Exception e) {
        BaseResp<?> response = new BaseResp<>();
        this.errorMessage(e, e.getMessage());
        response.setMessage(MessageType.DANGER, StringUtils.join("下載失敗, ", programError));
        return ResponseEntity.ok(response);
    }
    private String MakeCSV(List<Map<String, Object>> dt) {
        String rtn ="";
        String tempOutputField = "";
        String savedPath = WebConfiguration.getInstance().getCsvSavedPath();
        String a = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSSSS);
        StringBuilder sb = new StringBuilder();
        BufferedWriter sw = null;
        FileWriter fw = null;
        try {
            String rt ="2270"+"-"+a+".csv";
            rtn =savedPath+ "/" +rt;
            String rtnn =savedPath;
            File file = new File(CleanPathUtil.cleanString(rtn));
            File files = new File(CleanPathUtil.cleanString(rtnn));
            if(!files.exists()) {
                if(files.mkdir()) {
            		LogHelperFactory.getTraceLogger().warn(files.getPath(), " mkdirs failed!!!"); 
            	}
            }


            if(file.exists() && file.isFile()){
                file.delete();
            }

            tempOutputField="原交易STAN,財金查詢日期時間,財金STAN,原PCODE,原交易EJ,扣款帳號,卡片帳號,轉入帳號,入帳帳號,處理結果,RC1,RC2,\r\n";
            fw = new FileWriter(file, true);
            sw = new BufferedWriter(fw);
            sw.write("\uFEFF");
            sw.write(tempOutputField);
            for (Map<String, Object> dr : dt) {
                sb = new StringBuilder();
                String INBKPEND_ORI_BKNO_STAN = "";
                String INBKPEND_TX_DT = "";
                String INBKPEND_BKNO_STAN ="";
                String INBKPEND_ORI_PCODE ="";
                String INBKPEND_ORI_EJFNO ="";
                String INBKPEND_TROUTBKNO ="";
                String INBKPEND_MAJOR_ACTNO ="";
                String INBKPEND_TRIN_BKNO ="";
                String INBKPEND_TRIN_ACTNO_ACTUAL ="";
                String INBKPEND_PRC_RESULT ="";
                String INBKPEND_ORI_REP_RC ="";
                String INBKPEND_ORI_CON_RC ="";

                if(StringUtils.isBlank(dr.get("INBKPEND_ORI_BKNO").toString()) && StringUtils.isBlank(dr.get("INBKPEND_ORI_STAN").toString())) {
                    INBKPEND_ORI_BKNO_STAN =",";
                }
                else {
                    INBKPEND_ORI_BKNO_STAN = dr.get("INBKPEND_ORI_BKNO").toString() +"-"+dr.get("INBKPEND_ORI_STAN").toString() +",";
                }
                if(StringUtils.isBlank(dr.get("INBKPEND_TX_DATE").toString()) && StringUtils.isBlank(dr.get("INBKPEND_TX_TIME").toString())) {
                    INBKPEND_TX_DT =",";
                }
                else {
                    INBKPEND_TX_DT = dr.get("INBKPEND_TX_DATE").toString() +" "+dr.get("INBKPEND_TX_TIME").toString() +",";
                }
                if(StringUtils.isBlank(dr.get("INBKPEND_BKNO").toString()) && StringUtils.isBlank(dr.get("INBKPEND_STAN").toString())) {
                    INBKPEND_BKNO_STAN =",";
                }
                else {
                    INBKPEND_BKNO_STAN = dr.get("INBKPEND_BKNO").toString() +"-"+dr.get("INBKPEND_STAN").toString() +",";
                }
                if(dr.get("INBKPEND_ORI_PCODE") == null) {
                    INBKPEND_ORI_PCODE =",";
                }
                else {
                    INBKPEND_ORI_PCODE = dr.get("INBKPEND_ORI_PCODE").toString()+",";
                }
                if(dr.get("INBKPEND_ORI_EJFNO") == null) {
                    INBKPEND_ORI_EJFNO =",";
                }
                else {
                    INBKPEND_ORI_EJFNO = dr.get("INBKPEND_ORI_EJFNO").toString()+",";
                }
                if(StringUtils.isBlank(dr.get("INBKPEND_TROUTBKNO").toString()) && StringUtils.isBlank(dr.get("INBKPEND_TROUT_ACTNO").toString())) {
                    INBKPEND_TROUTBKNO =",";
                }
                else {
                    INBKPEND_TROUTBKNO = dr.get("INBKPEND_TROUTBKNO").toString() +"-"+dr.get("INBKPEND_TROUT_ACTNO").toString() +",";
                }
                if(dr.get("INBKPEND_MAJOR_ACTNO") == null) {
                    INBKPEND_MAJOR_ACTNO =",";
                }
                else {
                    INBKPEND_MAJOR_ACTNO = dr.get("INBKPEND_MAJOR_ACTNO").toString()+",";
                }
                if(StringUtils.isBlank(dr.get("INBKPEND_TRIN_BKNO").toString()) && StringUtils.isBlank(dr.get("INBKPEND_TRIN_ACTNO").toString())) {
                    INBKPEND_TRIN_BKNO =",";
                }
                else {
                    INBKPEND_TRIN_BKNO = dr.get("INBKPEND_TRIN_BKNO").toString() +"-"+dr.get("INBKPEND_TRIN_ACTNO").toString() +",";
                }
                if(dr.get("INBKPEND_TRIN_ACTNO_ACTUAL") == null) {
                    INBKPEND_TRIN_ACTNO_ACTUAL =",";
                }
                else {
                    INBKPEND_TRIN_ACTNO_ACTUAL = dr.get("INBKPEND_TRIN_ACTNO_ACTUAL").toString()+",";
                }
                if(dr.get("INBKPEND_PRC_RESULT") == null) {
                    INBKPEND_PRC_RESULT =",";
                }
                else {
                    INBKPEND_PRC_RESULT = dr.get("INBKPEND_PRC_RESULT").toString()+",";
                }
                if(dr.get("INBKPEND_ORI_REP_RC") == null) {
                    INBKPEND_ORI_REP_RC =",";
                }
                else {
                    INBKPEND_ORI_REP_RC = dr.get("INBKPEND_ORI_REP_RC").toString()+",";
                }
                if(dr.get("INBKPEND_ORI_CON_RC") == null) {
                    INBKPEND_ORI_CON_RC =",\r\n";
                }
                else {
                    INBKPEND_ORI_CON_RC = dr.get("INBKPEND_ORI_CON_RC").toString()+",\r\n";
                }
                sb.append(INBKPEND_ORI_BKNO_STAN);
                sb.append(INBKPEND_TX_DT);
                sb.append(INBKPEND_BKNO_STAN);
                sb.append(INBKPEND_ORI_PCODE);
                sb.append(INBKPEND_ORI_EJFNO);
                sb.append(INBKPEND_TROUTBKNO);
                sb.append(INBKPEND_MAJOR_ACTNO);
                sb.append(INBKPEND_TRIN_BKNO);
                sb.append(INBKPEND_TRIN_ACTNO_ACTUAL);
                sb.append(INBKPEND_PRC_RESULT);
                sb.append(INBKPEND_ORI_REP_RC);
                sb.append(INBKPEND_ORI_CON_RC);

                sw.write(sb.toString());
            }
            sw.flush();
            sw.close();
        }catch (Exception e) {
            this.errorMessage(e, e.getMessage());
        }finally {
        	if (sw != null) { 
        		try { 
        			sw.close(); 
    			} catch (IOException e) { 
    				this.errorMessage(e, e.getMessage());
    			}
        	} 
        	if (fw != null) { 
        		try { 
        			fw.close(); 
    			} catch (IOException e) { 
    				this.errorMessage(e, e.getMessage());
    			}
        	} 
        }
        return rtn;
    }
}
