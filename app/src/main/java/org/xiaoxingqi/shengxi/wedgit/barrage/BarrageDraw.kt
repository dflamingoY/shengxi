package org.xiaoxingqi.shengxi.wedgit.barrage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.Log
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import java.util.*

class BarrageDraw(val view: BarrageView, val context: Context, var index: Int, val startX: Float, private val endX: Float, private val startDelay: Float = 0f) {
    var bean: BarrageBean? = null
    private var current = 0f
    private var anim: ValueAnimator? = null
    private val paint by lazy {
        Paint()
    }
    private val random = Random()
    var isStart = false

    init {
        current = startX
//        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#b2b2b2")
        paint.textSize = 11f * context.resources.displayMetrics.density
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.isDither = true
        isStart = false
        initAnim()
    }

    private fun initAnim() {
        anim = ValueAnimator.ofFloat(startX, -endX).setDuration(15000)
        anim!!.interpolator = LinearInterpolator()
//        anim!!.repeatCount = ValueAnimator.INFINITE
//        anim!!.repeatMode = ValueAnimator.RESTART
        anim!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator?) {
                if (!TextUtils.isEmpty(bean!!.nextName)) {
                    bean!!.name = bean!!.nextName!!
                    bean!!.nextName = null
                    bean!!.realWidth = bean!!.nextRealWidth
                    bean!!.isCenter = random.nextBoolean()
                } else {
                    bean!!.name = ""
                }
            }

            override fun onAnimationStart(animation: Animator?) {
                isStart = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (index == 0) {//回调到主界面,请求网络, 获取下一批次的数据
                    view.setCallFinish()
                }
                isStart = false
            }

            override fun onAnimationCancel(animation: Animator?) {
                isStart = false
            }
        })
        anim!!.addUpdateListener {
            current = it.animatedValue as Float
        }
        anim!!.startDelay = startDelay.toLong()
    }

    fun start() {
        if (anim?.let {
                    if (!it.isStarted && !it.isRunning) {
                        it.start()
                    }
                    anim
                } == null) {
            initAnim()
            anim!!.start()
        }
    }

    fun end() {
        anim?.let {
            it.cancel()
            anim = null
        }
        isStart = false
        current = startX
    }

    fun onDraw(canvas: Canvas) {
        bean?.let {
            //0-2 第一行, 3-5 第二行  6-8 第三行
            val rate = index / 3
            //随机确定是否需要居中展示
            canvas.drawText(it.name, current + if (!it.isCenter) {
                0f
            } else {
                (it.textWidth - it.realWidth) / 2f
            }, (rate * 100 + 50).toFloat(), paint)
        }
    }
}

/**
 * @param name 当前展示的name
 * @param nextName 下一轮展示的名字
 * @param textWidth 最长名字的长度
 * @param realWidth 当前文字的真实长度
 * @param nextRealWidth 下一个真实长度
 * @param isCenter 是否在居中显示
 */
data class BarrageBean(var name: String, var nextName: String? = null,
                       var textWidth: Float = 0f, var realWidth: Float = 0f, var nextRealWidth: Float = 0f,
                       var isCenter: Boolean = false)