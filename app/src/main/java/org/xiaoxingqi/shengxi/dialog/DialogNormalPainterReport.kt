package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import org.xiaoxingqi.shengxi.R

class DialogNormalPainterReport(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_normal_painter_report
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Attach).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Porn).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Junk).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_illegal).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogNormalPainterReport {
        this.onClickListener = onClickListener
        return this
    }
}