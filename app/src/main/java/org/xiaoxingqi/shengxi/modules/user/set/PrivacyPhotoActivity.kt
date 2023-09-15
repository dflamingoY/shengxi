package org.xiaoxingqi.shengxi.modules.user.set

import android.view.View
import kotlinx.android.synthetic.main.activity_privacy_photo.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class PrivacyPhotoActivity : BaseThemeNoSwipeActivity() {
    private var value: Int = 7
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_photo
    }

    override fun initView() {
        iv_hint_drawable.visibility= View.GONE

    }

    override fun initData() {
        clear()
        when (intent.getIntExtra("privacy", 0)) {
            7 -> {
                viewTime.changeStatus(true)
                value = 7
            }
            30 -> {
                view_month.changeStatus(true)
                value = 30
            }
            -1 -> {
                view_all.changeStatus(true)
                value = -1
            }
            else -> {
                viewNever.changeStatus(true)
                value = 0
            }
        }
    }

    private fun clear() {
        viewTime.changeStatus(false)
        viewNever.changeStatus(false)
        view_month.changeStatus(false)
        view_all.changeStatus(false)
    }

    override fun initEvent() {
        viewTime.setOnClickListener {
            if (7 == value)
                return@setOnClickListener
            clear()
            viewTime.changeStatus(true)
            value = 7
            request(0)
        }
        viewNever.setOnClickListener {
            if (0 == value)
                return@setOnClickListener
            clear()
            viewNever.changeStatus(true)
            value = 0
            request(0)
        }
        view_month.setOnClickListener {
            if (30 == value)
                return@setOnClickListener
            clear()
            view_month.changeStatus(true)
            value = 30
            request(0)

        }
        view_all.setOnClickListener {
            if (-1 == value)
                return@setOnClickListener
            clear()
            view_all.changeStatus(true)
            value = -1
            request(0)
        }
        btn_Back.setOnClickListener { finish() }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        val infoData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        saveSetting(infoData.user_id, "voice_visible_days", value) {
            if (it != null) {
                if (it.code == 0)
                    SPUtils.setBoolean(this@PrivacyPhotoActivity, IConstant.STRANGEVIEW + infoData.user_id, value == 0)
                else
                    showToast(it.msg)
            }
            transLayout.showContent()
        }
    }
}