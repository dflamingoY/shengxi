package org.xiaoxingqi.shengxi.modules.user

import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_photo.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.model.BaseImg
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseDecorationAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.AblumeData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.utils.*
import java.lang.Deprecated
import java.text.ParseException

@Deprecated
class PhotoActivity : BaseAct() {
    private var id: String = ""
    private lateinit var adapter: BaseDecorationAdapter<BaseImg, BaseAdapterHelper>
    private val mData by lazy {
        ArrayList<BaseImg>()
    }
    private val tempData by lazy {
        ArrayList<String>()
    }
    private var current = 0
    private var userinfo: UserInfoData? = null
    private var lastId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_photo
    }

    override fun initView() {
        tv_Title.text = resources.getString(R.string.string_album)
        recyclerView.setPadding(AppTools.dp2px(this, 11), 0, AppTools.dp2px(this, 11), 0)
    }

    override fun initData() {
        userinfo = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        intent.getStringExtra("id")?.let {
            id = it
        }
        adapter = object : BaseDecorationAdapter<BaseImg, BaseAdapterHelper>(this, R.layout.item_item_title_text, R.layout.item_photo_img, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseImg?) {
                if (item!!.isSelect) {
                    try {
                        helper!!.getTextView(R.id.tv_Title).text = TimeUtils.getInstance().niumsDays(this@PhotoActivity, item.title)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                        helper!!.getTextView(R.id.tv_Title).text = item.title
                    }

                } else {
                    if (item.url.endsWith("gif")) {
                        helper!!.getView(R.id.iv_Gif).visibility = View.VISIBLE
                    } else {
                        helper!!.getView(R.id.iv_Gif).visibility = View.GONE
                    }
                    ImageLoader.getInstance().displayImage(item.url, helper.getImageView(R.id.iv_img), AppTools.options)
                }
            }
        }
        val manager = GridLayoutManager(this, 3)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.isHorzontal(position)) manager.spanCount else 1
            }
        }
        userinfo?.let {
            if (it.data.user_id != id) {
                findViewById<TextView>(R.id.tv_Photo_Hint).text = resources.getString(R.string.string_photoAct_1)
                findViewById<View>(R.id.tv_UploadPhoto).visibility = View.GONE
            }
        }
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, manager, LayoutInflater.from(this).inflate(R.layout.view_loadmore, recyclerView, false))
        request(current)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(0)
        }
        adapter.setOnItemClickListener { view, position ->
            DataHelper.getInstance().addData(tempData)
            startActivity(Intent(this, ShowPicActivity::class.java)
                    .putExtra("index", tempData.indexOf(mData[position].url))
                    .putExtra("bigData", true)
                    .putExtra("isVoice", false)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            overridePendingTransition(0, 0)
        }

        transLayout.findViewById<View>(R.id.tv_UploadPhoto).setOnClickListener {
            startActivity(Intent(this, SendAct::class.java).putExtra("type", 1))
            overridePendingTransition(R.anim.operate_enter, 0)
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "users/$id/photos?lastId=$lastId", AblumeData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                (result as AblumeData)
                if (result.data != null) {
                    for (bean in result.data) {
                        mData.add(BaseImg(true, bean.time))
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                        for (item in bean.img_list) {
                            mData.add(BaseImg(item))
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                            tempData.add(item)
                        }
                        lastId = bean.time
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                } else {
                    transLayout.showContent()
                }
                if (result.data != null && result.data.size > 0) {
                    adapter.setLoadStatue(BaseDecorationAdapter.ELoadState.READY)
                } else {
                    adapter.setLoadStatue(BaseDecorationAdapter.ELoadState.EMPTY)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showEmpty()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSendEvent(event: SendPicEvent) {
        lastId = null
        request(0)
    }

    class SendPicEvent

}