package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_long_press_artwork.*
import org.xiaoxingqi.shengxi.R

class DialogLongPressArtwork(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_long_press_artwork
    }

    override fun initView() {
        tvDelete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvPrivacy.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvOfficialTop.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvReport.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener { dismiss() }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogLongPressArtwork {
        this.onClickListener = onClickListener
        return this
    }


}