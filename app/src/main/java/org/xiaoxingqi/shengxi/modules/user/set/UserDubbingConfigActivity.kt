package org.xiaoxingqi.shengxi.modules.user.set

import android.view.View
import kotlinx.android.synthetic.main.activity_privacy_select.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class UserDubbingConfigActivity : BaseThemeNoSwipeActivity() {
    private var preValue = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {
        viewSelf.visibility = View.VISIBLE
        tvTitle.text = String.format(resources.getString(R.string.string_privacyAct_27), "配音")
        tv_Content.text = resources.getString(R.string.string_alarm_setting_1)
        tv_hint_2.text = resources.getString(R.string.string_alarm_setting_2)
    }

    override fun initData() {
        preValue = intent.getIntExtra("value", 0)
        when (preValue) {
            1 -> viewAll.isSelected = true
            2 -> viewFriend.isSelected = true
            3 -> viewSelf.isSelected = true
        }
    }

    override fun initEvent() {
        viewAll.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        viewFriend.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        viewSelf.setOnClickListener {
            clearSelected()
            it.isSelected = true
        }
        btn_Back.setOnClickListener { finish() }
    }

    private fun clearSelected() {
        viewAll.isSelected = false
        viewFriend.isSelected = false
        viewSelf.isSelected = false
    }

    override fun finish() {
        val current = if (viewAll.isSelected) 1 else if (viewFriend.isSelected) 2 else 3
        if (preValue == current)
            super.finish()
        else {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            transLayout.showProgress()
            saveSetting(loginBean.user_id, "dubbing_visibility", current) {
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