package org.xiaoxingqi.shengxi.modules.user.black

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.frag_recycler.view.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.WhiteListData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import kotlin.math.ceil

/**
 * 白名單
 */
class WhiteListFragment : BaseFragment() {
    private val mData by lazy { ArrayList<WhiteListData.WhiteListBean>() }
    private lateinit var adapter: QuickAdapter<WhiteListData.WhiteListBean>
    private var lastId: String = ""
    private lateinit var headView: View

    override fun getLayoutId(): Int {
        return R.layout.frag_recycler
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        headView = LayoutInflater.from(activity).inflate(R.layout.head_black_list, view.recyclerView, false)
        headView.findViewById<TextView>(R.id.tv_desc).text = resources.getString(R.string.string_black_white_desc)
    }

    override fun initData() {
        adapter = object : QuickAdapter<WhiteListData.WhiteListBean>(activity, R.layout.item_friend_list, mData, headView) {
            override fun convert(helper: BaseAdapterHelper?, item: WhiteListData.WhiteListBean?) {
                glideUtil.loadGlide(item!!.avatar_url, helper!!.getImageView(R.id.iv_img), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.nick_name
                helper.getTextView(R.id.tv_CountDown).text = Html.fromHtml(String.format(resources.getString(R.string.string_auto_unbind_white_list), ceil((item.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt()))
                helper.getTextView(R.id.tv_Remark).text = resources.getString(R.string.string_delete_white_list)
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
                helper.getTextView(R.id.tv_Remark).setOnClickListener {
                    removeWhiteList(item)
                }
            }
        }
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        mView!!.recyclerView.adapter = adapter
        request(0)
    }

    override fun initEvent() {
        adapter.setOnLoadListener {
            request(1)
        }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", mData[position].user_id))
        }
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "users/${loginBean.user_id}/whitelist?lastId=$lastId", WhiteListData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as WhiteListData
                if (result.code == 0) {
                    if (result.data != null) {
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    }
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                    lastId = mData[mData.size - 1].id.toString()
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    /**
     * 移除白名单
     */
    private fun removeWhiteList(item: WhiteListData.WhiteListBean) {
        mView!!.transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(activity, "users/${loginBean.user_id}/whitelist/${item.to_user_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V3.6")
    }
}