package org.xiaoxingqi.shengxi.core.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class AiliLoadManager {
    private String endpoint = "http://oss-cn-shenzhen.aliyuncs.com";
    private OSSAsyncTask task;

    public void load(Context context, String endpoint, String bucketName, String objectKey, String file, UpLoadListener listener) {
        if (!TextUtils.isEmpty(endpoint)) {
            this.endpoint = endpoint;
        }
        //        new Thread(() -> {        }).start();
        OSS oss = new OSSClient(context, this.endpoint, credentialProvider, conf);
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(bucketName, objectKey, paserSteam(file));
        put.setCRC64(OSSRequest.CRC64Config.YES);
        // 异步上传时可以设置进度回调
        put.setProgressCallback((request, currentSize, totalSize) -> {
            listener.progress(currentSize);
        });
        // 上传失败
        task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                listener.upFinish(result.getETag());
                LocalLogUtils.writeLog("AliLoadManager:文件上传成功", System.currentTimeMillis());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 上传失败
                listener.fail();
                try {
                    LocalLogUtils.writeLog("AliLoadManager:文件上传失败" + objectKey + clientExcepion.getMessage(), System.currentTimeMillis());
                } catch (Exception e) {
                    try {
                        LocalLogUtils.writeLog("AliLoadManager:文件上传失败" + objectKey + serviceException.getErrorCode(), System.currentTimeMillis());
                    } catch (Exception e1) {

                    }
                }
            }
        });
    }

    /**
     * 取消上传
     */
    public void cancelTask() {
        try {
            if (task != null) {
                if (!task.isCompleted())
                    task.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OSSCredentialProvider credentialProvider;
    private ClientConfiguration conf;

    public AiliLoadManager(String accessKeyId, String sercuryKeyId, String token) {
        credentialProvider = null;
        credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, sercuryKeyId, token);
        if (conf == null) {
            conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
            conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个
            conf.setMaxErrorRetry(0); // 失败后最大重试次数，默认2次
        }
    }

    /**
     * 1 检测图片是否是webp
     * 2 转换图片为jpeg
     * 3 上传
     *
     * @param path
     * @return
     */
    public byte[] paserSteam(String path) {
        //        byte[] buffer = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        String type = options.outMimeType;
        File file = new File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + System.currentTimeMillis() + ".jpg");
        if ("image/webp".equals(type)) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                path = file.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != bitmap)
                    bitmap.recycle();
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != file) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bos.toByteArray();
    }

}
