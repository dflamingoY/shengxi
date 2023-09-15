package org.xiaoxingqi.shengxi.modules.listen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.zyp.cardview.YcCardView
import kotlinx.android.synthetic.main.activity_friend_bang_list.*
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
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogChangeFriendListBg
import org.xiaoxingqi.shengxi.dialog.DialogCreateVoice
import org.xiaoxingqi.shengxi.dialog.DialogSongBang
import org.xiaoxingqi.shengxi.dialog.DialogUserSet
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.UpdateFriendInfoEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.ListenFriendData
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.CropFriendBgActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.RevelView
import skin.support.SkinCompatManager
import java.io.IOException

/**
 * 唱歌榜
 */
class FriendsBangListActivity : BaseAct() {
    private val colors = arrayListOf("#FF6399", "#FF98A5", "#FF8008", "#FFC031", "#F3CA50", "#FCD972", "#36B079", "#5ED591", "#1BC7CF", "#31E0EB", "#1FA2FF", "#14D2FB", "#9733EE", "#D324FD")
    private val REQUEST_PHOTO = 0x01
    private val REQUEST_CROP = 0x02
    private val REQUEST_WAVE = 0x03
    private lateinit var adapter: QuickAdapter<ListenFriendData.FriendsListBean>
    private val mData by lazy { ArrayList<ListenFriendData.FriendsListBean>() }
    private lateinit var audioPlayer: AudioPlayer
    private var playBean: ListenFriendData.FriendsListBean? = null
    private var isOn = false//是否上榜
    private var clickPosition = -1//点击跳转的角标
    private var currentPage = 1
    override fun writeHeadSet(): Boolean {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        return headsetOn || a2dpOn || scoOn
    }

