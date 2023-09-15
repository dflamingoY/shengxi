package org.xiaoxingqi.shengxi.core.http;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import org.xiaoxingqi.shengxi.model.SignCacheData;
import org.xiaoxingqi.shengxi.utils.IConstant;
import org.xiaoxingqi.shengxi.utils.PreferenceTools;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 用来加载头像背景图, 判断是否是最新的图片
 * 出现问题:引用了被销毁的Activity
 * 容易导致内存泄漏, 程序闪退
 * HashMap 频繁操作时, 存在内存泄漏的风险  寻找替代方案
 */
public class GlideJudeUtils {
    //存在内存泄漏的风险
    public static HashMap<String, String> timeMap = new HashMap<>();

    private OkHttpClient client;
    private Activity activity;

    public GlideJudeUtils(Activity activity) {
        client = new OkHttpClient.Builder()
                .connectTimeout(5L, TimeUnit.SECONDS)
                .readTimeout(5L, TimeUnit.SECONDS)
                .build();
        this.activity = activity;
    }

    /**
     * 获取最后一次修改的时间
     */
    public String getLastModified(String url) {
        String lastModified = timeMap.get(url);
        if (TextUtils.isEmpty(lastModified))
            return "";
        else
            return lastModified;
    }

    /**
     * @param url          请求地址
     * @param imageView    展示的iv
     * @param defaultResId 默认资源  -1 当前界面需要展示
     * @param lastModified {@link #getLastModified(String url)}
     * @param isLoadable   是否禁止立刻加载图片
     */
    public void loadGlide(String url, ImageView imageView, final int defaultResId, String lastModified, boolean... isLoadable) {
        /**
         * 预先加载缓存图片
         */
        if (null != isLoadable && isLoadable.length > 0 && isLoadable[0]) {//不加载

        } else {
            if (activity.isDestroyed()) {
                return;
            }
            Glide.with(activity)
                    .applyDefaultRequestOptions(new RequestOptions()
                            .placeholder(defaultResId)
                            .error(defaultResId)
                            .signature(new ObjectKey(lastModified)))
                    .asBitmap()
                    .load(url)
                    .into(imageView);
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        imageView.setContentDescription(url);
        Request request = new Request.Builder()
                .addHeader("If-Modified-Since", lastModified)
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!activity.isDestroyed())
                    try {
                        int code = response.code();
                        if (code / 100 == 2) {
                            // 图片变了
                            String newTime = response.header("Last-Modified");
                            String loadSign = timeMap.get(url);
                            timeMap.put(url, newTime);
                            PreferenceTools.saveObj(activity, IConstant.SIGNCACHE, new SignCacheData(timeMap));
                            //避免多次下载图片
                            if (/*!TextUtils.isEmpty(loadSign) && */null != newTime && !newTime.equals(loadSign) && imageView.isAttachedToWindow()) {//如果为空表示已经加载了图片, 或者相等  表示不需要加载
                                ObjectKey signature = new ObjectKey(newTime);
                                if (!activity.isDestroyed())
                                    activity.runOnUiThread(() -> {
                                        if (!activity.isDestroyed()) {
                                            RequestBuilder<Bitmap> load = Glide.with(activity)
                                                    .asBitmap()
                                                    .load(url);
                                            load.apply(new RequestOptions().signature(signature)
                                                    .placeholder(defaultResId)
                                                    .error(defaultResId))
                                                    .listener(new RequestListener<Bitmap>() {
                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                            try {
                                                                if (defaultResId == -1) {
                                                                    if (!activity.isDestroyed())
                                                                        if (imageView.isAttachedToWindow()) {
                                                                            //                                                                            if (model.equals(imageView.getContentDescription().toString()))
                                                                            if (!resource.isRecycled())
                                                                                imageView.setImageBitmap(resource);
                                                                        }
                                                                }
                                                            } catch (Exception e) {
                                                            }
                                                            return false;
                                                        }
                                                    }).preload();
                                        }
                                    });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    /**
     * 当Activity 退出时, 清空所有请求
     */
    public void cancelAll() {
        try {
            if (client != null) {
                client.dispatcher().cancelAll();
            }
            if (null != activity && !activity.isDestroyed())
                Glide.with(activity).onDestroy();
            client = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
