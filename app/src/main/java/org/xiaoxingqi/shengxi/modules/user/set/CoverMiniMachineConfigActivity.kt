package org.xiaoxingqi.shengxi.modules.user.set

import android.view.View
import kotlinx.android.synthetic.main.activity_cover_mini_machine_config.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class CoverMiniMachineConfigActivity : BaseThemeNoSwipeActivity() {
    private var value: Int = 2
    override fun getLayoutId(): Int {
        return R.layout.activity_cover_mini_machine_config
    }

    override fun initView() {
        iv_hint_drawable.setImageResource(R.drawable.draw_privacy_mini_machine)
        ivMiniIcon.visibility = View.VISIBLE
        ivMiniIcon1.visibility = View.VISIBLE
    }

    override fun initData() {
        value = intent.getIntExtra("value", 2)
        viewNever.changeStatus(value == 1)
        viewTime.changeStatus(value == 2)
    }

    override fun initEvent() {
        viewNever.setOnClickListener {
            viewTime.isSelected = false
            it.isSelected = true
        }
        viewTime.setOnClickListener {
            viewNever.isSelected = false
            it.isSelected = true
        }
        btn_Back.setOnClickListener { finish() }
    }

    override fun finish() {
        if (value == if (viewNever.isSelected) 1 else 2) {//没变
            super.finish()
        } else {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            transLayout.showProgress()
            saveSetting(loginBean.user_id, "minimachine_visibility", if (viewNever.isSelected) 1 else 2) {
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