    override fun changSpeakModel(type: Int) {
        if (type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clickPosition = -1
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_friend_bang_list
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStop() {
        super.onStop()
        audioPlayer.stop()
    }

    private lateinit var loginBean: LoginData.LoginBean
    override fun initData() {
        audioPlayer = AudioPlayer(this)
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            tv_friends_bang_hint_1.text = resources.getString(R.string.string_friendList_e_1)
        }
        adapter = object : QuickAdapter<ListenFriendData.FriendsListBean>(this, R.layout.item_friend_bang_details, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: ListenFriendData.FriendsListBean?) {
                val position = helper!!.itemView.tag as Int
                val ycCardView = helper.getView(R.id.ycCardView) as YcCardView
                if (TextUtils.isEmpty(item!!.friend_card_url)) {
                    val rem = position.rem(7)
                    val drawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor(colors[rem * 2]), Color.parseColor(colors[rem * 2 + 1])))
                    drawable.cornerRadius = AppTools.dp2px(this@FriendsBangListActivity, 12).toFloat()
                    helper.getImageView(R.id.iv_card_bg).setImageDrawable(drawable)
                    if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
                        ycCardView.setShadowColor(Color.parseColor(colors[rem * 2 + 1].replace("#", "#80")))
                    } else {
                        ycCardView.setShadowColor(Color.parseColor("#00ffffff"))
                    }
                    helper.getView(R.id.viewGreyLayer).visibility = View.GONE
                } else {
                    glideUtil.loadGlide(item.friend_card_url, helper.getImageView(R.id.iv_card_bg), 0, glideUtil.getLastModified(item.friend_card_url))
                    if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
                        ycCardView.setShadowColor(Color.parseColor("#999999"))
                    } else {
                        ycCardView.setShadowColor(Color.parseColor("#00ffffff"))
                    }
                    helper.getView(R.id.viewGreyLayer).visibility = View.VISIBLE
                }
                if (loginBean.user_id == item.id) {
                    helper.getView(R.id.iv_More).visibility = View.VISIBLE
                    helper.getView(R.id.linearStatusText).visibility = View.GONE
                } else {
                    helper.getView(R.id.iv_More).visibility = View.GONE
                    when (item.friend_status) {
                        "1" -> {
                            helper.getView(R.id.linearStatusText).visibility = View.VISIBLE
                            helper.getView(R.id.linearStatusText).isSelected = false
                        }
                        "2" -> helper.getView(R.id.linearStatusText).visibility = View.GONE
                        else -> {
                            helper.getView(R.id.linearStatusText).isSelected = true
                            helper.getView(R.id.linearStatusText).visibility = View.VISIBLE
                        }
                    }
                }
                if (position in 0..2) {
                    helper.getView(R.id.iv_top).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_top).setImageResource(if (position == 0) R.mipmap.icon_friend_top_1 else if (position == 1) R.mipmap.icon_friend_top_2 else R.mipmap.icon_friend_top_3)
                    helper.getTextView(R.id.tv_rank).visibility = View.GONE
                } else {
                    helper.getView(R.id.iv_top).visibility = View.GONE
                    helper.getTextView(R.id.tv_rank).visibility = View.VISIBLE
                    helper.getTextView(R.id.tv_rank).text = (position + 1).toString()
                }
                helper.getTextView(R.id.tv_Desc).text = if (TextUtils.isEmpty(item.self_intro)) resources.getString(R.string.string_empty_desc) else item.self_intro
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_Avatar), 0, glideUtil.getLastModified(item.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.nick_name
                helper.getTextView(R.id.tv_FriendsCount).text = String.format(resources.getString(R.string.string_friendslist_count), item.song_num.toString())
                helper.getView(R.id.linearClickPlay).setOnClickListener {
                    /**
                     * 播放
                     */
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        audioPlayer.stop()
                        if (item.isPlaying) {//当前正在播放
                            (helper.getView(R.id.revelView) as RevelView).stopView()
                            item.isPlaying = false
                            playBean = null
                            return@setOnClickListener
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                    }
                    download(helper, item)
                }
                helper.getView(R.id.linearStatusText).setOnClickListener {
                    /**
                     * 添加好友
                     */
                    if ("1" != item.friend_status)
                        addFriends(item, it)
                }
                helper.getView(R.id.iv_More).setOnClickListener {
                    DialogChangeFriendListBg(this@FriendsBangListActivity).setOnClickListener(View.OnClickListener {
                        /**
                         * 从相册中选取一张
                         */
                        startActivityForResult(Intent(this@FriendsBangListActivity, AlbumActivity::class.java)
                                .putExtra("isChat", true)
                                .putExtra("count", 1), REQUEST_PHOTO)
                    }).show()
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: ListenFriendData.FriendsListBean) {
                try {
                    if (TextUtils.isEmpty(item.wave_url)) {
                        /**
                         * 如果是自己 需要跳转录制封面独白
                         */
                        if (item.id == PreferenceTools.getObj(this@FriendsBangListActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id) {
                            startActivityForResult(Intent(this@FriendsBangListActivity, RecordVoiceActivity::class.java)
                                    .putExtra("resourceType", "1")
                                    .putExtra("recordType", 2), REQUEST_WAVE)
                            overridePendingTransition(0, 0)
                        } else
                            showToast(resources.getString(R.string.string_empty_voice_1))
                        return
                    }
                    OkClientHelper.downWave(this@FriendsBangListActivity, item.wave_url, { o ->
                        try {
                            if (null == o) {
                                showToast(resources.getString(R.string.string_error_file))
                                return@downWave
                            }
                            audioPlayer.setDataSource(o.toString())
                            audioPlayer.start(if (SPUtils.getBoolean(this@FriendsBangListActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }, { showToast(VolleyErrorHelper.getMessage(it)) })

                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onCompletion() {
                            item.isPlaying = false
                            (helper.getView(R.id.revelView) as RevelView).stopView()
                        }

                        override fun onInterrupt() {
                            item.isPlaying = false
                            (helper.getView(R.id.revelView) as RevelView).stopView()
                        }

                        override fun onPrepared() {
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            playBean = item
                            item.isPlaying = true
                            (helper.getView(R.id.revelView) as RevelView).startView()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white_grey, recyclerView, false))
        request(2)
    }

    override fun initEvent() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {//空闲的时候才开始加载
                    if (!isDestroyed) {
                        Glide.with(this@FriendsBangListActivity).resumeRequests()
                    }
                } else {
                    if (!isDestroyed)
                        Glide.with(this@FriendsBangListActivity).pauseRequests()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }
        })
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            clickPosition = position
            startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", mData[position].id))
        }
        /*adapter.setOnLoadListener {
            currentPage++
            request(1)
        }*/
        tv_CurrentStatus.setOnClickListener {
            /**
             * 滚动到指定的部位
             */
            if (!isToggleBang) {
                if (isOn) {
                    if (currentRank != -1) {
                        val scroller = TopSmoothScroller(this, currentRank)
                        scroller.targetPosition = currentRank
                        recyclerView.layoutManager?.startSmoothScroll(scroller)
                    } else {
                        showToast("正在努力加载")
                    }
                }
            } else {
                DialogSongBang(this).setOnCheck(isToggleBang).setOnCloseListener(object : DialogSongBang.OnCloseListener {
                    override fun dismiss(isCheck: Boolean) {
                        updateSet(isCheck)
                    }
                }).show()
            }
        }
        iv_set.setOnClickListener {
            DialogSongBang(this).setOnCheck(isToggleBang).setOnCloseListener(object : DialogSongBang.OnCloseListener {
                override fun dismiss(isCheck: Boolean) {
                    updateSet(isCheck)
                }
            }).show()
        }
    }

    private class TopSmoothScroller(context: Context?, index: Int) : LinearSmoothScroller(context) {
        private var rank: Int = index

        override fun getHorizontalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return if (rank in 0..99) {
                5f / displayMetrics?.densityDpi!!
            } else {
                5f / displayMetrics?.densityDpi!! / rank / 50
            }
        }
    }


    /**
     * 添加好友
     */
    private fun addFriends(item: ListenFriendData.FriendsListBean, view: View) {
        transLayout.showProgress()
        val infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        val formBody = FormBody.Builder()
                .add("toUserId", item.id)
                .build()
        OkClientHelper.post(this, "users/${infoData.data.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    item.friend_status = "1"
                    view.isSelected = false
                    if (SPUtils.getInt(this@FriendsBangListActivity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        DialogCreateVoice(this@FriendsBangListActivity).show()
                    } else if (SPUtils.getBoolean(this@FriendsBangListActivity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        DialogUserSet(this@FriendsBangListActivity).setOnClickListener(View.OnClickListener {
                            addWhiteList(item.id)
                        }).show()
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

    private fun updateSet(isCheck: Boolean) {
        if (isToggleBang == isCheck) {
            return
        }
        transLayout.showProgress()
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${obj.user_id}/setting", FormBody.Builder().add("joinSingRanking", if (isCheck) "0" else "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (isCheck) {
                        tv_CurrentStatus.text = resources.getString(R.string.string_song_not_on_bang)
                        if (selfBean != null) {
                            if (mData.contains(selfBean!!)) {
                                mData.remove(selfBean!!)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    } else {
                        if (isOn) {
                            if (selfBean != null) {

                                /**1.已上榜,未请求到数据
                                 * 2.已上榜,请求过数据
                                 * */
                                if (currentRank == -1) {
                                    if (mData.size.rem(10) != 0 || adapter.loadState == BaseQuickAdapter.ELoadState.EMPTY) {
                                        mData.add(selfBean!!)
                                        adapter.notifyDataSetChanged()
                                        currentRank = mData.size - 1
                                    }
                                } else {
                                    mData.add(currentRank, selfBean!!)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                        tv_CurrentStatus.text = if (isOn) resources.getString(R.string.string_jump_to_my_card) else String.format(resources.getString(R.string.string_poor_count_friends_card), selfBean?.song_num.toString(), (5 - selfBean?.song_num!!).toString())
                    }
                    isToggleBang = isCheck
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateFriendEvent(event: UpdateFriendInfoEvent) {
        try {
            when (event.type) {
                0 -> {//备注
                    if (clickPosition != -1) {
                        if (mData.size - 1 > clickPosition) {
                            if (mData[clickPosition].id == event.userId) {
                                mData[clickPosition].nick_name = event.remark
                                adapter.notifyItemChanged(clickPosition)
                            }
                        }
                    }
                }
                1 -> {//昵称
                    if (isOn)
                        if (clickPosition != -1) {
                            if (mData.size - 1 > clickPosition) {
                                if (mData[clickPosition].id == event.userId) {
                                    mData[clickPosition].nick_name = event.nickName
                                    adapter.notifyItemChanged(clickPosition)
                                }
                            }
                        }
                }
                2 -> {//简介
                    if (isOn)
                        if (clickPosition != -1) {
                            if (mData.size - 1 > clickPosition) {
                                if (mData[clickPosition].id == event.userId) {
                                    mData[clickPosition].self_intro = event.desc
                                    adapter.notifyItemChanged(clickPosition)
                                }
                            }
                        }
                }
                3 -> {//头像
                    if (isOn)
                        if (clickPosition != -1) {
                            if (mData.size - 1 > clickPosition) {
                                if (mData[clickPosition].id == event.userId) {
                                    adapter.notifyItemChanged(clickPosition)
                                }
                            }
                        }
                }
            }
        } catch (e: Exception) {

        }
    }

    //是否在榜上显示自己 true 不展示 false 展示
    private var isToggleBang = false
    private var currentRank = -1
    private var selfBean: ListenFriendData.FriendsListBean? = null
    override fun request(flag: Int) {
        when (flag) {
            1 -> {
                OkClientHelper.get(this, "leaderboard/users/songs?pageNo=$currentPage", ListenFriendData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenFriendData
                        if (currentPage == 1) {//查询用户是否上榜
                            isOn = result.data.self.is_on == 1
                            if (!isToggleBang)
                                tv_CurrentStatus.text = if (isOn) resources.getString(R.string.string_jump_to_my_card) else String.format(resources.getString(R.string.string_poor_count_friends_card), result.data.self.song_num.toString(), (5 - result.data.self.song_num).toString())
                        }
                        if (result.code == 0 && result.data != null) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                            for (bean in result.data.other) {
                                if (!isToggleBang) {
                                    if (isOn) {
                                        if (bean.id == loginBean.user_id) {
                                            currentRank = mData.size
                                            selfBean = bean
                                        }
                                    }
                                }
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                            /*if (selfBean == null) {
                                selfBean = result.data.self
                            }*/
                            if (result.data.other != null && result.data.other.size >= 10) {
                                currentPage++
                                request(3)
                            }
                            transLayout.showContent()
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            2 -> {
                transLayout.showProgress()
                val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/" + obj.user_id + "/setting", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any) {
                        result as PatchData
                        if (result.code == 0) {
                            isToggleBang = result.data.join_sing_ranking == 0
                            if (isToggleBang)
                                tv_CurrentStatus.text = resources.getString(R.string.string_song_not_on_bang)
                        }
                        request(1)
                    }

                    override fun onFailure(any: Any) {
                        transLayout.showContent()
                    }
                })
            }
            3 -> {
                OkClientHelper.get(this, "leaderboard/users/songs?pageNo=$currentPage", ListenFriendData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenFriendData
                        if (result.code == 0 && result.data != null) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                            for (bean in result.data.other) {
                                if (!isToggleBang) {
                                    if (isOn) {
                                        if (bean.id == loginBean.user_id) {
                                            currentRank = mData.size
                                            selfBean = bean
                                        }
                                    }
                                }
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                            if (result.data.other != null && result.data.other.size >= 10) {
                                if (mData.size < 50) {
                                    currentPage++
                                    request(3)
                                } else {
                                    if (selfBean == null && currentRank == -1) {
                                        if (isOn) {//上榜保存数据
                                            selfBean = result.data.self
                                            currentRank = 50
                                            mData.add(selfBean!!)
                                            adapter.notifyItemChanged(adapter.itemCount - 1)
                                        }
                                    }
                                }
                            } else {
                                transLayout.showContent()
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    private fun updateUserInfo(formBody: FormBody) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
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
            when (requestCode) {
                REQUEST_PHOTO -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as java.util.ArrayList<String>
                        if (result != null && result.size > 0) {
                            startActivityForResult(Intent(this, CropFriendBgActivity::class.java).putExtra("path", result[0]), REQUEST_CROP)
                        }
                    }
                }
                REQUEST_CROP -> {
                    /**
                     * 更换封面
                     */
                    data?.let {
                        val originPath = data.getStringExtra("originalPath")
                        if (isOn) {
                            if (currentRank != -1) {
                                if (currentRank < mData.size) {
                                    mData[currentRank].friend_card_url = originPath
                                    adapter.notifyItemChanged(currentRank)
                                }
                            }
                        }
                        /**
                         * 更换自己的背景卡图片
                         */
                        val result = data.getStringExtra("result")
                        updateUserInfo(FormBody.Builder().add("friendCardUri", result).add("bucketId", AppTools.bucketId).build())
                    }
                }
                REQUEST_WAVE -> {
                    data?.let {
                        val voicePath = it.getStringExtra("voice")
                        val voiceLength = it.getStringExtra("voiceLength")
                        val baseUri = it.getStringExtra("baseUri")
                        if (isOn) {
                            if (currentRank != -1) {
                                if (currentRank < mData.size) {
                                    mData[currentRank].wave_url = baseUri + voicePath
                                    mData[currentRank].wave_len = voiceLength
                                    adapter.notifyItemChanged(currentRank)
                                }
                            }
                        }
                        val formBody = FormBody.Builder()
                                .add("waveUri", voicePath)
                                .add("waveLen", voiceLength)
                                .build()
                        updateUserInfo(formBody)
                    }
                }
            }
        }
    }
}