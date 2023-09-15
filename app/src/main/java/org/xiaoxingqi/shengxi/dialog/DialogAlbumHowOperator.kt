package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import kotlinx.android.synthetic.main.dialog_album_how_opearator.*
import org.xiaoxingqi.shengxi.R

class DialogAlbumHowOperator(context: Context) : BaseDialog(context) {
    private var content: String? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_album_how_opearator
    }

    override fun initView() {
        if (!TextUtils.isEmpty(content))
            tvContent.text = content
        iv_close.setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
        window.attributes.windowAnimations = R.style.AlphaDialogAnim
    }

    fun setTitle(content: String): DialogAlbumHowOperator {
        this.content = content
        return this
    }
}