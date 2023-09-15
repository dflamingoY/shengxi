package org.xiaoxingqi.shengxi.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

/**
 * 灵魂画手中 单独白天模式的用处
 */
class DialogPainterMore(context: Context) : Dialog(context, R.style.FullDialogTheme) {
    private var isPrivacy = 0
    private var isForbid = 1

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_painter_more)
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tv_Delete).setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        val tvSelf = findViewById<TextView>(R.id.tv_Self)
        tvSelf.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        val banGraffiti = findViewById<TextView>(R.id.tvBanGraffiti)
        if (isPrivacy == 1) {
            banGraffiti.visibility = View.GONE
        } else {
            banGraffiti.visibility = View.VISIBLE
        }
        banGraffiti.text = if (isForbid == 1) {
            context.resources.getString(R.string.string_graffiti_ban)
        } else {
            context.resources.getString(R.string.string_graffiti_allow)
        }
        banGraffiti.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val p = window!!.attributes
        p.gravity = Gravity.BOTTOM
        p.width = AppTools.getWindowsWidth(context)
        window!!.attributes = p
    }

    /**
     * 设置是否禁止涂鸦
     */
    fun setForbidGraffiti(isForbid: Int): DialogPainterMore {
        this.isForbid = isForbid
        return this
    }

    /**
     * 1=是，0=否
     */
    fun setAnomouys(privacy: Int): DialogPainterMore {
        isPrivacy = privacy
        return this
    }

    var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogPainterMore {
        this.onClickListener = onClickListener
        return this
    }
}