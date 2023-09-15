package org.xiaoxingqi.shengxi.core.http;

import android.content.Context;

import org.xiaoxingqi.shengxi.impl.LoadStateListener;
import org.xiaoxingqi.shengxi.model.OssToken;
import org.xiaoxingqi.shengxi.model.qiniu.UploadData;

import java.util.Arrays;
import java.util.LinkedList;

public class AliLoadFactory {
    private LinkedList<UploadData> data = new LinkedList<>();
    private LoadStateListener mLoadStateListener;
    private final AiliLoadManager manager;
    private Context context;
    private String bucketName;
    private String endPoint;

    public AliLoadFactory(Context context, String endPoint, String bucketName, OssToken token, LoadStateListener listener, UploadData... ss) {
        data.clear();
        this.context = context;
        if (ss != null)
            data.addAll(Arrays.asList(ss));
        mLoadStateListener = listener;
        this.endPoint = endPoint;
        this.bucketName = bucketName;
        manager = new AiliLoadManager(token.getAccessKeyId(), token.getAccessKeySecret(), token.getSecurityToken());
        next();
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
            manager.load(context, endPoint, bucketName, key, path, new UpLoadListener() {
                @Override
                public void upFinish(String endTag) {
                    if (mLoadStateListener != null) {
                        mLoadStateListener.oneFinish(endTag, data.size() - 1);
                    }
                    data.removeFirst();
                    next();
                }

                @Override
                public void fail() {
                    if (mLoadStateListener != null) {
                        mLoadStateListener.fail();
                    }
                }

                @Override
                public void progress(long current) {
                    if (mLoadStateListener != null) {
                        mLoadStateListener.progress(current);
                    }
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            if (mLoadStateListener != null) {
                mLoadStateListener.fail();
            }
        }
    }

    public void cancel() {
        manager.cancelTask();
    }
}
