package org.xiaoxingqi.shengxi.modules.user

import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_language.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.model.LaunguageData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class LanguageActivity : BaseAct() {

    lateinit var adapter: QuickAdapter<LaunguageData>
    private val mData = arrayListOf(LaunguageData("简体中文", IConstant.CN, false)
            , LaunguageData("繁体中文（台湾）", IConstant.TW, false)
            , LaunguageData("繁体中文（香港）", IConstant.HK, false))
    private var current = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_language
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        adapter = object : QuickAdapter<LaunguageData>(this, R.layout.item_launguage, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: LaunguageData?) {
                helper?.getTextView(R.id.tvName)?.text = item!!.title
                helper!!.getView(R.id.iv_Status).visibility = if (item.isSelect) View.VISIBLE else View.GONE
            }
        }
        recyclerView.adapter = adapter
        val res = resources
        val config = res.configuration
        val locale = config.locale.country
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val string = SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "").toLowerCase()
        if (!TextUtils.isEmpty(string)) {//已经保存
            caseLang(string)
        } else {
            caseLang(locale)
        }
    }

    private fun caseLang(language: String) {
        when {
            IConstant.CN.equals(language, true) -> {
                mData[0].isSelect = true
                current = 0
            }
            IConstant.TW.equals(language, true) -> {
                mData[1].isSelect = true
                current = 1
            }
            IConstant.HK.equals(language, true) -> {
                mData[2].isSelect = true
                current = 2
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            if (current == position) {
                return@setOnItemClickListener
            }
            mData[current].isSelect = false
            mData[position].isSelect = true
            adapter.notifyDataSetChanged()
            current = position
            /**
             * 更新语言设置
             */
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setString(this, IConstant.LANGUAGE + loginBean.user_id, mData[current].name)
            EventBus.getDefault().post(IConstant.REFRESHLANGUAGE)
            updateSet(mData[current].name)
        }
    }


    private fun updateSet(language: String) {
        transLayout.showProgress()
        val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("lang", language)
                .build()
        OkClientHelper.patch(this, "users/" + loginData.user_id + "/setting", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any) {
                transLayout.showContent()
            }

            override fun onFailure(any: Any) {
                transLayout.showContent()
            }
        })
    }

    override fun onEvent(str: String) {
//
    }

}