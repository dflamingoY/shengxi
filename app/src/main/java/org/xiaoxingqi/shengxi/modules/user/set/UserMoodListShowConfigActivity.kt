package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_cover_mini_machine_config.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class UserMoodListShowConfigActivity : BaseThemeNoSwipeActivity() {
    private var value = 2
    override fun getLayoutId(): Int {
        return R.layout.activity_cover_mini_machine_config
    }

    override fun initView() {
        tvDesc.text = resources.getString(R.string.string_privacyAct_20)
        tvDesc.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        tvTitle.text = resources.getString(R.string.string_privacyAct_21)
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        tvRule.text = resources.getString(R.string.string_privacyAct_22)
        viewNever.setTitle(resources.getString(R.string.string_privacyAct_23))
        viewTime.setTitle(resources.getString(R.string.string_privacyAct_24))
        iv_hint_drawable.setImageResource(R.drawable.draw_privacy_home_user_album)
    }

    override fun initData() {
        value = intent.getIntExtra("value", 2)
        viewNever.isSelected = value == 2
        viewTime.isSelected = !viewNever.isSelected
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewNever.setOnClickListener {
            viewTime.isSelected = false
            it.isSelected = true
        }
        viewTime.setOnClickListener {
            it.isSelected = true
            viewNever.isSelected = false
        }
    }

    override fun finish() {
        if (value == if (viewTime.isSelected) 1 else 2) {//没变
            super.finish()
        } else {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            transLayout.showProgress()
            saveSetting(loginBean.user_id, "share_voice_visibility", if (viewTime.isSelected) 1 else 2) {
                transLayout.showContent()
                if (it != null) {
                    if (it.code == 0)
                        super.finish()
                    else
                        showToast(it.msg)
                }
            }
        }
    }
}