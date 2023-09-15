package org.xiaoxingqi.shengxi.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_delete_comment.*
import org.xiaoxingqi.shengxi.R

class DialogAdminOperatorConfirm(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var title: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_comment
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        setCancelable(false)
        tv_Hint.text = "操作确认：$title"
        tv_Commit.text = "确认执行"
        tv_Commit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAdminOperatorConfirm {
        this.onClickListener = onClickListener
        return this
    }

    fun setTitle(title: String): DialogAdminOperatorConfirm {
        this.title = title
        return this
    }
}