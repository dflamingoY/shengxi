package org.xiaoxingqi.shengxi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.model.BaseUserBean;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareUtils {

    /**
     * 本地资源图片 编译成内存图片
     */

    private static String decodeRes(Context context) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/share.jpg");
            if (!file.exists()) {
                file.createNewFile();
                Bitmap pic = BitmapFactory.decodeResource(context.getResources(), R.drawable.drawable_share);
                FileOutputStream fos = new FileOutputStream(file);
                pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    public static void share(Context context, String shareType, String voicePath, String sharePath, String actionTitle, BaseUserBean user) {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        actionTitle = ("分享我的一段记忆");
        sp.setText(actionTitle);
        sp.setTitle("声昔APP-我发现了一部时光机");
        sp.setImagePath(decodeRes(context));
        sp.setUrl(sharePath);
        sp.setMusicUrl(voicePath);
        sp.setTitleUrl(sharePath);
        sp.setSite(context.getString(R.string.app_name));
        sp.setSiteUrl(sharePath);

        if (SinaWeibo.NAME.equals(shareType)) {
            Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
            sp.setText("分享我的记忆" + "@声昔App");
            if (!weibo.isClientValid()) {
                Toast.makeText(context, "未安装微博客戶端", Toast.LENGTH_SHORT).show();
                return;
            }
            weibo.share(sp);
            //            weibo.setPlatformActionListener(listener);
        } else if (QQ.NAME.equals(shareType)) {
            sp.setShareType(Platform.SHARE_MUSIC);
            Platform qq = ShareSDK.getPlatform(QQ.NAME);
            if (!qq.isClientValid()) {
                Toast.makeText(context, "未安装QQ客戶端", Toast.LENGTH_SHORT).show();
                return;
            }
            qq.share(sp);
            //            qq.setPlatformActionListener(listener);
        } else if (QZone.NAME.equals(shareType)) {
            Platform qzone = ShareSDK.getPlatform(QZone.NAME);
            if (!qzone.isClientValid()) {
                Toast.makeText(context, "未安装QQ空间", Toast.LENGTH_SHORT).show();
                return;
            }
            qzone.share(sp);
            //            qzone.setPlatformActionListener(listener);
        } else if (Wechat.NAME.equals(shareType)) {
            Platform platform = ShareSDK.getPlatform(Wechat.NAME);
            if (!platform.isClientValid()) {
                Toast.makeText(context, "未安装微信客户端", Toast.LENGTH_SHORT).show();
                return;
            }
            Platform.ShareParams shareParams = new Platform.ShareParams();
            shareParams.setText(actionTitle);
            shareParams.setTitle("声昔APP-我发现了一部时光机！");
            shareParams.setImagePath(decodeRes(context));
            shareParams.setUrl(sharePath);
            shareParams.setMusicUrl(voicePath);
            shareParams.setSite("声昔");
            shareParams.setSiteUrl(sharePath);
            shareParams.setShareType(Platform.SHARE_WEBPAGE);
            platform.share(shareParams);
            //            platform.setPlatformActionListener(listener);

        } else if (WechatMoments.NAME.equals(shareType)) {
            Platform platform = ShareSDK.getPlatform(shareType);
            if (!platform.isClientValid()) {
                Toast.makeText(context, "未安装微信客户端", Toast.LENGTH_SHORT).show();
                return;
            }
            Platform.ShareParams shareParams = new Platform.ShareParams();
            shareParams.setTitle("声昔APP-我发现了一部时光机！");
            shareParams.setText(actionTitle);
            shareParams.setImagePath(decodeRes(context));
            shareParams.setMusicUrl(voicePath);
            shareParams.setUrl(sharePath);
            shareParams.setSite("声昔");
            shareParams.setSiteUrl(sharePath);
            shareParams.setShareType(Platform.SHARE_MUSIC);
            platform.share(shareParams);
            //            platform.setPlatformActionListener(listener);
        }
    }

    /**
     * 邀请好友
     *
     * @param context
     * @param shareType
     * @param content
     * @param imgPath
     */
    public static void invalidate(Context context, String shareType, String content, String imgPath, String title, String url) {
        PlatformActionListener listener = new PlatformActionListener() {

            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Toast.makeText(context, "邀请成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        };
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);
        sp.setText(content);
        sp.setTitle(title);
        sp.setImagePath(imgPath);
        sp.setUrl(url);
        sp.setTitleUrl(url);
        sp.setSite(context.getString(R.string.app_name));
        sp.setSiteUrl(url);
        if (SinaWeibo.NAME.equals(shareType)) {
            Platform weibo = ShareSDK.getPlatform(SinaWeibo.NAME);
            sp.setText("偷偷告诉你我发现了一部时光机@声昔App");
            if (!weibo.isClientValid()) {
                Toast.makeText(context, "未安装微博客戶端", Toast.LENGTH_SHORT).show();
                return;
            }
            weibo.setPlatformActionListener(listener);
            weibo.share(sp);
        } else if (QQ.NAME.equals(shareType)) {
            sp.setShareType(Platform.SHARE_MUSIC);
            Platform qq = ShareSDK.getPlatform(QQ.NAME);
            if (!qq.isClientValid()) {
                Toast.makeText(context, "未安装QQ客戶端", Toast.LENGTH_SHORT).show();
                return;
            }
            qq.setPlatformActionListener(listener);
            qq.share(sp);
        } else if (QZone.NAME.equals(shareType)) {
            Platform qzone = ShareSDK.getPlatform(QZone.NAME);
            if (!qzone.isClientValid()) {
                Toast.makeText(context, "未安装QQ空间", Toast.LENGTH_SHORT).show();
                return;
            }
            qzone.setPlatformActionListener(listener);
            qzone.share(sp);
        } else if (Wechat.NAME.equals(shareType)) {
            Platform platform = ShareSDK.getPlatform(Wechat.NAME);
            if (!platform.isClientValid()) {
                Toast.makeText(context, "未安装微信客户端", Toast.LENGTH_SHORT).show();
                return;
            }
            Platform.ShareParams shareParams = new Platform.ShareParams();
            shareParams.setText(content);
            shareParams.setTitle(title);
            shareParams.setImagePath(imgPath);
            shareParams.setUrl(url);
            shareParams.setSite("声昔");
            shareParams.setSiteUrl(url);
            shareParams.setShareType(Platform.SHARE_WEBPAGE);
            platform.setPlatformActionListener(listener);
            platform.share(shareParams);

        } else if (WechatMoments.NAME.equals(shareType)) {
            Platform platform = ShareSDK.getPlatform(shareType);
            if (!platform.isClientValid()) {
                Toast.makeText(context, "未安装微信客户端", Toast.LENGTH_SHORT).show();
                return;
            }
            Platform.ShareParams shareParams = new Platform.ShareParams();
            shareParams.setTitle(title);
            shareParams.setText(content);
            shareParams.setImagePath(imgPath);
            shareParams.setUrl(url);
            shareParams.setSite("声昔");
            shareParams.setSiteUrl(url);
            shareParams.setShareType(Platform.SHARE_WEBPAGE);
            platform.setPlatformActionListener(listener);
            platform.share(shareParams);
        }
    }

    /**
     * 分享普通的文本
     */
    public static void shareString(Context context, String shareType, String content, String imgPath, String title, String url) {

    }

}
