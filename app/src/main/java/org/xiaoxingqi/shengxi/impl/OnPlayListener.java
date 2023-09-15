package org.xiaoxingqi.shengxi.impl;

public interface OnPlayListener {
    void onPrepared();

    void onCompletion();

    void onInterrupt();

    void onError(String var1);

    void onPlaying(long var1);
}
