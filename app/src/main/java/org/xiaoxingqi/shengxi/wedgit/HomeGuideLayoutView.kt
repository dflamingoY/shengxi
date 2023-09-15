package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.layout_home_guide.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.*

class HomeGuideLayoutView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseLayout(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int {
        return R.layout.layout_home_guide
    }

    init {
        val layoutParams = ivGuideScenes.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin = AppTools.getStatusBarHeight(context) + AppTools.dp2px(context, 10)
        ivGuideScenes.layoutParams = layoutParams

        ivPlayMenu10.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                ivPlayMenu10.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (visibility == View.VISIBLE) {
                    startAnim(ivPlayMenu1, 0)
                    startAnim(ivPlayMenu2, 350 * 1 / 2)
                    startAnim(ivPlayMenu3, 350 * 2 / 2)
                    startAnim(ivPlayMenu4, 350 * 3 / 2)
                    startAnim(ivPlayMenu5, 350 * 4 / 2)
                    startAnim(ivPlayMenu6, 350 * 5 / 2)
                    startAnim(ivPlayMenu7, 350 * 6 / 2)
                    startAnim(ivPlayMenu8, 350 * 7 / 2)
                    startAnim(ivPlayMenu9, 350 * 8 / 2)
                    startAnim(ivPlayMenu10, 350 * 9 / 2)
                    ivPlayMenu10.postDelayed({
                        tvNext.visibility = View.VISIBLE
                    }, 1920)
                }
            }
        })
        tvDismiss.setOnClickListener {
            //指定延时动画
            val login = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setBoolean(context, IConstant.HOME_GUIDE_VISIBLE + login.user_id, true)
            this.visibility = View.GONE
        }
        tvNext.setOnClickListener {
            ivGuideScenes.alpha = 1f
            relativeOther.visibility = View.GONE
            framePlayMenu.visibility = View.GONE
            ivGuideArrow.visibility = View.VISIBLE
            it.visibility = View.GONE
            startAnim(ivGuide1, 0)
            startAnim(ivGuide2, 350 * 1 / 2)
            startAnim(ivGuide3, 350 * 2 / 2)
            startAnim(ivGuide4, 350 * 3 / 2)
            startAnim(ivGuide5, 350 * 4 / 2)
            startAnim(ivGuide6, 350 * 5 / 2)
            startAnim(ivGuide7, 350 * 6 / 2)
            startAnim(ivGuide8, 350 * 7 / 2)
            startAnim(ivGuide9, 350 * 8 / 2)
            ivGuide1.postDelayed({
                tvDismiss.visibility = View.VISIBLE
            }, 1750)
        }
    }

    private fun startAnim(view: View, delay: Long, duration: Long = 100) {
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
//        ObjectAnimator.ofFloat(view, "translationX", -AppTools.dp2px(context, 197).toFloat(), 0f).setDuration(duration).setDuration(delay).start()
    }

}