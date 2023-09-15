package org.xiaoxingqi.shengxi.core.http;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;


public class FasttObjectRequest<T> extends Request<T> {
    private Response.Listener<T> listener;
    private LinkedHashMap<String, String> params;
    private Class<T> clazz;
    private Context context;

    public FasttObjectRequest(String url, LinkedHashMap<String, String> params, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener, Context context) {
        super(Method.POST, url, errorListener);
        this.listener = listener;
        this.params = params;
        this.clazz = clazz;
        this.context = context;
    }

    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        T t=null;
        try {
            String json=new String(response.data,"UTF-8");
            if (!"".equals(json)&&json!=null) {
                t= JSON.parseObject(json, clazz);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Response.success(t, HttpHeaderParser.parseCacheHeaders(response));
    }


    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

//    protected Map<String, String> getParams() throws AuthFailureError {
//        return params;
//    }


    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }

    /**设置自请求时*/
    public RetryPolicy getRetryPolicy() {
        RetryPolicy retryPolicy = new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        return retryPolicy;
    }
}
