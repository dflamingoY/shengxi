package org.xiaoxingqi.shengxi.core.http;


import org.xiaoxingqi.shengxi.model.BaseRepData;

/**
 * Created by DoctorKevin on 2017/7/20.
 */

public interface OkHttpListener {

    void onSuccess(Object o);

    void onFailed(Exception e);

    void onStateError(BaseRepData resp);
}
