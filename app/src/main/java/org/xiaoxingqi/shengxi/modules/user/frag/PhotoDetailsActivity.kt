package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.os.Message
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.github.chrisbanes.photoview.PhotoView
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_photo_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.MODE_EARPIECE
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogSave
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.BaseImg
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.VoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.echoes.TalkListActivity
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.home.DynamicDetailsActivity
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException


/**
 * 相册中展示图片
 */
class PhotoDetailsActivity : BaseNormalActivity() {
    private lateinit var audioPlayer: AudioPlayer
    private var full = false
    private var isAniming = false
    private val imageViews: List<View> by lazy { imageViews() }
    private var mData: ArrayList<BaseImg>? = null
    private var lastId: String = ""
    private var preCurrent = 0
    private var isDrag = false
    private var userId: String? = null
    private lateinit var adapter: ImgAdapter

    @Volatile
    private var isLoad = false
    private var isMore = true
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            tv_ProgressTime.text = AppTools.parseTime2Str(audioPlayer.currentPosition)
            if (!isDrag)
                seekBar.progress = ((audioPlayer.currentPosition.toFloat() / audioPlayer.duration.toFloat()) * 1000).toInt()
        }
    }

    override fun writeHeadSet(): Boolean {
        val headsetOn = audioPlayer.audioManager.isWiredHeadsetOn
        val a2dpOn = audioPlayer.audioManager.isBluetoothA2dpOn
        val scoOn = audioPlayer.audioManager.isBluetoothScoOn
        return headsetOn || a2dpOn || scoOn
    }

    override fun changSpeakModel(type: Int) {
        if (type == 1) {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
            }
        } else {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_photo_details
    }

    override fun initView() {
        /*初始化设置statrBur的高度*/
        val params = statusBar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        statusBar.layoutParams = params
        seekBar.max = 1000
    }

    override fun initData() {
        userId = intent.getStringExtra("uid")
        setItemOperator(object : ItemOperatorAdapter() {

            override fun onthumb(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onUnThumb(bean: BaseBean?) {
                transLayout.showContent()
            }

            override fun onFailure(e: Any?) {
                if (e is String) {
                    showToast(e)
                }
            }
        })
        lastId = intent.getStringExtra("lastId")
        val current = intent.getIntExtra("current", 0)
        mData = DataHelper.getInstance().imgBeans as ArrayList<BaseImg>
        audioPlayer = AudioPlayer(this)
        adapter = ImgAdapter()
        viewPager.adapter = adapter
        try {
            if (current < mData!!.size) {
                viewPager.setCurrentItem(current, false)
            }
        } catch (e: Exception) {
        }
        preCurrent = current
        if (mData != null) {
            changeInfo(mData!![current])
        }
    }

    /**
     * 展示声兮的相关信息
     */
    @SuppressLint("SetTextI18n")
    private fun changeInfo(bean: BaseImg) {
        tv_index.visibility = if (bean.voiceBean.img_list.size == 1) View.GONE else View.VISIBLE
        tv_time.text = TimeUtils.getInstance().paserTimeMachine(this@PhotoDetailsActivity, bean.voiceBean.created_at)
        tv_index.text = bean.title
        tv_Sub.text = if (bean.voiceBean.played_num == 0) resources.getString(R.string.string_Listener) else resources.getString(R.string.string_Listener) + " ${bean.voiceBean?.played_num}"
        if (!TextUtils.isEmpty(bean.voiceBean.is_shared)) {
            tv_Sub.visibility = if (bean.voiceBean.is_private == 1) View.GONE else View.VISIBLE
            iv_Thumb.isSelected = bean.voiceBean.is_shared == "1"
            iv_Privacy.visibility = if (bean.voiceBean.is_private == 1) View.VISIBLE else View.GONE
            iv_Thumb.setImageResource(R.drawable.selector_white_photo_share)
            tv_Echo.text = if (bean.voiceBean.is_shared == "1") resources.getString(R.string.string_unshare_world) else resources.getString(R.string.string_share_world)
            if (bean.voiceBean.is_private == 1) {
                iv_Thumb.isSelected = false
                tv_Echo.text = resources.getString(R.string.string_share_world)
            }
            tv_Recommend.text = resources.getString(R.string.string_echoing) + if (bean.voiceBean.chat_num <= 0) "" else " " + bean.voiceBean.chat_num
        } else {
            tv_Sub.visibility = View.GONE
            iv_Privacy.visibility = View.GONE
            iv_Thumb.isSelected = bean.voiceBean.is_collected == 1
            iv_Thumb.setImageResource(R.drawable.selector_white_photo_thumb)
            tv_Echo.text = resources.getString(R.string.string_gongming)
            tv_Recommend.text = if (bean.voiceBean.dialog_num <= 0) resources.getString(R.string.string_echoing) else "${resources.getString(R.string.string_Talks)} " + bean.voiceBean.dialog_num
        }
        tv_Time.text = try {
            val length = Integer.parseInt(bean.voiceBean.voice_len)
            AppTools.parseTime2Str((length * 1000).toLong())
        } catch (e: Exception) {
            "00:00"
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                changeInfo(mData!![position])
                if (mData!![position].voiceBean != mData!![preCurrent].voiceBean) {
                    if (audioPlayer.isPlaying) {
                        audioPlayer.stop()
                    }
                    seekBar.progress = 0
                    tv_ProgressTime.text = "00:00"
                }
                preCurrent = position
                if (position == mData!!.size - 1) {
                    if (mData!!.size > 10) {
                        if (isMore)
                            if (!isLoad) {
                                isLoad = true
                                request(0)
                            }
                    }
                }
            }
        })
        relativeThumb.setOnClickListener {
            if (!TextUtils.isEmpty(mData!![viewPager.currentItem].voiceBean.is_shared)) {//共享
                if (audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
                queryCount(mData!![viewPager.currentItem].voiceBean, tv_Echo, iv_Thumb, transLayout)
            } else {//共鳴
                if (mData!![viewPager.currentItem].voiceBean.isNetStatus) {
                    return@setOnClickListener
                }
                if (mData!![viewPager.currentItem].voiceBean.is_collected == 1) {
//                    transLayout.showProgress()
                    mData!![viewPager.currentItem].voiceBean.isNetStatus = true
                    unThumb(mData!![viewPager.currentItem].voiceBean, iv_Thumb)
                } else {
                    transLayout.showProgress()
                    thumb(mData!![viewPager.currentItem].voiceBean, iv_Thumb)
                }
            }
        }
        linear_Echoes.setOnClickListener {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
            if (!TextUtils.isEmpty(mData!![viewPager.currentItem].voiceBean.is_shared)) {
                startActivity(Intent(this@PhotoDetailsActivity, DynamicDetailsActivity::class.java)
                        .putExtra("id", mData!![viewPager.currentItem].voiceBean.voice_id)
                        .putExtra("uid", mData!![viewPager.currentItem].voiceBean.user.id)
                        .putExtra("isExpend", mData!![viewPager.currentItem].voiceBean.chat_num > 0)
                )
            } else {
                if (mData!![viewPager.currentItem].voiceBean.dialog_num == 0) {
                    queryPermission(mData!![viewPager.currentItem].voiceBean, transLayout, AppTools.fastJson(mData!![viewPager.currentItem].voiceBean.user_id.toString(), 1, mData!![viewPager.currentItem].voiceBean.voice_id))
                } else {
                    startActivity(Intent(this@PhotoDetailsActivity, TalkListActivity::class.java)
                            .putExtra("voice_id", mData!![viewPager.currentItem].voiceBean.voice_id)
                            .putExtra("chat_id", mData!![viewPager.currentItem].voiceBean.chat_id)
                            .putExtra("uid", mData!![viewPager.currentItem].voiceBean.user_id.toString())
                    )
                }
            }
        }
        iv_play.setOnClickListener {
            /**
             * 播放當前item
             */
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            } else {
                down(mData!![viewPager.currentItem].voiceBean)
            }
        }
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            @SuppressLint("SetTextI18n")
            override fun onCompletion() {
                tv_ProgressTime.text = "00:00"
                iv_play.setImageResource(R.mipmap.icon_photo_pause)
                seekBar.progress = 0
                progressHandler.stop()
            }

            override fun onInterrupt() {
                progressHandler.stop()
                iv_play.setImageResource(R.mipmap.icon_photo_pause)
            }

            override fun onPrepared() {
                try {
                    addPlays(mData!![viewPager.currentItem])
                    audioPlayer.seekTo((seekBar.progress / 1000f * Integer.parseInt(mData!![viewPager.currentItem].voiceBean.voice_len) * 1000).toInt())
                } catch (e: Exception) {
                }
                iv_play.setImageResource(R.mipmap.icon_photo_play)
                progressHandler.start()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDrag = true
            }

            @SuppressLint("SetTextI18n")
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isDrag = false
                if (audioPlayer.isPlaying) {
                    audioPlayer.seekTo((seekBar!!.progress / 1000f * audioPlayer.duration).toInt())
                } else {
                    try {
                        if (audioPlayer.duration > 0)
                            tv_ProgressTime.text = (seekBar!!.progress / 1000f * audioPlayer.duration).toString()
                        else {
                            tv_ProgressTime.text = AppTools.parseTime2Str((seekBar!!.progress / 1000f * Integer.parseInt(mData!![viewPager.currentItem].voiceBean.voice_len)).toLong() * 1000)
                        }
                    } catch (e: Exception) {
                        tv_ProgressTime.text = "00:00"
                    }
                }
            }
        })
        iv_more.setOnClickListener {
            DialogSave(this).setOnClickListener(View.OnClickListener {
                /*
                保存当前图片
                 */
                save(mData!![viewPager.currentItem].img_url)
            }).show()
        }
    }

    private fun imageViews() = arrayListOf<View>().apply {
        for (index in 1..4) {
            add(View.inflate(this@PhotoDetailsActivity, R.layout.layout_show_pic, null) as RelativeLayout)
        }
    }

    /**
     * 下载播放音频
     */
    private fun down(voiceBean: BaseBean) {
        if (TextUtils.isEmpty(voiceBean.voice_url)) {
            showToast("路径出错")
            return
        }
        val file = getDownFilePath(voiceBean.voice_url)
        if (file.exists()) {
            audioPlayer.setDataSource(file.absolutePath)
            if (currentMode == MODE_EARPIECE)
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
            else
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        } else {
            OkClientHelper.downFile(this, voiceBean.voice_url, { o ->
                try {
                    if (null == o) {
                        showToast(resources.getString(R.string.string_error_file))
                        return@downFile
                    }
                    audioPlayer.setDataSource(o.toString())
                    if (currentMode == MODE_EARPIECE)
                        audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                    else
                        audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }, { volleyError ->
                showToast(VolleyErrorHelper.getMessage(volleyError))
            })
        }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "users/$userId/voices?moduleId=3&lastId=$lastId", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                if (result.data != null && result.data.size > 0) {
                    for (item in result.data) {
                        if (item.img_list != null && item.img_list.size > 0) {
                            for (bean in item.img_list) {
                                val baseImg = BaseImg()
                                baseImg.img_url = bean
                                baseImg.voiceBean = item
                                baseImg.title = "${item.img_list.indexOf(bean) + 1}/${item.img_list.size}"
                                mData!!.add(baseImg)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                    lastId = result.data[result.data.size - 1].voice_id
                    if (result.data.size < 10) {
                        isMore = false
                    }
                }
                isLoad = false
            }

            override fun onFailure(any: Any?) {
                isLoad = false
            }
        })
    }

    /**
     * 添加播放次数
     */
    fun addPlays(bean: BaseImg) {
        OkClientHelper.get(this, "users/${bean.voiceBean.user_id}/voices/${bean.voiceBean.voice_id}", BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.voiceBean.played_num++
                    if (mData!![viewPager.currentItem].voiceBean == bean.voiceBean) {
                        changeInfo(bean)
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }


    private fun anim() {
        appbar.animate()
                .translationY(if (!full) 0f else -appbar.height.toFloat())
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()

        val anim = if (relative_menu.y == transLayout.height.toFloat()) {
            ObjectAnimator.ofFloat(relative_menu, "translationY", 0f).setDuration(200)
        } else {
            ObjectAnimator.ofFloat(relative_menu, "translationY", AppTools.dp2px(this, 77).toFloat()).setDuration(220)
        }
        anim.interpolator = DecelerateInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                isAniming = false
            }

            override fun onAnimationStart(animation: Animator?) {
                isAniming = true
            }
        })
        anim.start()
    }

    private fun full() {
        if (isAniming) {
            return
        }
        full = !full
        anim()
    }

    private inner class ImgAdapter : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return if (mData == null) 0 else mData!!.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val view = imageViews[position.rem(4)]
            val img = view.findViewById<PhotoView>(R.id.showIamgeView)
            val progressBar = view.findViewById<View>(R.id.progress)
            val scaleImageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView)
            Glide.with(this@PhotoDetailsActivity)
                    .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(mData!![position].img_url)))
                    .downloadOnly()
                    .load(mData!![position].img_url)
