package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogSelectPhoto(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_select_photo
    }

    var visiable: Boolean = false
    var text: String = ""
    var otherVisible: Boolean = false
    override fun initView() {
        findViewById<View>(R.id.tv_Camera).setOnClickListener {
            itemClick?.itemView(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Album).setOnClickListener {
            itemClick?.itemView(it)
            dismiss()
        }
        val tvSave = findViewById<TextView>(R.id.tv_Save)
        val tvOther = findViewById<TextView>(R.id.tv_Other)
        tvOther.visibility = if (otherVisible) View.VISIBLE else View.GONE
        tvOther.setOnClickListener {
            itemClick?.itemView(it)
            dismiss()
        }
        if (!TextUtils.isEmpty(text)) {
            tvSave.text = text
        }
        tvSave.setOnClickListener {
            itemClick?.itemView(it)
            dismiss()
        }
        if (visiable) {
            findViewById<View>(R.id.tv_Album).visibility = View.GONE
            findViewById<View>(R.id.tv_Camera).visibility = View.GONE
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        initSystem()
    }

    var itemClick: OnItemClick? = null

    fun setOnItemClick(item: OnItemClick): DialogSelectPhoto {
        itemClick = item
        return this
    }

    /**
     * 隐藏其他文本
     */
    fun hideAction(isHide: Boolean): DialogSelectPhoto {
        visiable = isHide
        return this
    }

    /**
     * 是否显示其他的保存图片
     */
    fun hindOther(visible: Boolean): DialogSelectPhoto {
        otherVisible = visible
        return this
    }

    /**
     * 设置显示文本文案
     */
    fun changeText(text: String): DialogSelectPhoto {
        this.text = text
        return this
    }


    interface OnItemClick {
        fun itemView(view: View)
    }

}