package org.xiaoxingqi.shengxi.modules.echoes

import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.activity_system_info.*
import org.xiaoxingqi.shengxi.model.SystemInfoData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.MsgTextView
import skin.support.SkinCompatManager

class SystemInfoActivity : BaseAct() {

    private lateinit var adapter: QuickAdapter<SystemInfoData.SystemInfoBean>
    private val mData by lazy {
        ArrayList<SystemInfoData.SystemInfoBean>()
    }
    private var infoData: UserInfoData? = null

    private var lastId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_system_info
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<SystemInfoData.SystemInfoBean>(this, R.layout.item_system_info, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: SystemInfoData.SystemInfoBean?) {
                val tvContent = helper?.getView(R.id.tv_Content) as MsgTextView
                tvContent.setTextColor(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) Color.parseColor("#333333") else Color.parseColor("#72727f"))
                tvContent.setTextData(item?.content)
                helper.getTextView(R.id.tv_Title).text = item?.title
                helper.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserFriends(this@SystemInfoActivity, item!!.created_at)
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_little_height_grey, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_Custom.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).putExtra("uid", "1"))
        }
        adapter.setOnLoadListener {
            lastId = mData[mData.size - 1].id.toString()
            request(0)
        }
        iv_Custom.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java).putExtra("uid", "1"))
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "messages/${infoData?.data?.user_id}/1?lastId=$lastId", SystemInfoData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as SystemInfoData
                if (result.data != null) {
                    mData.addAll(result.data)
                    adapter.notifyDataSetChanged()
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showOffline()
            }
        })

    }
}