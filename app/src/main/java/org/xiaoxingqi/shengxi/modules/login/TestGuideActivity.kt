package org.xiaoxingqi.shengxi.modules.login

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import android.view.ViewAnimationUtils
import kotlinx.android.synthetic.main.activity_text_guide.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.listen.PersonalityActivity
import org.xiaoxingqi.shengxi.utils.AppTools

class TestGuideActivity : BaseNormalActivity() {
    private var isSign = false
    override fun getLayoutId(): Int {
        return R.layout.activity_text_guide
    }

    override fun initView() {
    }

    override fun initData() {
        isSign = intent.getBooleanExtra("isSign", false)
        if (isSign) {

        } else {
            frameTest.visibility = View.GONE
        }
    }

    override fun initEvent() {
        tv_TextNow.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).putExtra("isEnterTest", "start"))
            finish()
        }
        tv_Delay.setOnClickListener {
            val reveal = ViewAnimationUtils.createCircularReveal(frameTest, 0,
                    frameTest.height, Math.sqrt(Math.pow(AppTools.getWindowsWidth(this).toDouble(), 2.0)
                    + Math.pow(AppTools.getWindowsHeight(this).toDouble(), 2.0)).toFloat(), 0f)
            reveal.duration = 600
            reveal.start()
            reveal.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: android.animation.Animator?) {

                }

                override fun onAnimationEnd(animation: android.animation.Animator?) {
                    /**
                     * 有部分机型出现 动画结束在非UI 线程, 使用handler
                     */
                    runOnUiThread {
                        frameTest.visibility = View.GONE
                    }
                }

                override fun onAnimationCancel(animation: android.animation.Animator?) {

                }

                override fun onAnimationStart(animation: android.animation.Animator?) {

                }
            })
        }
        tv_Ignore.setOnClickListener {
            if (isSign)
                startActivity(Intent(this, MainActivity::class.java)
                        .putExtra("isSign", true))
            finish()
        }
        tv_Desc.setOnClickListener {
            if (isSign) {
                startActivity(Intent(this, MainActivity::class.java).putExtra("isEnterTest", "start"))
            } else {
                startActivity(Intent(this, PersonalityActivity::class.java))
            }
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSign) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}