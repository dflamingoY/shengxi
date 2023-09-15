package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_swipe_recycler.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.AlarmUpdateEvent
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils

/**
 * 台词的top榜单
 */
class TopWordActivity : BaseAct() {
    private var type = 0//榜单类型
    private var operatorType = ""
    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private lateinit var loginBean: LoginData.LoginBean
    private var uid: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_swipe_recycler
    }

    override fun initView() {
        swipeRefresh.isEnabled = false
    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        uid = intent.getStringExtra("uid")
        Log.d("Mozator", "id:$uid")
        type = intent.getIntExtra("type", 0)
        tv_Title.text = when (type) {
            0 -> {
                operatorType = "day"
                "今日台词Top 3"
            }
            1 -> {
                operatorType = "week"
                "本周台词Top 5"
            }
            2 -> {
                operatorType = "month"
                "本月台词Top 10"
            }
            else -> {
                "台词Top 10"
            }
        }
        adapter = object : QuickAdapter<BaseAlarmBean>(this, R.layout.item_alarm_dub, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseAlarmBean?) {
                helper!!.getView(R.id.voiceProgress).visibility = View.GONE
                helper.getView(R.id.linearOperate).visibility = View.GONE
                helper.getView(R.id.relative).visibility = View.VISIBLE
                helper.getView(R.id.linearWordDubbingStatus).visibility = View.VISIBLE
                if (item!!.is_anonymous == "1") {
                    Glide.with(helper.getImageView(R.id.roundImg))
                            .load(R.mipmap.icon_user_default)
                            .into(helper.getImageView(R.id.roundImg))
                    helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_alarm_anonymous)
                    helper.getView(R.id.iv_user_type).visibility = View.GONE
                } else {
                    glideUtil.loadGlide(item.user.avatar_url, helper.getImageView(R.id.roundImg), 0, glideUtil.getLastModified(item.user.avatar_url))
                    helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                    helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                }
                //自己切换显示的图片
                helper.getView(R.id.linearWordDubbingStatus).isSelected = item.is_dubbed == 1
                helper.getTextView(R.id.tvDubbingStatus).text = if (item.is_dubbed == 1) "已配音" else "配音"
                helper.getView(R.id.relative).isSelected = loginBean.user_id == item.user_id
                helper.getView(R.id.iv_more).isSelected = loginBean.user_id == item.user_id
                helper.getTextView(R.id.tv_alarm_word).text = "#${item.tag_name}#" + item.line_content
                try {
                    helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(this@TopWordActivity, item.created_at.toInt())
                } catch (e: Exception) {
                }
                helper.getTextView(R.id.tv_dubbing_count).apply {
                    text = if (item.dubbing_num > "0") item.dubbing_num else resources.getString(R.string.string_alarm_wording_dubbing)
                    setOnClickListener {
                        if (item.dubbing_num > "0")
                            startActivity(Intent(this@TopWordActivity, WordingVoiceActivity::class.java)
                                    .putExtra("isDubbed", item.is_dubbed)
                                    .putExtra("id", item.id)
                                    .putExtra("tagName", item.tag_name)
                                    .putExtra("dubbingNum", item.dubbing_num)
                                    .putExtra("userInfo", item.user)
                                    .putExtra("lineContent", item.line_content))
                        else {
                            startActivity(Intent(this@TopWordActivity, RecordVoiceActivity::class.java)
                                    .putExtra("wording", item.line_content)
                                    .putExtra("wordingId", item.id)
                                    .putExtra("resourceType", "22")
                                    .putExtra("recordType", 6))
                            overridePendingTransition(0, 0)
                        }
                    }
                }
                helper.getTextView(R.id.tv_angel).text = "您是天使" + if (item.vote_option_one > 0) " ${item.vote_option_one}" else ""
                helper.getTextView(R.id.tv_monster).text = "您是恶魔" + if (item.vote_option_two > 0) " ${item.vote_option_two}" else ""
                helper.getTextView(R.id.tv_god).text = "您是神" + if (item.vote_option_three > 0) " ${item.vote_option_three}" else ""
                changeSelected(helper)
                if (!TextUtils.isEmpty(item.vote_id))
                    when (item.vote_option) {
                        "1" -> helper.getView(R.id.iv_angel).isSelected = true
                        "2" -> helper.getView(R.id.iv_monster).isSelected = true
                        "3" -> helper.getView(R.id.iv_god).isSelected = true
                    }
                helper.getTextView(R.id.tv_alarm_word).setOnClickListener {
                    startActivity(Intent(this@TopWordActivity, WordingVoiceActivity::class.java)
                            .putExtra("isDubbed", item.is_dubbed)
                            .putExtra("id", item.id)
                            .putExtra("dubbingNum", item.dubbing_num)
                            .putExtra("tagName", item.tag_name)
                            .putExtra("userInfo", item.user)
                            .putExtra("lineContent", item.line_content))
                }
                helper.getImageView(R.id.roundImg).setOnClickListener {
                    if (item.is_anonymous != "1")
                        startActivity(Intent(this@TopWordActivity, UserDetailsActivity::class.java).putExtra("id", item.user.id))
                }
                helper.getView(R.id.relative).setOnClickListener {
                    if (item.user_id == loginBean.user_id) {
                        //删除或者只为匿名
                        DialogAlarmEdit(this@TopWordActivity).hideAnonymous(true).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete ->
                                    DialogDeleteWording(this@TopWordActivity).setOnClickListener(View.OnClickListener {
                                        deleteWord(item)
                                    }).show()
                            }
                        }).show()
                    } else {
                        //判断是否是adminUser
                        if (IConstant.userAdminArray.contains(loginBean.user_id)) {
                            AdminReportAlarmDialog(this@TopWordActivity).setType(1).setAnonymous(item.is_anonymous).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Report -> {
                                        DialogNormalReport(this@TopWordActivity).show { reportType ->
                                            reportNormalItem(item.id, reportType, "7")
                                        }
                                    }
                                    R.id.tv_deleteDubbing -> {
                                        dialogPwd = DialogCommitPwd(this@TopWordActivity).setOperator("deleteWord", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminDelete(item, FormBody.Builder().add("confirmPasswd", pwd).build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_anonymous_user -> {
                                        startActivity(Intent(this@TopWordActivity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
                                    }
                                }
                            }).show()
                        } else {
                            DialogNormalReport(this@TopWordActivity).show {
                                reportNormalItem(item.id, it, "7")
                            }
                        }
                    }
                }
                helper.getView(R.id.linearWordDubbingStatus).setOnClickListener {
                    if (item.is_dubbed == 1) {//已配音
                        startActivity(Intent(this@TopWordActivity, WordingVoiceActivity::class.java)
                                .putExtra("userInfo", item.user)
                                .putExtra("toUserId", item.to_user_id)
                                .putExtra("dubbingNum", item.dubbing_num)
                                .putExtra("isDubbed", item.is_dubbed)
                                .putExtra("tagName", item.tag_name)
                                .putExtra("id", item.id)
                                .putExtra("lineContent", "#${item.tag_name}#${item.line_content}"))
                    } else {//未配音
                        startActivity(Intent(this@TopWordActivity, RecordVoiceActivity::class.java)
                                .putExtra("wording", item.line_content)
                                .putExtra("wordingId", item.id)
                                .putExtra("resourceType", "22")
                                .putExtra("recordType", 6))
                        overridePendingTransition(0, 0)
                    }
                }
            }

            private fun changeSelected(helper: BaseAdapterHelper) {
                helper.getView(R.id.iv_angel).isSelected = false
                helper.getView(R.id.iv_monster).isSelected = false
                helper.getView(R.id.iv_god).isSelected = false
            }
        }
        recyclerReview.layoutManager = LinearLayoutManager(this)
        recyclerReview.adapter = adapter
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }

    private fun deleteWord(bean: BaseAlarmBean) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "lines/${bean.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    val position = mData.indexOf(bean)
                    mData.remove(bean)
                    adapter.notifyItemRemoved(position)
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.1")
    }

    //超管删除台词
    private fun adminDelete(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.delete(this, "admin/lines/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                    dialogPwd?.dismiss()
                } else {
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    override fun request(flag: Int) {
        executeRequest("leaderboard/lines/optimal?type=$operatorType&toUserId=$uid") {
            it?.let {
                it as WordingData
                it.data?.let { list ->
                    mData.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(event: AlarmUpdateEvent) {
        if (event.type == 1 && event.updateResource == 1) {//发布了新的配音
            if (!TextUtils.isEmpty(event.dubbingId)) {
                mData.loop {
                    it.id == event.dubbingId
                }?.let {
                    try {
                        it.dubbing_num = ((it.dubbing_num.toInt()) + 1).toString()
                        it.is_dubbed = 1
                        adapter.notifyItemChanged(mData.indexOf(it))
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}