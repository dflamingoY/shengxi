package org.xiaoxingqi.shengxi.core.http;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;

import org.xiaoxingqi.shengxi.impl.DownProxyListener;
import org.xiaoxingqi.shengxi.impl.VoiceLoadErrorListener;
import org.xiaoxingqi.shengxi.impl.VoiceLoadSuccessListener;
import org.xiaoxingqi.shengxi.model.BaseAnimBean;
import org.xiaoxingqi.shengxi.model.ISBNBaseData;
import org.xiaoxingqi.shengxi.model.login.LoginData;
import org.xiaoxingqi.shengxi.modules.login.LoginActivity;
import org.xiaoxingqi.shengxi.utils.AppTools;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.LoadingException;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;
import org.xiaoxingqi.shengxi.utils.PreferenceTools;
import org.xiaoxingqi.shengxi.utils.download.DownLoadPool;
import org.xiaoxingqi.shengxi.utils.download.DownVoiceProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkClientHelper {

    private static OkHttpClient mClient;

    public static void init() {
        synchronized (OkClientHelper.class) {
            if (mClient == null) {
                initClient();
            }
        }
    }

    public static OkHttpClient getClient() {
        if (mClient == null)
            initClient();
        return mClient;
    }

    /**
     * 取消所有的task
     */
    public static void cancelAllTask() {
        mClient.dispatcher().cancelAll();
    }

    private static void initClient() {
        mClient = getUnsafeOkHttpClient();
    }

    /**
     * 设置禁止加载的标记
     */
    public static void setCancel(boolean _isFilter) {
        isFilter = _isFilter;
    }

    private static boolean isFilter = false;

    /**
     * put请求
     */
    public static void put(Activity context, String url, RequestBody body, Class clazz, OkResponse okResponse, String... v2) {
        Request.Builder builder = new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(IConstant.PORT + url)
                .put(body);

        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }

        if (v2 != null && v2.length > 0) {
            for (String version : v2) {
                if (("V3.2".equalsIgnoreCase(version))) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.2+json");
                }
            }
        }

        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (e instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~
                        context.runOnUiThread(() -> okResponse.onFailure("亲，您的手机网络不太顺畅喔~"));
                    } else {
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                //                Log.d("NetLgo", "result-->" + result);
                int code = response.code();
                LocalLogUtils.writeLog("put刷新token result : " + result, System.currentTimeMillis());
                try {
                    context.runOnUiThread(() -> {
                        try {
                            if (code == 412) {//特殊处理
                                LocalLogUtils.writeLog("OkClientHelper:put requestUrl  : " + call.request().url(), System.currentTimeMillis());
                                loginOut(context);
                                okResponse.onFailure("412");
                            }
                            okResponse.success(JSON.parseObject(result, clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                            okResponse.onFailure(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    response.close();
                }
            }
        });
    }

    public static void options(Activity context, String url, RequestBody body, Class clazz, OkResponse okResponse) {
        Request.Builder builder = new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(IConstant.PORT + url)
                .method("OPTIONS", body);
        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }

        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (e instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~
                        context.runOnUiThread(() -> okResponse.onFailure("亲，您的手机网络不太顺畅喔~"));
                    } else {
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                int code = response.code();
                //                Log.d("NetLgo", "result-->" + result);
                try {
                    context.runOnUiThread(() -> {
                        try {
                            if (code == 412) {//特殊处理
                                LocalLogUtils.writeLog("OkClientHelper:options requestUrl  : " + call.request().url(), System.currentTimeMillis());
                                loginOut(context);
                                okResponse.onFailure("412");
                            }
                            okResponse.success(JSON.parseObject(result, clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                            okResponse.onFailure(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    response.close();
                }
            }
        });

    }

    /**
     * PATCH 请求  完善数据
     *
     * @param context
     * @param url
     * @param body
     * @param clazz
     * @param okResponse
     */
    public static void patch(Activity context, String url, RequestBody body, Class clazz, OkResponse okResponse, String... v2) {
        Request.Builder builder = new Request.Builder()
                .patch(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(IConstant.PORT + url);
        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }
        if (null != v2 && v2.length > 0) {
            for (String version : v2) {
                if ("V3.0".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.0+json");
                } else if ("V3.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.1+json");
                } else if (("V3.2".equalsIgnoreCase(version))) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.2+json");
                } else if ("V3.5".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.5+json");
                } else if ("V3.6".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.6+json");
                } else if ("V3.8".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.8+json");
                } else if ("V4.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.1+json");
                } else if ("V4.2".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.2+json");
                } else if ("V4.3".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.3+json");
                }
            }
        }
        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (e instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~
                        context.runOnUiThread(() -> okResponse.onFailure("亲，您的手机网络不太顺畅喔~"));
                    } else {
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                //                Log.d("NetLgo", "result-->" + result);
                int code = response.code();
                try {
                    context.runOnUiThread(() -> {
                        try {
                            if (code == 412) {//特殊处理
                                LocalLogUtils.writeLog("OkClientHelper:patch requestUrl  : " + call.request().url(), System.currentTimeMillis());
                                loginOut(context);
                                okResponse.onFailure("412");
                            }
                            okResponse.success(JSON.parseObject(result, clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                            okResponse.onFailure(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * DELETE  请求
     *
     * @param context
     * @param url
     * @param body
     * @param clazz
     * @param okResponse
     */
    public static void delete(Activity context, String url, FormBody body, Class clazz, OkResponse okResponse, String... v2) {
        if (isFilter) {
            return;
        }
        Request.Builder builder = new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(IConstant.PORT + url)
                .delete(body);
        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }
        if (v2 != null && v2.length > 0) {
            for (String version : v2) {
                if ("V3.0".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.0+json");
                } else if ("V3.2".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.2+json");
                } else if ("V3.6".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.6+json");
                } else if ("V3.8".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.8+json");
                } else if ("V4.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.1+json");
                } else if ("V4.2".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.2+json");
                } else if ("V4.3".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.3+json");
                }
            }
        }
        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (e instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~
                        context.runOnUiThread(() -> okResponse.onFailure("亲，您的手机网络不太顺畅喔~"));
                    } else {
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                //                Log.d("NetLgo", "result--> delete " + result);
                int code = response.code();
                try {
                    context.runOnUiThread(() -> {
                        try {
                            if (code == 412) {//特殊处理
                                LocalLogUtils.writeLog("OkClientHelper:delete requestUrl  : " + call.request().url(), System.currentTimeMillis());
                                loginOut(context);
                                okResponse.onFailure("412");
                            }
                            okResponse.success(JSON.parseObject(result, clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                            okResponse.onFailure(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    response.close();
                }
            }
        });
    }

    public static void post(Activity context, String url, FormBody body, Class clazz, OkResponse okResponse, String... v2) {
        Request.Builder builder = new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url(IConstant.PORT + url)
                .post(body);
        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }
        if (v2 != null && v2.length > 0) {
            for (String version : v2) {
                if ("V3.0".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.0+json");
                } else if ("V3.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.1+json");
                } else if (("V3.2".equalsIgnoreCase(version))) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.2+json");
                } else if ("V3.3".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.3+json");
                } else if ("V3.4".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.4+json");
                } else if ("V3.5".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.5+json");
                } else if ("V3.6".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.6+json");
                } else if ("V3.7".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.7+json");
                } else if ("V3.8".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.8+json");
                } else if ("V4.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.1+json");
                } else if ("V4.2".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.2+json");
                } else if ("V4.3".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.3+json");
                } else if ("V4.5".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.5+json");
                }
            }
        }
        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (e instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~
                        context.runOnUiThread(() -> okResponse.onFailure("亲，您的手机网络不太顺畅喔~"));
                    } else {
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                    LocalLogUtils.writeLog("post : request:url:" + call.request().url() + ",reason : " + e.getMessage(), System.currentTimeMillis());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                //                Log.d("NetLgo", "result--> post  " + result);
                int code = response.code();
                try {
                    context.runOnUiThread(() -> {
                        try {
                            if (code == 412) {//特殊处理
                                LocalLogUtils.writeLog("OkClientHelper: requestUrl  : " + call.request().url(), System.currentTimeMillis());
                                loginOut(context);
                                okResponse.onFailure("412");
                            }
                            okResponse.success(JSON.parseObject(result, clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                            okResponse.onFailure(result);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    response.close();
                }
            }
        });
    }

    public static void get(Activity context, String url, Class clazz, OkResponse okResponse, String... v2) {
        if (isFilter) {
            okResponse.onFailure("");
            return;
        }
        Request.Builder builder = new Request.Builder()
                .url(IConstant.PORT + url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded");
        if (v2 != null && v2.length > 0) {
            for (String version : v2) {
                if ("V3.0".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.0+json");
                } else if ("V3.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.1+json");
                } else if (("V3.2".equalsIgnoreCase(version))) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.2+json");
                } else if ("V3.3".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.3+json");
                } else if ("V3.5".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.5+json");
                } else if ("V3.6".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.6+json");
                } else if ("V3.7".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.7+json");
                } else if ("V3.8".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v3.8+json");
                } else if ("V4.0".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.0+json");
                } else if ("V4.1".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.1+json");
                } else if ("V4.2".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.2+json");
                } else if ("V4.3".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.3+json");
                } else if ("V4.5".equalsIgnoreCase(version)) {
                    builder.addHeader("Accept", "application/vnd.shengxi.v4.5+json");
                }
            }
        }
        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }
        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (e instanceof SocketTimeoutException) {//亲，您的手机网络不太顺畅喔~
                        context.runOnUiThread(() -> okResponse.onFailure("亲，您的手机网络不太顺畅喔~"));
                    } else {
                        context.runOnUiThread(() -> okResponse.onFailure(""));
                    }
                    LocalLogUtils.writeLog("get : request:url:" + call.request().url() + ",reason : " + e.getMessage(), System.currentTimeMillis());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("NetLgo", call.request().url() + "      result--> get " + result);
                int code = response.code();
                try {
                    context.runOnUiThread(() -> {
                        try {
                            if (code == 412) {//特殊处理
                                LocalLogUtils.writeLog("OkClientHelper:get requestUrl  : " + call.request().url(), System.currentTimeMillis());
                                loginOut(context);
                                okResponse.onFailure("412");
                            } else if (code != 200) {
                                LocalLogUtils.writeLog("OkClientHelper: " + call.request().url() + "  response  : " + result, System.currentTimeMillis());
                            }
                            okResponse.success(JSON.parseObject(result, clazz));
                        } catch (Exception e) {
                            e.printStackTrace();
                            okResponse.onFailure(e);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    response.close();
                }
            }
        });
    }

    private static void loginOut(Context context) {
        LocalLogUtils.writeLog("OkClientHelper: 重新登录App   " + context.getClass().getName(), System.currentTimeMillis());
        if (LoginActivity.Companion.getInstance() == null) {
            PreferenceTools.clear(context, IConstant.LOCALTOKEN);
            PreferenceTools.clear(context, IConstant.USERCACHE);
            context.startActivity(new Intent(context, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            NIMClient.getService(AuthService.class).logout();
        }
    }

    public static void getIsbn(Activity context, String url, Class clazz, OkResponse okResponse) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "APPCODE 457aa641da364b94a915d237fa8fb357")
                .url(url).build();

        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                context.runOnUiThread(() -> okResponse.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = new String(response.body().bytes(), Charset.forName("UTF-8"));
                //                Log.d("Mozator", "" + result);
                context.runOnUiThread(() -> {
                    try {
                        ISBNBaseData data = JSON.parseObject(result, ISBNBaseData.class);
                        if (data.getError_code() != 0) {
                            okResponse.onFailure(data.getReason());
                        } else {
                            okResponse.success(JSON.parseObject(result, clazz));
                        }
                    } catch (Exception e) {
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                });
            }
        });
    }

    public static void getWx(Activity context, String url, Class clazz, OkResponse okResponse) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                context.runOnUiThread(() -> okResponse.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = new String(response.body().bytes(), Charset.forName("UTF-8"));
                //                Log.d("NetLgo", "result-->" + result);
                context.runOnUiThread(() -> {
                    try {
                        okResponse.success(JSON.parseObject(result, clazz));
                    } catch (Exception e) {
                        e.printStackTrace();
                        context.runOnUiThread(() -> okResponse.onFailure(e));
                    }
                });
            }
        });
    }

    /**
     * 文件下载
     *
     * @param context
     * @param url
     */
    public static void downFile(Activity context, String url, VoiceLoadSuccessListener listener, VoiceLoadErrorListener errorListener) {
        if (context != null && DownLoadPool.getInstance().isLoading(url)) {
            errorListener.onErrorResponse(new LoadingException("正在加载中"));
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME + "/" + AppTools.getSuffix(url));
        if (file.exists() && file.length() > 0) {
            errorListener.onErrorResponse(new LoadingException("下载完成,请点击播放"));
            return;
        }
        if (TextUtils.isEmpty(url)) {
            errorListener.onErrorResponse(new LoadingException("音频资源错误"));
            return;
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                LocalLogUtils.writeLog("DOWNLOAD:音频文件下载出错:url:" + call.request().url() + "  reason: " + e.getMessage(), System.currentTimeMillis());
                if (context != null)
                    context.runOnUiThread(() -> errorListener.onErrorResponse(e));
                else
                    errorListener.onErrorResponse(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                /**
                 * 保存本地文件
                 */
                try {
                    String filename;
                    File bootFile = new File(Environment.getExternalStorageDirectory(),
                            IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME);
                    if (!bootFile.exists()) {
                        bootFile.mkdirs();
                    }
                    File file = new File(bootFile, AppTools.getSuffix(url));
                    filename = file.getAbsolutePath();
                    if (file.exists() && file.length() > 0) {
                        if (context != null)
                            context.runOnUiThread(() -> {
                                try {
                                    listener.onResponse(filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    context.runOnUiThread(() -> {
                                        errorListener.onErrorResponse(e);
                                    });
                                }
                            });
                    } else {
                        FileOutputStream oop = new FileOutputStream(file, false);
                        oop.write(response.body().bytes());
                        oop.close();
                        if (context != null)
                            context.runOnUiThread(() -> {
                                try {
                                    listener.onResponse(filename);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    context.runOnUiThread(() -> {
                                        errorListener.onErrorResponse(e);
                                    });
                                }
                            });
                        else {
                            listener.onResponse(filename);
                        }
                    }
                } catch (IOException e) {
                    LocalLogUtils.writeLog("DOWNLOAD:音频文件写入出错" + "  : " + e.getMessage(), System.currentTimeMillis());
                }
            }
        });
    }


    /**
     * 文件下载
     *
     * @param context
     * @param url
     */
    public static void downWave(Activity context, String url, VoiceLoadSuccessListener listener, VoiceLoadErrorListener errorListener) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {//
            @Override
            public void onFailure(Call call, IOException e) {
                LocalLogUtils.writeLog("DOWNLOAD:音频文件下载出错:url:" + call.request().url() + "  reason: " + e.getMessage(), System.currentTimeMillis());
                context.runOnUiThread(() -> errorListener.onErrorResponse(e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                /**
                 * 保存本地文件
                 */
                try {
                    String filename;
                    File bootFile = new File(Environment.getExternalStorageDirectory(),
                            IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME);
                    if (!bootFile.exists()) {
                        bootFile.mkdirs();
                    }
                    File file = new File(bootFile, AppTools.getSuffix(url));
                    filename = file.getAbsolutePath();
                    FileOutputStream oop = new FileOutputStream(file, false);
                    oop.write(response.body().bytes());
                    oop.close();
                    context.runOnUiThread(() -> {
                        try {
                            listener.onResponse(filename);
                        } catch (Exception e) {
                            e.printStackTrace();
                            context.runOnUiThread(() -> {
                                errorListener.onErrorResponse(e);
                            });
                        }
                    });
                } catch (IOException e) {
                    LocalLogUtils.writeLog("DOWNLOAD:音频文件写入出错" + "  : " + e.getMessage(), System.currentTimeMillis());
                }
            }
        });
    }

    //下载文件, 指定保存的路径
    public static void downByPath(Activity context, String url, String targetPath, VoiceLoadSuccessListener listener, VoiceLoadErrorListener errorListener) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LocalLogUtils.writeLog("DOWNLOAD:敏感词下载异常:url:" + call.request().url() + "  reason: " + e.getMessage(), System.currentTimeMillis());
                if (context != null) {
                    context.runOnUiThread(() -> errorListener.onErrorResponse(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    //通信成功, 保存文件
                    try {
                        FileOutputStream oop = new FileOutputStream(new File(targetPath), false);
                        oop.write(response.body().bytes());
                        oop.close();
                        context.runOnUiThread(() -> listener.onResponse(targetPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                        context.runOnUiThread(() -> errorListener.onErrorResponse(e));
                    }
                } else {
                    context.runOnUiThread(() -> errorListener.onErrorResponse(new LoadingException("0")));
                }
            }
        });
    }

    public static void downProxy(Activity context, BaseAnimBean bean, VoiceLoadSuccessListener listener, VoiceLoadErrorListener errorListener) {
        DownVoiceProxy.getDownProxy().addPool(bean, new DownProxyListener() {
            @Override
            public void fail(String o) {
                context.runOnUiThread(() -> errorListener.onErrorResponse(new LoadingException(o)));
            }

            @Override
            public void success(String o) {
                context.runOnUiThread(() -> listener.onResponse(o));
            }
        });
    }

    /**
     * 查询云信的用户信息
     *
     * @param context
     * @param url
     * @param clazz
     * @param okResponse
     */
    public static void syncGet(Context context, String url, Class clazz, OkResponse okResponse) {
        Request.Builder builder = new Request.Builder()
                .url(IConstant.PORT + url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded");
        LoginData.LoginBean loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean.class);
        if (loginBean != null && !TextUtils.isEmpty(loginBean.getToken())) {
            builder.addHeader("Authorization", "Bearer " + loginBean.getToken());
        }
        Call call = mClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okResponse.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                //                Log.d("NetLgo", "result-->" + result);
                int code = response.code();
                try {
                    okResponse.success(JSON.parseObject(result, clazz));
                } catch (Exception e) {
                    e.printStackTrace();
                    okResponse.onFailure(e);
                }
            }
        });
    }

    /**
     * 配置https 的请求协议安全
     *
     * @return
     */
    private static OkHttpClient getUnsafeOkHttpClient() {


        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }};
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext
                    .getSocketFactory();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient = okHttpClient.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
