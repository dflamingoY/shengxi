package org.xiaoxingqi.shengxi.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.xiaoxingqi.shengxi.impl.IUpdateEvent;

/**
 * 用来监听耳机的插拔
 */
public class HeadSetBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        EventBus.getDefault().post(new IUpdateEvent(action, intent.getIntExtra("state", 0), intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)));
    }
}
