package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import kotlinx.android.synthetic.main.dialog_permission_set.*
import org.xiaoxingqi.shengxi.R

class DialogEnterSystemSet(context: Context) : BaseDialog(context) {
    private var isReadPhone = false
    private var isWriteStorage = false
    private var isAudio = false

    override fun getLayoutId(): Int {
        return R.layout.dialog_permission_set
    }

    override fun initView() {
        setCancelable(false)
        relativeRequestPermission.setOnClickListener {
            val localIntent = Intent()
            localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            localIntent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(localIntent)
        }
        if (!isReadPhone) relativePhone.visibility = View.GONE
        if (!isWriteStorage) relativeStorage.visibility = View.GONE
        relativeAudio.visibility = if (!isAudio) View.GONE else View.VISIBLE
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    fun setLoginPermission(isReadPhone: Boolean, isWriteStorage: Boolean): DialogEnterSystemSet {
        this.isReadPhone = isReadPhone
        this.isWriteStorage = isWriteStorage
        return this
    }

    fun setAudio(isAudio: Boolean): DialogEnterSystemSet {
        this.isAudio = isAudio
        return this
    }
}