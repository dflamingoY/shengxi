package org.xiaoxingqi.shengxi.modules.user.set

import android.widget.TextView
import com.alibaba.fastjson.JSONArray
import kotlinx.android.synthetic.main.activity_user_talk_setting.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.EchoSetEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.EchoTypesData
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class UserTalkSettingActivity : BaseThemeNoSwipeActivity() {
    private lateinit var loginBean: LoginData.LoginBean
    private val dataMap by lazy {
        HashMap<String, List<EchoTypesData.EchoTypesBean>>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_user_talk_setting
    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }

    override fun initView() {
        tv_Short.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                tv_Long.isSelected = false
            }
        }
        tv_Long.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                tv_Short.isSelected = false
            }
        }
        tv_Wechat.setOnClickListener {
            it.isSelected = !it.isSelected
        }
//        tv_in.setOnClickListener {
//            it.isSelected = !it.isSelected
//        }
//        tv_Self.setOnClickListener {
//            it.isSelected = !it.isSelected
//        }
        tv_Qrcode.setOnClickListener {
            it.isSelected = !it.isSelected
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()

        when (flag) {
            0 -> {
                OkClientHelper.get(this, "chat/hobbies", EchoTypesData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as EchoTypesData
                        if (result.code == 0) {
                            result.data?.let {
                                dataMap["hobbies"] = it
                            }
                        }
                        request(1)
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.3")
            }
            1 -> {
                /**
                 * 获取聊天的私密
                 */
                OkClientHelper.get(this, "chat/tips", EchoTypesData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as EchoTypesData
                        if (result.code == 0) {
                            result.data?.let {
                                dataMap["tips"] = it
                            }
                        }
                        request(2)
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.3")
            }
            2 -> {
                OkClientHelper.get(this, "users/" + loginBean.user_id + "/setting", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any) {
                        result as PatchData
                        if (result.code == 0) {
                            recycle()
                            iv_Sleep.isSelected = result.data.auto_reply == 1
                            toggle_Button.isChecked = result.data.auto_reply == 1
                            if (result.data.chat_tips != null && result.data.chat_tips.size > 0) {
                                dataMap["tips"]?.let {
                                    for (bean in it) {
                                        for (item in result.data.chat_tips) {
                                            if (item.id == bean.id) {
                                                bean.isSelected = true
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                            if (result.data.chat_hobby != null) {
                                dataMap["hobbies"]?.let {
                                    for (bean in it) {
                                        if (result.data.chat_hobby.id == bean.id) {
                                            bean.isSelected = true
                                            break
                                        }
                                    }
                                }
                            }

                            try {
                                dataMap["hobbies"].let {
                                    if (it != null) {
                                        for (index in it.indices) {
                                            if (index == 0) {
                                                tv_Short.text = it[index].name
                                                tv_Short.isSelected = it[index].isSelected

                                            } else if (index == 1) {
                                                tv_Long.text = it[index].name
                                                tv_Long.isSelected = it[index].isSelected
                                            }
                                        }
                                    }
                                }
                                dataMap["tips"].let {
                                    if (it != null) {
                                        if (it.size >= linear_tips_container.childCount)
                                            for (index in it.indices) {
                                                if (index < linear_tips_container.childCount) {
                                                    (linear_tips_container.getChildAt(index) as TextView).text = it[index].name
                                                    linear_tips_container.getChildAt(index).isSelected = it[index].isSelected
                                                }
                                            }
                                    }
                                }
                            } catch (e: Exception) {
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any) {
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    private fun recycle() {
        dataMap["hobbies"]?.let {
            for (bean in it) {
                bean.isSelected = false
            }
        }
        dataMap["tips"]?.let {
            for (bean in it) {
                bean.isSelected = false
            }
        }
    }

    private fun update() {
        transLayout.showProgress()
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${obj.user_id}/setting", FormBody.Builder().add("autoReply", if (toggle_Button.isChecked) "1" else "0")
                .add("chatTips", parseTips())
                .add("chatHobby", getHobbies().toString())
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    transLayout.showProgress()
                    EventBus.getDefault().post(EchoSetEvent(toggle_Button.isChecked))
                    close()
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun parseTips(): String {
        val json = JSONArray()
        if (dataMap["tips"] != null) {//有数据
            for (count in 0 until linear_tips_container.childCount) {
                if (linear_tips_container.getChildAt(count).isSelected) {
                    json.add(dataMap["tips"]?.get(count)?.id ?: count + 1)
                }
            }
        } else {
            for (count in 0 until linear_tips_container.childCount) {
                if (linear_tips_container.getChildAt(count).isSelected) {
                    json.add("${count + 1}")
                }
            }
        }
        return json.toJSONString()
    }

    private fun getHobbies(): Int {
        var hobbies = 0
        if (dataMap["hobbies"] != null) {//有数据
            if (tv_Short.isSelected) {
                hobbies = dataMap["hobbies"]?.get(0)?.id ?: 1
            } else if (tv_Long.isSelected) {
                hobbies = dataMap["hobbies"]?.get(1)?.id ?: 2
            }
        } else {
            if (tv_Short.isSelected) {
                hobbies = 1
            } else if (tv_Long.isSelected) {
                hobbies = 2
            }
        }
        return hobbies
    }

    private fun close() {
        super.finish()
    }

    override fun finish() {
        //保存所有数据 不论更改
        update()
    }
}