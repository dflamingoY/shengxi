package org.xiaoxingqi.shengxi.modules.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Environment
import android.os.Message
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_user_info.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogCommitPwd
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.AdminLoginData
import org.xiaoxingqi.shengxi.model.BaseAnimBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.adminManager.ManagerAdminActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.CropBgActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import java.io.File
import java.io.IOException

class UserInfoActivity : BaseAct() {
    companion object {
        private const val RENAMECODE = 0x01
        private const val EDITDESCCODE = 0x02
        private const val EDITAVATARCODE = 0x03
        private const val EDIT_WAVE = 0x04
        private const val OPEN_GALLERY = 0x05
        private const val REQUEST_CROP_BG = 0x06
    }

    private var userInfoData: UserInfoData? = null
    private lateinit var audioPlayer: AudioPlayer

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
                voiceAnimProgress.data.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                voiceAnimProgress.data.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_user_info
    }

    override fun initView() {

    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        val infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        try {
            if (IConstant.userAdminArray.contains(infoData.data.user_id)) {
                tvSearch.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
        }
        infoData?.let {
            if (TextUtils.isEmpty(it.data.wave_url)) {
                voiceAnimProgress.visibility = View.GONE
                tv_Record.text = resources.getString(R.string.string_record_cover_voice)
            }
        }
        request(3)
    }

    val mHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {
        override fun handleMessage(msg: Message) {
            val currentPosition = audioPlayer.currentPosition.toInt()
            voiceAnimProgress?.changeProgress(currentPosition)
        }
    }

    override fun initEvent() {
        tvSearch.setOnClickListener {
            dialogPwd = DialogCommitPwd(this).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                loginAdmin(pwd)
            })
            dialogPwd?.show()
        }
        moreUserName.setOnClickListener {
            userInfoData?.let {
                startActivityForResult(Intent(this, EditUserNameActivity::class.java)
                        .putExtra("name", moreUserName.text), RENAMECODE)
            }
        }
        relative_Avatar.setOnClickListener {
            userInfoData?.let {
                startActivityForResult(Intent(this, UserAvatarActivity::class.java)
                        .putExtra("img", it.data.avatar_url)
                        .putExtra("isSelf", true)
                        .putExtra("nickname", it.data.nick_name)
                        , EDITAVATARCODE)
            }
        }
        relative_Desc.setOnClickListener {
            userInfoData?.let {
                startActivityForResult(Intent(this, EditDescActivity::class.java).putExtra("desc", if (tv_Desc.isSelected) "" else tv_Desc.text.toString().trim()), EDITDESCCODE)
            }
        }
        btn_Back.setOnClickListener {
            if (!isChange()) {
                request(4)
            } else
                finish()
        }
        tv_Record.setOnClickListener {
            userInfoData?.let {
                startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                        .putExtra("resourceType", "1")
                        .putExtra("recordType", 2), EDIT_WAVE)
                overridePendingTransition(0, 0)
            }
        }
        val seekProgress = voiceAnimProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress)
        seekProgress.setOnClickListener {
            sendObserver()
            if (userInfoData == null)
                return@setOnClickListener
            if (audioPlayer.isPlaying) {
                if (voiceAnimProgress.data != null && voiceAnimProgress.data.isPlaying) {//播放状态就暂停
                    voiceAnimProgress.data.pasuePosition = audioPlayer.currentPosition.toInt()
                    mHandler.stop()
                    audioPlayer.stop()
                    voiceAnimProgress.data.isPlaying = false
                    voiceAnimProgress.finish()
                    return@setOnClickListener
                }
            }
            if (userInfoData == null) {
                return@setOnClickListener
            }
            download()
        }

        seekProgress.setOnTrackListener(object : ProgressTrackListener {
            override fun startTrack() {
                if (!seekProgress.isPressed) {
                    if (audioPlayer.isPlaying) {
                        voiceAnimProgress.data.allDuration = audioPlayer.duration
                        mHandler.stop()
                        audioPlayer.stop()
                        voiceAnimProgress.data.isPlaying = false
                        voiceAnimProgress.finish()
                    }
                }
            }

            override fun endTrack(progress: Float) {
                /**
                 * 滑动停止
                 */
                voiceAnimProgress.data.pasuePosition = (progress * voiceAnimProgress.data.allDuration).toInt()
                download()
            }
        })

        voiceAnimProgress.findViewById<View>(R.id.iv_click_reStart).setOnClickListener {
            sendObserver()
            if (audioPlayer.isPlaying) {
                if (voiceAnimProgress.data != null && voiceAnimProgress.data.isPlaying) {//播放状态就暂停
                    mHandler.stop()
                    audioPlayer.stop()
                    voiceAnimProgress.data.isPlaying = false
                    voiceAnimProgress.data.pasuePosition = 1
                    voiceAnimProgress.finish()
                }
            }
            voiceAnimProgress.data.pasuePosition = 0
            download()
        }
        moreChangeCover.setOnClickListener {
            startActivity<DialogUserThemeActivity>()
        }
        user_identity_recorder.setOnClickListener {
            clearFlag()
            it.isSelected = true
        }
        user_identity_exchange.setOnClickListener {
            clearFlag()
            it.isSelected = true
        }
        user_identity_empty.setOnClickListener {
            clearFlag()
            it.isSelected = true
        }
    }

    private fun loginAdmin(pwd: String) {
        OkClientHelper.post(this, "admin/users/login", FormBody.Builder().add("confirmPasswd", pwd).build(), AdminLoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AdminLoginData
                if (result.code == 0) {
                    SPUtils.setString(this@UserInfoActivity, IConstant.ADMINTOKEN, result.data.token)
                    startActivity(Intent(this@UserInfoActivity, ManagerAdminActivity::class.java))
                    dialogPwd?.dismiss()
                } else {
                    dialogPwd?.setCallBack()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                showToast(any.toString())
            }
        })
    }

    private fun clearFlag() {
        user_identity_recorder.isSelected = false
        user_identity_exchange.isSelected = false
        user_identity_empty.isSelected = false
    }

    private fun getFlag(): String {
        return when {
            user_identity_recorder.isSelected -> "1"
            user_identity_exchange.isSelected -> "2"
            else -> "0"
        }
    }

    /**
     * 没有修改
     */
    private fun isChange(): Boolean {
        return if (userInfoData != null) {
            userInfoData!!.data.identity_type.toString() == getFlag()
        } else {
            true
        }
    }

    /**
     * 下载音频文件
     */
    private fun download() {
        try {
            if (TextUtils.isEmpty(userInfoData!!.data.wave_url)) {
                showToast(resources.getString(R.string.string_error_file))
                return
            }
            if (voiceAnimProgress.data.pasuePosition > 0) {
                val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME + "/" + AppTools.getSuffix(userInfoData!!.data.wave_url))
                audioPlayer.setDataSource(file.absolutePath)
                audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
            } else
                OkClientHelper.downWave(this, userInfoData!!.data.wave_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downWave
                        }
                        audioPlayer.setDataSource(o.toString())
                        audioPlayer.start(if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { volleyError -> showToast(VolleyErrorHelper.getMessage(volleyError)) })
            audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                override fun onCompletion() {
                    mHandler.stop()
                    voiceAnimProgress.data.isPlaying = false
                    voiceAnimProgress.finish()
                }

                override fun onInterrupt() {
                    mHandler.stop()
                    voiceAnimProgress.data.isPlaying = false
                    voiceAnimProgress.finish()
                }

                override fun onPrepared() {
                    audioPlayer.seekTo(voiceAnimProgress.data.pasuePosition)
                    mHandler.start()
                    voiceAnimProgress.data.pasuePosition = 0
                    voiceAnimProgress.data.isPlaying = true
//                    voiceAnimProgress.start()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        when (flag) {
            1 -> {
                val formBody = FormBody.Builder()
                        .add("nickName", moreUserName.text)
                        .build()
                OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            EventBus.getDefault().post(UpdateFriendInfoEvent(loginBean.user_id, 1, null, moreUserName.text.toString().trim(), null))
                        }

                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@UserInfoActivity)) {
                            showToast("网络连接异常")
                        }
                    }
                })
            }
            2 -> {
                val formBody = FormBody.Builder()
                        .add("selfIntro", tv_Desc.text.toString().trim())
                        .build()
                OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        if ((result as BaseRepData).code == 0) {
                            tv_Desc.isSelected = TextUtils.isEmpty(tv_Desc.text.toString().trim())
                            if (TextUtils.isEmpty(tv_Desc.text.toString().trim())) {
                                tv_Desc.text = resources.getString(R.string.string_empty_desc)
                            }
                            EventBus.getDefault().post(UpdateFriendInfoEvent(loginBean.user_id, 2, null, null, tv_Desc.text.toString().trim()))
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        if (!AppTools.isNetOk(this@UserInfoActivity)) {
                            showToast("网络连接异常")
                        }
                        transLayout.showContent()
                    }
                })
            }
            3 -> {
                OkClientHelper.get(this, "users/${loginBean.user_id}", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        userInfoData = result as UserInfoData
                        if (TextUtils.isEmpty(result.data.wave_url)) {
                            voiceAnimProgress.visibility = View.GONE
                            tv_Record.text = resources.getString(R.string.string_record_cover_voice)
                        } else {
                            voiceAnimProgress.visibility = View.VISIBLE
                            tv_Record.text = resources.getString(R.string.string_record_wave)
                        }
                        moreUserName.setMsgCount(userInfoData!!.data.nick_name)
                        PreferenceTools.saveObj(this@UserInfoActivity, IConstant.USERCACHE, userInfoData)
                        glideUtil.loadGlide(userInfoData!!.data.avatar_url, iv_img, R.mipmap.icon_user_default, glideUtil.getLastModified(userInfoData!!.data.avatar_url))
                        tv_Desc.text = if (TextUtils.isEmpty(result.data.self_intro)) {
                            tv_Desc.isSelected = true
                            resources.getString(R.string.string_empty_desc)
                        } else {
                            result.data.self_intro
                        }
                        voiceAnimProgress.data = BaseAnimBean(result.data.wave_len)
                        when (result.data.identity_type.toString()) {
                            "1" -> user_identity_recorder.isSelected = true
                            "2" -> user_identity_exchange.isSelected = true
                            else -> user_identity_empty.isSelected = true
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@UserInfoActivity)) {
                            showToast("网络连接异常")
                        }
                    }
                })
            }
            4 -> {
                OkClientHelper.patch(this, "users/${loginBean.user_id}", FormBody.Builder().add("identityType", getFlag()).build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            EventBus.getDefault().post(UpdateIdentityEvent(getFlag().toInt()))
                            finish()
                        } else {
                            transLayout.showContent()
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    private fun updateWave(formBody: FormBody, type: Int) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (type == 1) {
                        request(3)
                        /**
                         * 通知更新声波
                         */
                        EventBus.getDefault().post(MeFragment.UpdateUserInfoEvent())
                    } else if (type == 2) {
                        showToast("封面已更新")
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
                if (!AppTools.isNetOk(this@UserInfoActivity)) {
                    showToast("网络连接异常")
                }
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isChange()) {
                request(4)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDITAVATARCODE) {
            request(3)
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RENAMECODE) {
                data?.let {
                    moreUserName.setMsgCount(it.getStringExtra("name"))
                    request(1)
                }
            } else if (requestCode == EDITDESCCODE) {
                data?.let {
                    tv_Desc.text = it.getStringExtra("desc")
                    request(2)
                }
            } else if (requestCode == EDIT_WAVE) {
                val voicePath = data?.getStringExtra("voice")
                val voiceLength = data?.getStringExtra("voiceLength")
                val formBody = FormBody.Builder()
                        .add("waveUri", voicePath)
                        .add("waveLen", voiceLength)
                        .add("bucketId", AppTools.bucketId)
                        .build()
                updateWave(formBody, 1)
            } else if (requestCode == OPEN_GALLERY) {
                val selectedImage = data?.data
                val path = AppTools.getPath(selectedImage, this)
                if (null != path) {
                    startActivityForResult(Intent(this, CropBgActivity::class.java).putExtra("path", path), REQUEST_CROP_BG)
                } else {
                    showToast("图片异常")
                }
            } else if (requestCode == REQUEST_CROP_BG) {
                data?.let {
                    val result = it.getStringExtra("result")
                    val formBody = FormBody.Builder()
                            .add("mainCoverPhotoUri", result)
                            .add("bucketId", AppTools.bucketId)
                            .build()
                    updateWave(formBody, 2)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            userInfoData?.let {
                if (it.data.isPlaying) {
                    it.data.isPlaying = false
                    mHandler.stop()
                }
                audioPlayer.stop()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        mHandler.removeCallbacks(mHandler)
    }
}