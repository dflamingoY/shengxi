package org.xiaoxingqi.shengxi.modules.user.frag

import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_user_achieve.*
import kotlinx.android.synthetic.main.head_achieve.view.*
import kotlinx.android.synthetic.main.item_achieve_user.view.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import kotlin.math.floor

class UserAchieveActivity : BaseNormalActivity() {
    private lateinit var headView: View
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private val mData by lazy { ArrayList<BaseUserBean>() }
    private lateinit var user: UserInfoData
    private var pageNo = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_user_achieve
    }

    override fun initView() {
        val params = viewStatus.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        viewStatus.layoutParams = params
        recyclerView.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.head_achieve, recyclerView, false)
        headView.tvAchieveDate.text = String.format(resources.getString(R.string.string_achieve_continu_days), "0")
    }

    override fun initData() {
        adapter = object : QuickAdapter<BaseUserBean>(this, R.layout.item_achieve_user, mData, headView) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                helper!!.getTextView(R.id.tvName).text = item!!.nick_name
                helper.getTextView(R.id.tvAchieveDate).text = String.format(resources.getString(R.string.string_achieve_continu_days), item.total.toString())
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.ivAvatar), 0, glideUtil.getLastModified(item.avatar_url))
                helper.getView(R.id.ivAvatar).setOnClickListener {
                    startActivity<UserDetailsActivity>("id" to item.user_id)
                }
            }
        }
        recyclerView.adapter = adapter
        user = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        glideUtil.loadGlide(user.data.avatar_url, headView.ivAvatar, 0, glideUtil.getLastModified(user.data.avatar_url))
        headView.tvName.text = user.data.nick_name
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(1)
        request(2)
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(1)
        }
        tvUserRecord.setOnClickListener {
            startActivity<SendAct>("type" to 1)
            overridePendingTransition(R.anim.operate_enter, 0)
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "${user.data.user_id}/achievement?achievementType=1", AchieveData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AchieveData
                        result.data?.let {
                            if (it.isNotEmpty()) {
                                if (it[0].achievement_type == 1) {
                                    val today = (System.currentTimeMillis() / 1000).toInt()
                                    if (it[0].achievement_type == 1) {//心情
                                        if (floor((today.toDouble() - TimeUtils.achieveS2Int(it[0].latest_at)) / (60 * 60 * 24)).toInt() <= 1) {//表示今天已达成,否则未达成
                                            val recordDate = SPUtils.getString(this@UserAchieveActivity, IConstant.FIRST_PUSH_VOICES + user.data.user_id, "")
                                            val today = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
                                            if (TextUtils.isEmpty(recordDate)) {
                                                headView.tvAchieveDate.text = String.format(resources.getString(R.string.string_achieve_continu_days),
                                                        "${(TimeUtils.achieveS2Int(it[0].latest_at) - TimeUtils.achieveS2Int(it[0].started_at)) / (60 * 60 * 24)}"
                                                )
                                            } else
                                                headView.tvAchieveDate.text = String.format(resources.getString(R.string.string_achieve_continu_days),
                                                        "${(TimeUtils.achieveS2Int(it[0].latest_at) - TimeUtils.achieveS2Int(it[0].started_at)) / (60 * 60 * 24) + 1}"
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(this, "friends/achievement?pageNo=$pageNo", SearchUserData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SearchUserData
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result.data?.let {
                            mData.addAll(it)
                            adapter.notifyDataSetChanged()
                            if (it.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                pageNo++
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            2 -> {
                OkClientHelper.get(this, "users/${user.data.user_id}/settings?settingName=achievement_visibility&settingTag=other", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        result.data?.let { list ->
                            if (list.setting_name == "achievement_visibility") {
                                if (list.setting_value != 1) {
                                    headView.tvAchieveVisible.visibility = View.VISIBLE
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
        }
    }
}