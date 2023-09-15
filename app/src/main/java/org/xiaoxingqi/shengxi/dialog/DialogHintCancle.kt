package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 用户提示是否取消拉黑, 是否删除好友, 是否拉黑
 */
class DialogHintCancle(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_hint_cancel
    }

    override fun initView() {
        val tv_Title = findViewById<TextView>(R.id.tv_Title)
        hintText?.let {
            tv_Title.text = it
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        findViewById<View>(R.id.tv_Commit).setOnClickListener { view ->
            onClickListener?.let {
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

    private var hintText: String? = null

    fun setHintTitle(hint: String): DialogHintCancle {
        hintText = hint
        return this
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogHintCancle {
        this.onClickListener = onClickListener
        return this
    }

}