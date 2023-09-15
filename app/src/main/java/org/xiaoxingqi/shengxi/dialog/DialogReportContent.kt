package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.impl.OnReportItemListener

class DialogReportContent(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_report_content
    }

    override fun initView() {
//        findViewById<View>(R.id.tv_AddBlack).isSelected = true
        findViewById<View>(R.id.tv_Commit).isSelected = false
        findViewById<View>(R.id.tv_AddBlack).setOnClickListener { view ->
            clearSelected()
            view.isSelected = true
        }
        findViewById<View>(R.id.tv_rubbish).setOnClickListener { view ->
            clearSelected()
            view.isSelected = true
        }
        findViewById<View>(R.id.tv_selfish).setOnClickListener { view ->
            clearSelected()
            view.isSelected = true
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            if (TextUtils.isEmpty(getSelected())) {
                //未选中不能举报
                return@setOnClickListener
            }
            onResultListener?.onItemSelected(getSelected())
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

    private fun getSelected(): String {
        return when {
            findViewById<View>(R.id.tv_AddBlack).isSelected -> "1"
            findViewById<View>(R.id.tv_rubbish).isSelected -> "2"
            findViewById<View>(R.id.tv_selfish).isSelected -> "3"
            else -> ""
        }
    }

    private fun clearSelected() {
        findViewById<View>(R.id.tv_AddBlack).isSelected = false
        findViewById<View>(R.id.tv_rubbish).isSelected = false
        findViewById<View>(R.id.tv_selfish).isSelected = false
        findViewById<View>(R.id.tv_Commit).isSelected = true
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClicklistener(onClickListener: View.OnClickListener): DialogReportContent {
        this.onClickListener = onClickListener
        return this
    }

    private var onResultListener: OnReportItemListener? = null
    fun setOnResultListener(onResultListener: OnReportItemListener): DialogReportContent {
        this.onResultListener = onResultListener
        return this
    }
}