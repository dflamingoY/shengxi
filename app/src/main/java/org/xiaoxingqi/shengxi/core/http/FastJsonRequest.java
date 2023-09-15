package org.xiaoxingqi.shengxi.core.http;


import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DoctorKevin on 2017/6/1.
 */

public class FastJsonRequest<T> extends JsonRequest<T> {
    private Response.Listener mListener;
    private Class<T> clazz;
    private Response.ErrorListener errorListener;
    private Context mContext;

    public FastJsonRequest(Context context, String url, String requestBody, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, requestBody, listener, errorListener);
        mContext = context;
        mListener = listener;
        this.clazz = clazz;
        this.errorListener = errorListener;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
        T t = null;
        String json = null;
        try {
            json = new String(networkResponse.data, "utf-8");
            if (!"".equals(json) && json != null) {
                t = JSON.parseObject(json, clazz);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Response.success(t, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> map = new HashMap<>();
        //        String cookie = SPUtils.getString(mContext, "Cookie", "");
        //        map.put("cookie", cookie);
        map.put("Accept", "application/json");
        map.put("Content-Type", "application/json; charset=UTF-8");
        return map;
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        RetryPolicy retryPolicy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return retryPolicy;
    }
}
