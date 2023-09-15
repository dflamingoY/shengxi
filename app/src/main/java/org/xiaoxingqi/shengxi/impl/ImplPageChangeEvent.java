package org.xiaoxingqi.shengxi.impl;

/**
 * 切换到指定的界面
 */
public class ImplPageChangeEvent {
    private int page;

    public ImplPageChangeEvent(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }
}
