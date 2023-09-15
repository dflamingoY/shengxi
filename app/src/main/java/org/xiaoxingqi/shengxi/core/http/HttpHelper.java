package org.xiaoxingqi.shengxi.core.http;

import android.content.Context;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.xiaoxingqi.shengxi.model.BaseRepData;
import org.xiaoxingqi.shengxi.utils.IConstant;

import java.util.Map;

/**
 * 网络请求类
 * Created by Onlydyf on 2015/8/10.
 */
public class HttpHelper {
    private Context context;
    private ImageLoader loader;
    private RequestQueue queue;
    private Toast mToast;
    private String versionInfo = "";

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public HttpHelper(Context context) {
        this.context = context;
        if (queue == null) {
            this.queue = Volley.newRequestQueue(this.context);
            //            String makerName = PhoneUtil.getMakerName();//厂家
            //            String version = PhoneUtil.getVersion();//系统版本
            //            String model = PhoneUtil.getModel();//手机型号
            //            versionInfo = makerName + "_" + model + "_" + version;
            //            versionInfo = versionInfo.replace(" ", "");
        }
    }


    /**
     * 错误监听
     */
    public Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(context, VolleyErrorHelper.getMessage(volleyError), Toast.LENGTH_SHORT).show();
        }
    };


    public void NetObject(int requestType, String url, Map<String, String> params, Class clazz, Response.Listener listener, Response.ErrorListener nowErrorListener) {
        FastObjectRequest request;
        try {
            request = new FastObjectRequest(context, requestType, url, params, clazz, listener, nowErrorListener);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NetArray(String url, Map<String, String> params, Class clazz, Response.Listener listener, Response.ErrorListener nowErrorListener) {
        try {
            FastArrayRequest request = new FastArrayRequest(url, params, clazz, listener, nowErrorListener);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NetMap(String url, Map<String, String> params, Response.Listener listener, Response.ErrorListener nowErrorListener) {
        try {
            FastMapRequest request = new FastMapRequest(url, params, listener, nowErrorListener);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NetObject(int requestType, String url, Map<String, String> params, Class clazz, Response.Listener listener) {
        try {
            FastObjectRequest request = new FastObjectRequest(context, requestType, url /*+ "&device_version=" + versionInfo*/, params, clazz, listener, errorListener);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T> void NetObject(int requestType, String url, Map<String, String> params, Class<T> clazz, OkHttpListener listener) {
        FastObjectRequest request = new FastObjectRequest(context, requestType, url/* + "&device_version=" + versionInfo*/, params, String.class, o -> {
            try {
                BaseRepData temp = JSON.parseObject((String) o, BaseRepData.class);
                if (temp.getCode() == IConstant.RESULT_CODE_SUCCESS) {
                    T t = JSON.parseObject((String) o, clazz);
                    listener.onSuccess(t);
                } else {
                    BaseRepData response = JSON.parseObject((String) o, BaseRepData.class);
                    listener.onStateError(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailed(e);
            }
        }, volleyError -> {
            listener.onFailed(volleyError);
        });
        queue.add(request);
    }


    public void NetArray(String url, Map<String, String> params, Class clazz, Response.Listener listener) {
        try {
            FastArrayRequest request = new FastArrayRequest(url, params, clazz, listener, errorListener);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NetMap(String url, Map<String, String> params, Response.Listener listener) {
        try {
            FastMapRequest request = new FastMapRequest(url, params, listener, errorListener);
            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NetJson(String url, String body, Class clazz, Response.Listener listener, Response.ErrorListener errorListener) {
        FastJsonRequest request = new FastJsonRequest(context, url, body, clazz, listener, errorListener);
        queue.add(request);
    }

    public void NetDownLoad(String url, Response.Listener listener, Response.ErrorListener errorListener) {
        FileRequest request = new FileRequest(Request.Method.GET, url, listener, errorListener);
        queue.add(request);
    }

    /**
     * 谈吐死
     */
    private void showToast(String text) {
        if (mToast == null)
            mToast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        else
            mToast.setText(text);
        mToast.show();
    }

    public void stop() {
        queue.stop();
    }

    public void start() {
        queue.start();
    }

    //清除当前activity的请求队列。
    public void cancelAll() {
        queue.stop();
        queue.cancelAll(context);
    }

    public void special(Request request) {
        queue.add(request);
    }

}