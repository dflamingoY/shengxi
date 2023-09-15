package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 撤回消息
 */
class DialogCancelMsg(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var title: String? = null
    private var isCollection = false
    override fun getLayoutId(): Int {
        return R.layout.dialog_report
    }

    override fun initView() {
        val tvReport = findViewById<TextView>(R.id.tv_Report)
        findViewById<View>(R.id.tv_report_normal).visibility = View.GONE
        tvReport.text = if (TextUtils.isEmpty(title)) "撤回" else title
        tvReport.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        if (isCollection) {
            findViewById<View>(R.id.tvCollection).visibility = View.VISIBLE
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tvCollection).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }

        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogCancelMsg {
        this.onClickListener = onClickListener
        return this
    }

    fun setTitle(title: String): DialogCancelMsg {
        this.title = title
        return this
    }

    fun setCollection(isCollection: Boolean): DialogCancelMsg {
        this.isCollection = isCollection
        return this
    }

}