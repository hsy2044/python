package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028270_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class UI_028270Controller extends BaseController {

    @Autowired
    RmService rmService;

    //add by maxine on 2011/06/27 for SYSSTAT自行查
    private Rmstat _defRMSTAT = new Rmstat();
    private String _ShowMessage;
    private List<Sysstat> _dtSYSSTAT;
    private String _SYSSTAT_HBKNO;
    @PostMapping(value = "/rm/UI_028270/pageLoad")
    @ResponseBody
    public UI_028270_Form pageLoad() {
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        UI_028270_Form form = new UI_028270_Form();
        querySYSSTAT(form);
        String webType = WebConfiguration.getInstance().getWebType();
        if (webType.equals("SSTQ")) {
            form.setExecuteBtnEnabled("false");
        } else {
            form.setExecuteBtnEnabled("true");
        }
        rmSTATGetAndBind(form);
        form.setMessage(MessageType.DANGER,_ShowMessage);
        return form;
    }


    private void querySYSSTAT(UI_028270_Form form) {
        try {
            _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                form.setMessage(MessageType.DANGER,"SYSSTAT無資料!!");
                return;
            }

            _SYSSTAT_HBKNO = _dtSYSSTAT.get(0).getSysstatHbkno();

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }


    @PostMapping(value = "/rm/UI_028270/executeClick")
    @ResponseBody
    public UI_028270_Form executeClick(@RequestBody UI_028270_Form form) throws Exception {
        FEPReturnCode rtnCode;
        rtnCode = rmSTATUpdate(form);
        if (rtnCode == CommonReturnCode.Normal) {
            form.setMessage(MessageType.INFO,_ShowMessage);
        } else {
            form.setMessage(MessageType.DANGER,_ShowMessage);
        }
        return form;
    }

    /**
     依條件更新RMSTAT 的 RMSTAT_AMLFLAG



     */
    private FEPReturnCode rmSTATUpdate(UI_028270_Form form) {
        String strMsg = "";

        try {
            _ShowMessage = "";

            //更新RMSTAT FLAG
            _defRMSTAT.setRmstatHbkno(_SYSSTAT_HBKNO);
            _defRMSTAT.setRmstatAmlflag(form.getQryAmlFlag().substring(0, 1));
            form.setAmlFlag(form.getQryAmlFlag());

            if (rmService.updateRMSTATbyPK(_defRMSTAT) < 0) {
                strMsg = "AML傳送狀態(RMSTAT)更新失敗";
                _ShowMessage = UpdateFail;
                return IOReturnCode.RMSTATUpdateError;
            } else {
                strMsg = "調整AML傳送狀態＝'"+form.getQryAmlFlag()+"'調整成功";
            }

            prepareAndSendEMSData(strMsg);

        } catch (Exception ex) {
            _ShowMessage = ex.toString();
            return CommonReturnCode.ProgramException;
        }

        _ShowMessage = DealSuccess;
        return CommonReturnCode.Normal;
    }

    private void rmSTATGetAndBind(UI_028270_Form form) {
        try {
            _defRMSTAT.setRmstatHbkno(_SYSSTAT_HBKNO);
            Rmstat rmstat = rmService.getRmstatByPk(_defRMSTAT);
            if (rmstat != null) {
                _defRMSTAT = rmstat;
                switch (_defRMSTAT.getRmstatAmlflag()) {
                    case "Y":
                        form.setAmlFlag("Y:啟動");
                        break;
                    case "R":
                        form.setAmlFlag("R:暫停中");
                        break;
                    case "N":
                        form.setAmlFlag("N:暫停");
                        break;
                }
            } else {
                _ShowMessage = QueryFail;
            }
        } catch (Exception ex) {
            _ShowMessage = ex.toString();
        }
    }

    private void prepareAndSendEMSData(String strMsg) {
        LogData logContext = new LogData();

        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028270");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setDesBkno(_SYSSTAT_HBKNO);
        logContext.setMessageId("UI028270");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setRemark(strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.ChangeAMLRMStatus);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);

    }

}