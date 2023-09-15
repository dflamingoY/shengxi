package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewAnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.frag_listen.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogFindPermission
import org.xiaoxingqi.shengxi.impl.FragSkinUpdateTheme
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.UpdateSendVoice
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.home.WhoShakeActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.AlarmListActivity
import org.xiaoxingqi.shengxi.modules.listen.book.BookActivity
import org.xiaoxingqi.shengxi.modules.listen.cheers.CheersActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.MovieActivity
import org.xiaoxingqi.shengxi.modules.listen.music.MusicActivity
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.SoulCanvasDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicSearchActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import skin.support.SkinCompatManager

class ListenFragment : BaseFragment(), ITabClickCall {
    private var lastId: String? = null

    private val mData by lazy {
        ArrayList<ListenData.ListenBean>()
    }

    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    private lateinit var recyclerView: RecyclerView

    //    private lateinit var heardView: View
    private lateinit var linearSearchTopic: View
    private lateinit var adapter: QuickAdapter<ListenData.ListenBean>
    private lateinit var transLayout: TransLayout

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun getLayoutId(): Int {
        return R.layout.frag_listen
    }

    override fun initView(view: View?) {
        val params = view!!.view_status_bar_place.layoutParams
        params.height = AppTools.getStatusBarHeight(activity)
        view.view_status_bar_place.layoutParams = params
        updateTheme()
        recyclerView = view.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        linearSearchTopic = view.linearSearchTopic
        transLayout = view.transLayout
        swipeRefreshLayout = view.swipeRefresh
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators), ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
    }

    override fun initData() {
        parseCache()
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<ListenData.ListenBean>(activity, R.layout.item_listen_img, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: ListenData.ListenBean?) {
                Glide.with(activity!!)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop()
                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .signature(ObjectKey(item!!.banner_url))
                        )
                        .load(item.banner_url)
                        .into(helper!!.getImageView(R.id.img))
            }
        }
        recyclerView.adapter = adapter
        request(3)
        request(0)
    }

    /**
     * 先解析本地缓存
     */
    private fun parseCache() {
        val obj = PreferenceTools.getObj(activity, IConstant.LISTENARTICLECACHE, ListenData::class.java)
        if (obj != null) {
            if (obj.data != null) {
                mData.addAll(obj.data)
            }
        }
    }

    override fun initEvent() {
        mView!!.app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, p1 -> swipeRefreshLayout.isEnabled = p1 >= 0 })
        linearSearchTopic.setOnClickListener {
            startActivity(Intent(activity, TopicSearchActivity::class.java)
                    .putExtra("tag", mView!!.tv_HotTitle.text.toString().replace("#", ""))
            )
        }
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, ActionActivity::class.java)
                    .putExtra("title", mData[position].html_title)
                    .putExtra("url", mData[position].html_id.toString())
                    .putExtra("isHtml", true)
            )
        }
        mView!!.relativeJike.setOnClickListener {
            mView!!.frameContainer.visibility = View.GONE
            startActivity(Intent(activity, CheersActivity::class.java))
        }
        mView!!.relativeMovies.setOnClickListener { startActivity(Intent(activity, MovieActivity::class.java)) }
        mView!!.relativeBang.setOnClickListener {
            startActivity(Intent(activity, AlarmListActivity::class.java))
        }
        swipeRefreshLayout.setOnRefreshListener {
            lastId = null
            request(0)
            request(3)
        }
        mView!!.relativeTest.setOnClickListener {
            startActivity(Intent(activity, CharacterTestActivity::class.java))
        }
        //语音
        mView!!.relative_Phone.setOnClickListener {
            mView!!.frameContainer.visibility = View.GONE
            startActivity(Intent(activity, VoiceConnectActivity::class.java))
        }//魔法画板
        mView!!.relative_Canvas.setOnClickListener {
//            startActivity(Intent(activity, FindFriendsActivity::class.java))
            if (it.isSelected) {
                startActivity(Intent(activity, FindFriendsActivity::class.java))
            } else {
                DialogFindPermission(activity!!).show()
            }
        }
        mView!!.relative_temp1.setOnClickListener {
            startActivity(Intent(activity, BookActivity::class.java))
        }
        mView!!.relative_Music.setOnClickListener {
            startActivity(Intent(activity, MusicActivity::class.java))
        }
        mView!!.relative_canvas_local.setOnClickListener {
            startActivity(Intent(activity, SoulCanvasDetailsActivity::class.java))
        }
        mView!!.frameContainer.setOnClickListener {
            mView!!.frameContainer.visibility = View.GONE
        }
        mView!!.relative_More.setOnClickListener {
            mView!!.frameContainer.visibility = View.VISIBLE
            val local = IntArray(2)
            mView!!.relative_More.getLocationInWindow(local)
            //平移
            ObjectAnimator.ofFloat(mView!!.frameMoreMenu, "translationX", local[0].toFloat() + AppTools.dp2pxFloat(activity, 41) / 2f - transLayout.width / 2f, 0f).setDuration(400).start()
            ObjectAnimator.ofFloat(mView!!.frameMoreMenu, "translationY", local[1].toFloat() - AppTools.dp2pxFloat(activity, 41) - transLayout.height / 2f, 0f).setDuration(400).start()
            ObjectAnimator.ofFloat(mView!!.frameMoreMenu, "rotation", 0f, 360f).setDuration(400).start()
            val reveal = ViewAnimationUtils.createCircularReveal(mView!!.frameMoreMenu, AppTools.dp2px(activity, 110), AppTools.dp2px(activity, 110), AppTools.dp2pxFloat(activity, 20), AppTools.dp2pxFloat(activity, 110))
            reveal.duration = 400
            reveal.start()
        }
        mView!!.relativeShack.setOnClickListener {
            mView!!.frameContainer.visibility = View.GONE
            startActivity<WhoShakeActivity>()
        }
        mView!!.relativeMagicCanvas.setOnClickListener {
            mView!!.frameContainer.visibility = View.GONE
            startActivity<MagicCanvasActivity>()
        }
        mView!!.ivCustom.setOnClickListener {
            startActivity(Intent(activity, ChatActivity::class.java)
                    .putExtra("uid", "1")
                    .putExtra("userName", "声昔小二")
                    .putExtra("unreadCount", 0)
                    .putExtra("chatId", "")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        //判断用户的总时长是否超过30m
        try {
            val userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
            if (userInfo.data.voice_total_len >= 30 * 60) {
                mView!!.iv_canvas.isSelected = true
                mView!!.tvFindTitle.text = "找寻"
                mView!!.relative_Canvas.isSelected = true
            } else {
                mView!!.relative_Canvas.isSelected = false
                mView!!.tvFindTitle.text = "???"
                mView!!.iv_canvas.isSelected = false
            }
        } catch (e: Exception) {
        }
//        request(4)
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "h5/type?htmlType=4?lastId=$lastId", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null) {
                                PreferenceTools.saveObj(activity, IConstant.LISTENARTICLECACHE, result)//保存本地缓存
                                mData.clear()
                                mData.addAll(result.data)
                                adapter.notifyDataSetChanged()
                            }
                        }
                        swipeRefreshLayout.isRefreshing = false
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
            }
            1 -> {
                transLayout.showProgress()
                OkClientHelper.get(activity, "resources?resourceType=1&platformId=3", ResouceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ResouceData
                        if (result.code == 0 && result.data != null) {
                            friendBangUrl = result.data.resource_content
                            request(2)
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            2 -> {
                OkClientHelper.get(activity, "leaderboard/users", ListenFriendData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenFriendData
                        /*startActivity(Intent(activity, WebViewActivity::class.java).putExtra("title", "好友榜")
                                .putExtra("url", "$friendBangUrl?data=${URLEncoder.encode(AppTools.encode64(result.data))}")
                        )*/
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            3 -> {
                OkClientHelper.get(activity, "topics/hot/1", SearchTopicData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        if ((result as SearchTopicData).data != null && result.data.size > 0) {
                            mView!!.tv_HotTitle.text = "#${result.data[0].topic_name}#"
//                            mView!!.iv_Search.visibility = View.GONE
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            4 -> {
                OkClientHelper.delete(activity, "personality/match/1", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
                OkClientHelper.delete(activity, "personality/match/2", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")

            }
            5 -> {//查询用户的总时长

            }
        }
    }

    private var friendBangUrl: String? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSkinEvent(event: FragSkinUpdateTheme) {
        updateTheme()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(update: UpdateSendVoice) {
        if (update.type == 2) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateTheme() {
        /**
         * 如果 <6.0   夜间模式View 的背景色为黑
         *              白天模式 View 的背景色为0xffcccccc
         *     >=6.0  夜间模式为黑色  白天模式为白色
         */

        if (mView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.WHITE)
                } else {//夜间模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#181828"))
                }
            } else {
                if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#cccccc"))
                } else {//夜间模式
                    mView!!.view_status_bar_place.setBackgroundColor(Color.parseColor("#181828"))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}