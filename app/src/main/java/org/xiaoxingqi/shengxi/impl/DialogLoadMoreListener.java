package org.xiaoxingqi.shengxi.impl;

/**
 * 时光机中播放列表item 点击的监听
 */
public interface DialogLoadMoreListener {

    void onClickItem(int position);

    void changeData(String key);
}
