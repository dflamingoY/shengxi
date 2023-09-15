package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.layout_alarm_guide.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.*

class AlarmLayoutGuideView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseLayout(context, attrs, defStyleAttr) {
    override fun getLayoutId(): Int {
        return R.layout.layout_alarm_guide
    }

    init {
        iv13.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                iv13.viewTreeObserver.removeOnGlobalLayoutListener(this)
                //开始
                if (visibility == View.VISIBLE)
                    iv13.postDelayed({
                        startAnim(iv1, 0)
                        startAnim(iv2, 350 * 1 / 2)
                        startAnim(iv3, 350 * 2 / 2)
                        startAnim(iv4, 350 * 3 / 2)
                        startAnim(iv5, 350 * 4 / 2)
                        startAnim(iv6, 350 * 5 / 2)
                        startAnim(iv7, 350 * 6 / 2)
                        startAnim(iv8, 350 * 7 / 2)
                        startAnim(iv9, 350 * 8 / 2)
                        startAnim(iv10, 350 * 9 / 2)
                        startAnim(iv11, 350 * 10 / 2)
                        startAnim(iv12, 350 * 11 / 2)
                        startAnim(iv13, 350 * 12 / 2)
                        iv13.postDelayed({
                            tvDismiss.visibility = View.VISIBLE
                        }, 2450)
                    }, 1500)
            }
        })
        tvDismiss.setOnClickListener {
            this.visibility = View.GONE
            val loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setBoolean(context, IConstant.FIRST_ALARM_GUIDE + loginBean.user_id, false)
        }
    }

    private fun startAnim(view: View, delay: Long) {
        val animator = ValueAnimator.ofObject(ParabolaType(), PointF(-AppTools.dp2px(context, 197).toFloat(), AppTools.dp2px(context, 100).toFloat()), PointF(0f, 0f))
        animator.addUpdateListener {
            val value = it.animatedValue as PointF
            view.x = value.x
            view.y = value.y
        }
        animator.duration = 350
        animator.startDelay = delay
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.visibility = View.VISIBLE
            }
        })
        animator.start()
//        ObjectAnimator.ofFloat(view, "translationX", -AppTools.dp2px(context, 197).toFloat(), 0f).setDuration(350).setDuration(delay).start()
    }

}