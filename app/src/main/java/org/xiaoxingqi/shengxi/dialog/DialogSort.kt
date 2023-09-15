package org.xiaoxingqi.shengxi.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.dialog_sort.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

/**
 * 分类选择  最新和 热门
 */
class DialogSort(context: Context?) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    private var sortType = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_sort)
        if (sortType == 1) {
            viewNew.isSelected = true
            viewHot.isSelected = false
        } else {
            viewHot.isSelected = true
            viewNew.isSelected = false
        }
        viewNew.setOnClickListener { view ->
            view.isSelected = true
            viewHot.isSelected = false
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        viewHot.setOnClickListener { view ->
            view.isSelected = true
            viewNew.isSelected = false
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogSort {
        this.onClickListener = onClickListener
        return this
    }

    fun setSelectTitle(type: Int): DialogSort {
        sortType = type
        return this
    }

    /**
     * 设置显示的坐标
     */
    fun setLocation(x: Int, y: Int) {
        val lp = window!!.attributes
        window.setBackgroundDrawable(ColorDrawable(0))
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        lp.x = x // 新位置X坐标
        lp.y = y // 新位置Y坐标
        //        lp.width = AppTools.dp2px(mContext, ); // 宽度
        lp.width = AppTools.dp2px(context, 119)
        //        lp.alpha = 0.7f; // 透明度
        // dialog.onWindowAttributesChanged(lp);
        //(当Window的Attributes改变时系统会调用此函数)
        window.attributes = lp
        show()
    }
}