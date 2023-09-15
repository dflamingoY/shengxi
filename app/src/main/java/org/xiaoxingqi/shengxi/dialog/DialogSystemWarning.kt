package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogSystemWarning(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_system_warning
    }

    private var content: String? = null
    override fun initView() {
        setCancelable(false)
        val tvContent = findViewById<TextView>(R.id.tv_Content)
        if (!TextUtils.isEmpty(content)) {
            tvContent.text = content
        }
        findViewById<View>(R.id.btn_Cancel).setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    fun setShowContent(content: String): DialogSystemWarning {
        this.content = content
        return this
    }


    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogSystemWarning {
        this.onClickListener = onClickListener
        return this
    }
}