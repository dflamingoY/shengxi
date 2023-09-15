package org.xiaoxingqi.shengxi.impl;

/*

 */
public class SocketEvent {

    private int type;//1回声成功    2 用户被限制了

    public SocketEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    private String msg;

    public SocketEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
