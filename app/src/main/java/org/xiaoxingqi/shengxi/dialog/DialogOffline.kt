package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R

/**
 * 网络错误的时候 展示的Dialog   每个页面只展示一次
 */
class DialogOffline(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_offline
    }

    override fun initView() {
        findViewById<View>(R.id.iv_Click).setOnClickListener {
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
        window.attributes.windowAnimations = R.style.AlphaDialogAnim
    }

}