package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_delete_album_talk.*
import org.xiaoxingqi.shengxi.R

class DialogDeleteTalkAlbum(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_album_talk
    }

    override fun initView() {
        tv_Commit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        fillWidth()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteTalkAlbum {
        this.onClickListener = onClickListener
        return this
    }

}