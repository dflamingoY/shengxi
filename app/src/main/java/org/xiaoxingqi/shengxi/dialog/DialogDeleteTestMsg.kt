package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 删除测一测中的留言
 */
class DialogDeleteTestMsg(context: Context) : BaseDialog(context) {
    private var title: String? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_hint_cancel
    }

    override fun initView() {
        if (!TextUtils.isEmpty(title)) {
            findViewById<TextView>(R.id.tv_Title).text = title
        } else
            findViewById<TextView>(R.id.tv_Title).text = context.resources.getString(R.string.string_delete_test_comment)
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteTestMsg {
        this.onClickListener = onClickListener
        return this
    }

    fun setTitle(title: String): DialogDeleteTestMsg {
        this.title = title
        return this
    }
}