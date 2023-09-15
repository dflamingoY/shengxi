package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

//cheers 播放的带框的View  中间间隔固定距离, 画6条线
/**
 * 每隔300ms 更新一次UI 三个长条依次展示, 停止则默认展示全部
 */
class RectPlayView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SkinCompatSupportable {

    private var itemColorId = 0
    private var itemColor = 0
    private var rectWidth = 0f//中间间隔的宽度
    private val paint = Paint()
    private var shortLine = 13f
    private var longLine = 21f
    private var DP: Float
    private var itemWidth = 2f
    private var margin = 4f
    private var isStart = false
    private var currentValue = 0

    init {
        val arrays = context.obtainStyledAttributes(attrs, R.styleable.RectPlayView)
        rectWidth = arrays.getDimension(R.styleable.RectPlayView_playWidth, 0f)
        itemColorId = arrays.getResourceId(R.styleable.RectPlayView_playColor, 0)
        arrays.recycle()
        SkinCompatResources.init(context)
        applySkin()
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
        paint.color = itemColor
        shortLine = AppTools.dp2px(context, 13).toFloat()
        longLine = AppTools.dp2px(context, 21).toFloat()
        DP = context.resources.displayMetrics.density
        margin = DP * 4
        itemWidth = DP * 2
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(measuredWidth / 2f, measuredHeight / 2f)
        if (currentValue.rem(4) == 1 || (currentValue.rem(4) == 0 && !isStart) || currentValue.rem(4) == 2 || currentValue.rem(4) == 3) {
            canvas.drawRoundRect(-rectWidth / 2f - margin, -shortLine / 2, -rectWidth / 2 - margin - itemWidth, shortLine / 2, 3f, 3f, paint)
            canvas.drawRoundRect(rectWidth / 2f + margin, -shortLine / 2, rectWidth / 2 + margin + itemWidth, shortLine / 2, 3f, 3f, paint)
        }
        if (currentValue.rem(4) == 3 || (currentValue.rem(4) == 0 && !isStart) || currentValue.rem(4) == 2) {
            canvas.drawRoundRect(-rectWidth / 2f - margin * 2 - itemWidth, -longLine / 2, -rectWidth / 2 - margin * 2 - itemWidth * 2, longLine / 2, 3f, 3f, paint)
            canvas.drawRoundRect(rectWidth / 2f + margin * 2 + itemWidth, -longLine / 2, rectWidth / 2 + margin * 2 + itemWidth * 2, longLine / 2, 3f, 3f, paint)
        }
        if (currentValue.rem(4) == 3 || (currentValue.rem(4) == 0 && !isStart)) {
            canvas.drawRoundRect(-rectWidth / 2f - margin * 3 - itemWidth * 2, -shortLine / 2, -rectWidth / 2 - margin * 3 - itemWidth * 3, shortLine / 2, 3f, 3f, paint)
            canvas.drawRoundRect(rectWidth / 2f + margin * 3 + itemWidth * 2, -shortLine / 2, rectWidth / 2 + margin * 3 + itemWidth * 3, shortLine / 2, 3f, 3f, paint)
        }
        canvas.restore()
        if (isStart) {
            postDelayed({
                if (isStart) {
                    currentValue++
                    postInvalidateOnAnimation()
                }
            }, 200)
        }
    }

    override fun applySkin() {
        try {
            itemColor = SkinCompatResources.getInstance().getColorStateList(itemColorId).defaultColor
        } catch (e: Exception) {
        }
    }

    fun start() {
        isStart = true
        postInvalidateOnAnimation()
    }

    fun end() {
        isStart = false
        currentValue = 0
        postInvalidateOnAnimation()
    }

}