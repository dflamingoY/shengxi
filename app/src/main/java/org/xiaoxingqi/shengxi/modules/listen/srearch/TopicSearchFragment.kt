package org.xiaoxingqi.shengxi.modules.listen.srearch

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
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
import kotlinx.android.synthetic.main.frag_seaerch_topic.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.TopicDetailsData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.*
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.EditSendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.*
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.IOException

class TopicSearchFragment : BaseFragment(), ISearchTag {

    override fun setTag(tag: String?, isSearch: Boolean) {
        searchTag = tag
        isRequest = false
        //保存到本地缓存中
        tag?.let {
            EventBus.getDefault().post(SearchTopicEvent(tag))
        }
        if (isSearch && isCurrentVisibility) {
            search()
        }
    }

    private var isCurrentVisibility = true
    override fun visible(page: Int) {
        isCurrentVisibility = page == 0
        if (!isRequest) {
            search()
        }
    }

    private var isRequest = false
    private lateinit var adapter: QuickAdapter<BaseBean>
    private var sortType = 1
    private var searchTag: String? = null
    private var playBean: BaseBean? = null
    private var searchTagId: String? = "0"
    private var lastId: String? = null
    private var current = 1
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private val audioPlayer by lazy { AudioPlayer(activity) }

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

    override fun getLayoutId(): Int {
        return R.layout.frag_seaerch_topic
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        view.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators), ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        setOperator(object : ItemOperatorAdapter() {

            override fun onDelete(bean: BaseBean?) {
                showToast("删除成功")
                mData.remove(bean)
                adapter.notifyDataSetChanged()
                mView!!.transLayout.showContent()
            }

            override fun onRecommend(bean: BaseBean?) {

            }

            override fun onUnRecommend(bean: BaseBean?) {

            }

            override fun onthumb(bean: BaseBean?) {
                adapter.notifyHeart(bean)
                mView!!.transLayout.showContent()
            }

            override fun onUnThumb(bean: BaseBean?) {
                mView!!.transLayout.showContent()
            }

            override fun onFailure(e: Any?) {
                if (e is String) {
                    showToast(e)
                }
                mView!!.transLayout.showContent()
            }

            override fun onComment(from_id: String?) {
                mView!!.transLayout.showContent()
            }

            override fun onReport(type: String) {
                mView!!.transLayout.showContent()
                DialogPBBlack(activity!!).setType(type).show()
            }

            override fun onFriend() {
                mView!!.transLayout.showContent()
            }

            override fun onPrivacy(bean: BaseBean?) {
                adapter.notifyItemChanged(mData.indexOf(bean))
                mView!!.transLayout.showContent()
            }

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
        })
        adapter = object : QuickAdapter<BaseBean>(activity, R.layout.item_voice, mData) {
            var cache = ArrayList<BaseAdapterHelper>()
            private var anim: ValueAnimator? = null

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                val statusText = helper?.getView(R.id.linearStatusText) as LinearStatusText
                glideUtil.loadGlide(item!!.user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(activity, item.created_at)
                helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == 1) View.VISIBLE else View.GONE
                (helper.getView(R.id.heartView) as HeartWaveView).attachBean(item)
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                (helper.getView(R.id.imageGroup) as ImageGroupView).setData(item.img_list)
                if (!TextUtils.isEmpty(item.topic_name)) {
                    helper.getTextView(R.id.tv_Action).text = "#${item.topic_name}#"
                } else {
                    helper.getTextView(R.id.tv_Action).text = ""
                }
                try {
                    if (!item.intersect_tags.isNullOrEmpty()) {
                        helper.getView(R.id.linearInterested).visibility = View.VISIBLE
                        when (item.user_gender) {
                            1 -> helper.getView(R.id.linearInterested).isSelected = true
                            2 -> helper.getView(R.id.linearInterested).isSelected = false
                            else -> {
                                helper.getView(R.id.linearInterested).visibility = View.GONE
                            }
                        }
                        helper.getTextView(R.id.tvInterestedTitle).text = String.format(resources.getString(R.string.string_49), item.intersect_tags.size, item.intersect_tags.joinToString("、"))
                    } else {
                        helper.getView(R.id.linearInterested).visibility = View.GONE
                    }
                } catch (e: Exception) {
                }
                helper.getTextView(R.id.tv_Sub).text = if (item.played_num == 0) resources.getString(R.string.string_Listener) else "${resources.getString(R.string.string_Listener)} ${item.played_num}"
                if (!TextUtils.isEmpty(item.is_shared)) {//是自己
                    helper.getImageView(R.id.iv_Thumb).visibility = View.VISIBLE
                    helper.getView(R.id.heartView).visibility = View.GONE
                    helper.getView(R.id.iv_Thumb).isSelected = item.is_shared == "1"
                    statusText.visibility = View.GONE
                    helper.getView(R.id.tv_Sub).visibility = if (item.is_private == 1) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_Privacy).visibility = if (item.is_private == 0) View.GONE else View.VISIBLE
                    if (item.is_shared == "1") {
                        helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_unshare_world)
                    } else {
                        helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_share_world)
                    }
                    helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_echoing) + if (item.chat_num == 0) {
                        ""
                    } else {
                        " " + item.chat_num
                    }
                } else {//
                    helper.getView(R.id.heartView).visibility = View.VISIBLE
                    helper.getImageView(R.id.iv_Thumb).visibility = View.GONE
                    helper.getView(R.id.iv_Privacy).visibility = View.GONE
                    helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_gongming)
                    if (item.dialog_num == 0) {
                        helper.getTextView(R.id.tv_Recommend)?.text = resources.getString(R.string.string_echoing)
                    } else {
                        helper.getTextView(R.id.tv_Recommend)?.text = "${resources.getString(R.string.string_Talks)} ${item.dialog_num}"
                    }
                    helper.getView(R.id.tv_Sub).visibility = View.GONE
                    when (item.friend_status) {
                        2 -> {
                            statusText.visibility = View.GONE
                            helper.getView(R.id.tv_Sub).visibility = View.GONE
                        }
                        1 -> {
                            statusText.visibility = View.VISIBLE
                            statusText.isSelected = false
                        }
                        0 -> {
                            statusText.isSelected = true
                            statusText.visibility = View.VISIBLE
                        }
                    }
                }
                (helper.getView(R.id.imageGroup) as ImageGroupView).setOnClickViewListener {
                    startActivity(Intent(activity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    activity?.overridePendingTransition(0, 0)
                }
                helper.getView(R.id.lineaer_Recommend)?.setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        startActivity(Intent(activity, DynamicDetailsActivity::class.java)
                                .putExtra("id", item.voice_id.toString())
                                .putExtra("uid", item.user_id.toString())
                                .putExtra("isExpend", item.chat_num > 0)
                        )
                    } else {
                        if (item.dialog_num > 0) {
                            startActivity(Intent(activity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id.toString())
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user.id))
                        } else {
                            queryPermission(item, mView!!.transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        }
                    }
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    item.topic_name?.let {
                        if (it != searchTag)
                            startActivity(Intent(activity, TopicResultActivity::class.java)
                                    .putExtra("tagId", item.topic_id.toString())
                                    .putExtra("tag", item.topic_name))
                    }
                }
                statusText.setOnClickListener {
                    if (statusText.isSelected) {
                        mView!!.transLayout.showProgress()
                        friends(item, it)
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
//                            mView!!.transLayout.showProgress()
                            item.isNetStatus = true
                            unThumb(item, helper.getView(R.id.heartView))
                        } else {
                            mView!!.transLayout.showProgress()
                            LightUtils.contains(item.voice_id)
                            tuhmb(item, helper.getView(R.id.heartView))
                        }
                    }
                }
                helper.getView(R.id.cardView).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.user.id))
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {
                        DialogMore(activity!!).setEditable(item.resource_id == "0").setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_ShareWechat -> {
                                    DialogShare(activity!!, true).setOnClickListener(View.OnClickListener { share ->
                                        when (share.id) {
                                            R.id.linearWechat -> {
                                                ShareUtils.share(activity, Wechat.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                            R.id.linearMoment -> {
                                                ShareUtils.share(activity, WechatMoments.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_ShareWeibo -> {
                                    ShareUtils.share(activity, SinaWeibo.NAME, item.voice_url, item.share_url, "", null)
                                }
                                R.id.tv_ShareQQ -> {
                                    DialogShare(activity!!, false).setOnClickListener(View.OnClickListener { share ->
                                        when (share.id) {
                                            R.id.linearQQ -> {
                                                ShareUtils.share(activity, QQ.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                            R.id.linearQzone -> {
                                                ShareUtils.share(activity, QZone.NAME, item.voice_url, item.share_url, "", null)
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(activity!!).setHintText(resources.getString(R.string.string_delete_voice)).setOnClickListener(View.OnClickListener {
                                        delete(item)
                                    }).show()
                                }
                                R.id.tv_Self -> {
                                    if (item.is_private == 1) {
                                        mView!!.transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    } else {
                                        DialogPrivacy(activity!!).setOnClickListener(View.OnClickListener {
                                            mView!!.transLayout.showProgress()
                                            setVoicePrivacy(item)
                                        }).show()
                                    }
                                }
                                R.id.tv_add_album -> {
                                    startActivity(Intent(activity, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
                                }
                                R.id.tvReEditVoice -> {
                                    startActivity<EditSendAct>("data" to item)
                                }
                            }
                        }).show()
                    } else {
                        DialogReport(activity!!).setOnClickListener(View.OnClickListener { report ->
                            when (report.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminSetPrivacy(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report ->
                                    DialogReportContent(activity!!).setOnResultListener(OnReportItemListener {
                                        mView!!.transLayout.showProgress()
                                        report(item.voice_id, it, item.user.id)
                                    }).show()
                                R.id.tv_report_normal -> {
                                    activity!!.reportNormal { reportType ->
                                        reportNormalItem(item.voice_id, reportType)
                                    }
                                }
                            }
                        }).show()
                    }
                }
                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                voiceProgress.setOnClickListener {
                    //此条播放状态 暂停
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            voiceProgress.finish()
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
                seekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!seekProgress.isPressed) {
                            if (audioPlayer.isPlaying) {
                                item.allDuration = audioPlayer.duration
                                progressHandler.stop()
                                audioPlayer.stop()
                                item.isPlaying = false
                                voiceProgress.finish()
                            }
                        }
                    }

                    override fun endTrack(progress: Float) {
                        item.pasuePosition = (progress * item.allDuration).toInt()
                        download(helper, item)
                    }
                })
                voiceProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            item.pasuePosition = 1
                            voiceProgress.finish()
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

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                down(item, false)
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
                super.changeStatue(isSelect)
                for (helper in cache) {
                    val currentPosition = audioPlayer.currentPosition.toInt()
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                        (helper.getView(R.id.heartView) as HeartWaveView).waterLevelRatio = currentPosition / audioPlayer.duration.toFloat()
                    } catch (e: Exception) {
                        e.printStackTrace()
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

            override fun notifyHeart(bean: BaseBean?) {
                bean?.let { b ->
                    cache.loop { baseAda ->
                        (baseAda.getView(R.id.voiceProgress) as VoiceProgress).data.voicePath == bean.voicePath
                    }?.let { helper ->
                        if (TextUtils.isEmpty(b.is_shared)) {
                            helper.getView(R.id.iv_Thumb).let {
                                it.isSelected = b.is_collected == 1
                                if (mView!!.recyclerView.isOffsetScreen(mData.indexOf(bean))) {
                                    SmallBang.attach2Window(activity).bang(it, 60f, null)
                                }
                            }
                        }
                    }
                }
            }
        }
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.loadmore_padding_bottom, mView!!.recyclerView, false))
        search()
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onCompletion() {
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                    if (TextUtils.isEmpty(it.is_shared) && it.is_collected != 1 && LightUtils.contains(it.voice_id)) {
                        it.is_collected = 1
                        tuhmb(it, null, 5)
                    }
                }
                adapter.animStatus(false)
                progressHandler.stop()
                try {
                    playBean?.let {
                        if (SPUtils.getBoolean(activity, IConstant.PLAY_MENU_AUTO, false)) {
                            var index = mData.indexOf(it) + 1
                            if (index >= mData.size) index = 0
                            if (index == 0) mView!!.recyclerView.scrollToPosition(0)
                            down(mData[index], index != 0)
                            if (mData.indexOf(it) == mData.size - 2) {
                                current++
                                request(current)
                            }
                        } else {
                            mView!!.customPlayMenu.isSelected = false
                        }
                    }
                } catch (e: Exception) {
                }
            }

            override fun onInterrupt() {
                mView!!.customPlayMenu.isSelected = false
                playBean?.let {
                    it.isPlaying = false
                    adapter.changeStatue(false)
                }
                adapter.animStatus(false)
                progressHandler.stop()
            }

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
                            current++
                            request(current)
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
                    mView!!.recyclerView.scrollToPosition(0)
                } else
                    playBean.let {
                        mView!!.recyclerView.scroll(activity, mData.indexOf(it), false)
                    }
            }
        })
        adapter.setOnLoadListener {
            current++
            request(current)
        }
        mView!!.transLayout.findViewById<View>(R.id.tv_CreateTopic).setOnClickListener {
            startActivity(Intent(activity, SendAct::class.java)
                    .putExtra("type", 3)
                    .putExtra("topicName", searchTag)
                    .putExtra("topicId", if (searchTagId == "0") "" else searchTagId)
            )
            activity!!.overridePendingTransition(R.anim.operate_enter, 0)
        }
        mView!!.tvSort.setOnClickListener {
            val loca = IntArray(2)
            it.getLocationOnScreen(loca)
            DialogSort(activity).setOnClickListener(View.OnClickListener { sort ->
                when (sort.id) {
                    R.id.viewNew -> {
                        mView!!.tvSort.text = resources.getString(R.string.string_new)
                        sortType = 1
                    }
                    R.id.viewHot -> {
                        mView!!.tvSort.text = resources.getString(R.string.string_hot)
                        sortType = 2
                    }
                }
                if (audioPlayer.isPlaying) audioPlayer.stop()
                playBean = null
                current = 1
                lastId = null
                mView!!.transLayout.showProgress()
//                mData.clear()
//                adapter.notifyDataSetChanged()
                request(current)
            }).setSelectTitle(sortType).setLocation(AppTools.getWindowsWidth(activity) - AppTools.dp2px(activity, 119 + 15), loca[1])
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying)
                audioPlayer.stop()
            playBean = null
            current = 1
            lastId = null
            mView!!.transLayout.showProgress()
//            mData.clear()
//            adapter.notifyDataSetChanged()
            request(current)
        }
    }

    private fun down(item: BaseBean, isScroll: Boolean = true) {
        if (isScroll && mView!!.recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            mView!!.recyclerView.scroll(activity, mData.indexOf(item))
        }
        if (TextUtils.isEmpty(item.voice_url)) {
            showToast(resources.getString(R.string.string_error_file))
            return
        }
        playBean = item
        val file = getDownFilePath(item.voice_url)
        if (file.exists()) {
            audioPlayer.setDataSource(file.absolutePath)
            audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        } else {
            OkClientHelper.downFile(activity, item.voice_url, { o ->
                try {
                    if (null == o) {
                        showToast(resources.getString(R.string.string_error_file))
                        return@downFile
                    }
                    audioPlayer.setDataSource(o.toString())
                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }, { showToast(VolleyErrorHelper.getMessage(it)) })
        }
    }

    private fun search() {
        try {
            mView!!.transLayout.showProgress()
            searchContent(searchTagId, searchTag)
            if (audioPlayer.isPlaying) audioPlayer.stop()
//        mData.clear()
//        adapter.notifyDataSetChanged()
            mView!!.transLayout.showProgress()
            playBean = null
            lastId = null
            current = 1
            request(current)
        } catch (e: Exception) {
        }
    }

    private fun searchContent(flag: String?, topicName: String?) {
        OkClientHelper.get(activity, "topics/$flag?topicName=$topicName", TopicDetailsData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as TopicDetailsData
                if (result.data != null)
                    mView!!.tv_Count.text = String.format(resources.getString(R.string.string_topic_search_count), result.data.voice_num)
                else {
                    mView!!.tv_Count.text = String.format(resources.getString(R.string.string_topic_search_count), "0")
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.tv_Count.text = String.format(resources.getString(R.string.string_topic_search_count), "0")
            }
        })
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "topics/$searchTagId/voices?topicName=$searchTag&lastId=$lastId&sortType=$sortType&page=$flag&recognition=1", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                mView!!.transLayout.showContent()
                result as VoiceData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (flag == 1) {
                        if (audioPlayer.isPlaying) {
                            audioPlayer.stop()
                            progressHandler.stop()
                        }
                        mData.clear()
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else
                        for (item in result.data) {
                            mData.add(item)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    result.data?.let {
                        if (it.size >= 10) {
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                        }
                    }
                    lastId = mData[mData.size - 1].voice_id
                }
                if (mData.size == 0) {
                    mView!!.transLayout.showEmpty()
                }
                isRequest = true
            }

            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing
                mView!!.transLayout.showOffline()
                isRequest = true
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        progressHandler.removeCallbacks(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isResumed || !isVisible) {
            playBean?.let {
                if (it.isPlaying) {
                    it.isPlaying = false
                    it.pasuePosition = 0
                    adapter.notifyDataSetChanged()
                    progressHandler.stop()
                }
            }
            audioPlayer.stop()
        }
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
            //移除列表, 不在此搜索结果列表中
            mData.remove(it)
            adapter.notifyDataSetChanged()
        }
    }
}