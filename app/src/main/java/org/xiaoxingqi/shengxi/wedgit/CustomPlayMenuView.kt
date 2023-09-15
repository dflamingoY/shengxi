package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_custom_play_menu.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.App
import org.xiaoxingqi.shengxi.impl.OnCircleMenuOperatorListener
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.wedgit.customPlayMenu.CustomHoverRelative

class CustomPlayMenuView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    companion object {
        private val playMenuList = ArrayList<CustomPlayMenuView>()
        fun clearList() {
            playMenuList.clear()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_custom_play_menu
    }

    init {
        if (!playMenuList.contains(this)) {
            playMenuList.add(this)
        }
        viewTreeObserver.addOnWindowAttachListener(object : ViewTreeObserver.OnWindowAttachListener {
            override fun onWindowDetached() {
                playMenuList.remove(this@CustomPlayMenuView)
            }

            override fun onWindowAttached() {
            }
        })
        customRelative.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                customRelative.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (customRelative.currentX < 0 || customRelative.currentX >= AppTools.getWindowsWidth(context)) {
                    customRelative.x = customRelative.currentX
                    customRelative.y = customRelative.currentY
                    if (customRelative.currentX < 0) {
                        touchViewLeft.visibility = View.VISIBLE
                        touchViewRight.visibility = View.GONE
                    } else {
                        touchViewLeft.visibility = View.GONE
                        touchViewRight.visibility = View.VISIBLE
                    }
                } else if (customRelative.floatX != -1f && customRelative.floatY != -1f) {//没有移动过小助手
                    customRelative.x = customRelative.floatX
                    customRelative.y = customRelative.floatY
                    touchViewRight.visibility = View.GONE
                    touchViewLeft.visibility = View.GONE
                }
                //改变Y轴 首次进入
                touchViewRight.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        touchViewRight.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        touchViewRight.y = customRelative.y
                    }
                })
                touchViewLeft.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        touchViewLeft.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        touchViewLeft.y = customRelative.y
                    }
                })
            }
        })
        ivAuto.isSelected = SPUtils.getBoolean(context, IConstant.PLAY_MENU_AUTO, false)
        touchViewRight.setOnClickListener {
            it.visibility = GONE
            clickShowMenu()
        }
        touchViewLeft.setOnClickListener {
            it.visibility = View.GONE
            clickShowMenu()
        }
        ivPre.setOnClickListener {
            listener?.pre()
        }
        ivPosition.setOnClickListener {
            listener?.top()
        }
        ivNext.setOnClickListener {
            listener?.next()
        }
        ivPlay.setOnClickListener {
            listener?.play()
        }
        ivAuto.setOnClickListener {
            ivAuto.isSelected = !it.isSelected
            SPUtils.setBoolean(context, IConstant.PLAY_MENU_AUTO, ivAuto.isSelected)
            //第一次关闭时, 提示,跟随手机, 不跟帐号
            if (!ivAuto.isSelected) {
                if (!SPUtils.getBoolean(context, IConstant.PLAY_MENU_IS_TOAST + App.uid, false)) {
                    SPUtils.setBoolean(context, IConstant.PLAY_MENU_IS_TOAST + App.uid, true)
                    Toast.makeText(context, "已关闭自动播放", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (!SPUtils.getBoolean(context, IConstant.PLAY_MENU_IS_TOAST_OPEN + App.uid, false)) {
                    Toast.makeText(context, "已开启自动播放", Toast.LENGTH_SHORT).show()
                    SPUtils.setBoolean(context, IConstant.PLAY_MENU_IS_TOAST_OPEN + App.uid, true)
                }
            }
            //更新之前的UI
            playMenuList.forEach { view ->
                if (view != this@CustomPlayMenuView) {
                    view.changeSelected(ivAuto.isSelected)
                }
            }
        }
        customRelative.setOnSlideChangeListener(object : CustomHoverRelative.OnSlideChangeListener {
            override fun onChange(x: Float, y: Float) {
                playMenuList.forEach {
                    it.updateLocation(x, y, it == this@CustomPlayMenuView)
                }
            }

            override fun visible() {
                playMenuList.forEach {
                    it.changeVisible()
                }
            }
        })
    }

    private fun clickShowMenu() {
        customRelative.x = customRelative.floatX
        customRelative.y = customRelative.floatY
        //重置 currentX currentY的位置
        customRelative.setCurrent(customRelative.floatX, customRelative.floatY)
        playMenuList.forEach {
            if (it != this@CustomPlayMenuView) {
                it.resetStatus()
            }
        }
    }

    private fun resetStatus() {
        touchViewRight.visibility = View.GONE
        touchViewLeft.visibility = View.GONE
        customRelative.x = customRelative.floatX
        customRelative.y = customRelative.floatY
    }

    private fun changeVisible() {
        if (customRelative.currentX < 0) {
            touchViewLeft.visibility = View.VISIBLE
        } else
            touchViewRight.visibility = View.VISIBLE
    }

    private fun changeSelected(isSelected: Boolean) {
        ivAuto.isSelected = isSelected
    }

    //更新位置
    private fun updateLocation(x: Float, y: Float, isCurrent: Boolean) {
        if (!isCurrent)
            customRelative.floatX = x
        customRelative.y = y
        if (x < 0 || x >= measuredWidth) {
            if (x < 0) {
                if (touchViewLeft.visibility != View.VISIBLE)
                    touchViewLeft.visibility = View.VISIBLE
            } else {
                if (touchViewRight.visibility != View.VISIBLE)
                    touchViewRight.visibility = View.VISIBLE
            }
        } else {
            if (touchViewLeft.visibility != View.GONE)
                touchViewLeft.visibility = View.GONE
            if (touchViewRight.visibility != View.GONE)
                touchViewRight.visibility = View.GONE
        }
        touchViewLeft.y = customRelative.y
        touchViewRight.y = customRelative.y
    }

    override fun setSelected(selected: Boolean) {
        if (customRelative.visibility != View.VISIBLE) {
            customRelative.visibility = View.VISIBLE
        }
        ivPlay.isSelected = selected
    }

    private var listener: OnCircleMenuOperatorListener? = null

    fun setOnCircleMenuListener(listener: OnCircleMenuOperatorListener) {
        this.listener = listener
    }
}