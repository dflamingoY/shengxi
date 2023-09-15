package org.xiaoxingqi.shengxi.core.http;


import org.xiaoxingqi.shengxi.model.qiniu.UploadData;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by yzm on 2017/11/17.
 * 阻塞式图片上传
 */

public class UpQiNiuManager {

    private LinkedList<UploadData> data = new LinkedList<>();
    private String token;
    private LoadStateListener mLoadStateListener;

    public UpQiNiuManager(String token, LoadStateListener listener, UploadData... ss) {
        data.clear();
        if (ss != null)
            data.addAll(Arrays.asList(ss));
        this.token = token;
        mLoadStateListener = listener;
    }


    /**
     * 取出数据继续上传
     */
    public void next() {
        if (data != null) {
            if (data.size() > 0) {
                String first = data.getFirst().getFile();
                upload(first, data.getFirst().getKey());
            } else {
                //上传完成
                if (mLoadStateListener != null) {
                    mLoadStateListener.success();
                    data.clear();
                }
            }
        }
    }

    private void upload(String path, String key) {
        try {
        } catch (NullPointerException e) {
            e.printStackTrace();
            if (mLoadStateListener != null) {
                mLoadStateListener.fail();
            }
        }
    }

    private void cancel() {
        QIniuUpLoadManager.cancelUp();
    }

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
    }
}
