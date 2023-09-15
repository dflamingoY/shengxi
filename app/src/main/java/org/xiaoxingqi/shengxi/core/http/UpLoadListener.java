package org.xiaoxingqi.shengxi.core.http;

/**
 * Created by yzm on 2017/11/17.
 */

public interface UpLoadListener {

    void upFinish(String endTag);

    void fail();

    void progress(long current);

}
