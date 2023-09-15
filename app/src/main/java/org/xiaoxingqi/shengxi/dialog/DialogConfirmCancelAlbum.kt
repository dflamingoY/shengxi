package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.dialog_cancel_commit.*
import org.xiaoxingqi.shengxi.R

class DialogConfirmCancelAlbum(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_cancel_commit
    }

    override fun initView() {
        tv_content.visibility = View.GONE
        tv_Title.text = context.resources.getString(R.string.string_cancel_submit_album)
        tv_Commit.text = context.getString(R.string.string_confirm)
        tv_Cancel.text = context.resources.getString(R.string.string_cancel)
        tv_Commit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogConfirmCancelAlbum {
        this.onClickListener = onClickListener
        return this
    }

}