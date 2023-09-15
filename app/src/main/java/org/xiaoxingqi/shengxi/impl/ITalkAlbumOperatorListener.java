package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.BaseTalkVoiceBean;

public interface ITalkAlbumOperatorListener {
    void clickItem(int position);

    void operatedItem(BaseTalkVoiceBean bean);
}
