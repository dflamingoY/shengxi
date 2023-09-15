package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_find_1.*
import org.xiaoxingqi.shengxi.R

class DialogFind1(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_find_1
    }

    override fun initView() {
        linearRoot.setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
//        fillWidth()
    }
}