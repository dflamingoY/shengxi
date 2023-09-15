package org.xiaoxingqi.shengxi.model;

public class BaseRepData {


    /**
     * code : 0
     * msg : success
     * data : {"is_exist":0,"is_forbidden":0}
     */

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
