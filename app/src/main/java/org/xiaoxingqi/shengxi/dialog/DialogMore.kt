package org.xiaoxingqi.shengxi.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.dialog_more.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

class DialogMore : Dialog {
    private var mContext: Context? = null
    private var isPrivacy = 0
    private var adminId: String? = null
    private var isTop = false
    private var isReEditVoice = false

    constructor(context: Context) : super(context, R.style.FullDialogTheme) {
        mContext = context
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_more)
        if (!TextUtils.isEmpty(adminId) && adminId == "1") {
            tvOfficialTop.visibility = View.VISIBLE
            if (isTop) {
                tvOfficialTop.text = "取消置顶"
            }
        }
        if (isReEditVoice) {
            tvReEditVoice.visibility = View.VISIBLE
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        tv_ShareWechat.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        tv_ShareWeibo.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        tv_ShareQQ.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        tv_Delete.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        tvOfficialTop.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        if (isPrivacy == 1) {
            tv_Self.text = "取消仅自己可见"
        } else {
            tv_Self.text = context.resources.getString(R.string.string_just_self_visible)
        }
        tvReEditVoice.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Self.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        tv_add_album.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val p = window!!.attributes
        p.gravity = Gravity.BOTTOM
        p.width = AppTools.getWindowsWidth(mContext)
        window!!.attributes = p
    }

    /**
     * 1=是，0=否
     */
    fun setPrivacyStatus(privacy: Int): DialogMore {
        isPrivacy = privacy
        return this
    }

    var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogMore {
        this.onClickListener = onClickListener
        return this
    }

    fun setAdmin(uid: String, isTop: Boolean = false): DialogMore {
        adminId = uid
        this.isTop = isTop
        return this
    }

    //编辑话题, 只能操作普通语音
    fun setEditable(isEditable: Boolean): DialogMore {
        isReEditVoice = isEditable
        return this
    }

}