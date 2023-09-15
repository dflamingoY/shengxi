package org.xiaoxingqi.shengxi.impl;

import android.support.annotation.Nullable;

import org.xiaoxingqi.shengxi.model.BaseBean;

public interface ItemOperator {
    /**
     * 删除
     *
     * @param bean
     */
    void onDelete(BaseBean bean);

    /**
     * 共享
     *
     * @param bean
     */
    void onRecommend(BaseBean bean);

    /**
     * 取消共享
     *
     * @param bean
     */
    void onUnRecommend(BaseBean bean);

    /**
     * 共鸣
     *
     * @param bean
     */
    void onthumb(BaseBean bean);

    /**
     * 取消共鸣
     *
     * @param bean
     */
    void onUnThumb(BaseBean bean);

    /**
     * 失败 网络异常
     */
    void onFailure(Object e);

    void onComment(@Nullable String from_id);

    /**
     * 举报完成
     */
    void onReport(String type);

    /**
     * 好友请求
     */
    void onFriend();

    /**
     * 设置隐私设置
     */
    void onPrivacy(BaseBean bean);

    /**
     * 超管设置设置用户的声兮为隐藏
     */
    void onAdminPrivacy(BaseBean bean);

    /**
     * 超管操作失败
     */
    void onAdminFail();
}
