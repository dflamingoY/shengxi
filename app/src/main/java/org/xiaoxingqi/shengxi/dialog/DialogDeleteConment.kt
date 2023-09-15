package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogDeleteConment(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {

    private var hintText: String? = null
    private var btnTitle: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_comment
    }

    override fun initView() {
        if (!TextUtils.isEmpty(hintText))
            findViewById<TextView>(R.id.tv_Hint).text = hintText
        if (!TextUtils.isEmpty(btnTitle)) {
            findViewById<TextView>(R.id.tv_Commit).text = btnTitle
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        initSystem()
    }

    fun setHintText(hintText: String): DialogDeleteConment {
        this.hintText = hintText
        return this
    }

    fun setClickBtnTitle(btnTitle: String): DialogDeleteConment {
        this.btnTitle = btnTitle
        return this
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteConment {
        this.onClickListener = onClickListener
        return this
    }


}