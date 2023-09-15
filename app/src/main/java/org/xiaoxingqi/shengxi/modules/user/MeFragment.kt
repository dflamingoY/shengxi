package org.xiaoxingqi.shengxi.modules.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.frag_me.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.alarm.downPlay
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.user.frag.UserAchieveActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.overScroll.IOverScrollState
import org.xiaoxingqi.shengxi.wedgit.overScroll.OverScrollDecoratorHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutionException

class MeFragment : BaseFragment(), ITabClickCall {
    companion object {
        private const val REQUEST_WAVE = 0x03
    }

    private var visible = false

    override fun tabClick(isVisible: Boolean) {
        if (visible == isVisible) {
            return
        }
        visible = isVisible
        try {
            if (visible) {
                animatorButton?.let {
                    if (!it.isRunning)
                        it.start()
                }
                //需要切换当前的界面
            } else {
                animatorButton?.let {
                    if (it.isRunning) {
                        it.end()
                        randomCover()
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun doubleClickRefresh() {

    }

    private var userInfoData: UserInfoData? = null
    private var dialog: DialogSelectPhoto? = null
    private lateinit var transLayout: TransLayout
    private var playViewWave = true
    private var coverList: List<String>? = null
    override fun getLayoutId(): Int {
        return R.layout.frag_me
    }

    private lateinit var audioPlayer: AudioPlayer

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
        if (event.type == 1) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        } else if (event.type == 2) {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        }
    }

    private fun randomCover() {
        try {
            val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.getString(activity, IConstant.USER_CACHE_COVER_LIST + loginBean.user_id, null)?.let {
                it.split(",").filter { predicate ->
                    predicate != "," && predicate != ""
                }.let { list ->
                    if (list.isEmpty()) {
                        if (userInfoData?.let { info ->
                                    glideUtil.loadGlide(info.data.main_cover_photo_url, mView!!.iv_Cover, 0, glideUtil.getLastModified(info.data.main_cover_photo_url))
                                    mView!!.iv_Cover.setImageUrl(info.data.main_cover_photo_url)
                                    info.data.main_cover_photo_url
                                }.isNullOrEmpty()) {
                            mView!!.iv_Cover.setImageUrl(null)
                            Glide.with(this@MeFragment)
                                    .load(R.drawable.drawable_default_bg_1)
                                    .apply(RequestOptions().centerCrop())
                                    .into(mView!!.iv_Cover)
                        }
                        return
                    }
                    coverList = list
                    val url = list[Random().nextInt(list.size)]
                    if (!TextUtils.isEmpty(url)) {
                        Glide.with(activity)
                                .asDrawable()
                                .load(url)
                                .apply(RequestOptions().centerCrop().signature(ObjectKey(url)).error(R.drawable.drawable_default_bg_1))
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                        return false
                                    }

                                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                        mView!!.iv_Cover.setImageUrl(model.toString())
//                                        mView!!.iv_Cover.setImageDrawable(resource)
                                        return false
                                    }
                                }).into(mView!!.iv_Cover)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (SPUtils.getString(activity, IConstant.USER_THEME_LIGHT_MODEL + loginBean.user_id, "1") == "2") {
            mView!!.viewCoverLayer.visibility = View.INVISIBLE
        } else {
            mView!!.viewCoverLayer.visibility = View.VISIBLE
        }
        SPUtils.getString(activity, IConstant.USER_THEME_COVER_MODEL + loginBean.user_id, "0").let {
            mView!!.flowView.startView(when (it) {
                "1" -> IConstant.THEME_RAIN
                "2" -> IConstant.THEME_SNOW
                "3" -> IConstant.THEME_LEAVES
                "4" -> IConstant.THEME_FLOWER
                "5" -> IConstant.THEME_MAPLE
                else -> {
                    mView!!.flowView.stopView()
                    null
                }
            })
        }
        isShowDialog = true
        request(0)
        animatorButton?.start()
    }

    private var animatorButton: ObjectAnimator? = null
    override fun initView(view: View?) {
        transLayout = view!!.transLayout
        animatorButton = ObjectAnimator.ofFloat(view.iv_Home, "translationX", AppTools.dp2px(activity, 15).toFloat())
        animatorButton?.duration = 2000
        animatorButton?.repeatMode = ObjectAnimator.REVERSE
        animatorButton?.repeatCount = ObjectAnimator.INFINITE
        animatorButton?.interpolator = BounceInterpolator()
        /*val themeParams = view!!.ivTheme.layoutParams as RelativeLayout.LayoutParams
        themeParams.topMargin = AppTools.getStatusBarHeight(context) + AppTools.dp2px(context, 9)
        view!!.ivTheme.layoutParams = themeParams*/
        val setParams = view!!.linearSet.layoutParams as RelativeLayout.LayoutParams
        setParams.topMargin = AppTools.getStatusBarHeight(context) + AppTools.dp2px(context, 17)
        view!!.linearSet.layoutParams = setParams
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        audioPlayer = AudioPlayer(activity, null, object : OnPlayListenAdapter() {
            override fun onPrepared() {
                userInfoData!!.data.isPlaying = true
                anim(if (playViewWave) mView!!.iv_Play else mView!!.ivRandomPlayer)
            }

            override fun onCompletion() {
                userInfoData!!.data.isPlaying = false
                animator?.end()
                animator = null
            }

            override fun onInterrupt() {
                userInfoData!!.data.isPlaying = false
                animator?.end()
                animator = null
            }
        })
        /* val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
         SPUtils.getString(activity, IConstant.USER_CACHE_COVER_LIST + loginBean.user_id, null)?.let {
             it.split(",").filter { predicate ->
                 predicate != ","
             }.let { list ->
                 val url = list[Random().nextInt(list.size)]
                 Glide.with(activity)
                         .load(url)
                         .apply(RequestOptions().centerCrop())
                         .into(mView!!.iv_Cover)
             }
         }*/
        request(1)
        request(2)
    }

    private var isOpen = false
    override fun initEvent() {
        mView!!.ivFriends.setOnClickListener {
            startActivity<FriendsActivity>()
        }
        val scrollDecor = OverScrollDecoratorHelper.setUpStaticOverScroll(mView!!.iv_Cover, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        scrollDecor.setOverScrollUpdateListener { _, state, offset ->
            if (state == IOverScrollState.STATE_BOUNCE_BACK) {//空闲状态
                if (offset <= -50 && !isOpen) {
                    isOpen = true
                    isShowDialog = false
                    userInfoData?.let {
                        startActivity(Intent(activity, UserHomeActivity::class.java)
                                .putExtra("friendCount", userInfoData?.data?.friend_num)
                                .putExtra("id", userInfoData?.data?.user_id))
                    }
                }
            }
            if (offset == 0f) {
                isOpen = false
            }
        }
        mView!!.linearSet.setOnClickListener { startActivity(Intent(activity, SettingActivity::class.java)) }
        mView!!.tvUserName.setOnClickListener {
            userInfoData?.let {
                it.data?.let {
                    startActivity(Intent(activity, UserInfoActivity::class.java))
                }
            }
        }
        mView!!.tv_Desc.setOnClickListener {
            userInfoData?.let {
                it.data?.let {
                    startActivity(Intent(activity, UserInfoActivity::class.java))
                }
            }
        }
        mView!!.iv_Play.setOnClickListener {
            sendObserver()
            if (userInfoData == null)
                return@setOnClickListener
            animator?.let {
                //停止播放
                it.end()
                it.cancel()
            }
            animator = null
            if (audioPlayer.isPlaying && playViewWave) {
                audioPlayer.stop()
                return@setOnClickListener
            } else {
                audioPlayer.stop()
            }
            try {
                if (TextUtils.isEmpty(userInfoData!!.data.wave_url)) {
//                    if ((System.currentTimeMillis() - clickTime) < 500) {
//                        return@setOnClickListener
//                    }
//                    clickTime = System.currentTimeMillis()
//                    startActivityForResult(Intent(activity, RecordVoiceActivity::class.java)
//                            .putExtra("resourceType", "1")
//                            .putExtra("recordType", 2), REQUEST_WAVE)
//                    activity?.overridePendingTransition(0, 0)

                    showToast(resources.getString(R.string.string_empty_voice_1))
                    return@setOnClickListener
                }
                /* if (!SPUtils.getBoolean(activity, IConstant.IS_FIRST_PLAY_WAVE + userInfoData!!.data.user_id, false)) {
                     SPUtils.setBoolean(activity, IConstant.IS_FIRST_PLAY_WAVE + userInfoData!!.data.user_id, true)
                     showToast("当前正在播放封面独白")
                 }*/
                OkClientHelper.downWave(activity, userInfoData!!.data.wave_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downWave
                        }
                        if (!audioPlayer.isPlaying) {
                            audioPlayer.setDataSource(o.toString())
                            audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                        }
                        playViewWave = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { volleyError -> showToast(VolleyErrorHelper.getMessage(volleyError)) })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        mView!!.iv_Cover.setOnLongClickListener {
            userInfoData?.let {
                if (null != dialog) {
                    if (dialog?.isShowing!!) {
                        dialog!!.dismiss()
                    }
                }
                dialog = DialogSelectPhoto(activity!!).hideAction(true).hindOther(false).setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                    override fun itemView(view: View) {
                        when (view.id) {
                            R.id.tv_Save -> {
                                save(mView!!.iv_Cover.url)
                            }
                        }
                    }
                })
                if (isShowDialog)
                    dialog?.let { dia ->
                        if (!dia.isShowing) {
                            dia.show()
                        }
                    }
            }
            false
        }
        mView!!.iv_Home.setOnClickListener {
            if (System.currentTimeMillis() - clickTime < 1000) {
                return@setOnClickListener
            }
            clickTime = System.currentTimeMillis()
            userInfoData?.let {
                startActivity(Intent(activity, UserHomeActivity::class.java)
                        .putExtra("friendCount", it.data.friend_num)
                        .putExtra("id", it.data.user_id))
            }
        }
        mView!!.iv_Cover.setOnClickListener {
            userInfoData?.let {
                if (null != dialog) {
                    if (dialog?.isShowing!!) {
                        dialog!!.dismiss()
                    }
                }
                dialog = DialogSelectPhoto(activity!!).hideAction(true).hindOther(false).setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                    override fun itemView(view: View) {
                        when (view.id) {
                            R.id.tv_Save -> {
                                save(mView!!.iv_Cover.url)
                            }
                        }
                    }
                })
                if (isShowDialog) {
                    dialog?.let { dia ->
                        if (!dia.isShowing) {
                            dia.show()
                        }
                    }
                }
            }
        }
        mView!!.ivTheme.setOnClickListener {
            startActivity(Intent(activity, DialogUserThemeActivity::class.java))
            activity?.overridePendingTransition(R.anim.operator_top_2_bottom, 0)
        }
        mView!!.ivRandomPlayer.setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying && !playViewWave) {
                audioPlayer.stop()
                return@setOnClickListener
            } else {
                audioPlayer.stop()
            }
            animator?.let {
                //停止播放
                it.end()
                it.cancel()
            }
            animator = null
            request(3)
        }
        mView!!.ivAchieve.setOnClickListener {
            startActivity<UserAchieveActivity>()
        }
    }

    private var clickTime = 0L

    private var animator: ObjectAnimator? = null

    private fun anim(view: View) {
        animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        animator?.duration = 15000
        animator?.interpolator = LinearInterpolator()
        animator?.repeatCount = ObjectAnimator.INFINITE
        animator?.start()
    }

    override fun onPause() {
        super.onPause()
        if (null != dialog) {
            dialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        }
        animatorButton?.end()
    }

    /**
     * 避免滑动造成的点击事件
     */
    private var isShowDialog = true

    @SuppressLint("StaticFieldLeak")
    private fun save(url: String?) {
        if (TextUtils.isEmpty(url)) {
            showToast(resources.getString(R.string.string_save_cover_fail_1))
            return
        }
        var suffix = url!!.substring(url.lastIndexOf("/") + 1)
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
                return try {
                    Glide.with(activity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(url)))
                            .downloadOnly()
                            .load(url)
                            .submit()
                            .get()
                } catch (e: Exception) {
                    null
                }
            }

            override fun onPostExecute(s: File?) {
                transLayout.showContent()
                if (null == s) {
                    showToast("图片保存失败")
                } else {
                    val bootFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                    if (!bootFile.exists()) {
                        bootFile.mkdirs()
                    }
                    val file = File(bootFile.absolutePath + "/" + suffix)
                    FileUtils.copyFile(s, file)
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(file)
                    mediaScanIntent.data = contentUri
                    activity!!.sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    override fun request(flag: Int) {
        val infoData = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "users/${infoData.user_id}", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        userInfoData = result as UserInfoData
                        SPUtils.setInt(activity, IConstant.TOTALLENGTH + result.data.user_id, result.data.voice_total_len)
                        PreferenceTools.saveObj(activity, IConstant.USERCACHE, result)
                        view!!.tvUserName.text = result.data.nick_name
                        view?.tv_Desc?.text = if (TextUtils.isEmpty(result.data.self_intro)) {
                            resources.getString(R.string.string_empty_desc_me_1)
                        } else {
                            result.data.self_intro
                        }
                        if (!TextUtils.isEmpty(result.data.main_cover_photo_url)) {
                            if (coverList == null || coverList.isNullOrEmpty()) {//无数据 则展示
                                glideUtil.loadGlide(result.data.main_cover_photo_url, mView!!.iv_Cover, 0, glideUtil.getLastModified(result.data.main_cover_photo_url))
                                mView!!.iv_Cover.setImageUrl(result.data.main_cover_photo_url)
                            }
                        }
                        if (mView!!.viewCoverLayer.visibility != View.INVISIBLE) {
                            mView!!.viewCoverLayer.visibility = View.VISIBLE
                        }
                        mView!!.transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.transLayout.showContent()
                    }
                })
            }
            1 -> {//查询用户的高亮设置
                OkClientHelper.get(activity, "users/${infoData.user_id}/settings?settingName=cover_brightness&settingTag=moodbook", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        result.data?.let { data ->
                            //用户的默认是否打开 遮罩层
                            SPUtils.setString(activity, IConstant.USER_THEME_LIGHT_MODEL + infoData.user_id, data.setting_value.toString())
                            if (data.setting_value == 2) {
                                mView!!.viewCoverLayer.visibility = View.INVISIBLE
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            2 -> {//获取所有的封面
                OkClientHelper.get(activity, "users/${infoData.user_id}/covers?coverName=moodbook_cover", NewVersionCoverData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionCoverData
                        if (result.code == 0) {
                            result.data?.let {
                                coverList = it.map { bean ->
                                    bean.coverUrl
                                }
                                //随机展示图片
                                val url = it[Random().nextInt(it.size)].coverUrl
                                Glide.with(activity)
                                        .load(url)
                                        .apply(RequestOptions().centerCrop().placeholder(0).error(R.drawable.drawable_default_bg_1))
                                        .into(mView!!.iv_Cover)
                                mView!!.iv_Cover.setImageUrl(url)
                                SPUtils.setString(activity, IConstant.USER_CACHE_COVER_LIST + infoData.user_id, it.joinToString(",") { bean -> bean.coverUrl })
                            }
                            if (result.data == null || result.data.isEmpty()) {
                                mView!!.iv_Cover.setImageResource(R.drawable.drawable_default_bg_1)
                            }
                        } else {
                            if (userInfoData?.let {
                                        if (!TextUtils.isEmpty(it.data.main_cover_photo_url)) {
                                            glideUtil.loadGlide(it.data.main_cover_photo_url, mView!!.iv_Cover, 0, glideUtil.getLastModified(it.data.main_cover_photo_url))
                                            mView!!.iv_Cover.setImageUrl(it.data.main_cover_photo_url)
                                        } else {
                                            Glide.with(this@MeFragment)
                                                    .load(R.drawable.drawable_default_bg_1)
                                                    .apply(RequestOptions().centerCrop())
                                                    .into(mView!!.iv_Cover)
                                        }
                                        it
                                    } == null) {
                                Glide.with(this@MeFragment)
                                        .load(R.drawable.drawable_default_bg_1)
                                        .apply(RequestOptions().centerCrop())
                                        .into(mView!!.iv_Cover)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        try {
                            Glide.with(activity?.applicationContext)
                                    .load(R.drawable.drawable_default_bg_1)
                                    .apply(RequestOptions().centerCrop())
                                    .into(mView!!.iv_Cover)
                        } catch (e: Exception) {
                        }
                    }
                }, "V4.2")
            }
            3 -> {
                OkClientHelper.get(activity, "users/${infoData.user_id}/voices/random", DynamicDatailData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as DynamicDatailData
                        if (result.code == 0 && result.data != null) {
                            if (!TextUtils.isEmpty(result.data.voice_url)) {
                                if (!SPUtils.getBoolean(activity, IConstant.IS_FIRSE_PLAY_MINI_MACHINE + userInfoData!!.data.user_id, false)) {
                                    SPUtils.setBoolean(activity, IConstant.IS_FIRSE_PLAY_MINI_MACHINE + userInfoData!!.data.user_id, true)
                                    showToast("当前正在播放mini时光机")
                                }
                                activity!!.downPlay(result.data.voice_url) { it, _ ->
                                    playViewWave = false
                                    audioPlayer.setDataSource(it)
                                    audioPlayer.start(if (SPUtils.getBoolean(activity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
//                                    if (TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()) != SPUtils.getString(activity, IConstant.FIRST_PUSH_ACHIEVEMENT + userInfoData?.data?.user_id, "")) {
//                                        //提交成就播放
//                                        userInfoData?.data?.user_id?.let { it1 -> pushTravel(it1) }
//                                    }
                                }
                            }
                        } else {
                            showToast("还没有心情")
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }

        }
    }

    private fun updateUserinfo(formBody: FormBody) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(activity, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                request(0)
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_WAVE -> data?.let {
                    val voicePath = data?.getStringExtra("voice")
                    val voiceLength = data?.getStringExtra("voiceLength")
                    val formBody = FormBody.Builder()
                            .add("waveUri", voicePath)
                            .add("waveLen", voiceLength)
                            .build()
                    mView!!.transLayout.showProgress()
                    updateUserinfo(formBody)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!visible || !isResumed) {
            userInfoData?.let {
                if (it.data.isPlaying) {
                    it.data.isPlaying = false
//                    mView!!.iv_Play.stop()
                }
                animator?.end()
                animator = null
            }
            audioPlayer.stop()
        }
    }

    /**
     * 更新用户的信息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateUserInfo(event: UpdateUserInfoEvent) {
        request(0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateCover(event: IUpdateUserCoverEvent) {
        randomCover()
    }

    class UpdateUserInfoEvent

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}