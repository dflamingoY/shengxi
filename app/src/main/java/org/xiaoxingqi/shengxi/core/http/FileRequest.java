package org.xiaoxingqi.shengxi.core.http;


import android.os.Environment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;


import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.IConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yzm on 2018/4/4.
 */

public class FileRequest<T> extends Request<T> {
    private Class<T> clazz;
    private String url;
    private Response.Listener mListener;

    public FileRequest(int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.url = url;
        mListener = listener;
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse networkResponse) {
        String filename = null;
        try {
            File bootFile = new File(Environment.getExternalStorageDirectory(),
                    IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME);
            if (!bootFile.exists()) {
                bootFile.mkdirs();
            }
            File file = new File(bootFile, AppTools.getSuffix(url));
            FileOutputStream oop = new FileOutputStream(file, false);
            oop.write(networkResponse.data);
            oop.close();
            filename = file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.success(filename, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }

    @Override
    protected void deliverResponse(Object o) {
        mListener.onResponse(o);
    }

    /**
     * 更改超时时间 默认时长 10000s
     */
    @Override
    public RetryPolicy getRetryPolicy() {
        RetryPolicy retryPolicy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return retryPolicy;
    }
}
