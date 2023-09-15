package org.xiaoxingqi.shengxi.modules.listen.cheers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.alibaba.fastjson.JSON
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_cheers.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.CenterHintDialog
import org.xiaoxingqi.shengxi.dialog.DialogAlbumHowOperator
import org.xiaoxingqi.shengxi.dialog.DialogCheersPercent
import org.xiaoxingqi.shengxi.impl.CheersUserDataEvent
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.user.CropActivity
import org.xiaoxingqi.shengxi.utils.*
import skin.support.SkinCompatManager
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

const val REQUEST_CHEERS_VOICE = 0x00
private const val REQUEST_GALLERY_CODE = 0x01
private const val REQUEST_EDIT_IMG = 0x02

class CheersActivity : BaseNormalActivity() {

    private var userCheerData: UserCheersData.UserCheersBean? = null
    private val audioPlayer by lazy { AudioPlayer(this) }
    private var otherCheersData: UserCheersData.UserCheersBean? = null
    private var playBean: UserCheersData.UserCheersBean? = null
    private var isFirst = 1
    private var isChangeAvatar = false
    private var cheersTitles: List<String>? = null
    private var rate: Double = 0.0
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

    override fun getLayoutId(): Int {
        return R.layout.activity_cheers
    }

    override fun initView() {
        val params = viewStatus.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        viewStatus.layoutParams = params
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                viewStatus.setBackgroundColor(Color.WHITE)
            } else {//夜间模式
                viewStatus.setBackgroundColor(Color.parseColor("#181828"))
            }
        } else {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                viewStatus.setBackgroundColor(Color.parseColor("#cccccc"))
            } else {//夜间模式
                viewStatus.setBackgroundColor(Color.parseColor("#181828"))
            }
        }
        setStatusBarFontIconDark(TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
        val params1 = frameContainer.layoutParams
        val width = AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 38 * 2)
        params1.width = width
        val height = 425f * width / 285f
        params1.height = height.toInt()
        frameContainer.layoutParams = params1
        //125     259
        val loadingParams = cheersLoading.layoutParams as FrameLayout.LayoutParams
        loadingParams.topMargin = (125f * height / 425).toInt()
        cheersLoading.layoutParams = loadingParams
        val txtParams = tvHint.layoutParams as FrameLayout.LayoutParams
        txtParams.bottomMargin = (102 * height / 425).toInt()
        tvHint.layoutParams = txtParams
    }

    override fun initData() {
        val obj = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        glideUtil.loadGlide(obj.data.avatar_url, ivUser, 0, glideUtil.getLastModified(obj.data.avatar_url))
        glideUtil.loadGlide(obj.data.avatar_url, ivUserAvatar, 0, glideUtil.getLastModified(obj.data.avatar_url))
        cheersTitles = JSON.parseArray(String(assets.open("cheers.json").readBytes()), String::class.java)
        cheersTitles?.let {
            tvHint.text = it[Random.nextInt(it.size)]
        }
        hintAnimator()
        request(0)
        request(1)
        request(4)

    }

    private fun hintAnimator() {
        val anim = ObjectAnimator.ofFloat(tvHint, "alpha", 0f, 1f, 0f).setDuration(6000)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator?) {
                if (frameLoading.visibility != View.VISIBLE) {
                    anim.end()
                    anim.cancel()
                } else {
                    cheersTitles?.let {
                        tvHint.text = it[Random.nextInt(it.size)]
                    }
                }
            }
        })
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = ObjectAnimator.INFINITE
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.start()
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onCompletion() {
                playBean?.isPlaying = false
                rectPlay.visibility = View.GONE
                rectPlay.end()
            }

            override fun onInterrupt() {
                rectPlay.visibility = View.GONE
                playBean?.isPlaying = false
                rectPlay.end()
            }

            override fun onPrepared() {
                audioPlayer.seekTo(playBean?.pasuePosition!!)
                playBean!!.pasuePosition = 0
                playBean?.isPlaying = true
                rectPlay.visibility = View.VISIBLE
                rectPlay.start()
            }
        }
        relativePass.setOnClickListener {
            cheers(2)
        }
        relativeCheers.setOnClickListener {
            //首次如果用户没有录音 则直接展示录音界面
            if (userCheerData == null) {
                CenterHintDialog(this).setOnClickListener(View.OnClickListener {
                    startActivity<UserCheersCardActivity>("isOpen" to true)
                }).show()
            } else {
                cheers(1)
            }
        }
        ivUser.setOnClickListener {
            startActivity<UserCheersCardActivity>("data" to userCheerData)
        }
        btn_Back.setOnClickListener {
            finish()
        }
        framePlay.setOnClickListener {
            //播放音频
            otherCheersData?.let {
                playBean = it
                if (playBean!!.isPlaying) {
                    playBean!!.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                } else
                    down(it.recording_url)
            }
        }
        tvCheersPercent.setOnClickListener {
//            DialogCheersPercent(this).show()
            DialogAlbumHowOperator(this).setTitle(resources.getString(R.string.string_16) + "$rate%").show()

        }
    }

    private fun down(url: String) {
        val file = getDownFilePath(url)
        if (file.exists()) {
            audioPlayer.setDataSource(file.absolutePath)
            audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
        } else {
            OkClientHelper.downFile(this, url, { o ->
                try {
                    if (null == o) {
                        showToast(resources.getString(R.string.string_error_file))
                        return@downFile
                    }
                    audioPlayer.setDataSource(o.toString())
                    audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }, { showToast(VolleyErrorHelper.getMessage(it)) })
        }
    }

    /*
    更新语音
     */
    private fun updateUserCheers(formBody: FormBody) {
        transLayout.showProgress()
        OkClientHelper.post(this, "cheersRecordings", formBody, IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    transLayout.showProgress()
                    request(1)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.3")
    }

    /*
    匹配
     */
    private fun cheers(type: Int) {
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        OkClientHelper.post(this, "recordingCard/browseLog", FormBody.Builder()
                .add("toUserId", otherCheersData?.user_id ?: "")
                .add("flag", "$type")
                .add("recordingId", otherCheersData?.id
                        ?: "").build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                //请求下一条数据
                result as BaseRepData
                if (result.code == 0) {
                    //showLoading
                    cheersLoading.startLoading()
                    frameLoading.visibility = View.VISIBLE
                    hintAnimator()
                    request(0)
                } else {
                    showToast(result.msg)
                }
            }
        }, "V4.3")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 ->//进入队列获取数据
                OkClientHelper.get(this, "recordingCard/random?isFirst=$isFirst", CheersData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        transLayout.showContent()
                        relativePass.isEnabled = true
                        relativeCheers.isEnabled = true
                        result as CheersData

                        cheersLoading.stop()
                        frameLoading.visibility = View.GONE

                        if (result.data != null) {
                            //展示数据
                            otherCheersData = result.data.recording
                            if (relativeContent.visibility != View.VISIBLE)
                                relativeContent.visibility = View.VISIBLE
                            glideUtil.loadGlide(result.data.user.avatar_url, ivUserAvatar, 0, glideUtil.getLastModified(result.data.user.avatar_url))
                            if (!TextUtils.isEmpty(result.data.cover_url)) {
                                Glide.with(this@CheersActivity)
                                        .load(result.data.cover_url)
                                        .into(ivCheerBg)
                            } else {
                                ivCheerBg.setImageResource(R.drawable.drawable_cheers_default_bg)
                            }
                            if (linearButton.visibility != View.VISIBLE) {
                                linearButton.visibility = View.VISIBLE
                            }
                        } else {
                            transLayout.showEmpty()
                        }
                        isFirst = 0
                    }

                    override fun onFailure(any: Any?) {
                        if (isFirst != 1) {//第一次
                            if (linearButton.visibility != View.VISIBLE) {
                                linearButton.visibility = View.VISIBLE
                            }
                        }

                        cheersLoading.stop()
                        frameLoading.visibility = View.GONE

                        linearButton.isEnabled = true
                        transLayout.showEmpty()
                    }
                }, "V4.3")
            1 -> {//查询用户的cheers数据
                val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${login.user_id}/cheersRecording", UserCheersData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserCheersData
                        if (result.data != null) {
                            userCheerData = result.data
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showEmpty()
                    }
                }, "V4.3")
            }
            2 -> {//用户信息
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserInfoData
                        transLayout.showContent()
                        if (result.code == 0) {
                            val url = result.data.avatar_url
                            isChangeAvatar = true
                            glideUtil.loadGlide(url, ivUserAvatar, -1, glideUtil.getLastModified(url))
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            3 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "/users/${loginBean.user_id}/covers?coverName=cheers_cover", NewVersionCoverData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as NewVersionCoverData
                        result.data?.let {
                            Glide.with(this@CheersActivity)
                                    .load(it[0].coverUrl)
                                    .into(ivCheerBg)
                        }
                    }
                }, "V4.2")
            }
            4 -> OkClientHelper.get(this, "modules/cheers/statistics", IntegerRespData::class.java, object : OkResponse {
                override fun onFailure(any: Any?) {

                }

                override fun success(result: Any?) {
                    result as IntegerRespData
                    if (result.code == 0) {
                        tvCheersPercent.visibility = View.VISIBLE
                        rate = BigDecimal(result.data.rate * 100).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                        tvCheersPercent.text = "$rate%"
                    }
                }
            }, "V4.3")
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save() {
        val obj = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        var url = obj.data.avatar_url
        if (TextUtils.isEmpty(url)) {
            showToast(resources.getString(R.string.string_save_cover_fail_1))
            return
        }
        if (url.contains("?")) {
            url = url?.substring(0, url.lastIndexOf("?"))
        }
        transLayout.showProgress()
        object : AsyncTask<Void, Void, File>() {
            override fun doInBackground(vararg voids: Void): File? {
                return try {
                    Glide.with(this@CheersActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(System.currentTimeMillis())))
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
                    val resultFile = File(bootFile.absolutePath + "/" + "${UUID.randomUUID()}.png")
                    FileUtils.copyFile(s, resultFile)
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(resultFile)
                    mediaScanIntent.data = contentUri
                    sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCheersEvent(event: CheersUserDataEvent) {
        userCheerData = event.event
    }

    private fun updateUserAvatar(avatar: String) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formbody = FormBody.Builder()
                .add("avatarUri", avatar)
                .add("bucketId", AppTools.bucketId)
                .build()
        OkClientHelper.patch(this, "users/${loginBean.user_id}", formbody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    request(2)
                } else
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
                REQUEST_CHEERS_VOICE -> {
                    data?.let {
                        val length = it.getStringExtra("voiceLength")
                        val path = it.getStringExtra("voice")
                        updateUserCheers(FormBody.Builder().add("recordingUri", path).add("recordingLen", length).add("bucketId", AppTools.bucketId).build())
                    }
                }
                REQUEST_GALLERY_CODE -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<String>
                        if (result.size > 0) {
                            startActivityForResult(Intent(this, CropActivity::class.java).putExtra("path", result[0]), REQUEST_EDIT_IMG)
                        }
                    }
                }
                REQUEST_EDIT_IMG -> {
                    data?.let {
                        updateUserAvatar(it.getStringExtra("result"))
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
    }
}
