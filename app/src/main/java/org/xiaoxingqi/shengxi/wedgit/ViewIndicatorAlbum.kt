package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class ViewIndicatorAlbum : View, SkinCompatSupportable {
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val array = context?.obtainStyledAttributes(attrs, R.styleable.ViewIndicatorAlbum)
        try {
            normalColor = array!!.getColor(R.styleable.ViewIndicatorAlbum_normalColorIndicator, 0)
            selectedColor = array.getColor(R.styleable.ViewIndicatorAlbum_selectedColor, 0)
            selectedColorId = array.getResourceId(R.styleable.ViewIndicatorAlbum_selectedColor, 0)
            normalColorId = array.getResourceId(R.styleable.ViewIndicatorAlbum_normalColorIndicator, 0)
        } catch (e: Exception) {

        }
        array?.recycle()
    }

    private var selectedColor = 0
    private var normalColor = 0
    private var selectedColorId = 0
    private var normalColorId = 0

    private var viewPager: ViewPager? = null
    private val paint: Paint = Paint()
    private var current = 0
    private val RADIUS = AppTools.dp2px(context, 3).toFloat() //dp 半径
    private val MARGIN = AppTools.dp2px(context, 13)//dp  间距

    init {
        paint.isDither = true
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        viewPager?.let {
            if (null != it.adapter && it.adapter!!.count > 1) {
                canvas?.save()
                canvas?.translate(measuredWidth / 2f - (it.adapter!!.count * RADIUS + (it.adapter!!.count - 1) * MARGIN) / 2, measuredHeight / 2f)
                for (index in 0 until it.adapter!!.count) {
                    if (current == index) {
                        paint.color = selectedColor
                    } else {
                        paint.color = normalColor
                    }
                    canvas?.drawCircle(RADIUS + (index + 1) + MARGIN * index, 0F, RADIUS, paint)
                }
                canvas?.restore()
            }
        }
    }

    fun attachPager(viewPager: ViewPager) {
        if (null == viewPager.adapter) {
            invalidate()
            return
        }
        if (viewPager.adapter!!.count == 1) {
            invalidate()
            return
        }
        this.viewPager = viewPager
        current = viewPager.currentItem
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                current = position
                invalidate()
            }
        })
        invalidate()
    }

    override fun applySkin() {
        try {
            val color = SkinCompatResources.getInstance().getColorStateList(selectedColorId)
            selectedColor = color.defaultColor
            val color1 = SkinCompatResources.getInstance().getColorStateList(normalColorId)
            normalColor = color1.defaultColor
            invalidate()
        } catch (e: Exception) {
        }
    }
}