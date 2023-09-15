package org.xiaoxingqi.shengxi.modules.user.set

import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_privacy_select.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class LightPrivacyActivity : BaseThemeNoSwipeActivity() {
    private var value = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {
        tv_Content.text = resources.getString(R.string.string_11)
        tvTitle.text = "点亮"
        viewAll.setTitle("展示")
        viewFriend.setTitle("不展示")
        val params = iv_hint_drawable.layoutParams as LinearLayout.LayoutParams
        val width = AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 92 * 2)
        params.width = width
        params.height = (width * 300f / 172).toInt()
        iv_hint_drawable.setImageResource(R.drawable.drawable_light_guide)
    }

    override fun initData() {
        value = intent.getIntExtra("value", 1)
        if (value == 1) {
            viewAll.isSelected = true
        } else
            viewFriend.isSelected = true
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        viewAll.setOnClickListener {
            it.isSelected = true
            viewFriend.isSelected = false
        }
        viewFriend.setOnClickListener {
            it.isSelected = true
            viewAll.isSelected = false
        }
    }

    override fun finish() {
        if (value == if (viewAll.isSelected) 1 else 0) {//没变
            super.finish()
        } else {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            transLayout.showProgress()
            saveSetting(loginBean.user_id, "voice_collection_visibility", if (viewAll.isSelected) 1 else 0, "other") {
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