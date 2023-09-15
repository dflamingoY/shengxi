package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R

class DialogCancelEchoes(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_cance_echoes
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener { view ->
            onClicklistener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClicklistener: View.OnClickListener? = null
    fun setOnCLickListener(onClickListener: View.OnClickListener): DialogCancelEchoes {
        onClicklistener = onClickListener
        return this
    }

}