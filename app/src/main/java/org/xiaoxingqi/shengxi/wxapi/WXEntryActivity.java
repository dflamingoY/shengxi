package org.xiaoxingqi.shengxi.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.impl.WechatLoginEvent;
import org.xiaoxingqi.shengxi.model.RespWxData;
import org.xiaoxingqi.shengxi.core.http.OkClientHelper;
import org.xiaoxingqi.shengxi.core.http.OkResponse;
import org.xiaoxingqi.shengxi.model.WeUserInf;
import org.xiaoxingqi.shengxi.modules.user.AccountActivity;
import org.xiaoxingqi.shengxi.utils.WXHelper;

import cn.sharesdk.wechat.utils.WechatHandlerActivity;

public class WXEntryActivity extends WechatHandlerActivity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_wx_result);
        api = WXAPIFactory.createWXAPI(this, WXHelper.APP_ID, false);// 通过WXAPIFactory工厂，获取IWXAPI的实例
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_LAUNCH_BY_WX:
                break;
            default:
                break;
        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            getToken(((SendAuth.Resp) resp).code);
        } else {
            EventBus.getDefault().post(new WechatLoginEvent(null));//登陆出错
            EventBus.getDefault().post(new AccountActivity.WeChatEvent(null));
            finish();
        }
    }

    /**
     * 根据CODE获取token
     *
     * @param CODE
     */
    private void getToken(String CODE) {
        if (CODE == null) {
            EventBus.getDefault().post(new WechatLoginEvent(null));//登陆出错
            EventBus.getDefault().post(new AccountActivity.WeChatEvent(null));
            finish();
        } else {
            String getUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WXHelper.APP_ID + "&secret=" + WXHelper.APP_SECRET + "&code=" + CODE + "&grant_type=authorization_code";
            OkClientHelper.getWx(this, getUrl, RespWxData.class, new OkResponse() {
                @Override
                public void success(Object result) {
                    RespWxData data = (RespWxData) result;
                    requestNet(data);
                }

                @Override
                public void onFailure(Object any) {
                    EventBus.getDefault().post(new WechatLoginEvent(null));
                    EventBus.getDefault().post(new AccountActivity.WeChatEvent(null));
                    finish();
                }
            });
        }
    }

    private void requestNet(RespWxData weRespose) {
        String getUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + weRespose.getAccess_token() + "&openid=" + weRespose.getOpenid() + "&lang=zh_CN";
        OkClientHelper.getWx(this, getUrl, WeUserInf.class, new OkResponse() {
            @Override
            public void success(Object result) {
                WeUserInf info = (WeUserInf) result;
                info.setAccess_token(weRespose.getAccess_token());
                info.setRefresh_token(weRespose.getRefresh_token());
                EventBus.getDefault().post(new WechatLoginEvent(info));
                EventBus.getDefault().post(new AccountActivity.WeChatEvent(info));
                finish();
            }

            @Override
            public void onFailure(Object any) {
                EventBus.getDefault().post(new WechatLoginEvent(null));
                EventBus.getDefault().post(new AccountActivity.WeChatEvent(null));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}