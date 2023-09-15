package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_check_season_album.*
import org.xiaoxingqi.shengxi.R

class DialogCheckSeasonAlbum(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_check_season_album
    }

    override fun initView() {
        tv_Cancel.setOnClickListener { dismiss() }
        tv_stranger.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_friend.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogCheckSeasonAlbum {
        this.onClickListener = onClickListener
        return this
    }
}