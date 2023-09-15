package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_find_2.*
import org.xiaoxingqi.shengxi.R

class DialogFind2(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_find_2
    }

    override fun initView() {
        linearRoot.setOnClickListener { dismiss() }
//        fillWidth()
        window.setBackgroundDrawable(ColorDrawable(0))
    }
}