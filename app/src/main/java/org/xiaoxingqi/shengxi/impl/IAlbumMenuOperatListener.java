package org.xiaoxingqi.shengxi.impl;

import org.xiaoxingqi.shengxi.model.BaseBean;

public interface IAlbumMenuOperatListener {

    void clickItem(int position);

    void operatedItem(BaseBean bean);
}
