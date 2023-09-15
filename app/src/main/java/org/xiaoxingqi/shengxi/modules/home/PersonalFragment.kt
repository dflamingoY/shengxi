package org.xiaoxingqi.shengxi.modules.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import cn.sharesdk.sina.weibo.SinaWeibo
import cn.sharesdk.tencent.qq.QQ
import cn.sharesdk.tencent.qzone.QZone
import cn.sharesdk.wechat.friends.Wechat
import cn.sharesdk.wechat.moments.WechatMoments
import kotlinx.android.synthetic.main.frag_personal.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.listen.srearch.TopicResultActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddAlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.*
import java.io.IOException
import kotlin.collections.ArrayList
import java.lang.Deprecated

@Deprecated
class PersonalFragment : BaseFragment(), HomeTabClick {
    private lateinit var adapter: QuickAdapter<BaseBean>
    private var playBean: BaseBean? = null
    private val mData by lazy {
        ArrayList<BaseBean>()
    }
    private lateinit var audioPlayer: AudioPlayer
    private var isVisiblePage = false
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
        }
    }

    private lateinit var loadMoreView: View
    override fun currentPage(page: Int) {
        isVisiblePage = (page == 1)
        try {
            if (!isVisiblePage) {
                if (mView!!.swipeRefresh.isRefreshing) {
                    mView!!.swipeRefresh.isRefreshing = false
                }
            }
        } catch (e: Exception) {

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
        return R.layout.frag_personal
    }

    override fun initView(view: View?) {
        view!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        view.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.colorIndecators), resources.getColor(R.color.colorMovieTextColor),
                resources.getColor(R.color.color_Text_Black))
    }

    private var playUrl: String? = null
    override fun initData() {
        EventBus.getDefault().register(this)
        audioPlayer = AudioPlayer(activity)
        setOperator(object : ItemOperator {
            override fun onAdminPrivacy(bean: BaseBean?) {
                val indexOf = mData.indexOf(bean)
                mData.remove(bean)
                adapter.notifyItemRemoved(indexOf)
                dialogPwd?.dismiss()
            }

            override fun onAdminFail() {
                dialogPwd?.setCallBack()
            }

            override fun onPrivacy(bean: BaseBean?) {
                mData.remove(bean)
                adapter.notifyDataSetChanged()
                mView!!.transLayout.showContent()
            }

            override fun onFriend() {

            }

            override fun onReport(type: String) {
                mView!!.transLayout.showContent()
                DialogPBBlack(activity!!).setType(type).show()
            }

            override fun onDelete(bean: BaseBean?) {
                /*showToast("删除成功")
                mData.remove(bean)
                adapter.notifyDataSetChanged()
                mView!!.transLayout.showContent()
                if (mData.size == 0) {
                    mView!!.transLayout.showEmpty()
                }*/
                /**
                 * 查询总时长
                 */
            }

            override fun onRecommend(bean: BaseBean?) {

            }

            override fun onUnRecommend(bean: BaseBean?) {
            }

            override fun onthumb(bean: BaseBean?) {
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
                commentBean?.let {
                    it.dialog_num = 1
                    it.chat_id = from_id
                    adapter.notifyItemChanged(mData.indexOf(it))
                }
                mView!!.transLayout.showContent()
            }
        })
        adapter = object : QuickAdapter<BaseBean>(activity, R.layout.item_voice, mData) {
            var cache: MutableList<BaseAdapterHelper> = ArrayList()

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseBean?) {
                val statusText = helper!!.getView(R.id.linearStatusText) as LinearStatusText
                statusText.visibility = View.VISIBLE
                helper.getView(R.id.tv_Sub).visibility = View.GONE
                glideUtil.loadGlide(item!!.user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_UserName)?.text = item.user.nick_name
                helper.getTextView(R.id.tvTime)?.text = resources.getString(R.string.string_home_hint_2) + TimeUtils.getInstance().paserWorl(activity, item.shared_at)
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tv_Action).visibility = if (TextUtils.isEmpty(item.topic_name)) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                (helper.getView(R.id.imageGroup) as ImageGroupView).setData(item.img_list)
                helper.getView(R.id.tv_Interested).visibility = View.GONE
                if (!TextUtils.isEmpty(item.first_share_voice) && item.first_share_voice == "1" && TextUtils.isEmpty(item.is_shared)) {
                    helper.getView(R.id.tv_first_share_world).visibility = View.VISIBLE
                } else if (item.may_interested == 1) {
                    helper.getView(R.id.tv_first_share_world).visibility = View.GONE
                } else {
                    helper.getView(R.id.tv_first_share_world).visibility = View.GONE
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
                if (!TextUtils.isEmpty(item.is_shared)) {//是自己
                    helper.getImageView(R.id.iv_Thumb).isSelected = item.is_shared == "1"
                    helper.getView(R.id.tv_Sub).visibility = View.VISIBLE
                    statusText.visibility = View.GONE
                    helper.getImageView(R.id.iv_Thumb).setImageResource(R.drawable.selector_share_world)
                    if (item.is_shared == "1") {
                        helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_unshare_world)
                    } else {
                        helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_share_world)
                    }
                    helper.getTextView(R.id.tv_Recommend).text = resources.getString(R.string.string_echoing) + if (item.chat_num <= 0) {
                        ""
                    } else {
                        " " + item.chat_num
                    }
                } else {
                    helper.getImageView(R.id.iv_Thumb).isSelected = item.is_collected == 1
                    helper.getView(R.id.tv_Sub).visibility = View.GONE
                    helper.getImageView(R.id.iv_Thumb).setImageResource(R.drawable.selector_thumb)
                    helper.getTextView(R.id.tv_Echo)?.text = resources.getString(R.string.string_gongming)
                    if (item.dialog_num <= 0) {
                        helper.getTextView(R.id.tv_Recommend).text = resources.getString(R.string.string_echoing)
                    } else {
                        helper.getTextView(R.id.tv_Recommend).text = "${resources.getString(R.string.string_Talks)} " + item.dialog_num
                    }
                    statusText.isSelected = item.friend_status == 0
                }
                (helper.getView(R.id.imageGroup) as ImageGroupView).setOnClickViewListener {
                    startActivity(Intent(activity, ShowPicActivity::class.java)
                            .putExtra("index", it)
                            .putExtra("data", item.img_list)
                    )
                    activity!!.overridePendingTransition(R.anim.act_enter_alpha, 0)
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    startActivity(Intent(activity, TopicResultActivity::class.java)
                            .putExtra("tag", item.topic_name)
                            .putExtra("tagId", item.topic_id.toString())
                    )
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
                            queryPermission(item, mView!!.transLayout, AppTools.fastJson(item.user_id.toString(), 1, item.voice_id))
                        } else {
                            startActivity(Intent(activity, TalkListActivity::class.java)
                                    .putExtra("voice_id", item.voice_id)
                                    .putExtra("chat_id", item.chat_id)
                                    .putExtra("uid", item.user_id.toString())
                            )
                        }
                    }
                }
                /**
                 * 请求添加好友
                 */
                statusText.setOnClickListener {
                    if (statusText.isSelected)
                        requestFriends(item, it)
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared)) {//共享
                        queryCount(item, helper.getTextView(R.id.tv_Echo), helper.getView(R.id.iv_Thumb))
                    } else {                //共鳴
                        if (item.isNetStatus) {
                            return@setOnClickListener
                        }
                        if (item.is_collected == 1) {
                            item.isNetStatus = true
//                            mView!!.transLayout.showProgress()
                            unThumb(item, helper.getView(R.id.iv_Thumb))
                        } else {
                            mView!!.transLayout.showProgress()
                            tuhmb(item, helper.getView(R.id.iv_Thumb))
                        }
                    }
                }
                helper.getView(R.id.cardView).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("url", item.user.avatar_url).putExtra("id", item.user.id))
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (!TextUtils.isEmpty(item.is_shared))
                        DialogMore(activity!!).setPrivacyStatus(item.is_private).setOnClickListener(View.OnClickListener {
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
                                R.id.tv_Self -> {//切换是否是自己可见
                                    DialogPrivacy(activity!!).setOnClickListener(View.OnClickListener {
                                        mView!!.transLayout.showProgress()
                                        setVoicePrivacy(item)
                                    }).show()
                                }
                                R.id.tv_add_album -> {
                                    startActivity(Intent(activity, DialogAddAlbumActivity::class.java).putExtra("voiceId", item.voice_id))
                                }
                            }
                        }).show()
                    else
                        DialogReport(activity!!).setResource(item.resource_type, item.subscription_id != 0).setOnClickListener(View.OnClickListener {
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
                                                mView!!.transLayout.showProgress()
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
                                            1 -> resources.getString(R.string.string_follow_book)
                                            1 -> resources.getString(R.string.string_follow_song)
                                            else -> resources.getString(R.string.string_follow_movies)
                                        }), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                            deletedSubscriber(item)
                                        }).show()
                                    }
                                }
                            }
                        }).show()
                }
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                voiceProgress.data = item
                voiceProgress/*.findViewById<View>(R.id.viewSeekProgress)*/.setOnClickListener {
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

                val seekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
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
                        /**
                         * 滑动停止
                         */
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

            override fun changeStatue(isSelect: Boolean) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                for (helper in cache) {
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceProgress
                        voiceProgress.updateProgress(currentPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            private fun download(helper: BaseAdapterHelper, item: BaseBean) {
                try {
                    if (TextUtils.isEmpty(item.voice_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }
                    playUrl = AppTools.getSuffix(item.voice_url)
                    helper.getView(R.id.play).isSelected = !helper.getView(R.id.play).isSelected
                    val file = getDownFilePath(item.voice_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.bean = item
                        audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(activity, item.voice_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                                    return@downFile
                                if (o.toString().contains(playUrl!!, true)) {
                                    audioPlayer.setDataSource(o.toString())
                                    audioPlayer.bean = item
                                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, {
                            showToast(VolleyErrorHelper.getMessage(it))
                        })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onCompletion() {
                            item.isPlaying = false
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            progressHandler.stop()
                            changeStatue(false)
                        }

                        override fun onInterrupt() {
                            item.isPlaying = false
                            (helper.getView(R.id.voiceProgress) as VoiceProgress).finish()
                            progressHandler.stop()
                        }

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            addPlays(item, helper.getTextView(R.id.tv_Sub)) {}
                            playBean = item
                            item.isPlaying = true
                            progressHandler.start()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        mView!!.recyclerView.adapter = adapter
        loadMoreView = LayoutInflater.from(activity).inflate(R.layout.loadmore_personal, mView!!.recyclerView, false)
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadMoreView)
        request(0)
    }

    override fun initEvent() {
        mView!!.transLayout.findViewById<View>(R.id.tvEmpty).setOnClickListener {
            DialogAlbumHowOperator(activity!!).setTitle(resources.getString(R.string.string_whats_personal)).show()
        }
        loadMoreView.findViewById<View>(R.id.tvWhatsPersonal).setOnClickListener {
            DialogAlbumHowOperator(activity!!).setTitle(resources.getString(R.string.string_whats_personal)).show()
        }
        adapter.setOnLoadListener {
            request(0)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            request(0)
        }
    }

    private fun requestFriends(bean: BaseBean, view: View) {
        mView!!.transLayout.showProgress()
        val infoData = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("toUserId", bean.user_id.toString())
                .build()
        OkClientHelper.post(activity, "users/${infoData.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.friend_status = 1
                    view.isSelected = false
                    if (SPUtils.getInt(activity, IConstant.TOTALLENGTH + infoData.user_id, 0) == 0) {
                        activity?.let { DialogCreateVoice(it).show() }
                    } else if (SPUtils.getBoolean(activity, IConstant.STRANGEVIEW + infoData.user_id, false)) {
                        activity?.let {
                            DialogUserSet(it).setOnClickListener(View.OnClickListener {
                                mView!!.transLayout.showProgress()
                                addWhiteBlack(bean.user_id.toString())
                            }).show()
                        }
                    }
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                    } else
                        showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        })
    }

    private var userCount = 0
    override fun request(flag: Int) {
        OkClientHelper.get(activity, "voice/personalise", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                mData.clear()
                if (result.data != null) {
                    if (audioPlayer.isPlaying) {
                        progressHandler.stop()
                        audioPlayer.stop()
                    }
                    for (item in result.data) {
                        mData.add(item)
                    }
                }
                adapter.notifyDataSetChanged()
                if (mData.size == 0) {
                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                    mView!!.transLayout.showEmpty()
                } else {
                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                    mView!!.transLayout.showContent()
                }
                mView!!.swipeRefresh.isRefreshing = false
                this@PersonalFragment.parentFragment?.fragmentManager?.fragments?.let {
                    it.forEach { frag ->
                        if (frag is WorldPageFrag) {
                            val tempList = arrayListOf<String>()
                            for (mDatum in mData) {
                                if (!tempList.contains(mDatum.user_id.toString())) {
                                    tempList.add(mDatum.user_id.toString())
                                }
                            }
                            frag.setPersonalCount(tempList.size)
                            tempList.clear()
                            return@forEach
                        }
                    }
                }
            }

            override fun onFailure(any: Any?) {
                if (AppTools.isNetOk(activity)) {
                    showToast(any.toString())
                } else {
                    mData.clear()
                    adapter.notifyDataSetChanged()
                    mView!!.transLayout.showEmpty()
                }
                mView!!.swipeRefresh.isRefreshing = false
            }
        }, "V4.0")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisiblePage || !isResumed) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("Personal : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}