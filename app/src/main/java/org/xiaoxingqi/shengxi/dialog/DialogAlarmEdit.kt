package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_painter_more.*
import org.xiaoxingqi.shengxi.R

class DialogAlarmEdit(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var isAnonymous: String = "0"
    private var isHideAnonymous = false
    override fun getLayoutId(): Int {
        return R.layout.dialog_painter_more
    }

    override fun initView() {
        tvBanGraffiti.visibility = View.GONE
        if (isAnonymous == "1") {
            tv_Self.text = context.resources.getString(R.string.string_paint_set_public)
        } else {
            tv_Self.text = context.resources.getString(R.string.string_paint_set_anonynous)
        }
        if (isHideAnonymous) {
            tv_Self.visibility = View.GONE
        } else {
            tv_Self.visibility = View.VISIBLE
        }
        tv_Cancel.setOnClickListener { dismiss() }
        tv_Delete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Self.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    fun setProperty(isAnonymous: String): DialogAlarmEdit {
        this.isAnonymous = isAnonymous
        return this
    }

    //隐藏设置匿名按钮
    fun hideAnonymous(isHideAnonymous: Boolean): DialogAlarmEdit {
        this.isHideAnonymous = isHideAnonymous
        return this
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAlarmEdit {
        this.onClickListener = onClickListener
        return this
    }
}