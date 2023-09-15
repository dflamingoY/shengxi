package org.xiaoxingqi.shengxi.modules.listen

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_select_friend.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class SelectedFriendActivity : BaseAct() {
    private var lastId = ""
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private var mData = ArrayList<BaseUserBean>()
    private var selectedId = "0"
    private var selectedBean: BaseUserBean? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_select_friend
    }

    override fun initView() {

    }

    override fun initData() {
        selectedId = intent.getStringExtra("id")
        adapter = object : QuickAdapter<BaseUserBean>(this, R.layout.item_friend_list, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                glideUtil.loadGlide(item?.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item?.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item?.nick_name
                helper.getTextView(R.id.tv_CountDown).text = Html.fromHtml(String.format(resources.getString(R.string.string_auto_unbind_friend), Math.ceil((item?.release_at!! - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt()))
                helper.getView(R.id.linearAgain).visibility = View.GONE
                helper.getView(R.id.tv_Remark).visibility = View.GONE
                helper.getView(R.id.checkbox).visibility = View.VISIBLE
                helper.getView(R.id.checkbox).isSelected = item.isSelected
                helper.getView(R.id.tvAgain).visibility = View.GONE
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        recyclerView.adapter = adapter
        request(0)
        request(1)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, if ("0" == selectedId) null else {
                Intent().putExtra("data", selectedBean)
            })
            finish()
        }
        adapter.setOnLoadListener {
            lastId = mData[mData.size - 1].id
            request(0)
        }
        adapter.setOnItemClickListener { _, position ->
            setResult(Activity.RESULT_OK, Intent().putExtra("data", mData[position]))
            finish()
        }
        tv_Explain_Friends.setOnClickListener {
            selectedBean?.let {
                it.isSelected = false
                adapter.notifyDataSetChanged()
            }
            selectedId = "0"
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}/friends?lastId=$lastId", SearchUserData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as SearchUserData
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0 && result.data != null) {
                            for (bean in result.data) {
                                if (bean.user_id == selectedId) {
                                    bean.isSelected = true
                                    selectedBean = bean
                                }
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                                lastId = bean.id
                            }
                        }
                        result.data?.let {
                            if (it.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            1 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}/about", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            tv_FriendCount.text = String.format(resources.getString(R.string.string_friends_count), result.data.friend_num.toString())
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, if ("0" == selectedId) null else {
            Intent().putExtra("data", selectedBean)
        })
        super.onBackPressed()
    }
}