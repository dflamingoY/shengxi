package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import kotlinx.android.synthetic.main.activity_select_freinds.*
import kotlinx.android.synthetic.main.activity_select_freinds.btn_Back
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.BaseUserBean
import org.xiaoxingqi.shengxi.model.SearchUserData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.*
import kotlin.math.ceil

class SelectFriendActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<BaseUserBean>
    private var mData = ArrayList<BaseUserBean>()
    private var lastId: String? = null
    private lateinit var artUrl: String
    private lateinit var resourceId: String
    override fun getLayoutId(): Int {
        return R.layout.activity_select_freinds
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        artUrl = intent.getStringExtra("artworkUrl")
        resourceId = intent.getStringExtra("resourceId")
        adapter = object : QuickAdapter<BaseUserBean>(this, R.layout.item_friend_list, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseUserBean?) {
                /* glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                 helper.getTextView(R.id.tv_UserName).text = item.nick_name
                 helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                 helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1*/
                glideUtil.loadGlide(item?.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item?.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item?.nick_name
                helper.getView(R.id.linearAgain).visibility = View.GONE
                helper.getTextView(R.id.tv_CountDown).text = Html.fromHtml(String.format(resources.getString(R.string.string_auto_unbind_friend), ceil((item?.release_at!! - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt()))
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
                helper.getView(R.id.linearAgain).isSelected = item.renewed_at != 0
                helper.getTextView(R.id.tvAgain).text = resources.getString(R.string.string_again_friend_relation)
                helper.getView(R.id.tvLianXu).visibility = if (item.renew_months > 1) View.VISIBLE else View.GONE
                helper.getTextView(R.id.tvLianXu).text = "连续${item.renew_months}个月是好友"
                helper.getView(R.id.tvSend).visibility = View.VISIBLE
            }
        }
        recyclerView.adapter = adapter
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            //直接转发给好友
            send(mData[position].user_id, mData[position].nick_name)
        }
    }

    private fun send(id: String, name: String) {
        OkClientHelper.post(this, "artworkGifts", FormBody.Builder()
                .add("toUserId", id)
                .add("artworkId", resourceId).build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast(resources.getString(R.string.string_28) + name)
                    finish()
                } else
                    showToast(result.msg)
            }
        }, "V4.3")
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}/friends?lastId=$lastId", SearchUserData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SearchUserData
                result.data?.let {
                    mData.addAll(it)
                    adapter.notifyDataSetChanged()
                }
                result.data?.let {
                    if (it.size >= 10) {
                        lastId = it[it.size - 1].id
                        request(0)
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }
}