package org.xiaoxingqi.shengxi.impl;

public interface LoadStateListener {
    /**
     * 必须全部图片成功 才会回调
     */
    void success();

    /**
     * 一张图片失败 全部失败
     */
    void fail();

    /**
     * 成功一张的回调 角标和key
     *
     * @param endTag
     * @param position
     */
    void oneFinish(String endTag, int position);

    /**
     * 进度
     *
     * @param current
     */
    void progress(long current);

}
