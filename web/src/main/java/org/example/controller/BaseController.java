package org.example.controller;


import org.apache.commons.lang3.StringUtils;
import org.example.base.FEPWebBase;
import org.example.util.WebUtil;
import org.springframework.ui.ModelMap;


public class BaseController extends FEPWebBase {
    /**
     * 頁面加載初始化方法
     *
     * @param mode
     */
    public void pageOnLoad(ModelMap mode) {
    }
    protected String redirectToUrl(String url) {
        String contextPath = WebUtil.getRequest().getContextPath();
        if (StringUtils.isNotBlank(contextPath) && url.contains(contextPath)) {
            url = StringUtils.replace(url, contextPath, StringUtils.EMPTY);
        }
//        this.infoMessage("redirect to [", url, "]");
        return StringUtils.join("redirect:", url);
    }
}