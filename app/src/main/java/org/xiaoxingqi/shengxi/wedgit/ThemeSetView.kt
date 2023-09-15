package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.layout_theme_set.view.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.impl.OnThemeEvent
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import skin.support.SkinCompatManager

class ThemeSetView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {
    init {
        val params = view_status_height.layoutParams
        params.height = AppTools.getStatusBarHeight(context)
        view_status_height.layoutParams = params

        ObjectAnimator.ofFloat(linearContent, "translationY", -AppTools.getWindowsHeight(context).toFloat()).setDuration(0).start()
        ObjectAnimator.ofFloat(frame_theme_bg, "alpha", 1f, 0f).setDuration(0).start()
        frame_theme_bg.visibility = View.GONE
        judeTheme()
        frame_theme_bg.setOnClickListener {
            show()
        }
        themeDay.setOnClickListener {
            themeNight.isSelected = false
            themeDay.isSelected = true
            if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
                EventBus.getDefault().post(OnThemeEvent())
//                RippleAnimation.create(themeDay).setDuration(1000).start(500)
            }
        }
        themeNight.setOnClickListener {
            themeDay.isSelected = false
            themeNight.isSelected = true
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
                EventBus.getDefault().post(OnThemeEvent())
//                RippleAnimation.create(themeNight).setDuration(1000).start(500)
            }
        }
        themeEmpty.setOnClickListener {
            clearSelected()
            themeEmpty.isSelected = true
        }
        themeRain.setOnClickListener {
            clearSelected()
            themeRain.isSelected = true
        }
        themeSnow.setOnClickListener {
            clearSelected()
            themeSnow.isSelected = true
        }
        themeLeaves.setOnClickListener {
            clearSelected()
            themeLeaves.isSelected = true
        }
        themeFlower.setOnClickListener {
            clearSelected()
            themeFlower.isSelected = true
        }
        themeMaple.setOnClickListener {
            clearSelected()
            themeMaple.isSelected = true
        }
    }

    private var currentScenes = ""

    private fun judeTheme() {
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            themeDay.isSelected = true
            themeNight.isSelected = false
        } else {
            themeDay.isSelected = false
            themeNight.isSelected = true
        }
        var theme = try {
            val obj = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.getString(context, IConstant.THEMEKEY + obj.user_id, "")
        } catch (e: Exception) {
            ""
        }
        clearSelected()
        when (theme) {
            IConstant.THEME_RAIN -> {
                currentScenes = IConstant.THEME_RAIN
                themeRain.isSelected = true
            }
            IConstant.THEME_SNOW -> {
                currentScenes = IConstant.THEME_SNOW
                themeSnow.isSelected = true
            }
            IConstant.THEME_FLOWER -> {
                currentScenes = IConstant.THEME_FLOWER
                themeFlower.isSelected = true
            }
            IConstant.THEME_LEAVES -> {
                currentScenes = IConstant.THEME_LEAVES
                themeLeaves.isSelected = true
            }
            IConstant.THEME_MAPLE -> {
                currentScenes = IConstant.THEME_MAPLE
                themeMaple.isSelected = true
            }
            else -> {
                currentScenes = ""
                themeEmpty.isSelected = true
            }
        }
    }

    private fun clearSelected() {
        themeEmpty.isSelected = false
        themeRain.isSelected = false
        themeSnow.isSelected = false
        themeLeaves.isSelected = false
        themeFlower.isSelected = false
        themeMaple.isSelected = false
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_theme_set
    }

    private var isShow = false
    private var isRun = false

    private fun isChange(): String {
        return when {
            themeEmpty.isSelected -> ""
            themeRain.isSelected -> IConstant.THEME_RAIN
            themeSnow.isSelected -> IConstant.THEME_SNOW
            themeLeaves.isSelected -> IConstant.THEME_LEAVES
            themeFlower.isSelected -> IConstant.THEME_FLOWER
            themeMaple.isSelected -> IConstant.THEME_MAPLE
            else -> ""
        }
    }

    fun show() {
        if (isRun)
            return
        if (isShow) {
            ObjectAnimator.ofFloat(frame_theme_bg, "alpha", 1f, 0f).setDuration(320).start()
            val animator = ObjectAnimator.ofFloat(linearContent, "translationY", -AppTools.getWindowsHeight(context).toFloat()).setDuration(320)
            animator.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator?) {
                    isRun = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isRun = false
                    isShow = false
                    frame_theme_bg.visibility = View.GONE
                    if (!currentScenes.equals(isChange(), true)) {
                        val obj = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                        SPUtils.setString(context, IConstant.THEMEKEY + obj.user_id, isChange())
                        dismissListener?.onDismiss()
                    }
                }
            })
            animator.start()
        } else {
            judeTheme()
            frame_theme_bg.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(frame_theme_bg, "alpha", 0f, 1f).setDuration(320).start()
            val animator = ObjectAnimator.ofFloat(linearContent, "translationY", 0f).setDuration(320)
            animator.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator?) {
                    isRun = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isRun = false
                    isShow = true
                }
            })
            animator.start()
        }
    }

    fun isShow(): Boolean {
        return isShow
    }

    private var dismissListener: OnDismissListener? = null

    fun setOnDismissListener(dismissListener: OnDismissListener) {
        this.dismissListener = dismissListener
    }

    interface OnDismissListener {
        fun onDismiss()

        fun skinChange()
    }
}