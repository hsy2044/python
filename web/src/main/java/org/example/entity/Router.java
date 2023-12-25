package org.example.entity;

import org.apache.commons.lang3.StringUtils;



public enum Router {
    DEFAULT(StringUtils.EMPTY, StringUtils.EMPTY, "預設頁面", StringUtils.EMPTY),

    LOGIN(StringUtils.EMPTY, StringUtils.EMPTY, "登錄頁面", "login"),
    FIRST(StringUtils.EMPTY, StringUtils.EMPTY, "開始畫面1", "first");
    private String parentId, parentName, name, url, view, code = StringUtils.EMPTY;
    private boolean sub;

    private Router(String parentId, String parentName, String name, String view) {
        this(parentId, parentName, name, view, false);
    }

    private Router(String parentId, String parentName, String name, String view, boolean sub) {
        this.parentId = parentId;
        this.parentName = parentName;
        this.name = name;
        this.view = view;
        this.sub = sub;
        if (StringUtils.isBlank(parentId) && StringUtils.isBlank(parentName)) {
            this.url = StringUtils.join("/", view);
        } else {
            this.url = StringUtils.join("/", parentId, "/", view, "/index");
        }
        this.code = getCode(view);
    }

    public static String getCode(String view) {
        try {
            String str = "/UI_";
            int index = view.indexOf(str);
            if (index > 0) {
                int beginIndex = index + str.length();
                int endIndex = view.indexOf("_", beginIndex + 1);
                if (endIndex > 0) {
                    return view.substring(beginIndex, endIndex);
                } else {
                    return view.substring(beginIndex, view.length());
                }
            }
        } catch (Exception e) {
        }
        int index = view.indexOf("/");
        if (index >= 0) {
            return view.substring(index + 1);
        }
        return view;
    }

    public static Router fromUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        for (Router router : values()) {
            if (router.getUrl().equals(url)) {
                return router;
            }
        }
        return null;
    }
    public String getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getView() {
        return view;
    }

    public boolean isSub() {
        return sub;
    }

    public String getCode() {
        return code;
    }
    public static Router fromView(String view) {
        if (StringUtils.isBlank(view)) {
            return null;
        }
        for (Router router : values()) {
            if (router.getView().equals(view)) {
                return router;
            }
        }
        return null;
    }
}
