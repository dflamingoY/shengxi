package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogCancelTest(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_cancel_commit
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_Title).text = context.resources.getString(R.string.string_test_19)
        findViewById<TextView>(R.id.tv_content).text = context.getString(R.string.string_test_20)
        findViewById<TextView>(R.id.tv_Commit).text = context.getString(R.string.string_test_21)
        findViewById<TextView>(R.id.tv_Cancel).text = context.resources.getString(R.string.string_test_22)
        findViewById<TextView>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<TextView>(R.id.tv_Commit).setOnClickListener {
            onClickListener?.onClick(it)
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

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogCancelTest {
        this.onClickListener = onClickListener
        return this
    }
}