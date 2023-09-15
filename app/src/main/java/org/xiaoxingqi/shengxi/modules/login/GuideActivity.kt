package org.xiaoxingqi.shengxi.modules.login

import android.content.Intent
import android.os.CountDownTimer
import android.view.*
import kotlinx.android.synthetic.main.activity_guide.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.wedgit.skinView.cardView.SkinCardViewInflater
import skin.support.SkinCompatManager
import skin.support.design.SkinMaterialManager
import skin.support.design.app.SkinMaterialViewInflater


class GuideActivity : BaseNormalActivity() {

    private var timer: TimeDown? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_guide
    }

    override fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun initData() {
        if (SkinMaterialManager.getInstance() == null) {
            SkinMaterialManager.init(this)
            SkinCompatManager.init(this)                         // 基础控件换肤初始化
                    .addInflater(SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
                    .addInflater(SkinCardViewInflater())
                    .loadSkin()
        }

        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            iv_guide.setImageResource(R.drawable.draw_guide_e)
        }
        timer = TimeDown(5000, 1000)
        timer?.start()
    }

    override fun initEvent() {
        iv_guide.setOnClickListener {
            timer?.onFinish()
            timer?.cancel()
        }
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.let {
            it.cancel()
            timer = null
        }
    }

    /**
     * 3S倒计时进入主页面
     */
    private inner class TimeDown(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

        override fun onFinish() {
            startActivity(Intent(this@GuideActivity, MainActivity::class.java).putExtra("isCheckIp", true))
            finish()
        }

        override fun onTick(millisUntilFinished: Long) {

        }
    }

    override fun finish() {
        overridePendingTransition(R.anim.act_enter_alpha, R.anim.act_guide_anim_exit)
        super.finish()
    }
}
