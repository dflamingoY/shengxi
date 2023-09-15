package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_white_recycler.*
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogAlbumHowOperator
import org.xiaoxingqi.shengxi.dialog.DialogDeleteConment
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.EchoeData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.TimeUtils

//用户被别人涂鸦的列表
class GraffitiListActivity : BaseAct() {
    private val mData by lazy { ArrayList<EchoeData.EchoesBean>() }
    private lateinit var adapter: QuickAdapter<EchoeData.EchoesBean>
    private var resourceId = 0
    private var lastId: String = ""

    companion object {
        var instances: GraffitiListActivity? = null
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_white_recycler
    }

    override fun initView() {
        instances = this
        tv_Title.text = "涂鸦对话"
    }

    override fun initData() {
        resourceId = intent.getIntExtra("resourceId", 0)
        adapter = object : QuickAdapter<EchoeData.EchoesBean>(this, R.layout.item_talk_graffiti_list, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: EchoeData.EchoesBean?) {
                helper!!.getTextView(R.id.tvUserName)!!.text = item!!.with_user_name
                glideUtil.loadGlide(item.with_user_avatar_url, helper.getImageView(R.id.roundCircle), R.mipmap.icon_user_default, glideUtil.getLastModified(item.with_user_avatar_url))
                helper.getTextView(R.id.tv_Time)?.text = TimeUtils.getInstance().paserFriends(this@GraffitiListActivity, item.updated_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.with_user_identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.with_user_identity_type == 1
                helper.getView(R.id.tv_Delete).setOnClickListener {
                    DialogDeleteConment(this@GraffitiListActivity).setOnClickListener(View.OnClickListener {
                        deleteComments(item)
                    }).show()
                }
                helper.getTextView(R.id.tv_ChatType).text = "${item.dialog_num}张涂鸦"
                helper.getView(R.id.tv_Report).setOnClickListener {
                    DialogAlbumHowOperator(this@GraffitiListActivity).setTitle(resources.getString(R.string.string_27)).show()
                }
                helper.getView(R.id.roundCircle).setOnClickListener {
                    startActivity(Intent(this@GraffitiListActivity, UserDetailsActivity::class.java).putExtra("id", item.with_user_id.toString()))
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.echoes_loadmore, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            TalkGraffitiDetailsActivity.open(this, Intent(this, TalkGraffitiDetailsActivity::class.java)
                    .putExtra("resourceId", mData[position].resource_id)
                    .putExtra("chatId", mData[position].chat_id.toString())
                    .putExtra("uid", mData[position].with_user_id.toString())
            )
        }
        adapter.setOnLoadListener {
            request(0)
        }
    }

    private fun deleteComments(item: EchoeData.EchoesBean) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "chats/${item.chat_id}", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                    recyclerView.smoothCloseMenu()
                    if (mData.size == 0) {
                        finish()
                    }
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.1")
    }


    override fun request(flag: Int) {
        OkClientHelper.get(this, "artworks/${resourceId}/chats?lastId=$lastId", EchoeData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as EchoeData
                result.data?.let {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                    }
                    mData.addAll(it)
                    adapter.notifyDataSetChanged()
                    if (it.size >= 10) {
                        lastId = it[it.size - 1].chat_id.toString()
                    }
                }
            }
        }, "V4.3")
    }

    override fun onDestroy() {
        super.onDestroy()
        instances = null
    }
}