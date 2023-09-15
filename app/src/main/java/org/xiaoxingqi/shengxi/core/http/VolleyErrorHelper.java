package org.xiaoxingqi.shengxi.core.http;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.xiaoxingqi.shengxi.utils.LoadingException;
import org.xiaoxingqi.shengxi.utils.LocalLogUtils;

import java.net.SocketTimeoutException;

/**
 * Volley 错误请求辅助类
 * Created by Onlydyf on 2015/8/10.
 */
public class VolleyErrorHelper {

    public static String getMessage(Object error) {
        if (error instanceof Exception) {//记录错误日志到本地
            LocalLogUtils.writeLog(((Exception) error).getLocalizedMessage(), System.currentTimeMillis());
        }
        if (error instanceof IllegalArgumentException) {
            return "";
        } else if (error instanceof TimeoutError) {
            return "亲，您的手机网络不太顺畅喔~";
        } else if (isServerProblem(error)) {
            return "亲，服务器异常~";
        } else if (isNetWorkProblem(error)) {
            return "拜托拜托,亲快开网络~";
        } else if (error instanceof VolleyError) {
            return "";
        } else if (error instanceof LoadingException) {
            return ((LoadingException) error).getMessage();
        } else if (error instanceof SocketTimeoutException) {
            return "语音正在加载中，请耐心等一下哦~";
        }
        return "网络异常,请稍后再试~";
    }

    /**
     * 无网络连接
     *
     * @param error
     * @return
     */
    private static boolean isNetWorkProblem(Object error) {
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }

    /**
     * 服务器的响应的一个错误，最有可能的4xx或5xx HTTP状态代码
     *
     * @param error
     * @return
     */
    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }
}
