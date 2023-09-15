package org.xiaoxingqi.shengxi.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import org.xiaoxingqi.shengxi.model.SignCacheData;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GlideUtil {
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5L, TimeUnit.SECONDS)
            .build();
    //  目前将lastModified保存在内存中，也就是每次打开app都会刷新图片，如有必要，可以保存在本地磁盘中
    public static HashMap<String, String> timeMap = new HashMap<>();

    public static void load(Activity context, String url, ImageView imageView, int defaultResId) {
        String lastModified = timeMap.get(url);
        if (lastModified == null) lastModified = "";
        request(context, url, imageView, lastModified, defaultResId);
    }

    private static void request(final Activity context
                , final String url
            , final ImageView imageView
            , final String lastModified
            , final int defaultResId) {
        Glide.with(context)
                .applyDefaultRequestOptions(new RequestOptions()
                        .error(defaultResId)
                        .signature(new ObjectKey(lastModified)))
                .asBitmap()
                .load(url)
                .into(imageView);
        Request request = new Request.Builder()
                .addHeader("If-Modified-Since", lastModified)
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    if (!context.isDestroyed())
                        context.runOnUiThread(() -> Glide.with(context)
                                .applyDefaultRequestOptions(new RequestOptions()
                                        .error(defaultResId)
                                        .signature(new ObjectKey(lastModified)))
                                .asBitmap()
                                .load(url)
                                .into(imageView));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!context.isDestroyed())
                    try {
                        int code = response.code();
                        ObjectKey signature = null;
                        if (code == 304) {
                            // 说明图片没变
                            signature = new ObjectKey(lastModified);
                        } else if (code / 100 == 2) {
                            // 图片变了
                            final String newTime = response.header("Last-Modified");
                            timeMap.put(url, newTime);
                            PreferenceTools.saveObj(context, IConstant.SIGNCACHE, new SignCacheData(timeMap));
                            signature = new ObjectKey(newTime);
                        }
                        final ObjectKey stringSignature = signature;
                        ObjectKey finalSignature = signature;
                        if (!context.isDestroyed())
                            context.runOnUiThread(() -> {
                                if (!context.isDestroyed()) {
                                    RequestBuilder<Bitmap> load = Glide.with(context)
                                            .asBitmap()
                                            .load(url);
                                    if (stringSignature != null) {
                                        load.apply(new RequestOptions().signature(finalSignature)
                                                .error(defaultResId))
                                                .into(imageView);
                                    } else {
                                        load.apply(new RequestOptions().error(defaultResId)).into(imageView);
                                    }
                                }
                            });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }
}
