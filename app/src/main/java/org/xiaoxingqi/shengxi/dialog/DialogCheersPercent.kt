package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import org.xiaoxingqi.shengxi.R

class DialogCheersPercent(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_cheers_percent
    }

    override fun initView() {
        fillWidth()
    }
}