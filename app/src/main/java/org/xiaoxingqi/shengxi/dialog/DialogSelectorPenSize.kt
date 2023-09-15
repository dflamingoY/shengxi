package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

/**
 * 选择画笔的大小
 */
class DialogSelectorPenSize(context: Context) : BaseDialog(context) {
    private var current = 1//默认最小的笔
    override fun getLayoutId(): Int {
        return R.layout.dialog_selector_pen_size
    }

    override fun initView() {
        val penSize1 = findViewById<View>(R.id.iv_pen_size_1)
        val penSize2 = findViewById<View>(R.id.iv_pen_size_2)
        val penSize3 = findViewById<View>(R.id.iv_pen_size_3)
        val penSize4 = findViewById<View>(R.id.iv_pen_size_4)
        val penSize5 = findViewById<View>(R.id.iv_pen_size_5)
        penSize1.isSelected = true
        penSize2.isSelected = true
        penSize3.isSelected = true
        penSize4.isSelected = true
        penSize5.isSelected = true
        when (current) {
            1 -> penSize1.isSelected = false
            2 -> penSize2.isSelected = false
            3 -> penSize3.isSelected = false
            4 -> penSize4.isSelected = false
            5 -> penSize5.isSelected = false
        }
        penSize1.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        penSize2.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        penSize3.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        penSize4.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        penSize5.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
    }

    fun setLocation(x: Int, y: Int): DialogSelectorPenSize {
        window?.setBackgroundDrawable(ColorDrawable(0))
        val lp = window?.attributes
        lp?.gravity = Gravity.LEFT or Gravity.TOP
        lp?.x = x - AppTools.dp2px(context, 39)// 新位置X坐标
        lp?.y = y - AppTools.dp2px(context, 61) - AppTools.getStatusBarHeight(context) // 新位置Y坐标
        window?.attributes = lp
        return this
    }

    fun setCurrentPenSize(current: Int): DialogSelectorPenSize {
        this.current = current
        return this
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogSelectorPenSize {
        this.onClickListener = onClickListener
        return this
    }

}