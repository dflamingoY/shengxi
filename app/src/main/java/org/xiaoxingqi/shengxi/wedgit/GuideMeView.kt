package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.nineoldandroids.animation.AnimatorSet
import com.nineoldandroids.animation.ObjectAnimator
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

class GuideMeView : BaseLayout {
    private var relativeGuide2 = findViewById<View>(R.id.relative_guide_2)
    //    private var relativeGuide3 = findViewById<View>(R.id.relative_guide_3)
    private var ivGuide = findViewById<View>(R.id.iv_guide_3)
//    private var tvGuide = findViewById<TextView>(R.id.tv_Guide_3)

    override fun getLayoutId(): Int {
        return R.layout.layout_me_empty_guide
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setBackgroundResource(R.drawable.draw_self_center_bg)
    }

    /**
     * 执行顺序动画
     */
    fun startGuide() {
        val set2 = AnimatorSet()
        set2.playTogether(
                ObjectAnimator.ofFloat(relativeGuide2, "alpha", 0f, 1f).setDuration(320),
                ObjectAnimator.ofFloat(relativeGuide2, "translationY", -AppTools.dp2px(context, 131).toFloat(), 0f).setDuration(520),
                ObjectAnimator.ofFloat(ivGuide, "alpha", 0f, 1f).setDuration(620)
        )
        set2.start()
        /*val set3 = AnimatorSet()
        set3.playTogether(
                ObjectAnimator.ofFloat(relativeGuide3, "alpha", 0f, 1f).setDuration(420)
        )
        set3.startDelay = 1300
        set3.start()*/
    }

    fun setHintText(text: String) {
//        if (tvGuide != null) {
//            tvGuide.text = text
//        }
    }

}