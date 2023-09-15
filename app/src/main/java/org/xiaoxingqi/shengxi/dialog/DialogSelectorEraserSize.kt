package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

/**
 * 选择橡皮擦的大小
 */
class DialogSelectorEraserSize(context: Context) : BaseDialog(context) {
    private var current = 1//默认最小的笔
    override fun getLayoutId(): Int {
        return R.layout.dialog_selector_earser_size
    }

    override fun initView() {
        val eraser1 = findViewById<View>(R.id.iv_eraser_size_1)
        val eraser2 = findViewById<View>(R.id.iv_eraser_size_2)
        val eraser3 = findViewById<View>(R.id.iv_eraser_size_3)
        eraser1.isSelected = true
        eraser2.isSelected = true
        eraser3.isSelected = true
        when (current) {
            1 -> eraser1.isSelected = false
            2 -> eraser2.isSelected = false
            3 -> eraser3.isSelected = false
        }
        eraser1.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        eraser2.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        eraser3.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
    }

    fun setLocation(x: Int, y: Int): DialogSelectorEraserSize {
        window?.setBackgroundDrawable(ColorDrawable(0))
        val lp = window?.attributes
        lp?.gravity = Gravity.LEFT or Gravity.TOP
        lp?.x = x - AppTools.dp2px(context, 37)// 新位置X坐标
        lp?.y = y - AppTools.dp2px(context, 61) - AppTools.getStatusBarHeight(context) // 新位置Y坐标
        window?.attributes = lp
        return this
    }

    fun setCurrentPenSize(current: Int): DialogSelectorEraserSize {
        this.current = current
        return this
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogSelectorEraserSize {
        this.onClickListener = onClickListener
        return this
    }

}