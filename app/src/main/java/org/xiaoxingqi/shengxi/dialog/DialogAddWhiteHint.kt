package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R

/**
 * 加入白名单之后提示
 */
class DialogAddWhiteHint(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_add_white_list
    }

    override fun initView() {
        findViewById<View>(R.id.tv_dismiss).setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}