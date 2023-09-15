package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct

/**
 * 添加好友之后， 如果是0s的语音， 则提示改弹窗
 */
class DialogCreateVoice(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_create_voice
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            context.startActivity(Intent(context, SendAct::class.java)
                    .putExtra("isHome", true)
                    .putExtra("type", 1))
            dismiss()
        }

        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}