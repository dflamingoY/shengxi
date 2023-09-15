package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_center_hint.*
import org.xiaoxingqi.shengxi.R

class CenterHintDialog(context: Context) : BaseDialog(context) {
    private var title: String? = null
    private var leftTitle: String? = null
    private var rightTitle: String? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_center_hint
    }

    override fun initView() {
        if (!title.isNullOrEmpty())
            tvTitle.text = title
        if (!leftTitle.isNullOrEmpty())
            tvCommit.text = leftTitle
        if (!rightTitle.isNullOrEmpty())
            tvCancel.text = rightTitle
        tvCommit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvCancel.setOnClickListener { dismiss() }
        fillWidth()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): CenterHintDialog {
        this.onClickListener = onClickListener
        return this
    }

    fun setTitle(title: String? = null, leftTitle: String? = null, rightTitle: String? = null): CenterHintDialog {
        this.title = title
        this.leftTitle = leftTitle
        this.rightTitle = rightTitle
        return this
    }

}