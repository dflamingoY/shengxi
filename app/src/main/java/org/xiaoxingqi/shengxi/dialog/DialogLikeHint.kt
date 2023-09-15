package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R

class DialogLikeHint(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_like_hint
    }

    override fun initView() {
        setCancelable(false)
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Submit).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_right).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.iv_close).setOnClickListener {
            dismiss()
        }
        window!!.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogLikeHint {
        this.onClickListener = onClickListener
        return this
    }

}