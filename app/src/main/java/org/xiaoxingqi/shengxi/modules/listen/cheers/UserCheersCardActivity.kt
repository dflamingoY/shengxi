package org.xiaoxingqi.shengxi.modules.listen.cheers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_user_cheers_card.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.CenterHintDialog
import org.xiaoxingqi.shengxi.dialog.DialogCheersDelete
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.impl.CheersUserDataEvent
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.CropBgActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.io.IOException

class UserCheersCardActivity : BaseAct() {
    private var cacheData: UserCheersData.UserCheersBean? = null
    private val audioPlayer by lazy { AudioPlayer(this) }
    private var url: String? = null

    companion object {
        private const val REQUEST_GALLEY = 0x01
        private const val REQUEST_CROP_BG = 0x02
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
                val currentPosition = audioPlayer.currentPosition
                cacheData?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                cacheData?.let {
                    audioPlayer.start(AudioManager.STREAM_MUSIC)
                }
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                cacheData?.pasuePosition = currentPosition.toInt()
                audioPlayer.stop()
                cacheData?.let {
                    audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_user_cheers_card
    }

    override fun initView() {//285 :425
        val params = frameContainer.layoutParams
        val width = AppTools.getWindowsWidth(this) - AppTools.dp2px(this, 38 * 2)
        params.width = width
        params.height = 425 * width / 285
        frameContainer.layoutParams = params
    }

    override fun initData() {
        cacheData = intent.getParcelableExtra("data")
        if (intent.getBooleanExtra("isOpen", false)) {
            //打开录制界面
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java).putExtra("recordType", 7)
                    .putExtra("resourceType", "24")
                    .putExtra("isSend", true), REQUEST_CHEERS_VOICE)
            overridePendingTransition(0, 0)
        }
        val info = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        val cacheCover = SPUtils.getString(this@UserCheersCardActivity, IConstant.CHEERS_USER_COVER + info.data.user_id, null)
        if (!TextUtils.isEmpty(cacheCover)) {
            Glide.with(this)
                    .load(cacheCover)
                    .into(ivCheerBg)
            url = cacheCover
        }
        glideUtil.loadGlide(info.data.avatar_url, ivUserAvatar, 0, glideUtil.getLastModified(info.data.avatar_url))
        if (cacheData == null) {
            ivRecordHint.isSelected = true
            request(0)
            linearButton.visibility = View.GONE
        } else {
            linearRecord.visibility = View.GONE
            framePlay.visibility = View.VISIBLE
        }
        request(1)
    }

    override fun initEvent() {
        audioPlayer.onPlayListener = object : OnPlayListenAdapter() {
            override fun onCompletion() {
                cacheData?.isPlaying = false
                rectPlay.end()
                rectPlay.visibility = View.GONE
            }

            override fun onInterrupt() {
                cacheData?.isPlaying = false
                rectPlay.visibility = View.GONE
                rectPlay.end()
            }

            override fun onPrepared() {
                audioPlayer.seekTo(cacheData?.pasuePosition!!)
                cacheData!!.pasuePosition = 0
                cacheData?.isPlaying = true
                rectPlay.visibility = View.VISIBLE
                rectPlay.start()
            }
        }
        linearRecord.setOnClickListener {
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java).putExtra("recordType", 7)
                    .putExtra("resourceType", "24")
                    .putExtra("isSend", true), REQUEST_CHEERS_VOICE)
            overridePendingTransition(0, 0)
        }
        framePlay.setOnClickListener {
            cacheData?.let {
                if (cacheData!!.isPlaying) {
                    cacheData!!.pasuePosition = audioPlayer.currentPosition.toInt()
                    audioPlayer.stop()
                } else
                    down(it.recording_url)
            }
        }
        ivCheerBg.setOnLongClickListener {
            //保存图片.或者更新图片
            DialogSelectPhoto(this).hideAction(true)
                    .hindOther(true)
                    .changeText("修改封面")
                    .setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                        override fun itemView(view: View) {
                            //保存或者修改
                            when (view.id) {
                                R.id.tv_Save -> {
                                    openGallery()
                                }
                                R.id.tv_Other -> {//保存
                                    url?.let {
                                        save(it)
                                    }
                                }
                            }
                        }
                    })
                    .show()
            false
        }
        btn_Back.setOnClickListener { finish() }
        relativePass.setOnClickListener {//删除
            DialogCheersDelete(this).setOnClickListener(View.OnClickListener {
                deleteUserCheers()
            }).show()
        }
        relativeCheers.setOnClickListener {//重录
            //修改cheers
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java).putExtra("recordType", 7)
                    .putExtra("resourceType", "24")
                    .putExtra("isSend", true), REQUEST_CHEERS_VOICE)
            overridePendingTransition(0, 0)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun openGallery() {
        startActivityForResult(Intent(this, AlbumActivity::class.java)
                .putExtra("isChat", true)
                .putExtra("count", 1), REQUEST_GALLEY)
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
      删除自己的cheers
     */
    private fun deleteUserCheers() {
        cacheData?.let {
            if (it.isPlaying && audioPlayer.isPlaying) {
                audioPlayer.stop()
            }
        }
        OkClientHelper.delete(this, "cheersRecordings/${cacheData?.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    cacheData = null
                    ivRecordHint.isSelected = true
                    linearButton.visibility = View.GONE
                    framePlay.visibility = View.GONE
                    linearRecord.visibility = View.VISIBLE
                    //删除cheers通知界面
                    EventBus.getDefault().post(CheersUserDataEvent(null))
                } else {
                    showToast(result.msg)
                }
            }
        }, "V4.3")
    }

    override fun request(flag: Int) {
        val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "users/${login.user_id}/cheersRecording", UserCheersData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as UserCheersData
                        if (result.data != null) {
                            cacheData = result.data
                            ivRecordHint.isSelected = false
                            framePlay.visibility = View.VISIBLE
                            linearRecord.visibility = View.GONE
                            linearButton.visibility = View.VISIBLE
                            EventBus.getDefault().post(CheersUserDataEvent(cacheData))
                        } else {
                            linearButton.visibility = View.GONE
                            framePlay.visibility = View.GONE
                            linearRecord.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(this, "users/${login.user_id}/covers?coverName=cheers_cover", NewVersionCoverData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as NewVersionCoverData
                        if (result.data?.let {
                                    url = it[0].coverUrl
                                    Glide.with(this@UserCheersCardActivity)
                                            .load(it[0].coverUrl)
                                            .into(ivCheerBg)
                                    SPUtils.setString(this@UserCheersCardActivity, IConstant.CHEERS_USER_COVER + login.user_id, it[0].coverUrl)
                                    it
                                }.isNullOrEmpty()) {
                            tvLongPressHint.visibility = View.VISIBLE
                        }
                    }
                }, "V4.2")
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(path: String) {
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg") && !suffix.contains(".png") && !suffix.contains(".gif")) {
            suffix = "$suffix.jpg"
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        if (file.exists()) {
            showToast("图片已存在")
            return
        }
        object : AsyncTask<Void, Void, File>() {
            override fun doInBackground(vararg voids: Void): File? {
                return try {
                    Glide.with(this@UserCheersCardActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(System.currentTimeMillis())))
                            .downloadOnly()
                            .load(path)
                            .submit()
                            .get()
                } catch (e: Exception) {
                    null
                }
            }

            override fun onPostExecute(s: File?) {
                if (null == s) {
                    showToast("图片保存失败")
                } else {
                    val bootFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                    if (!bootFile.exists()) {
                        bootFile.mkdirs()
                    }
                    val resultFile = File(bootFile.absolutePath + "/" + suffix)
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

    /*
     更新语音
      */
    private fun updateUserCheers(formBody: FormBody) {
        OkClientHelper.post(this, "cheersRecordings", formBody, IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    //新增cheers
                    request(0)
                }
            }

            override fun onFailure(any: Any?) {
            }
        }, "V4.3")
    }

    private fun updateUserCheersCover(formBody: FormBody, url: String) {
        val token = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        SPUtils.setString(this@UserCheersCardActivity, IConstant.CHEERS_USER_COVER + token.user_id, url)
        OkClientHelper.post(this, "users/${token.user_id}/covers", formBody, BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code != 0) {
                    showToast(result.msg)
                } else {
                    tvLongPressHint.visibility = View.GONE
                }
            }
        }, "V4.2")
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
                REQUEST_GALLEY -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<*>
                        if (result.size > 0) {
                            startActivityForResult(Intent(this, CropBgActivity::class.java)
                                    .putExtra("resourceType", "23")
                                    .putExtra("path", result[0] as String), REQUEST_CROP_BG)
                        }
                    }
                }
                REQUEST_CROP_BG -> {
                    //上传保存封面图
                    data?.let {
                        val path = it.getStringExtra("result")
                        val originalPath = it.getStringExtra("originalPath")
                        Glide.with(this)
                                .load(originalPath)
                                .into(ivCheerBg)
                        updateUserCheersCover(FormBody.Builder().add("coverUri", path)
                                .add("coverName", "cheers_cover")
                                .add("bucketId", AppTools.bucketId)
                                .build(), originalPath)
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