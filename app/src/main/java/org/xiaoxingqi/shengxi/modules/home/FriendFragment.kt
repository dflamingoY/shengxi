package org.xiaoxingqi.shengxi.modules.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.frag_friends.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.LightUtils
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.isOffsetScreen
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.EditSendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.scroll
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.*
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.IOException
import java.util.*

class FriendFragment : BaseFragment(), HomeTabClick {
    private lateinit var adapter: QuickAdapter<BaseBean>
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var transLayout: TransLayout
    private var playBean: BaseBean? = null
    private var lastId: String? = null
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private var isVisiblePage = false
    private lateinit var audioPlayer: AudioPlayer
    private var currentIndex = 1

    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(20) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSensorEvent(event: SensorChangeEvent) {
        /**
         * 发生变化
         */
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        if (headsetOn || a2dpOn || scoOn) {
            return
        }
        if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) {
            return
        }
        if (event.type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                    audioPlayer.seekTo(it.pasuePosition)
                }
            }
        } else if (event.type == 2) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                playBean?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                    audioPlayer.seekTo(it.pasuePosition)
                }
            }
        }
    }

    override fun currentPage(page: Int) {
        isVisiblePage = (page == currentIndex)
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_friends
    }

    override fun initView(view: View?) {
        recyclerView = view!!.recyclerView
        swipeRefresh = view.swipeRefresh
        transLayout = view.transLayout
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators),
                ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
    }

    override fun onResume() {
        super.onResume()
        try {
            transLayout.findViewById<View>(R.id.tv_home_mood_hint).isSelected = AppTools.getLanguage(activity) == IConstant.HK || AppTools.getLanguage(activity) == IConstant.TW
        } catch (e: Exception) {
        }
    }

    override fun initData() {
        val login = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (SPUtils.getBoolean(activity, IConstant.IS_GUIDE_USER_HOME + login.user_id, true)) {
            mView!!.ivAnimGuide.visibility = View.VISIBLE
        } else {
            mView!!.ivAnimGuide.visibility = View.GONE
        }
        if (SPUtils.getString(activity, IConstant.TAB_HOME_INDEX + login.user_id, "0") == "1") {
            isVisiblePage = true
            currentIndex = 0
        }
        audioPlayer = AudioPlayer(activity)
        parseLocalCache()
        setOperator(object : ItemOperator {
            override fun onAdminPrivacy(bean: BaseBean?) {
                try {
                    val indexOf = mData.indexOf(bean)
                    mData.remove(bean)
                    adapter.notifyItemRemoved(indexOf)
                    dialogPwd?.dismiss()
                } catch (e: Exception) {

                }
            }

            override fun onAdminFail() {
                dialogPwd?.setCallBack()
            }

            override fun onPrivacy(bean: BaseBean?) {
                adapter.notifyItemChanged(mData.indexOf(bean))
                transLayout.showContent()
            }

            override fun onFriend() {

            }

            override fun onReport(type: String) {
                transLayout.showContent()
                DialogPBBlack(activity!!).setType(type).show()
            }

            override fun onDelete(bean: BaseBean?) {
                showToast("删除成功")
                mData.remove(bean)
                adapter.notifyDataSetChanged()
                transLayout.showContent()
                if (mData.size == 0) {
                    transLayout.showEmpty()
                }
                /**
                 *  查询总时长
                 */
                about()
            }

            override fun onRecommend(bean: BaseBean?) {
                /**
                 * 数据同步到世界
                 */
//                EventBus.getDefault().post(IShareWorldData(bean))
                transLayout.showContent()
            }

            override fun onUnRecommend(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onthumb(bean: BaseBean?) {
                adapter.notifyHeart(bean)
                transLayout.showContent()
            }

            override fun onUnThumb(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onFailure(e: Any?) {
                if (e is String) {
                    showToast(e)
                }
                transLayout.showContent()
            }

            override fun onComment(from_id: String?) {
                commentBean?.let {
                    it.chat_id = from_id
                    it.dialog_num = 1
                    adapter.notifyItemChanged(mData.indexOf(it))
                }
                transLayout.showContent()
            }
        })
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<BaseBean>(activity, R.layout.item_voice, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                glideUtil.loadGlide(item!!.user.avatar_url, helper!!.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tv_Action).visibility = if (TextUtils.isEmpty(item.topic_name)) View.GONE else View.VISIBLE
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(activity, item.created_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == 1) View.VISIBLE else View.GONE
                (helper.getView(R.id.heartView) as HeartWaveView).attachBean(item)
                val imgGroup = helper.getView(R.id.imageGroup) as ImageGroupView
                imgGroup.setData(item.img_list)
                imgGroup.setOnClickViewListener {
                    startActivity(Intent(activity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    activity!!.overridePendingTransition(R.anim.act_enter_alpha, 0)
                }
                if (item.resource == null) {
                    helper.getView(R.id.itemDynamic).visibility = View.GONE
                }
                item.resource?.let {
                    if (!TextUtils.isEmpty(it.id)) {
                        helper.getView(R.id.itemDynamic).visibility = View.VISIBLE
                        (helper.getView(R.id.itemDynamic) as ItemDynamicView).setData(it, item.resource_type, item.user_score)
                    } else {
                        helper.getView(R.id.itemDynamic).visibility = View.GONE
                    }
                }
                helper.getTextView(R.id.tv_Sub).text = if (item.played_num == 0) resources.getString(R.string.string_Listener) else resources.getString(R.string.string_Listener) + " ${item.played_num}"
                helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                if (!TextUtils.isEmpty(item.is_shared)) {
                    helper.getImageView(R.id.iv_Thumb).isSelected = item.is_shared == "1"
                    helper.getImageView(R.id.iv_Thumb).visibility = View.VISIBLE
                    helper.getView(R.id.heartView).visibility = View.GONE
                    helper.getView(R.id.tv_Sub).visibility = if (item.is_private == 1) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_Privacy).visibility = if (item.is_private == 1) View.VISIBLE else View.GONE
                    if (item.is_shared == "1") {
                        helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_unshare_world)
                    } else {
                        helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_share_world)
                    }
                    helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_echoing) + if (item.chat_num <= 0) {
                        ""
                    } else {
                        " " + item.chat_num
                    }
                } else {
                    helper.getView(R.id.heartView).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.GONE
                    helper.getView(R.id.tv_Sub).visibility = View.GONE
                    helper.getView(R.id.iv_Privacy).visibility = View.GONE
                    helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_gongming)
                    if (item.dialog_num <= 0) {
                        helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_echoing)
                    } else {
                        helper.getTextView(R.id.tv_Recommend)?.text = "${resources.getString(R.string.string_Talks)} " + item.dialog_num
                    }
                }

                helper.getImageView(R.id.roundImg)?.setOnClickListener {
                    UserDetailsActivity.start(activity as Activity, item.user.avatar_url, item.user.id, it)
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    item.topic_name?.let {
                        startActivity(Intent(activity, TopicResultActivity::class.java)
                                .putExtra("tagId", item.topic_id.toString())
                                .putExtra("tag", item.topic_name))
                    }
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {//共享
                        queryCount(item, helper.getTextView(R.id.tv_Echo), helper.getView(R.id.iv_Thumb))
                    } else {//共鳴
                        if (item.isNetStatus) {
                            return@setOnClickListener
                        }
                        if (item.is_collected == 1) {
                            item.isNetStatus = true
//                            transLayout.showProgress()
                            unThumb(item, helper.getView(R.id.heartView))
                        } else {
                            transLayout.showProgress()
                            LightUtils.addItem(item.voice_id)
                            tuhmb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        DialogMore(this@FriendFragment.activity!!).setEditable(item.resource_id == "0").setAdmin(item.user_id.toString()).setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_ShareWechat -> {
                                    DialogShare(activity!!, true).setOnClickListener(View.OnClickListener { dialog ->
                                        when (dialog.id) {
                                            R.id.linearWechat -> {
                                                ShareUtils.share(this@FriendFragment.activity, Wechat.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                            R.id.linearMoment -> {
                                                ShareUtils.share(this@FriendFragment.activity, WechatMoments.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_ShareWeibo -> {
                                    ShareUtils.share(this@FriendFragment.activity, SinaWeibo.NAME, item.voice_url, item.share_url, "", null)
                                }
                                R.id.tv_ShareQQ -> {
                                    DialogShare(this@FriendFragment.activity!!, false).setOnClickListener(View.OnClickListener { dialog ->
                                        when (dialog.id) {
                                            R.id.linearQQ -> {
                                                ShareUtils.share(this@FriendFragment.activity, QQ.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                            R.id.linearQzone -> {
                                                ShareUtils.share(this@FriendFragment.activity, QZone.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(activity!!).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        transLayout.showProgress()
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {//切换是否是自己可见
                                    if (item.is_private == 1) {
                                        transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(activity!!).setOnClickListener(View.OnClickListener {
                                            transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                                R.id.tv_add_album -> {
                                    startActivity(Intent(activity, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
                                }
                                R.id.tvOfficialTop -> {//小二置顶聊天
                                    adminTopUser(item)
                                }
                                R.id.tvReEditVoice -> {
                                    startActivity<EditSendAct>("data" to item)
                                }
                            }
                        }).show()
                    else {
                        DialogReport(this@FriendFragment.activity!!).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report -> {
                                    DialogReportContent(activity!!)
                                            .setOnResultListener(OnReportItemListener { report ->
                                                transLayout.showProgress()
                                                report(item.voice_id, report, item.user.id)
                                            }).show()
                                }
                                R.id.tv_report_normal -> {
                                    DialogNormalReport(activity!!).setOnClickListener(View.OnClickListener { report ->
                                        when (report.id) {
                                            R.id.tv_Attach -> {
                                                reportNormalItem(item.voice_id, "1")
                                            }
                                            R.id.tv_Porn -> {
                                                reportNormalItem(item.voice_id, "2")
                                            }
                                            R.id.tv_Junk -> {
                                                reportNormalItem(item.voice_id, "3")
                                            }
                                            R.id.tv_illegal -> {
                                                reportNormalItem(item.voice_id, "4")
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_Follow -> {
                                    if (item.subscription_id == 0) {
                                        addSubscriber(item)
                                    } else {
                                        DialogGraffiti(context).setTitle(String.format(resources.getString(R.string.string_follow_explor1), when (item.resource_type) {
                                            1 -> resources.getString(R.string.string_follow_movies)
                                            2 -> resources.getString(R.string.string_follow_book)
                                            3 -> resources.getString(R.string.string_follow_song)
                                            else -> resources.getString(R.string.string_follow_movies)
                                        }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                            deletedSubscriber(item)
                                        }).show()
                                    }
                                }
                            }
                        }).show()
                    }
                }
                helper.getView(R.id.lineaer_Recommend)?.setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                                .putExtra("id", item.voice_id)
                                .putExtra("uid", item.user.id)
                                .putExtra("isExpend", item.chat_num > 0)
                        )
                    } else {
                        if (item.dialog_num == 0) {
                            queryPermission(item, transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        } else {
                            startActivity(Intent(activity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id)
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user_id.toString())
                            )
                        }
                    }
                }
                val progress = (helper.getView(R.id.voiceProgress) as VoiceProgress)
                progress.data = item
                progress/*.findViewById<View>(R.id.viewSeekProgress)*/.setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            progress.finish()
                            return@setOnClickListener
                        } else {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                    }
                    playBean?.let {
                        if (item !== it) {
                            it.isPlaying = false
                            it.isPause = false
                            it.pasuePosition = 0
                        }
                    }
                    download(helper, item)
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                seekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!seekProgress.isPressed) {
                            if (audioPlayer.isPlaying) {
                                item.allDuration = audioPlayer.duration
                                progressHandler.stop()
                                audioPlayer.stop()
                                item.isPlaying = false
                                progress.finish()
                            }
                        }
                    }

                    override fun endTrack(progress: Float) {
                        /**
                         * 滑动停止
                         */
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        download(helper, item)
                    }
                })
                progress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                    //播放状态直接停止
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = 1
                            progress.finish()
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
                    }
                    item.pasuePosition = 0
                    download(helper, item)
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            override fun animStatus(isStart: Boolean) {
                if (isStart) {
                    if (anim == null) {
                        anim = ValueAnimator.ofFloat(0f, 1f).setDuration(1500)
                        anim!!.interpolator = LinearInterpolator()
                        anim!!.repeatCount = ValueAnimator.INFINITE
                        anim!!.addUpdateListener {
                            val value = it.animatedValue as Float
                            if (audioPlayer.isPlaying) {
                                cache.forEach { helper ->
                                    (helper.getView(R.id.heartView) as HeartWaveView).waveShiftRatio = value
                                }
                            }
                        }
                    }
                    anim!!.start()
                } else {
                    cache.forEach { helper ->
                        (helper.getView(R.id.heartView) as HeartWaveView).end()
                    }
                    anim?.let {
                        it.cancel()
                        anim = null
                    }
                }
            }

            override fun changeStatue(isSelect: Boolean) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                for (helper in cache) {
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                        (helper.getView(R.id.heartView) as HeartWaveView).waterLevelRatio = currentPosition / audioPlayer.duration.toFloat()
                    } catch (e: Exception) {
                    }
                }
            }

            override fun notifyHelperStatus(bean: BaseBean) {
                if (!TextUtils.isEmpty(bean.is_shared))
                    if (playBean == bean) {
                        //更新当前播放的次数++ 正在播放的item
                        cache.loop { baseAda ->
                            (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                        }?.let { ada ->
                            ada.getTextView(R.id.tv_Sub).text = if (bean.played_num == 0) context.resources.getString(R.string.string_Listener) else context.resources.getString(R.string.string_Listener) + " ${bean.played_num}"
                        }
                    }
            }

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                try {
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    down(item, false)
                } catch (e: IOException) {
                }
            }

            override fun notifyHeart(bean: BaseBean?) {
                bean?.let { b ->
                    cache.loop { baseAda ->
                        (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                    }?.let { helper ->
                        if (TextUtils.isEmpty(b.is_shared)) {
                            helper.getView(R.id.iv_Thumb).let {
                                it.isSelected = b.is_collected == 1
                                if (recyclerView.isOffsetScreen(mData.indexOf(bean))) {
                                    SmallBang.attach2Window(activity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_transparent, recyclerView, false))
        request(0)
    }

    /**
     * 解析本地缓存文件，不区分账号
     */
    private fun parseLocalCache() {
        try {
            val voiceCache = PreferenceTools.getObj(activity, IConstant.FRIENDPAGECACHE, VoiceData::class.java)
            if (null != voiceCache) {
                if (voiceCache.data != null) {
                    mData.addAll(voiceCache.data)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.scroll(context, mData.indexOf(item))
        }
        try {
            if (TextUtils.isEmpty(item.voice_url)) {
                showToast(resources.getString(R.string.string_error_file))
            }
            playBean = item
            val file = getDownFilePath(item.voice_url)
            if (file.exists()) {
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else {
                OkClientHelper.downProxy(activity, item, {
                    audioPlayer.setDataSource(it)
                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                }, {
//                    showToast(VolleyErrorHelper.getMessage(it))
                    //如果发生了错误, 则再次请求一次, 只请求一次, 就跳过
                    if (!item.isReDown) {
                        item.isReDown = true
                        down(item, false)
                    } else
                        nextVoice()
                })
            }
        } catch (e: Exception) {

        }
    }

    private fun nextVoice() {
        playBean?.let {
            if (SPUtils.getBoolean(context, IConstant.PLAY_MENU_AUTO, false)) {
                var index = mData.indexOf(it) + 1
                if (index >= mData.size) index = 0
                if (index == 0) recyclerView.smoothScrollToPosition(0)
                down(mData[index], index != 0)
                if (mData.indexOf(it) == mData.size - 2) {
                    request(0)
                }
            } else {
                mView!!.customPlayMenu.isSelected = false
            }
        }
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

            override fun onPrepared() {
                mView!!.customPlayMenu.isSelected = true
                playBean?.let {
                    if (TextUtils.isEmpty(it.is_shared) && LightUtils.contains(it.voice_id)) {
                        adapter.animStatus(true)
                    }
                    it.allDuration = audioPlayer.duration
                    audioPlayer.seekTo(it.pasuePosition)
                    it.pasuePosition = 0
                    addPlays(it, null) { bean ->
                        adapter.notifyHelperStatus(bean)
                    }
                    it.isPlaying = true
                }
                progressHandler.start()
            }

            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(true)
                    if (TextUtils.isEmpty(it.is_shared) && it.is_collected != 1 && LightUtils.contains(it.voice_id)) {
                        it.is_collected = 1
                        tuhmb(it, null, 5)
                    }
                }
                adapter.animStatus(false)
                progressHandler.stop()
                try {
                    nextVoice()
                } catch (e: Exception) {
                }
            }

            override fun onInterrupt() {
                adapter.animStatus(false)
                mView!!.customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(true)
                }
                progressHandler.stop()
            }
        }
        swipeRefresh.setOnRefreshListener {
            playBean?.let {
                if (it.isPlaying) {
                    if (audioPlayer.isPlaying) {
                        progressHandler.stop()
                        audioPlayer.stop()
                    }
                    playBean!!.isPlaying = false
                }
            }
            playBean = null
            lastId = null
            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
        }
        mView!!.customPlayMenu.setOnCircleMenuListener(object : OnCircleMenuOperatorListener {
            override fun next() {
                audioPlayer.stop()
                try {
                    playBean?.let {
                        val index = mData.indexOf(it) + 1
                        if (index >= mData.size) {
                            return@let
                        }
                        if (index == mData.size - 2) {//加载下一页, 重复加载问题 倒数第2条开始加载
                            request(0)
                        }
                        down(mData[index], index != 0)
                    }
                } catch (e: Exception) {

                }
            }

            override fun play() {
                if (audioPlayer.isPlaying) {
                    playBean?.let {
                        it.pasuePosition = audioPlayer.currentPosition.toInt()
                    }
                    audioPlayer.stop()
                } else {
                    if (playBean?.let {
                                down(it)
                                it
                            } == null) {
                        //播放0角标
                        if (mData.size > 0) {
                            down(mData[0])
                        }
                    }
                }
            }

            override fun pre() {
                audioPlayer.stop()
                playBean?.let {
                    var index = mData.indexOf(it) - 1
                    if (index < 0) index = 0
                    down(mData[index])
                }
            }

            override fun top() {
                if (!audioPlayer.isPlaying) {
                    recyclerView.scrollToPosition(0)
                } else
                    playBean.let {
                        recyclerView.scroll(context, mData.indexOf(it), false)
                    }
            }
        })
        mView!!.ivAnimGuide.setOnClickListener {
            //观看完 寝室banner 之后此按钮自动消失
            startActivityForResult<AnimGuideActivity>(0xff, "isGuide" to true, "name" to "voiceVisible")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(update: UpdateSendVoice) {
        if (update.type == 1) {
            if (!TextUtils.isEmpty(update.voice_id)) {
                checkShare(update.voice_id)
            }
            lastId = null
            swipeRefresh.isRefreshing = true
            recyclerView.scrollToPosition(0)
            queryVoicesStatus(isPush = true)
            request(0)
        } else if (update.type == 3) {//刷新
            if (isVisiblePage) {
                recyclerView.scrollToPosition(0)
                lastId = null
                swipeRefresh.isRefreshing = true
                request(0)
            }
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    private var dialog: Dialog? = null

    //小二音频置顶
    private fun adminTopUser(item: BaseBean) {
        OkClientHelper.patch(activity, "users/${item.user_id}/voices/${item.voice_id}", FormBody.Builder().add("topAt", "${System.currentTimeMillis() / 1000}").build(),
                BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("操作成功")
                } else {
                    showToast(result.msg)
                }
            }
        })
    }

    private fun checkShare(flag: String) {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        dialog = DialogHintToWorld(activity!!).setOnClickListener {
            when (it.id) {
                R.id.tv_Commit -> {
                    shareWorld(flag)
                }
                R.id.tv_Cancel -> {
                    showToast(resources.getString(R.string.string_sendAct_11))
                }
            }
        }
        dialog?.show()
    }

    private fun about() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "users/${loginBean.user_id}/about", UserInfoData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as UserInfoData
                if (result.code == 0) {
                    SPUtils.setInt(activity, IConstant.TOTALLENGTH + result.data.user_id, result.data.voice_total_len)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private fun shareWorld(voiceId: String) {
        //共享到世界
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "users/${loginBean.user_id}/voices/$voiceId/share", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    showToast("分享成功，快去世界看看吧！")
                    EventBus.getDefault().post(ImplPageChangeEvent(1))
                    lastId = null
                    request(0)
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHomeTab(event: HomeTabChangeEvent) {
        currentIndex = if (event.index == "0") 1 else 0
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisiblePage || !isResumed) {

            playBean?.let {
                if (it.isPlaying || it.isPause) {
                    it.isPlaying = false
                    it.isPause = false
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            audioPlayer.stop()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateIdentity(event: UpdateIdentityEvent) {
        if (mData.size > 0) {
            val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            mData.forEach {
                if (loginBean.user_id == it.user_id.toString()) {
                    it.user.identity_type = event.type
                    val indexOf = mData.indexOf(it)
                    adapter.notifyItemChanged(indexOf)
                }
            }
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "voices/type/1?lastId=$lastId&recognition=1", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (lastId == null) {
                    mData.clear()
                }
                if (result.data != null) {
                    if (lastId == null) {
                        /**
                         * 添加本地缓存
                         */
                        if (audioPlayer.isPlaying) {
                            progressHandler.stop()
                            audioPlayer.stop()
                        }
                        PreferenceTools.saveObj(activity, IConstant.FRIENDPAGECACHE, result)
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        for (item in result.data) {
                            mData.add(item)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    result.data?.let {
                        if (it.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                    }
                    lastId = mData[mData.size - 1].voice_id
                } else {
                    if (lastId == null) {
                        adapter.notifyDataSetChanged()
                        try {
                            PreferenceTools.clear(activity, IConstant.FRIENDPAGECACHE)
                        } catch (e: Exception) {
                        }
                    }
                }
                if (mData.size == 0) {
                    transLayout.showEmpty()
                } else {
                    transLayout.showContent()
                }
                swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                if (AppTools.isNetOk(activity)) {
                    showToast(any.toString())
                } else {
                    if (mData.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    } else {
                        mData.clear()
                        adapter.notifyDataSetChanged()
                        transLayout.showOffline()
                    }
                }
                swipeRefresh.isRefreshing = false
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgNotifyEvent(event: SendMsgSocketEvent) {
        try {
            commentBean?.let {
                if (it.voice_id == event.voiceId) {
                    /**
                     * 更新数据
                     */
                    it.chat_id = event.chatId
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (it.user_id.toString() == loginBean.user_id) {
                        it.chat_num++
                    } else {
                        it.dialog_num++
                    }
                    adapter.notifyItemChanged(mData.indexOf(it))
                }
            }
        } catch (e: Exception) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun topicChangeEvent(event: ChangeTopicInfoEvent) {
        mData.loop {
            event.voiceId == it.voice_id
        }?.let {
            try {
                it.topic_id = event.topicId.toInt()
                it.topic_name = event.topicName
                adapter.notifyItemChanged(mData.indexOf(it))
            } catch (e: Exception) {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("HomeMood : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            } else if (requestCode == 0xff) {
                //隐藏按钮显示
                mView!!.ivAnimGuide.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}