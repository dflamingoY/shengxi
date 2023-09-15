package org.xiaoxingqi.shengxi.wedgit.barrage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.xiaoxingqi.shengxi.utils.AppTools
import java.util.*

/**
 * 1.一行展示3条记录  始终只有9条记录
 * 2.平分屏幕 延时发射
 * 3.不断加入发射队列
 * 4.超出屏幕之后, 查看数据池,继续发射新的弹幕
 */
class BarrageView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val random by lazy { Random() }
    private var isStart = false
    private val paint = Paint().apply {
        textSize = 11f * context.resources.displayMetrics.density
    }
    private var array: Array<BarrageDraw> = Array(9) {
        BarrageDraw(this, context, it, AppTools.getWindowsWidth(context).toFloat(), paint.measureText("十个字的名字张无忌"), it.rem(3) * paint.measureText("十个字的名字张无忌") / (AppTools.getWindowsWidth(context) + paint.measureText("十个字的名字张无忌") / 2) * 15000)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == GONE) {
            stop()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (isStart) {
            array.forEach {
                it.onDraw(canvas)
            }
            postInvalidateOnAnimation()
        }
    }

    fun stop() {
        try {
            isStart = false
            array.forEach {
                it.end()
            }
        } catch (e: Exception) {
        }
    }

    fun setStart() {
        isStart = true
        invalidate()
    }

    /*
     * 统一回调参数到主界面
     */
    fun setData(datas: Array<String>) {
        try {
            val textSize = paint.measureText("十个字的名字张无忌")
            for (index in array.indices) {
                if (index < datas.size) {
                    val name = datas[index]
                    if (!TextUtils.isEmpty(name)) {
                        val realWidth = paint.measureText(name)
                        if (array[index].bean == null) {
                            array[index].bean = BarrageBean(name, nextName = null, textWidth = textSize, realWidth = realWidth, nextRealWidth = 0f, isCenter = random.nextBoolean())
                        } else {
                            array[index].bean?.let {
                                it.nextName = name
                                it.nextRealWidth = realWidth
                            }
                        }
                        if (TextUtils.isEmpty(array[index].bean!!.name) && array[index].isStart && TextUtils.isEmpty(array[index].bean!!.nextName)) {
                            array[index].bean?.let {
                                it.name = name
                                it.nextRealWidth = realWidth
                            }
                        }
                        if (isStart) {
                            array[index].bean?.let {
                                it.nextName = name
                                it.nextRealWidth = realWidth
                            }
                        }
                    }
                } else {
                    array[index].bean = BarrageBean("", nextName = null, textWidth = textSize, realWidth = 0f, nextRealWidth = 0f, isCenter = random.nextBoolean())
                }
//                if (!isStart) {
                array[index].start()
//                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCallFinish() {
        onDelayListener?.onDelay()
    }

    private var onDelayListener: OnDelayListener? = null

    fun setOnDelayListener(listener: OnDelayListener) {
        onDelayListener = listener
    }

    interface OnDelayListener {
        fun onDelay()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }
}