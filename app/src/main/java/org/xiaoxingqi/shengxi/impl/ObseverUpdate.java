package org.xiaoxingqi.shengxi.impl;


import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter;

/**
 * Created by DoctorKevin on 2017/6/22.
 * 观察者模式 实时高效的 更改按钮状态
 */

public interface ObseverUpdate {
    void changeStatue(boolean isSelect);

    void notifyParent(QuickAdapter adapter, boolean isSelected);
}
