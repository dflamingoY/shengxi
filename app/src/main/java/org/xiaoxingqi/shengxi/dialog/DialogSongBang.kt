package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.Gravity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.wedgit.SwitchButton

class DialogSongBang(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_song_bang
    }

    private var toggleStatus = false
    override fun initView() {
        var toggleButton = findViewById<SwitchButton>(R.id.toggle_Button)
        toggleButton.isChecked = toggleStatus
        toggleButton.setOnCheckedChangeListener { view, isChecked ->
            /*
             *是否选中
             */
            toggleStatus = isChecked
        }
        val p = window!!.attributes
        p.windowAnimations = R.style.dialog_echo_anim
        p.gravity = Gravity.TOP
        p.width = AppTools.getWindowsWidth(context)
        window!!.attributes = p
    }

    override fun dismiss() {
        closeListener?.dismiss(toggleStatus)
        super.dismiss()
    }

    fun setOnCheck(toggleStatus: Boolean): DialogSongBang {
        this.toggleStatus = toggleStatus
        return this
    }

    private var closeListener: OnCloseListener? = null
    fun setOnCloseListener(closeListener: OnCloseListener): DialogSongBang {
        this.closeListener = closeListener
        return this
    }

    interface OnCloseListener {
        fun dismiss(isCheck: Boolean)
    }

}