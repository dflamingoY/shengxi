package org.xiaoxingqi.shengxi.modules.user.userResource

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.frag_user_movies.view.*
import kotlinx.android.synthetic.main.view_empty_user_frag_movies.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.BaseObjectActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.SensorChangeMoodEvent
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.model.UserMoviesData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.alarm.downPlay
import org.xiaoxingqi.shengxi.modules.listen.movies.MovieActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.UserMoviesListDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.PlayMenuImageView
import skin.support.SkinCompatManager

class MoodMoviesFragment : BaseFragment() {

    private val mData by lazy { ArrayList<BaseSearchBean>() }
    private lateinit var adapter: QuickAdapter<BaseSearchBean>
    private var lastId: String? = ""
    private var userId: String? = null
    private var isSelf = false
    private var title: String? = ""
    private val audioPlayer by lazy { AudioPlayer(activity) }
    private var playBean: BaseSearchBean? = null
    private var isDrag = false
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {
        override fun handleMessage(msg: Message?) {
            if (!isDrag) {
                mView!!.seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
            }
            mView!!.tv_ProgressTime.text = AppTools.parseTime2Str(audioPlayer.currentPosition)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeSensorEvent(event: SensorChangeMoodEvent) {
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
                mView!!.seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else if (event.type == 2) {
            if (audioPlayer.isPlaying) {
                mView!!.seekBar.progress = (audioPlayer.currentPosition.toFloat() / audioPlayer.duration * 1000 + 0.5f).toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.frag_user_movies
    }

    override fun initView(view: View?) {
        view!!.relativeUserBottomPlayer.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view.relativeUserBottomPlayer, "translationY", AppTools.dp2px(context, 56).toFloat()).setDuration(0).start()
        view.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(activity!!, R.color.colorIndecators),
                ContextCompat.getColor(activity!!, R.color.colorMovieTextColor),
                ContextCompat.getColor(activity!!, R.color.color_Text_Black))
        view.transLayout.findViewById<View>(R.id.frameHomeUserResource).visibility = View.VISIBLE
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        adapter = object : QuickAdapter<BaseSearchBean>(activity, R.layout.item_user_movies, mData) {
            private var cache: MutableList<BaseAdapterHelper> = ArrayList()

            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: BaseSearchBean?) {
                helper!!.getView(R.id.ivPlay).visibility = View.VISIBLE
                helper.getView(R.id.tv_UserComment).visibility = View.GONE
                helper.getView(R.id.tvMore).visibility = View.VISIBLE
                Glide.with(this@MoodMoviesFragment)
                        .applyDefaultRequestOptions(RequestOptions().centerCrop()
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night))
                        .load(item!!.movie_poster)
                        .into(helper.getImageView(R.id.ivMovieCover))
                try {
                    if (item.released_at != 0 || TimeUtils.string2Long(item.released_date) == 0) {
                        val parseYyMm = TimeUtils.getInstance().paserYyMm(item.released_at)
                        if (parseYyMm.contains("-")) {
                            helper.getTextView(R.id.tv_Years).text = "（${parseYyMm.substring(0, parseYyMm.indexOf("-"))}）"
                        } else {
                            helper.getTextView(R.id.tv_Years).text = "（$parseYyMm）"
                        }
                    } else {
                        helper.getTextView(R.id.tv_Years).text = "（${item.released_date.substring(0, item.released_date.indexOf("-"))}）"
                    }
                } catch (e: Exception) {
                }
                helper.getTextView(R.id.tv_MovieName).text = item.movie_title
                val starring = AppTools.array2String(item.movie_starring)
                helper.getTextView(R.id.tv_MovieType).text = if (!TextUtils.isEmpty(starring)) {
                    starring + "/" + AppTools.array2String(item.movie_type) + "/" + item.movie_len + "分"
                } else {
                    AppTools.array2String(item.movie_type) + "/" + item.movie_len + "分"
                }
                helper.getTextView(R.id.tv_actors).text = AppTools.array2String(item.movie_area)
                helper.getView(R.id.ivPlay).isSelected = item.first_voice.isPlaying
                (helper.getView(R.id.ivPlay) as PlayMenuImageView).attchData(item)
                val position = helper.itemView.tag as Int
                helper.getView(R.id.viewLine).visibility = if (position != mData.size - 1) View.VISIBLE else View.GONE
                helper.getView(R.id.ivPlay).setOnClickListener {
                    if (audioPlayer.isPlaying) {
                        if (item == playBean) {
                            audioPlayer.stop()
                            helper.getView(R.id.ivPlay).isSelected = false
                        } else {//播放下一条
                            autoPlay(item)
                            mView!!.seekBar.progress = 0
                        }
                    } else {
                        autoPlay(item)
                    }
                    mView!!.relativeUserBottomPlayer.anim(320L)
                }
                helper.getView(R.id.tvMore).setOnClickListener {
                    if (audioPlayer.isPlaying) {
                        audioPlayer.stop()
                    }
                    startActivity(Intent(activity, UserMoviesListDetailsActivity::class.java)
                            .putExtra("resourceType", 1)
                            .putExtra("id", item.id)
                            .putExtra("uid", userId)
                            .putExtra("title", if (isSelf) "" else title)
                    )
                }
                if (!cache.contains(helper))
                    cache.add(helper)
            }

            override fun notifyBFootView(isAttach: Boolean) {
                cache.forEach {
                    (it.getView(R.id.ivPlay) as PlayMenuImageView).update()
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_white, mView!!.recyclerView, false))
        arguments?.let {
            title = it.getString("title")
            userId = it.getString("userId")
            val permission = it.getInt("permission")
            if (loginBean.user_id != userId) {
                mView!!.transLayout.tvDesc.text = if (it.getInt("relation") != 2 && permission == 2) {
                    mView!!.transLayout.showEmpty()
                    mView!!.swipeRefresh.isEnabled = false
                    resources.getString(R.string.string_resource_empty_7)
                } else {
                    request(0)
                    "ta${resources.getString(R.string.string_user_movies_1)}"
                }
            } else {
                request(0)
                isSelf = true
            }
        }
    }

    override fun initEvent() {
        mView!!.transLayout.findViewById<View>(R.id.tv_UploadPhoto).setOnClickListener {
            startActivity<MovieActivity>()
        }
        adapter.setOnLoadListener {
            request(0)
        }
        mView!!.swipeRefresh.setOnRefreshListener {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                playBean = null
            }
            request(0)
        }
        mView!!.ivPlays.setOnClickListener {
            //暂停或者播放
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            } else {
                //播放当前的缓存
                playBean?.let {
                    autoPlay(it)
                }
            }
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onPrepared() {
                playBean?.let {
                    it.first_voice.isPlaying = true
                    adapter.notifyBFootView(false)
                    audioPlayer.seekTo((mView!!.seekBar.progress / 1000f * Integer.parseInt(it.first_voice.voice_len) * 1000).toInt())
                }
                mView!!.ivPlays.isSelected = true
                progressHandler.start()
            }

            override fun onCompletion() {
                progressHandler.stop()
                playBean?.let {
                    if (!isSelf) {
                        it.first_voice.user_id = userId?.toInt() ?: 0
                        it.first_voice.voice_id = it.first_voice.id
                        tuhmb(it.first_voice, null, 5)
                    }
                    it.first_voice.isPlaying = false
                    adapter.notifyBFootView(false)
                    if (mData.indexOf(it) == mData.size - 2) {
                        if (!TextUtils.isEmpty(lastId)) {//请求加载更多数据
                            request(0)
                        }
                    }
                    try {
                        val next = mData.indexOf(it) + 1
                        autoPlay(mData[if (next >= mData.size) 0 else next])
                    } catch (e: Exception) {
                    }
                }
                mView!!.seekBar.progress = 0
            }

            override fun onInterrupt() {
                //异常停止 restory page
                playBean?.let {
                    it.first_voice.isPlaying = false
                    adapter.notifyBFootView(false)
                }
                progressHandler.stop()
                mView!!.ivPlays.isSelected = false
            }
        }
        mView!!.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDrag = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDrag = false
                if (audioPlayer.isPlaying) {
                    audioPlayer.seekTo((seekBar!!.progress / 1000f * audioPlayer.duration).toInt())
                } else {
                    try {
                        if (audioPlayer.duration > 0)
                            mView!!.tv_ProgressTime.text = (seekBar!!.progress / 1000f * audioPlayer.duration).toString()
                        else {
                            mView!!.tv_ProgressTime.text = AppTools.parseTime2Str((seekBar!!.progress / 1000f * Integer.parseInt(playBean?.first_voice?.voice_len)).toLong() * 1000)
                        }
                    } catch (e: Exception) {
                        mView!!.tv_ProgressTime.text = "00:00"
                    }
                }
            }
        })
    }

    /*
   展示当条资源信息
    */
    private fun showInfo(item: BaseSearchBean) {
        mView!!.tv_Time.text = AppTools.parseHMS(item.first_voice.voice_len.toInt())
        mView!!.ivPlays.isSelected = true
    }

    //列表自动循环播放
    private fun autoPlay(item: BaseSearchBean) {
        if (audioPlayer.isPlaying && playBean == item) {
            return
        }
        playBean?.let {
            if (it != item)
                mView!!.seekBar.progress = 0
            it.first_voice.isPlaying = false
            adapter.notifyBFootView(false)
        }
        mView!!.seekBar.max = 1000
        playBean = item
        showInfo(item)
        //下载播放
        activity!!.downPlay(item.first_voice.voice_url) {it, _ ->
            audioPlayer.setDataSource(it)
            if (BaseObjectActivity.currentMode == MODE_EARPIECE) {
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            } else
                audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "users/$userId/resources/1?lastId=$lastId", UserMoviesData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as UserMoviesData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                mView!!.transLayout.showContent()
                if (result.data?.list != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data.list)
                        adapter.notifyDataSetChanged()
                    } else {
                        if (result.data.list.size > 0) {
                            adapter.notifyItemChanged(mData.size - 1)
                            for (item in result.data.list) {
                                mData.add(item)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                        }
                    }
                    if (result.data.list.size >= 10) {
                        lastId = result.data.list[result.data.list.size - 1].rate_id
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    } else {
                        lastId = ""
                    }
                    mView!!.tv_AllCount.text = "共${result.data.total}部"
                }
                if (mData.size == 0) {
                    mView!!.transLayout.showEmpty()
                    mView!!.tv_AllCount.visibility = View.GONE
                } else {
                    mView!!.tv_AllCount.visibility = View.VISIBLE
                }
                mView!!.swipeRefresh.isRefreshing = false
            }

            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        }, "V3.2")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        audioPlayer.stop()
    }

}

fun View.anim(duration: Long) {
    if (this.top != 0)
        ObjectAnimator.ofFloat(this, "translationY", 0f).setDuration(duration).start()
}