package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.dialog_delete_wording.*
import org.xiaoxingqi.shengxi.R

//删除台词
class DialogDeleteWording(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var title: String? = null
    private var actionTitle: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_wording
    }

    override fun initView() {
        if (!TextUtils.isEmpty(title)) {
            tvSecondTitle.visibility = View.GONE
            tv_Report.text = title
        }
        if (!TextUtils.isEmpty(actionTitle)) {
            tv_Delete.text = actionTitle
        }
        tv_Delete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener { dismiss() }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOtherTitle(title: String, actionTitle: String): DialogDeleteWording {
        this.title = title
        this.actionTitle = actionTitle
        return this
    }

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteWording {
        this.onClickListener = onClickListener
        return this
    }

}