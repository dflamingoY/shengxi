package org.xiaoxingqi.shengxi.impl;

public class EchoSetEvent {
    private boolean isCheck;//是否设置繁忙状态
    private String tips;
    private int hobbies;

    public EchoSetEvent(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public EchoSetEvent(boolean isCheck, String tips, int hobbies) {
        this.isCheck = isCheck;
        this.tips = tips;
        this.hobbies = hobbies;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public String getTips() {
        return tips;
    }

    public int getHobbies() {
        return hobbies;
    }
}
