package org.xiaoxingqi.shengxi.modules.login

import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_sign_guide_1.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseLoginAct

class SignGuideActivity1 : BaseLoginAct() {
    private var identityType = "A"

    override fun getLayoutId(): Int {
        return R.layout.activity_sign_guide_1
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initEvent() {
        tvPorn.setOnClickListener {
            linearSignSeason.visibility = View.GONE
            linearFinish.visibility = View.VISIBLE
        }
        tvTalk.setOnClickListener {
            linearSignSeason.visibility = View.GONE
            linearIdentity.visibility = View.VISIBLE
        }
        tvFinish.setOnClickListener {
            linearSignSeason.visibility = View.VISIBLE
            linearFinish.visibility = View.GONE
        }
        tvRecord.setOnClickListener {
            linearLastStep.visibility = View.GONE
            linearLastStep.visibility = View.VISIBLE
            linearIdentity.visibility = View.GONE
            identityType = "A"
        }
        tvIdentityTalk.setOnClickListener {
            linearLastStep.visibility = View.GONE
            linearLastStep.visibility = View.VISIBLE
            linearIdentity.visibility = View.GONE
            identityType = "B"
        }
        tvIgnore.setOnClickListener {
            startActivity<SignRecordActivity>("openId" to intent.getStringExtra("openId"),
                    "unionId" to intent.getStringExtra("unionId"),
                    "authType" to intent.getIntExtra("authType", 1),
                    "name" to intent.getStringExtra("name"),
                    "answers" to "[\"B\", \"${identityType}\", \"A\"]",
                    "third" to intent.getBooleanExtra("third", false),
                    "identity" to if (identityType == "A") "I" else "E",
                    "phone" to intent.getStringExtra("phone")
            )
            finish()
        }
        btn_Back.setOnClickListener {
            //允许 操作返回到上一级
            when {
                linearLastStep.visibility == View.VISIBLE -> {
                    linearIdentity.visibility = View.VISIBLE
                    linearLastStep.visibility = View.GONE
                }
                linearIdentity.visibility == View.VISIBLE -> {
                    linearIdentity.visibility = View.GONE
                    linearSignSeason.visibility = View.VISIBLE
                }
                linearFinish.visibility == View.VISIBLE -> {
                    linearSignSeason.visibility = View.VISIBLE
                    linearFinish.visibility = View.GONE
                }
                else -> {
                    finish()
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                linearLastStep.visibility == View.VISIBLE -> {
                    linearIdentity.visibility = View.VISIBLE
                    linearLastStep.visibility = View.GONE
                }
                linearIdentity.visibility == View.VISIBLE -> {
                    linearIdentity.visibility = View.GONE
                    linearSignSeason.visibility = View.VISIBLE
                }
                linearFinish.visibility == View.VISIBLE -> {
                    linearSignSeason.visibility = View.VISIBLE
                    linearFinish.visibility = View.GONE
                }
                else -> {
                    finish()
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}