//                    .error(Glide.with(this@PhotoDetailsActivity)
//                            .applyDefaultRequestOptions(RequestOptions()
//                                    .signature(ObjectKey(if (mData!![position].img_url.contains("?")) mData!![position].img_url.substring(0, mData!![position].img_url.indexOf("?")) else mData!![position].img_url)))
//                            .load(if (mData!![position].img_url.contains("?")) mData!![position].img_url.substring(0, mData!![position].img_url.indexOf("?")) else mData!![position].img_url))
                    .listener(object : RequestListener<File> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?, isFirstResource: Boolean): Boolean {
                            progressBar.visibility = View.GONE
                            val errorPath = mData!![position].img_url.substring(0, mData!![position].img_url.indexOf("?"))
                            Glide.with(this@PhotoDetailsActivity)
                                    .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(errorPath)))
                                    .downloadOnly()
                                    .load(errorPath).listener(object : RequestListener<File> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?, isFirstResource: Boolean): Boolean {
                                            return false
                                        }

                                        override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            val options = BitmapFactory.Options()
                                            options.inJustDecodeBounds = true
                                            BitmapFactory.decodeFile(resource?.absolutePath, options)
                                            if (options.outWidth < AppTools.getWindowsWidth(this@PhotoDetailsActivity) * AppTools.getPhoneDensity(this@PhotoDetailsActivity)
                                                    && options.outHeight < AppTools.getWindowsHeight(this@PhotoDetailsActivity) * 2) {
                                                img.visibility = View.VISIBLE
                                                scaleImageView.visibility = View.GONE
                                                Glide.with(this@PhotoDetailsActivity).load(resource)
                                                        .into(img)
                                            } else {
                                                scaleImageView.visibility = View.VISIBLE
                                                img.visibility = View.GONE
                                                scaleImageView.setImage(ImageSource.uri(resource?.absolutePath).tiling(true), ImageViewState(1f, PointF(), 0))
                                            }
                                            return false
                                        }
                                    }).preload()

                            return false
                        }

                        override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            progressBar.visibility = View.GONE
                            val options = BitmapFactory.Options()
                            /**
                             * 最关键在此，把options.inJustDecodeBounds = true;
                             * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
                             * 判断图片是用什么展示
                             * 获取当前手机的分辨率
                             */
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeFile(resource?.absolutePath, options)
                            if (options.outWidth < AppTools.getWindowsWidth(this@PhotoDetailsActivity) * AppTools.getPhoneDensity(this@PhotoDetailsActivity)
                                    && options.outHeight < AppTools.getWindowsHeight(this@PhotoDetailsActivity) * 2) {
                                img.visibility = View.VISIBLE
                                scaleImageView.visibility = View.GONE
                                Glide.with(this@PhotoDetailsActivity).load(resource)
                                        .into(img)
                            } else {
                                scaleImageView.visibility = View.VISIBLE
                                img.visibility = View.GONE
                                scaleImageView.setImage(ImageSource.uri(resource?.absolutePath).tiling(true), ImageViewState(1f, PointF(), 0))
                            }
                            return false
                        }
                    })
                    .preload()
            container.addView(view)
            view.setOnClickListener {
                full()
            }
            img.setOnClickListener {
                full()
            }
            scaleImageView.setOnClickListener {
                full()
            }
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val i = position % 4
            val view = imageViews[i]
            container.removeView(view)
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_RECORD) {
                data?.let {
                    val voice = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    LocalLogUtils.writeLog("TimeMachine : 资源上传成功,请求socket 发送", System.currentTimeMillis())
                    EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(commentBean?.user_id.toString(), 1, commentBean?.voice_id, voice, voiceLength)))
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(baseImg: String) {
        var path: String = baseImg
        if (baseImg.contains("?")) {
            path = baseImg.substring(0, baseImg.lastIndexOf("?"))
        }
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg") && !suffix.contains(".png") && !suffix.contains(".gif")) {
            suffix = "$suffix.jpg"
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        if (file.exists()) {
            showToast("图片已存在")
            return
        }
        transLayout.showProgress()
        object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(this@PhotoDetailsActivity)
                            .load(path)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(s: File?) {
                transLayout.showContent()
                if (s == null) {
                    showToast("图片保存失败")
                } else {
                    val bootfile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                    if (!bootfile.exists()) {
                        bootfile.mkdirs()
                    }
                    val file = File(bootfile.absolutePath + "/" + suffix)
                    FileUtils.copyFile(s, file)
                    //在手机相册中显示刚拍摄的图片
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(file)
                    mediaScanIntent.data = contentUri
                    sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    override fun finish() {
        /**
         * 更新最新的数据到前一个页面的相册
         */
        setResult(Activity.RESULT_CANCELED, Intent().putExtra("lastId", lastId))
        super.finish()
        audioPlayer.stop()
        progressHandler.stop()
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
                    val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (it.user_id.toString() == loginBean.user_id) {
                        it.chat_num++
                    } else {
                        it.dialog_num++
                    }
                    //TODO 当前界面如果是正在回声的item 则刷新界面
                    if (mData!![viewPager.currentItem].voiceBean.voice_id == it.voice_id) {
                        changeInfo(mData!![viewPager.currentItem])
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressHandler)
        mData = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun secondary(event: EventVoiceBean) {
        /**
         * 获取月份的key
         */
        mData?.let {
            for (bean in it) {
                if (bean.voiceBean.voice_id == event.voiceId) {
                    if (event.type == 1) {
                        bean.voiceBean.is_shared = event.isShare
                    } else if (event.type == 2) {
                        bean.voiceBean.is_collected = event.isCollected
                    } else if (event.type == 3) {
                        bean.voiceBean.is_private = event.isPrivacy
                    } else if (event.type == 4) {
                        if (TextUtils.isEmpty(bean.voiceBean.is_shared)) {//别人 则是dialog_num
                            bean.voiceBean.dialog_num = event.dialogNum
                        } else {//自己
                            bean.voiceBean.chat_num = event.dialogNum
                        }
                    }
                    changeInfo(bean)
                    break
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operatorEvent(event: OperatorVoiceListEvent) {
        if (event.type == 3) {
            /**
             * 删除集合中的数据
             */
            mData?.let {
                var indicator = -1
                for (index in it.indices) {
                    if (it[index].voiceBean.voice_id == event.voice_id) {
                        //记录当前的角标和数量  移除列表
                        indicator = index
                        break
                    }
                }
                if (indicator != -1) {
                    for (count in 1..it[indicator].voiceBean.img_list.size) {
                        mData!!.removeAt(indicator)
                    }
                    adapter.notifyDataSetChanged()
                    if (it.size == 0) {
                        finish()
                    }
                }
            }
        }
    }

}