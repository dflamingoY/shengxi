package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.main.dialog_machine_sort.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

class DialogMachineSort(context: Context) : BaseDialog(context) {
    private var currentType = 1//最早優先  1 最新优先
    override fun getLayoutId(): Int {
        return R.layout.dialog_machine_sort
    }

    override fun initView() {
        if (currentType == 0) {
            tvOrder.isSelected = true
        } else {
            tvDesc.isSelected = true
        }
        tvOrder.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvDesc.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(type: Int, onClickListener: View.OnClickListener): DialogMachineSort {
        currentType = type
        this.onClickListener = onClickListener
        return this
    }

    /**
     * 设置显示的坐标
     */
    fun setLocation(y: Int) {
        window.setDimAmount(0f)
        val lp = window!!.attributes
        window.setBackgroundDrawable(ColorDrawable(0))
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        lp.y = AppTools.getWindowsHeight(context) - y + AppTools.getActionBarSize(context) + AppTools.getStatusBarHeight(context) // 新位置Y坐标
        window.attributes = lp
        show()
    }
}