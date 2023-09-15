package org.xiaoxingqi.shengxi.wedgit.customPlayMenu

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import com.nineoldandroids.view.ViewHelper
import kotlin.math.abs

class TouchSlidView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {
    private var gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return performClick()
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (ViewHelper.getY(this@TouchSlidView) > 0 && ViewHelper.getY(this@TouchSlidView) < (parentHeight - height)) {
                    if (ViewHelper.getTranslationY(this@TouchSlidView) + (e2.y - e1.y) < 0 || ViewHelper.getTranslationY(this@TouchSlidView) + (e2.y - e1.y) > (parentHeight - height)) {
                        return false
                    }
                    ViewHelper.setTranslationY(this@TouchSlidView, ViewHelper.getTranslationY(this@TouchSlidView) + (e2.y - e1.y))
                    return true
                } else {
                    val translationY = ViewHelper.getY(this@TouchSlidView)
                    if (abs(translationY - marginT) < abs(translationY - marginB)) {
                        ViewHelper.setY(this@TouchSlidView, 0f)
                        return if (distanceY < 0) {
                            ViewHelper.setTranslationY(this@TouchSlidView, ViewHelper.getTranslationY(this@TouchSlidView) + (e2.y - e1.y))
                            true
                        } else {
                            false
                        }
                    } else {
                        ViewHelper.setY(this@TouchSlidView, (parentHeight - height).toFloat())
                        return if (distanceY > 0) {
                            ViewHelper.setTranslationY(this@TouchSlidView, ViewHelper.getTranslationY(this@TouchSlidView) + (e2.y - e1.y))
                            true
                        } else {
                            false
                        }
                    }
                }
                return true
            }
        })
    }

    private var marginT = -1
    private var marginB = -1
    private var parentHeight = -1
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                parent?.let {
                    if (marginT == -1 && marginB == -1) {
                        val group = (it as ViewGroup)
                        marginT = group.top
                        marginB = group.bottom
                        parentHeight = group.height
                    }
                }
            }
        }
        return true
    }

}