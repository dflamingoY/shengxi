package org.xiaoxingqi.shengxi.receiver;

import android.content.Context;
import android.os.Bundle;

import com.huawei.hms.support.api.push.PushReceiver;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.impl.IHwTokenEvent;


public class HuaweiReceiver extends PushReceiver {

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        super.onToken(context, token, extras);
        EventBus.getDefault().post(new IHwTokenEvent(token));
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msgBytes, Bundle extras) {
        return super.onPushMsg(context, msgBytes, extras);
    }

    @Override
    public void onEvent(Context context, Event event, Bundle extras) {
        super.onEvent(context, event, extras);
    }

    @Override
    public void onPushState(Context context, boolean pushState) {
        super.onPushState(context, pushState);
    }


}

