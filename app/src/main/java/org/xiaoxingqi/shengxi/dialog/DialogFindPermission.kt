package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_find_permission.*
import org.xiaoxingqi.shengxi.R

class DialogFindPermission(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_find_permission
    }

    override fun initView() {
//        fillWidth()
        frameRoot.setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
    }
}