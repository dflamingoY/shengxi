package org.xiaoxingqi.zxing.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.ValueAnimator
import skin.support.SkinCompatManager

class FindView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mwidth = 0
    private var mheight = 0
    private var DP = 0F
    private var paint: Paint
    private var fraction = 0F

    init {
        DP = context!!.resources.displayMetrics.density
        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            paint.color = Color.parseColor("#54E2E4")
        } else {
            paint.color = Color.parseColor("#3B9E9F")
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10F
        /*  val anim = ValueAnimator.ofFloat(0F, 1F).setDuration(2400)
          anim.repeatMode = ValueAnimator.REVERSE
          anim.repeatCount = ValueAnimator.INFINITE
          anim.interpolator = LinearInterpolator()
          anim.addUpdateListener {
              fraction = it.animatedValue as Float
              postInvalidate()
          }
          anim.start()*/
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w
        mheight = h
    }

    override fun onDraw(canvas: Canvas?) {
        val mPath = Path()

        mPath.moveTo(16 * DP, -5F)
        mPath.lineTo(-5F, -5F)
        mPath.lineTo(-5F, 16 * DP)

        mPath.moveTo(mwidth - 16 * DP, -5F)
        mPath.lineTo(mwidth.toFloat() + 5, -5F)
        mPath.lineTo(mwidth.toFloat() + 5, 16 * DP)

        mPath.moveTo(mwidth.toFloat() + 5f, mheight.toFloat() - 16 * DP)
        mPath.lineTo(mwidth.toFloat() + 5, mheight.toFloat() + 5)
        mPath.lineTo(mwidth.toFloat() - 16 * DP, mheight.toFloat() + 5)

        mPath.moveTo(16 * DP, mheight.toFloat() + 5)
        mPath.lineTo(-5f, mheight.toFloat() + 5)
        mPath.lineTo(-5F, mheight.toFloat() - 16 * DP)
        canvas?.drawPath(mPath, paint)

        /* if (fraction == 1F) {
             canvas?.drawRect(2.toFloat(), fraction * mheight - 2, (mwidth - 2).toFloat(), fraction * mheight, paint)
         } else {
             canvas?.drawRect(2.toFloat(), fraction * mheight, (mwidth - 2).toFloat(), fraction * mheight + 2, paint)
         }*/

    }

}