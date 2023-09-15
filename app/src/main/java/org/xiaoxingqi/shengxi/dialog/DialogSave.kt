package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogSave(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_report
    }

    override fun initView() {
        val text = findViewById<TextView>(R.id.tv_Report)
        text.text = "保存图片"
        text.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_report_normal).visibility = View.GONE
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogSave {
        this.onClickListener = onClickListener
        return this
    }
}