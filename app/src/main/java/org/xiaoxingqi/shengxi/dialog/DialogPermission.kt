package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.dialog_permission_login.*
import org.xiaoxingqi.shengxi.R

class DialogPermission(context: Context) : BaseDialog(context) {
    private var isPhoneState = true
    private var isWriteStorage = true
    override fun getLayoutId(): Int {
        return R.layout.dialog_permission_login
    }

    override fun initView() {
        setCancelable(false)
        tvDismiss.setOnClickListener {
            DialogAbortProcess(context).show()
        }
        relativeRequestPermission.setOnClickListener {
            dismiss()
            onClickListener?.onClick(it)
        }
        if (!isPhoneState) relativePhone.visibility = View.GONE
        if (!isWriteStorage) relativeStorage.visibility = View.GONE
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogPermission {
        this.onClickListener = onClickListener
        return this
    }

    fun setPermissionState(isPhoneState: Boolean, isWriteStorage: Boolean): DialogPermission {
        this.isPhoneState = isPhoneState
        this.isWriteStorage = isWriteStorage
        return this
    }

}