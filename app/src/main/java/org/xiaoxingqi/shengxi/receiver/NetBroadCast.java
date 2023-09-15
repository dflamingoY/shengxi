package org.xiaoxingqi.shengxi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.modules.MainActivity;

/**
 * Created by yzm on 2017/12/19.
 */

public class NetBroadCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /**
         * 断网之后 进行重连
         */
        EventBus.getDefault().post(new MainActivity.NetUpdateEvent());
    }
}
