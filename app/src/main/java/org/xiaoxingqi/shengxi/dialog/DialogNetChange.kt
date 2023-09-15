package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.modules.user.NetServerActivity

class DialogNetChange(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_net_change
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            context.startActivity(Intent(context, NetServerActivity::class.java))
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        window!!.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}