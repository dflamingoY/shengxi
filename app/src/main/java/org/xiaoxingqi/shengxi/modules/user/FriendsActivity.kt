package org.xiaoxingqi.shengxi.modules.user

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_recycler.*
import kotlinx.android.synthetic.main.view_empty_friends.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.IDeleteFriends
import org.xiaoxingqi.shengxi.impl.INotifyFriendStatus
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.friendsRenew
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.listen.ShackActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class FriendsActivity : BaseAct() {
    val REQUEST_REMARK = 0x00
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private var mData = ArrayList<BaseUserBean>()
    private var id: String = ""
    private var userbean: BaseUserBean? = null
    private var lastId: String? = null
    private var friendsCount = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_recycler
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
//        relative_Custom.visibility = View.VISIBLE
    }

    override fun initData() {
        intent.getStringExtra("id")?.let {
            id = it
        }
        tv_friends_hint_1.isSelected = AppTools.getLanguage(this) == IConstant.HK || AppTools.getLanguage(this) == IConstant.TW
        adapter = object : QuickAdapter<BaseUserBean>(this, R.layout.item_friend_list, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                glideUtil.loadGlide(item?.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item?.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item?.nick_name
                helper.getTextView(R.id.tv_CountDown).text = Html.fromHtml(String.format(resources.getString(R.string.string_auto_unbind_friend), Math.ceil((item?.release_at!! - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt()))
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
                helper.getView(R.id.linearAgain).isSelected = item.renewed_at != 0
                helper.getTextView(R.id.tvAgain).text = resources.getString(R.string.string_again_friend_relation)
                helper.getView(R.id.tvLianXu).visibility = if (item.renew_months > 1) View.VISIBLE else View.GONE
                helper.getTextView(R.id.tvLianXu).text = "连续${item.renew_months}个月是好友"
                helper.getView(R.id.tv_Remark).setOnClickListener {
                    userbean = item
                    startActivityForResult(Intent(this@FriendsActivity, EditUserNameActivity::class.java).putExtra("name", item.nick_name), REQUEST_REMARK)
                }
                helper.getView(R.id.linearAgain).setOnClickListener {
                    //好友续期
                    friendRenew(item, helper)
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(0)
        query(1)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { view, position ->
            UserDetailsActivity.start(this, mData[position].avatar_url, mData[position].user_id.toString(), view.findViewById(R.id.iv_img))
        }
        btn_Back.setOnClickListener { finish() }
        tv_Nearby.setOnClickListener {
            startActivity(Intent(this, ShackActivity::class.java))
        }
        adapter.setOnLoadListener {
            request(1)
        }
        tv_Explain_Friends.setOnClickListener {
            query(0)
        }
        relative_Custom.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java)
                    .putExtra("uid", "1")
                    .putExtra("userName", "声昔小二")
                    .putExtra("unreadCount", 0)
                    .putExtra("chatId", "")
            )
        }
    }

    private fun friendRenew(item: BaseUserBean, helper: BaseAdapterHelper) {
        transLayout.showProgress()
        friendsRenew(item.id, if (item.renewed_at == 0) (System.currentTimeMillis() / 1000).toInt() else 0) {
            it?.let {
                if (it.code == 0) {
                    if (item.renewed_at == 0) {
                        item.renewed_at = (System.currentTimeMillis() / 1000).toInt()
                    } else {
                        item.renewed_at = 0
                    }
                    helper.getView(R.id.linearAgain).isSelected = item.renewed_at != 0
                    helper.getTextView(R.id.tvAgain).text = resources.getString(R.string.string_again_friend_relation)
                } else {
                    showToast(it.msg)
                }
            }
            transLayout.showContent()
        }
    }

    private fun query(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "h5/html/declare/1", ArgumentData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ArgumentData
                        if (result.code == 0 && result.data != null) {
                            startActivity(Intent(this@FriendsActivity, ActionActivity::class.java)
                                    .putExtra("url", result.data.html_title)
                                    .putExtra("isHtml", true)
                                    .putExtra("url", result.data.html_id)
                            )
                        } else {
                            showToast(result.msg)
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
                            friendsCount = result.data.friend_num
                            if (result.data.friend_num == 0) {
                                tv_user_visible.text = resources.getString(R.string.string_friends_remove_30)
                            } else {
                                tv_FriendCount.text = String.format(resources.getString(R.string.string_friends_count), result.data.friend_num.toString())
                                tv_user_visible.text = resources.getString(R.string.string_empty_voice_list_14)
                            }
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

    override fun request(flag: Int) {
//        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}/friends?lastId=$lastId", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                if (TextUtils.isEmpty(lastId)) {
                    mData.clear()
                    adapter.notifyDataSetChanged()
                }
                if (result.code == 0 && result.data != null) {
                    for (bean in result.data) {
                        mData.add(bean)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                        lastId = bean.id
                    }
                }
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result.data?.let {
                    if (it.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                    tv_user_visible.text = resources.getString(R.string.string_friends_remove_30)
                } else {
                    relativeFriendsInfo.visibility = View.VISIBLE
                }
            }

            override fun onFailure(any: Any?) {
                if (AppTools.isNetOk(this@FriendsActivity)) {

                } else {
                    transLayout.showOffline()
                    if (mData.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }
        })
    }

    private fun updateRemark(remark: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("nickName", remark)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${loginBean.user_id}/friends/${userbean?.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    userbean?.let {
                        it.nick_name = remark
                        adapter.notifyItemChanged(mData.indexOf(it))
                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_REMARK) {
                data?.let {
                    val name = data.getStringExtra("name")
                    updateRemark(name)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun deleteEvent(event: IDeleteFriends) {
        if (mData.size > 0) {
            var tempBean: BaseUserBean? = null
            for (bean in mData) {
                if (bean.user_id == event.userid) {
                    tempBean = bean
                    break
                }
            }
            tempBean?.let {
                mData.remove(tempBean)
                adapter.notifyDataSetChanged()
                friendsCount--
                tv_FriendCount.text = String.format(resources.getString(R.string.string_friends_count), friendsCount.toString())
                if (mData.size == 0) {
                    transLayout.showEmpty()
                    tv_FriendCount.text = resources.getString(R.string.string_friends_remove_30)
                }
            }
        }
    }

    /**
     * 更新好友请求的状态,
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 2) {
            lastId = null
            request(0)
        }
    }


}