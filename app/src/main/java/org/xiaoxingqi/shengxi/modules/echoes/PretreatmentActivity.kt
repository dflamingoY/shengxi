package org.xiaoxingqi.shengxi.modules.echoes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.os.Environment
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.activity_pretreatment.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogCancelMsg
import org.xiaoxingqi.shengxi.dialog.DialogEnterContent
import org.xiaoxingqi.shengxi.impl.OnPlayListenAdapter
import org.xiaoxingqi.shengxi.impl.ProgressHelper
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.PretreatmentData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewSeekProgress
import org.xiaoxingqi.shengxi.wedgit.VoiceNearbyProgress
import java.io.File
import java.io.IOException

class PretreatmentActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<PretreatmentData.PretreatmentBean>
    private val mData by lazy { ArrayList<PretreatmentData.PretreatmentBean>() }
    private var lastId: String = ""
    private var editBean: PretreatmentData.PretreatmentBean? = null
    private var playBean: PretreatmentData.PretreatmentBean? = null

    companion object {
        private const val REQUEST_PRETREATMENT = 0x00
        private const val REQUEST_EDIT_PRETREATMENT = 0x01
    }

    private lateinit var audioPlayer: AudioPlayer
    private var isSelected = false
    private val progressHandler = @SuppressLint("HandlerLeak")
    object : ProgressHelper(100) {

        override fun handleMessage(msg: Message?) {
            adapter.changeStatue(false)
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
                val currentPosition = audioPlayer.currentPosition
                playBean?.let { it.pasuePosition = currentPosition.toInt() }
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_MUSIC)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        } else {
            if (audioPlayer.isPlaying) {
                val currentPosition = audioPlayer.currentPosition
                playBean?.let { it.pasuePosition = currentPosition.toInt() }
                audioPlayer.stop()
                audioPlayer.start(AudioManager.STREAM_VOICE_CALL)
                audioPlayer.seekTo(currentPosition.toInt())
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_pretreatment
    }

    override fun initView() {

    }

    override fun initData() {
        isSelected = intent.getBooleanExtra("isSelected", false)
        audioPlayer = AudioPlayer(this)
        adapter = object : QuickAdapter<PretreatmentData.PretreatmentBean>(this, R.layout.item_ptetreatment, mData) {
            var cache: MutableList<BaseAdapterHelper> = java.util.ArrayList()
            override fun convert(helper: BaseAdapterHelper?, item: PretreatmentData.PretreatmentBean?) {
                helper!!.getTextView(R.id.tvRemark).text = item!!.reply_remark
                val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceNearbyProgress
                voiceProgress.data = item
                helper.getView(R.id.tvRecord).setOnClickListener {
                    editBean = item
                    startActivityForResult(Intent(this@PretreatmentActivity, RecordVoiceActivity::class.java)
                            .putExtra("isSend", true)
                            .putExtra("recordType", 5)
                            .putExtra("resourceType", "19"), REQUEST_EDIT_PRETREATMENT)
                    overridePendingTransition(0, 0)
                }
                helper.getView(R.id.tvRemark).setOnClickListener {
                    DialogEnterContent(this@PretreatmentActivity).setRemark(item.reply_remark).setOnCommitListener(object : DialogEnterContent.OnCommitListener {
                        override fun commit(result: String) {
                            updateRemark(item, remark = result)
                            item.reply_remark = result
                            helper.getTextView(R.id.tvRemark).text = item.reply_remark
                        }
                    }).show()
                }
                voiceProgress.findViewById<ViewSeekProgress>(R.id.viewSeekProgress).setOnClickListener {
                    sendObserver()
                    if (audioPlayer.isPlaying) {
                        if (item.isPlaying) {//当前正在播放
                            item.pasuePosition = audioPlayer.currentPosition.toInt()
                            audioPlayer.stop()
                            progressHandler.stop()
                            item.isPlaying = false
                            voiceProgress.finish()
                            playBean = null
                            return@setOnClickListener
                        }
                    }
                    playBean?.let {
                        it.isPlaying = false
                        it.pasuePosition = 0
                    }
                    download(helper, item)
                }
                val viewSeekProgress = helper.getView(R.id.viewSeekProgress) as ViewSeekProgress
                viewSeekProgress.setOnTrackListener(object : ProgressTrackListener {
                    override fun startTrack() {
                        if (!viewSeekProgress.isPressed) {
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

                if (!cache.contains(helper))
                    cache.add(helper)
            }

            private fun download(helper: BaseAdapterHelper, item: PretreatmentData.PretreatmentBean) {
                try {
                    if (TextUtils.isEmpty(item.resource_url)) {
                        showToast(resources.getString(R.string.string_error_file))
                    }

                    val file = getDownFilePath(item.resource_url)
                    if (file.exists()) {
                        audioPlayer.setDataSource(file.absolutePath)
                        audioPlayer.start(if (SPUtils.getBoolean(this@PretreatmentActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                    } else {
                        OkClientHelper.downFile(this@PretreatmentActivity, item.resource_url, { o ->
                            try {
                                if (null == o) {
                                    showToast(resources.getString(R.string.string_error_file))
                                    return@downFile
                                }
                                if (audioPlayer.isPlaying && o.toString() == audioPlayer.getmAudioFile())
                                    return@downFile
                                audioPlayer.setDataSource(o.toString())
                                audioPlayer.start(if (SPUtils.getBoolean(this@PretreatmentActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }, { showToast(VolleyErrorHelper.getMessage(it)) })
                    }
                    audioPlayer.onPlayListener = object : OnPlayListenAdapter() {

                        override fun onCompletion() {
                            (helper.getView(R.id.voiceProgress) as VoiceNearbyProgress).finish()
                            item.isPlaying = false
                            progressHandler.stop()
                            changeStatue(false)
                        }

                        override fun onInterrupt() {
                            (helper.getView(R.id.voiceProgress) as VoiceNearbyProgress).finish()
                            item.isPlaying = false
                            progressHandler.stop()
                        }

                        override fun onPrepared() {
                            item.allDuration = audioPlayer.duration
                            audioPlayer.seekTo(item.pasuePosition)
                            item.pasuePosition = 0
                            playBean = item
                            item.isPlaying = true
                            progressHandler.start()
                        }
                    }
                } catch (e: java.lang.Exception) {

                }
            }

            override fun changeStatue(isSelect: Boolean) {
                val currentPosition = audioPlayer.currentPosition.toInt()
                for (helper in cache) {
                    try {
                        val voiceProgress = helper.getView(R.id.voiceProgress) as VoiceNearbyProgress
                        voiceProgress.changeProgress(currentPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white, recyclerView, false))
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        swipeRefresh.setOnRefreshListener {
            request(0)
        }
        iv_Other.setOnClickListener {
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                    .putExtra("isSend", true)
                    .putExtra("recordType", 5)
                    .putExtra("resourceType", "19"), REQUEST_PRETREATMENT)
            overridePendingTransition(0, 0)
        }
        adapter.setOnItemLongClickListener { _, position ->
            DialogCancelMsg(this).setTitle("删除这条回复语").setOnClickListener(View.OnClickListener {
                deleteItem(mData[position])
            }).show()
        }
        adapter.setOnItemClickListener { _, position ->
            if (isSelected) {
                setResult(Activity.RESULT_OK, Intent().putExtra("voiceUri", mData[position].resource_uri).putExtra("voiceLength", mData[position].resource_len))
                finish()
            }
        }
        adapter.setOnLoadListener {
            request(1)
        }
    }

    private fun updateRemark(item: PretreatmentData.PretreatmentBean, remark: String? = null, voice: String? = null, voiceLength: String? = null) {
        val builder = FormBody.Builder()
        remark?.let {
            builder.add("replyRemark", remark).add("resourceUri", item.resource_uri).add("resourceLen", item.resource_len).add("bucketId", item.bucket_id.toString())
        }
        voice?.let {
            builder.add("resourceUri", voice).add("resourceLen", voiceLength).add("bucketId", AppTools.bucketId).add("replyRemark", item.reply_remark)
        }
        builder.add("resourceType", "1")
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "admin/${loginBean.user_id}/defaultReply/${item.id}", builder.build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("更改成功")
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 新增的时候 立马请求第一条
     */
    private fun requestFirst() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "admin/${loginBean.user_id}/defaultReply?lastId=", PretreatmentData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as PretreatmentData
                if (result.data != null) {
                    mData.add(0, result.data[0])
                    adapter.notifyDataSetChanged()
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                transLayout.showContent()
            }
        })
    }

    private fun deleteItem(bean: PretreatmentData.PretreatmentBean) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(this, "admin/${loginBean.user_id}/defaultReply/${bean.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
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

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "admin/${loginBean.user_id}/defaultReply?lastId=$lastId", PretreatmentData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                swipeRefresh.isRefreshing = false
                result as PretreatmentData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        for (bean in result.data) {
                            mData.add(bean)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    lastId = result.data[result.data.size - 1].id
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }

            override fun onFailure(any: Any?) {
                swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        })
    }

    private fun addPretreatment(formBody: FormBody) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "admin/${loginBean.user_id}/defaultReply", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("添加成功")
                    requestFirst()
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PRETREATMENT) {
                data?.let {
                    val voicePath = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    addPretreatment(FormBody.Builder().add("resourceUri", voicePath)
                            .add("resourceType", "1")
                            .add("resourceLen", voiceLength)
                            .add("bucketId", AppTools.bucketId)
                            .add("replyRemark", "回复语" + (mData.size + 1)).build())
                }
            } else if (requestCode == REQUEST_EDIT_PRETREATMENT) {
                data?.let {
                    val voicePath = it.getStringExtra("voice")
                    val voiceLength = it.getStringExtra("voiceLength")
                    editBean?.let { bean ->
                        updateRemark(bean, voice = voicePath, voiceLength = voiceLength)
                    }
                }
            }
        }

    }
}