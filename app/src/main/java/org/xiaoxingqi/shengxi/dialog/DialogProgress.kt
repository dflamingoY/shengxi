package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import org.xiaoxingqi.shengxi.R

class DialogProgress(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_progress
    }

    override fun initView() {
        window.setBackgroundDrawable(ColorDrawable(0))
    }
}