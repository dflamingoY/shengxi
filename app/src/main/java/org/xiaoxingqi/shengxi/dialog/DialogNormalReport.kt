package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.dialog_normal_report.*
import org.xiaoxingqi.shengxi.R

class DialogNormalReport(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_normal_report
    }

    override fun initView() {
        tv_Attach.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        tv_Porn.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        tv_Junk.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        tv_illegal.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        tv_Commit.setOnClickListener {
            val id = getSelected()
            if (id != null) {
                onClickListener?.onClick(id)
                dismiss()
            }
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private fun getSelected(): View? {
        return when {
            tv_Attach.isSelected -> tv_Attach
            tv_Porn.isSelected -> tv_Porn
            tv_Junk.isSelected -> tv_Junk
            tv_illegal.isSelected -> tv_illegal
            else -> null
        }
    }

    private fun clearSelected() {
        tv_Attach.isSelected = false
        tv_Porn.isSelected = false
        tv_Junk.isSelected = false
        tv_illegal.isSelected = false
        tv_Commit.isSelected = true
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogNormalReport {
        this.onClickListener = onClickListener
        return this
    }
}