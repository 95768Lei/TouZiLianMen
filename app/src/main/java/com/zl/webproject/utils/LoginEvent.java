package com.zl.webproject.utils;

/**
 * Created by Administrator on 2017/9/11.
 */

public class LoginEvent {
    private boolean isOk;
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }
}
