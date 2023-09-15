package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import org.xiaoxingqi.shengxi.R

class DialogEnterContent(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_enter_content
    }

    private var remark: String? = null
    override fun initView() {
        setCancelable(false)
        val edit = findViewById<EditText>(R.id.et_Content)
        if (!TextUtils.isEmpty(remark)) {
            edit.setText(remark)
        }
        edit.setOnEditorActionListener { _, _, event -> (event?.keyCode == KeyEvent.KEYCODE_ENTER) }
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            onCommitListener?.commit(edit.text.toString().trim())
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    interface OnCommitListener {
        fun commit(result: String)
    }

    fun setRemark(remark: String): DialogEnterContent {
        this.remark = remark
        return this
    }

    private var onCommitListener: OnCommitListener? = null
    fun setOnCommitListener(onCommitListener: OnCommitListener): DialogEnterContent {
        this.onCommitListener = onCommitListener
        return this
    }

}