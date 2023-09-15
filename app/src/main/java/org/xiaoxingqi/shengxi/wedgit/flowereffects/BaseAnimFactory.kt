package org.xiaoxingqi.shengxi.wedgit.flowereffects

import android.graphics.Canvas
import com.nineoldandroids.animation.Animator

abstract class BaseAnimFactory {
    private var animator: Animator? = null
    protected var bean: FlowBean

    constructor(bean: FlowBean) {
        this.bean = bean
        animator = createAnim()
        animator?.start()
    }

    /**
     * 绘制
     */
    abstract fun onDraw(canvas: Canvas)

    /**
     * 创建动画
     */
    abstract fun createAnim(): Animator

    open fun stop() {
        animator?.let {
            it.end()
            it.cancel()
        }
    }
}