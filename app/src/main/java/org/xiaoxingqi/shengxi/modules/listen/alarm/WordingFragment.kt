package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.frag_alarm_wording.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

class WordingFragment : BaseFragment(), ITabAlarmClickCall {

    override fun tabSelected(position: Int) {

    }

    private var lastId: String = ""
    override fun tabClick(isVisible: Boolean) {
        try {
            if (mView!!.swipeRefresh.isEnabled != isVisible) {
                mView!!.swipeRefresh.isEnabled = isVisible
                mView!!.swipeRefresh.isRefreshing = false
            }
        } catch (e: Exception) {

        }
    }

    override fun doubleClickRefresh() {

    }

    private val mData by lazy { ArrayList<BaseAlarmBean>() }
    private lateinit var adapter: QuickAdapter<BaseAlarmBean>
    private lateinit var loginBean: LoginData.LoginBean
    private lateinit var userInfo: UserInfoData
    private var tagId: String = ""

    override fun getLayoutId(): Int {
        return R.layout.frag_alarm_wording
    }

    override fun initView(view: View?) {
        view!!.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
    }

    override fun initData() {
        userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<BaseAlarmBean>(context, R.layout.item_alarm_dub, mData) {
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
                helper.getView(R.id.ivOfficial).visibility = if (item.user.id == "1") View.VISIBLE else View.GONE
                try {
                    helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(activity, item.created_at.toInt())
                } catch (e: Exception) {
                }
                helper.getTextView(R.id.tv_dubbing_count).apply {
                    text = if (item.dubbing_num > "0") "${item.dubbing_num}" else resources.getString(R.string.string_alarm_wording_dubbing)
                    setOnClickListener {
                        if (item.dubbing_num > "0")
                            startActivity(Intent(activity, WordingVoiceActivity::class.java)
                                    .putExtra("isDubbed", item.is_dubbed)
                                    .putExtra("id", item.id)
                                    .putExtra("dubbingNum", item.dubbing_num)
                                    .putExtra("tagName", item.tag_name)
                                    .putExtra("userInfo", item.user)
                                    .putExtra("lineContent", item.line_content))
                        else {
                            startActivity(Intent(activity, RecordVoiceActivity::class.java)
                                    .putExtra("wording", item.line_content)
                                    .putExtra("wordingId", item.id)
                                    .putExtra("tagId", item.tag_name)
                                    .putExtra("resourceType", "22")
                                    .putExtra("recordType", 6))
                            activity!!.overridePendingTransition(0, 0)
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
                helper.getView(R.id.linear_angel).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "1")
                        deleteVote(item, helper.getView(R.id.iv_angel))
                    else
                        artVote(item, helper.getView(R.id.iv_angel), 1)
                }
                helper.getView(R.id.linear_monster).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "2")
                        deleteVote(item, helper.getView(R.id.iv_monster))
                    else
                        artVote(item, helper.getView(R.id.iv_monster), 2)
                }
                helper.getView(R.id.linear_god).setOnClickListener {
                    if (!TextUtils.isEmpty(item.vote_id) && item.vote_option == "3")
                        deleteVote(item, helper.getView(R.id.iv_god))
                    else
                        artVote(item, helper.getView(R.id.iv_god), 3)
                }
                helper.getView(R.id.relativeWord).setOnClickListener {
                    if (item.dubbing_num > "0") {
                        startActivity(Intent(activity, WordingVoiceActivity::class.java)
                                .putExtra("isDubbed", item.is_dubbed)
                                .putExtra("id", item.id)
                                .putExtra("dubbingNum", item.dubbing_num)
                                .putExtra("tagName", item.tag_name)
                                .putExtra("userInfo", item.user)
                                .putExtra("lineContent", item.line_content))
                    } else {
                        //录制配音
                        startActivity(Intent(activity, RecordVoiceActivity::class.java)
                                .putExtra("wording", item.line_content)
                                .putExtra("wordingId", item.id)
                                .putExtra("tagId", item.tag_name)
                                .putExtra("resourceType", "22")
                                .putExtra("recordType", 6))
                        activity!!.overridePendingTransition(0, 0)
                    }
                }
                helper.getImageView(R.id.roundImg).setOnClickListener {
                    if (item.is_anonymous != "1")
                        startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.user.id))
                }
                helper.getView(R.id.relative).setOnClickListener {
                    if (item.user_id == loginBean.user_id) {
                        //删除或者只为匿名
                        DialogAlarmEdit(activity!!).hideAnonymous(true).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete ->
                                    DialogDeleteWording(activity!!).setOnClickListener(View.OnClickListener {
                                        deleteWord(item)
                                    }).show()
                                R.id.tv_Self -> editWord(item)
                            }
                        }).show()
                    } else {
                        //判断是否是adminUser
                        if (IConstant.userAdminArray.contains(loginBean.user_id)) {
                            AdminReportAlarmDialog(activity!!).setType(1).setAnonymous(item.is_anonymous).setOnClickListener(View.OnClickListener {
                                when (it.id) {
                                    R.id.tv_Report -> {
                                        DialogNormalReport(activity!!).show { reportType ->
                                            reportNormalItem(item.id, reportType, 7)
                                        }
                                    }
                                    R.id.tv_deleteDubbing -> {
                                        dialogPwd = DialogCommitPwd(activity!!).setOperator("deleteWord", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminDelete(item, FormBody.Builder().add("confirmPasswd", pwd).build())
                                        })
                                        dialogPwd?.show()
                                    }
                                    R.id.tv_hide -> {//隐藏台词
                                        dialogPwd = DialogCommitPwd(activity!!).setOperator("deleteWord", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                            adminHide(item, FormBody.Builder().add("confirmPasswd", pwd).add("hideAt", "${System.currentTimeMillis()}").build())
                                        })
                                        dialogPwd?.show()
                                    }
                                }
                            }).show()
                        } else {
                            DialogNormalReport(activity!!).show {
                                reportNormalItem(item.id, it, 7)
                            }
                        }
                    }
                }
                helper.getView(R.id.linearWordDubbingStatus).setOnClickListener {
                    if (item.is_dubbed == 1) {//已配音
                        startActivity(Intent(activity, WordingVoiceActivity::class.java)
                                .putExtra("userInfo", item.user)
                                .putExtra("toUserId", item.to_user_id)
                                .putExtra("isDubbed", item.is_dubbed)
                                .putExtra("dubbingNum", item.dubbing_num)
                                .putExtra("tagName", item.tag_name)
                                .putExtra("id", item.id)
                                .putExtra("lineContent", "#${item.tag_name}#${item.line_content}"))
                    } else {//未配音
                        startActivity(Intent(activity, RecordVoiceActivity::class.java)
                                .putExtra("wording", item.line_content)
                                .putExtra("wordingId", item.id)
                                .putExtra("tagId", item.tag_name)
                                .putExtra("resourceType", "22")
                                .putExtra("recordType", 6))
                        activity!!.overridePendingTransition(0, 0)
                    }
                }
            }

            private fun changeSelected(helper: BaseAdapterHelper) {
                helper.getView(R.id.iv_angel).isSelected = false
                helper.getView(R.id.iv_monster).isSelected = false
                helper.getView(R.id.iv_god).isSelected = false
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
        adapter.setOnLoadListener {
            request(1)
        }
        mView!!.headItemClickView.setOnItemClick(object : OnAlarmItemClickListener {
            override fun itemClick(type: Int) {
                tagId = if (type == 0) "" else type.toString()
                lastId = ""
                request(0)
            }
        })
    }

    //超管删除台词
    private fun adminDelete(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.delete(activity, "admin/lines/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
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

    //超管删除台词
    private fun adminHide(bean: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.patch(activity, "admin/lines/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
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

    private fun artVote(bean: BaseAlarmBean, view: View, type: Int) {
        mView!!.transLayout.showProgress()
        OkClientHelper.post(activity, "linesVote", FormBody.Builder()
                .add("lineId", bean.id)
                .add("voteOption", "$type").build(), IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(bean.vote_id)) {
                        when (bean.vote_option) {
                            "1" -> bean.vote_option_one--
                            "2" -> bean.vote_option_two--
                            "3" -> bean.vote_option_three--
                        }
                    }
                    bean.vote_id = result.data.id.toString()
                    when (type) {
                        1 -> bean.vote_option_one += 1
                        2 -> bean.vote_option_two += 1
                        3 -> bean.vote_option_three += 1
                    }
                    bean.vote_option = type.toString()
                    SmallBang.attach2Window(activity).bang(view, 60f, null)
                    adapter.notifyItemChanged(mData.indexOf(bean))
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V4.1")
    }

    private fun deleteVote(bean: BaseAlarmBean, view: View) {
        mView!!.transLayout.showContent()
        OkClientHelper.delete(activity, "linesVote/${bean.vote_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(bean.vote_id)) {
                        when (bean.vote_option) {
                            "1" -> bean.vote_option_one--
                            "2" -> bean.vote_option_two--
                            "3" -> bean.vote_option_three--
                        }
                    }
                    bean.vote_id = null
                    SmallBang.attach2Window(activity).bang(view, 60f, null)
                    adapter.notifyItemChanged(mData.indexOf(bean))
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V4.1")
    }

    private fun deleteWord(bean: BaseAlarmBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "lines/${bean.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    val position = mData.indexOf(bean)
                    mData.remove(bean)
                    adapter.notifyItemRemoved(position)
                } else
                    showToast(result.msg)
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V4.1")
    }

    private fun editWord(bean: BaseAlarmBean) {
        mView!!.transLayout.showProgress()
        OkClientHelper.patch(activity, "lines/${bean.id}", FormBody.Builder().add("isAnonymous", if (bean.is_anonymous == "1") "0" else "1").build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.is_anonymous = if (bean.is_anonymous == "1") "0" else "1"
                    adapter.notifyItemChanged(mData.indexOf(bean))
                } else showToast(result.msg)
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V4.1")
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "lines?lastId=$lastId&tagId=$tagId", WordingData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                mView!!.swipeRefresh.isRefreshing = false
                result as WordingData
                if (result.code == 0) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        try {
                            result.data?.forEach {
                                mData.add(it.checkWord(userInfo))
                            }
                        } catch (e: Exception) {
                        }
                        adapter.notifyDataSetChanged()
                        mView!!.recyclerView.scrollToPosition(0)
                    } else {
                        result.data?.forEach {
                            mData.add(it.checkWord(userInfo))
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    if (result.data != null && result.data.size >= 10) {
                        lastId = mData[mData.size - 1].id
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
            }
        }, "V4.1")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(event: AlarmUpdateEvent) {//发布了台词
        if (event.type == 2 && event.updateResource == 2) {
            (activity as AlarmListActivity).changeCurrentPage(2)
            lastId = ""
            request(0)
        } else if (event.type == 1 && event.updateResource == 1) {//发布了新的配音
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
        } else if (event.type == 4) {//删除了配音文件
            if (!TextUtils.isEmpty(event.deletePath)) {
                mData.loop {
                    it.id == event.deletePath
                }?.let {
                    it.is_dubbed = 0
                    try {
                        it.dubbing_num = ((it.dubbing_num.toInt()) - 1).toString()
                        adapter.notifyItemChanged(mData.indexOf(it))
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}