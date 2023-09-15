package org.xiaoxingqi.shengxi.core.http;

public interface OkResponse {

    void success(Object result);

    void onFailure(Object any);
}
