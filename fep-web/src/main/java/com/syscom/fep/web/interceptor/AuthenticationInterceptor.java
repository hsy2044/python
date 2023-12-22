package com.syscom.fep.web.interceptor;

import com.google.gson.Gson;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.web.base.WebConst;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private LogHelper logger = LogHelperFactory.getTraceLogger();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            User user = WebUtil.getUser();
            if (user == null) {
                if ((handler instanceof HandlerMethod && WebUtil.isAjax(request, (HandlerMethod) handler))
                        || WebUtil.isAjax(request)) {
                    WebUtil.doAjaxRedirectToLogin(request, response);
                } else {
                    WebUtil.doRedirectToLogin(request, response);
                }
                logger.warn("Session Expired, there is no Logon User info which keep in session, Redirect to login page!!!");
                return false;
            }
        } catch (Throwable t) {
            logger.exceptionMsg(t, t.getMessage());
        }
        return true;
    }
}
