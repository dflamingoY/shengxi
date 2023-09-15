package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.alibaba.fastjson.JSONArray
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_send.*
import kotlinx.android.synthetic.main.view_progress_speed.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.database.VoiceCacheDao
import org.xiaoxingqi.shengxi.dialog.DialogCancelCommit
import org.xiaoxingqi.shengxi.dialog.DialogHintToWorld
import org.xiaoxingqi.shengxi.dialog.DialogSaveCache
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.QiniuToken
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.EditTopicSearchActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

private const val REQUESTALBUM = 0x00
private const val REQUESTTAG = 0x01

class SendAct : BaseAct() {
    private val REQUEST_RECORD = 0x03
    private var sendType = 0  // 1 发布动态(发送按钮无背景)  2  发布影评 3發佈話題  4 唱回忆  5 看过的书
    private lateinit var adapter: QuickAdapter<String>
    private val imgData = ArrayList<String>()
    private var original: String? = null
    private var voiceLenth: String? = null
    private var topicId: String? = null
    private var moviesId: String? = null
    private var movieBean: BaseSearchBean? = null
    private var voicePath: String? = null
    private var topicName: String? = null
    private var voiceType = "2"
    private var isHome = false//是否要设置到首页心情界面
    private var bookId: String? = null
    private var musicId: String? = null
    private var albumId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_send
    }

    override fun initView() {
        imgRecycler.layoutManager = GridLayoutManager(this, 3)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (!SPUtils.getBoolean(this, IConstant.ISSENDVOICEHINT + loginBean.user_id, false) && intent.getBooleanExtra("isHome", false)) {
            relativeShowShareHint.visibility = View.VISIBLE
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M && (OsUtil.isOppo() || OsUtil.isVivo())) || OsUtil.isMeizu()) {
                tv_Send_HintText.setPadding(0, AppTools.dp2px(this, 8), 0, 0)
            }
        }
        albumId = intent.getStringExtra("albumId")
        original = intent.getStringExtra("originalPath")
        voiceLenth = intent.getStringExtra("length")
        isHome = intent.getBooleanExtra("isHome", false)
        sendObserver()
        tvPush.isSelected = false
        tvPush.isEnabled = false
        sendType = intent.getIntExtra("type", 1)
        when (sendType) {
            1 -> {//普通动态
                voiceType = "2"
                frame_normal.visibility = View.VISIBLE
                frame_Movie.visibility = View.GONE
//                tv_Commit.background = null
//                tv_Commit.textSize = 15F
//                tv_Commit.text = resources.getString(R.string.string_sendAct_8)
//                tv_Commit.setPadding(0, 0, 0, 0)
                relativeTopic.visibility = View.VISIBLE
            }
            2 -> {//影评
                voiceType = "3"
                frame_normal.visibility = View.GONE
                frame_Movie.visibility = View.VISIBLE
//                tv_Commit.text = resources.getString(R.string.string_sendAct_8)
//                tv_Commit.textSize = 12F
                tv_Title.text = resources.getString(R.string.string_sendAct_9)
                movieBean = intent.getParcelableExtra("data")
                dynamicMovie.setData(movieBean, 1)
                moviesId = movieBean?.id
            }
            3 -> {//话题
                voiceType = "2"
                relativeTopic.visibility = View.VISIBLE
//                tv_Commit.setPadding(0, 0, 0, 0)
//                tv_Commit.background = null
//                tv_Commit.text = resources.getString(R.string.string_sendAct_8)
                frame_normal.visibility = View.VISIBLE
//                tv_Commit.textSize = 15F
                topicName = intent.getStringExtra("topicName")
                topicId = intent.getStringExtra("topicId")
                tvTopic.text = "#$topicName# X"
                tv_AddTopic.visibility = View.GONE
            }
            4 -> {//唱回忆
                voiceType = "3"
                frame_normal.visibility = View.GONE
                frame_Movie.visibility = View.VISIBLE
                selectorScore.visibility = View.GONE
                selectorMusic.visibility = View.VISIBLE
//                tv_Commit.text = resources.getString(R.string.string_sendAct_8)
//                tv_Commit.textSize = 12F
                tv_Title.text = resources.getString(R.string.string_music_7)
                movieBean = intent.getParcelableExtra("data")
                tv_Recommend.text = resources.getString(R.string.string_music_6)
                dynamicMovie.setData(movieBean, 3)
                musicId = movieBean?.id
            }
            5 -> {//看过的书
                voiceType = "3"
                frame_normal.visibility = View.GONE
                frame_Movie.visibility = View.VISIBLE
//                tv_Commit.text = resources.getString(R.string.string_sendAct_8)
//                tv_Commit.textSize = 12F
                tv_Title.text = resources.getString(R.string.string_book_11)
                tv_Recommend.text = resources.getString(R.string.string_do_you_like_books)
                movieBean = intent.getParcelableExtra("data")
                dynamicMovie.setData(movieBean, 2)
                bookId = movieBean?.id
            }
        }
        if (!TextUtils.isEmpty(original)) {
            voiceAnimProgress.visibility = View.VISIBLE
            voiceAnimProgress.data = BaseAnimBean(voiceLenth)
            tv_Record.visibility = View.GONE
            if (sendType != 2 && sendType != 4 && sendType != 5) {
                tvPush.isEnabled = true
                tvPush.isSelected = true
            }
        }
        LocalLogUtils.writeLog("发声兮:发送类型: ${tv_Title.text}", System.currentTimeMillis())
        imgData.add("相机")
        adapter = object : QuickAdapter<String>(this, R.layout.item_img, imgData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                if ("相机" == item) {
                    helper?.getImageView(R.id.iv_img)?.setImageResource(R.mipmap.btn_default_select)
                    helper?.getView(R.id.iv_Delete)?.visibility = View.GONE
                    helper?.getView(R.id.view_Stroke)?.visibility = View.VISIBLE
                } else {
                    helper!!.getView(R.id.view_Stroke)?.visibility = View.GONE
                    Glide.with(this@SendAct)
                            .asBitmap()
                            .load(item)
                            .apply(RequestOptions().centerCrop().error(R.mipmap.ic_launcher))
                            .into(helper.getImageView(R.id.iv_img))
                    helper.getView(R.id.iv_Delete).visibility = View.VISIBLE
                }
                helper?.getView(R.id.iv_Delete)?.setOnClickListener {
                    imgData.remove(item)
                    if (imgData.size == 2) {
                        if (imgData[1] != "相机")
                            imgData.add("相机")
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
        imgRecycler.adapter = adapter
        if (intent.getBooleanExtra("isOpenRecord", false)) {
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                    .putExtra("isSend", false)
                    .putExtra("isOpenRecord", true)
                    .putExtra("isComment", sendType == 2 || sendType == 4 || sendType == 5)
                    .putExtra("recordType", 1), REQUEST_RECORD)
            overridePendingTransition(0, 0)
        }
    }

    override fun initEvent() {
        iv_Close.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setBoolean(this, IConstant.ISSENDVOICEHINT + loginBean.user_id, true)
            relativeShowShareHint.visibility = View.GONE
        }
        adapter.setOnItemClickListener { _, position ->
            if ("相机" == imgData[position])
                startActivityForResult(Intent(this, AlbumActivity::class.java).putExtra("count", 4 - imgData.size), REQUESTALBUM)
            else {
                startActivity(Intent(this, ShowPicActivity::class.java).putExtra("path", imgData[position]))
                overridePendingTransition(0, 0)
            }
        }

        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.layoutPosition
                if ("相机" == imgData[position]) {
                    return makeMovementFlags(0, 0)
                }
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //允许上下左右的拖动
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1f, 0.7f, 1f).setDuration(320).start()
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1f, 0.7f, 1f).setDuration(320).start()
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // 真实的Position：通过ViewHolder拿到的position都需要减掉HeadView的数量。
                val fromPosition = viewHolder.layoutPosition
                if ("相机" == imgData[fromPosition]) {
                    return false
                }
                val toPosition = target.layoutPosition
                if ("相机" == imgData[toPosition]) {
                    return false
                }
                if (fromPosition < toPosition)
                    for (i in fromPosition until toPosition)
                        Collections.swap(imgData, i, i + 1)
                else
                    for (i in fromPosition downTo toPosition + 1)
                        Collections.swap(imgData, i, i - 1)
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                adapter.notifyDataSetChanged()
            }
        })
        helper.attachToRecyclerView(imgRecycler)
        tv_Record.setOnClickListener {
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                    .putExtra("isSend", false)
                    .putExtra("isMusic", sendType == 4)
                    .putExtra("isComment", sendType == 2 || sendType == 4 || sendType == 5)
                    .putExtra("recordType", 1), REQUEST_RECORD)
            overridePendingTransition(0, 0)
        }
        tv_AddTopic.setOnClickListener {
            startActivityForResult(Intent(this, EditTopicSearchActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    , REQUESTTAG)
        }
        tvPush.setOnClickListener {
            commit()
        }
        tvTopic.setOnClickListener {
            tvTopic.text = ""
            tv_AddTopic.visibility = View.VISIBLE
            topicId = ""
            topicName = ""
        }
        voiceAnimProgress.setOnClickListener {
            startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                    .putExtra("isMusic", sendType == 4)
                    .putExtra("original", original)
                    .putExtra("isSend", false)
                    .putExtra("recordType", 1)
                    .putExtra("isComment", sendType == 2 || sendType == 4 || sendType == 5)
                    .putExtra("voicelength", voiceLenth), REQUEST_RECORD)
            overridePendingTransition(0, 0)
        }
        btn_Back.setOnClickListener {
            if (TextUtils.isEmpty(original)) {
                finish()
            } else
                DialogCancelCommit(this).setOnClickListener(View.OnClickListener {
                    if (!TextUtils.isEmpty(original)) {
                        val deleteFile = File(original)
                        if (deleteFile.exists()) {
                            deleteFile.delete()
                        }
                    }
                    finish()
                }).show()
        }
        selectorScore.setOnTabClick {
            if (!TextUtils.isEmpty(original)) {
                tvPush.isEnabled = true
                tvPush.isSelected = true
//                tv_Commit.setTextColor(Color.WHITE)
            }
        }
        selectorMusic.setOnScoreClickListener(OnScoreClickListener {
            if (!TextUtils.isEmpty(original)) {
                tvPush.isEnabled = true
                tvPush.isSelected = true
//                tv_Commit.setTextColor(Color.WHITE)
            }
        })
        linearPrivacy.setOnClickListener {
            linearPrivacy.isSelected = !linearPrivacy.isSelected
        }
    }

    @Synchronized
    private fun commit() {
        if (!isCommit) {
            synchronized(SendAct::class.java) {
                if (!isCommit) {
                    isCommit = true
                    transLayout.showProgress()
                    /**
                     * 计算上传的总大小
                     * 开始时间
                     */
                    allLength = calcAllSize()
                    startTime = System.currentTimeMillis()
                    if (imgData.size > 1) {
                        request(0)
                    } else {
                        request(1)
                    }
                }
            }
        }
    }

    private fun calcAllSize(): Long {
        var allLength = 0L
        currentLength = 0L
        for (bean in imgData) {
            if (bean == "相机") {
                continue
            }
            allLength += File(bean).length()
        }
        allLength += File(original).length()
        return allLength
    }

    @Volatile
    private var isCommit = false
    private var currentLength = 0L
    private var allLength = 0L
    private var startTime = 0L
    private var aliLoad: AliLoadFactory? = null

    /**
     * 压缩图片 鲁班压缩
     */
//    private fun compression() {
//
//    }


    private var bucketId: String? = "0"
    var imgJson: String? = null
    override fun request(flag: Int) {
        val loginBean1 = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        transLayout.showProgress()
        when (flag) {
            0 -> {
                /**
                 * 上传图片
                 */
                val formBody = FormBody.Builder()
                        .add("resourceType", "5")
                        .add("resourceContent", calcMd5())
                        .build()
                File("").length()
                OkClientHelper.post(this, "resource", formBody, QiniuToken::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuToken
                        LocalLogUtils.writeLog("发声兮:上传图片:${imgData.size}${result.data}", System.currentTimeMillis())
                        if (result.code == 0) {
                            aliLoad = AliLoadFactory(this@SendAct, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {
                                    if (System.currentTimeMillis() - startTime > 30000) {//超过30s 放弃所有上传 直接缓存到本地
                                        LocalLogUtils.writeLog("发声兮:上传图片:more than 30s   cancel all task", System.currentTimeMillis())
                                        aliLoad?.cancel()
                                    } else if (System.currentTimeMillis() - startTime >= 10000) {
                                        if (tv_progress.visibility != View.VISIBLE) {
                                            tv_progress.post {
                                                tv_progress.visibility = View.VISIBLE
                                            }
                                        }
                                        tv_progress.post {
                                            tv_progress.text = "${(((currentLength + current) * 1f / allLength) * 100).toInt()}%"
                                        }

                                    }
                                }

                                var mJSONArray = JSONArray()
                                override fun success() {
                                    imgJson = mJSONArray.toJSONString()
                                    request(1)
                                }

                                override fun fail() {
                                    runOnUiThread {
                                        tv_progress.visibility = View.GONE
                                        isCommit = false
                                        transLayout.showContent()
                                        LocalLogUtils.writeLog("发声兮:传图片失败存入数据库缓存oss error", System.currentTimeMillis())
                                        saveCache()
                                    }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {
                                    currentLength += File(imgData[result.data.resource_content.size - 1 - position]).length()
                                    mJSONArray.add(result.data.resource_content[result.data.resource_content.size - 1 - position])
                                }
                            }, *parseData(result.data.resource_content))
                        } else {
                            isCommit = false
                            transLayout.showContent()
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        LocalLogUtils.writeLog("发声兮:传图片失败存入数据库缓存 接口error ${any.toString()}", System.currentTimeMillis())
                        isCommit = false
                        if (!AppTools.isNetOk(this@SendAct)) {
                            showToast("网络连接异常")
                            /**
                             * 保存数据
                             */
                            saveCache()
                        } else {
                            if (any is String) {
                                saveCache()
                            } else {
                                showToast(any.toString())
                            }
                        }
                        transLayout.showContent()
                    }
                })
            }
            1 -> {
                val formBody = FormBody.Builder()
                        .add("resourceType", "2")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(original))}.aac")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            result.data.bucket_id?.let {
                                bucketId = it
                            }
                            LocalLogUtils.writeLog("发声兮:上传声兮源文件:${result.data}", System.currentTimeMillis())
                            aliLoad = AliLoadFactory(this@SendAct, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {
                                    if (System.currentTimeMillis() - startTime > 30000) {//超过30s 放弃所有上传 直接缓存到本地
                                        LocalLogUtils.writeLog("发声兮:上传声兮源文件:more than 30s cancel all task", System.currentTimeMillis())
                                        aliLoad?.cancel()
                                    } else if (System.currentTimeMillis() - startTime >= 10000) {
                                        if (tv_progress.visibility != View.VISIBLE) {
                                            tv_progress.post {
                                                tv_progress.visibility = View.VISIBLE
                                            }
                                        }
                                        tv_progress.post {
                                            tv_progress.text = "${(((currentLength + current) * 1f / allLength) * 100).toInt()}%"
                                        }
                                    }
                                }

                                override fun success() {
                                    voicePath = result.data.resource_content
                                    //语音文件发送成功, 删除本地记录
                                    try {
                                        File(original).let {
                                            if (it.exists()) {
                                                it.delete()
                                            }
                                        }
                                    } catch (e: Exception) {

                                    }
                                    request(2)
                                }

                                override fun fail() {//oss 异常
                                    runOnUiThread {
                                        isCommit = false
                                        tv_progress.visibility = View.GONE
                                        transLayout.showContent()
                                        LocalLogUtils.writeLog("发声兮:传音频失败存入数据库缓存 oss error", System.currentTimeMillis())
                                        saveCache()
                                    }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {

                                }
                            }, UploadData(result.data.resource_content, original))
                        } else {
                            isCommit = false
                            showToast(result.msg)
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        LocalLogUtils.writeLog("发声兮:传音频失败存入数据库缓存接口 error${any.toString()}", System.currentTimeMillis())
                        isCommit = false
                        if (!AppTools.isNetOk(this@SendAct)) {
//                            showToast("网络连接异常")
                            /**
                             * 保存数据
                             */
                            saveCache()
                        } else {
                            if (any is String) {
                                saveCache()
                            } else {
                                showToast(any.toString())
                            }
                        }
                        transLayout.showContent()
                    }
                })
            }
            2 -> {
                if (tv_progress.visibility == View.VISIBLE) {
                    tv_progress.post {
                        tv_progress.visibility = View.GONE
                    }
                }
                val builder = FormBody.Builder()
                        .add("voiceType", voiceType)
                        .add("voiceUri", voicePath)
                        .add("voiceLen", voiceLenth)
                        .add("bucketId", bucketId)
                if (!TextUtils.isEmpty(imgJson)) {
                    builder.add("voiceImg", imgJson)
                }
                if (!TextUtils.isEmpty(topicName)) {
                    builder.add("topicId", topicId)
                            .add("topicName", topicName)
                }
                if (!TextUtils.isEmpty(bookId)) {
                    builder.add("bookId", bookId)
                            .add("score", selectorScore.score)
                            .add("resourceType", "2")
                }
                if (!TextUtils.isEmpty(musicId)) {
                    builder.add("songId", musicId)
                            .add("score", selectorMusic.getScore())
                            .add("resourceType", "3")
                }
                if (!TextUtils.isEmpty(moviesId)) {
                    builder.add("movieId", moviesId)
                            .add("score", selectorScore.score)
                            .add("resourceType", "1")
                }
                if (!TextUtils.isEmpty(albumId)) {
                    builder.add("albumId", albumId)
                }
                builder.add("isPrivate", if (linearPrivacy.isSelected) "1" else "0")
                LocalLogUtils.writeLog("发声兮:数据发布到服务器", System.currentTimeMillis())
                OkClientHelper.post(this, "voices", builder.build(), SendVoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 发送通知 刷新界面
                         */
                        transLayout.showContent()
                        result as SendVoiceData
                        LocalLogUtils.writeLog("发声兮:发送结果:${result.code}${result.msg}", System.currentTimeMillis())
                        if (result.code == 0) {
                            /**
                             * 重置总时长
                             */
                            SPUtils.setInt(this@SendAct, IConstant.TOTALLENGTH + loginBean1.user_id, 1)
                            if (isHome) {
                                //切换界面到首页->心情
                                EventBus.getDefault().post(ImplPageChangeEvent(0))
                            }
                            EventBus.getDefault().post(UpdateSendVoice(1, null))
                            if (!TextUtils.isEmpty(imgJson)) {
                                /**
                                 * 更新相册或者声兮列表
                                 */
                                EventBus.getDefault().post(OperatorVoiceListEvent(2))
                            } else {
                                EventBus.getDefault().post(OperatorVoiceListEvent(1))
                            }
                            when (sendType) {
                                2 -> {
                                    EventBus.getDefault().post(object : UpdateMovieEvent(3) {})
                                }
                                4 -> {//唱回忆
                                    EventBus.getDefault().post(object : UpdateMovieEvent(5) {})
                                }
                                5 -> {//书评
                                    EventBus.getDefault().post(object : UpdateMovieEvent(4) {})
                                }
                            }
                            val obj = PreferenceTools.getObj(this@SendAct, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                            SPUtils.setString(this@SendAct, IConstant.FIRST_PUSH_VOICES + obj.user_id, TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()))
                            /*else
                                when (sendType) {
                                    2 -> {
                                        showToast(resources.getString(R.string.string_sendAct_12))
                                    }
                                    4 -> {//唱回忆
                                        showToast("唱回忆已发布")
                                    }
                                    5 -> {//书评
                                        showToast("书评心情已发布")
                                    }
                                    else -> {
                                        if (null == result.data || TextUtils.isEmpty(result.data.voice_id)) {
                                            showToast(resources.getString(R.string.string_sendAct_11))
                                        }
                                    }
                                }*/
                            /* if (null != result.data && !TextUtils.isEmpty(result.data.voice_id)) {//提示共享到世界
                                 checkShare(result.data.voice_id)
                             } else*/
                            finish()
                        } else {
                            showToast(result.msg)
                            transLayout.showContent()
                            isCommit = false
                        }
                    }

                    override fun onFailure(any: Any?) {
                        LocalLogUtils.writeLog("发声兮:发送结果:${any.toString()}", System.currentTimeMillis())
                        isCommit = false
                        transLayout.showContent()
                        if (!AppTools.isNetOk(this@SendAct)) {
                            showToast("网络连接异常")
                        } else {
                            showToast(any.toString())
                        }
                    }
                })
            }
        }
    }

    private fun checkShare(voiceId: String) {
        val dialog = DialogHintToWorld(this).setOnClickListener {
            when (it.id) {
                R.id.tv_Commit -> {
                    shareWorld(voiceId)
                }
                R.id.tv_Cancel -> {
                    showToast(resources.getString(R.string.string_sendAct_11))
                    finish()
                }
            }
        }
        dialog.show()
    }

    /**
     * 共享到世界
     */
    private fun shareWorld(voiceId: String) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/voices/$voiceId/share", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    showToast("分享成功，快去世界看看吧！")
                    EventBus.getDefault().post(ImplPageChangeEvent(1))
                } else {
                    showToast(result.msg)
                }
                finish()
            }

            override fun onFailure(any: Any?) {
                finish()
            }
        })
    }

    private fun saveCache() {
        if (isVisibleActivity) {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            DialogSaveCache(this@SendAct).setOnClickListener(View.OnClickListener {
                when (it.id) {
                    R.id.tv_Commit -> {
                        if (imgData.contains("相机")) {
                            imgData.remove("相机")
                        }
                        VoiceCacheDao(this@SendAct).insetData(loginBean.user_id, original, sendType, voiceType, topicId, topicName, Gson().toJson(imgData),
                                if (!TextUtils.isEmpty(moviesId)) moviesId else if (!TextUtils.isEmpty(musicId)) musicId else bookId,
                                if (!TextUtils.isEmpty(moviesId) || !TextUtils.isEmpty(bookId)) selectorScore.score else if (!TextUtils.isEmpty(musicId)) selectorMusic.getScore() else ""
                                , voiceLenth)
                        finish()
                    }
                    R.id.tv_Cancel -> {
                        finish()
                    }
                }
            }).show()
        }
    }

    /**
     * 数据异常  存在有图检测不到的情况
     */
    private fun calcMd5(): String {
        LocalLogUtils.writeLog("发声兮:计算图片的MD5值: ${imgData.size}", System.currentTimeMillis())
        val jsonArray = JSONArray()
        for (bean in imgData) {
            if ("相机" == bean) {
                continue
            }
            var endSuffix: String
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bean, options)
            val type = options.outMimeType ?: continue
            endSuffix = if (type.contains("gif", true)) {
                ".gif"
            } else {
                try {
                    bean.substring(bean.lastIndexOf("."), bean.length)
                } catch (e: Exception) {//图片中不包含后缀
                    when {
                        type.contains("png", true) -> ".png"
                        type.contains("jpeg", true) -> ".jpg"
                        else -> ".png"
                    }
                }
            }
            var suffix: String = TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())
            jsonArray.add(suffix + "_" + AppTools.getFileMD5(File(bean)) + endSuffix)
        }
        LocalLogUtils.writeLog("发声兮:计算图片的MD5结果: ${jsonArray.toJSONString()}", System.currentTimeMillis())
        return jsonArray.toJSONString()
    }

    private fun parseData(keys: List<String>): Array<UploadData?> {
        LocalLogUtils.writeLog("发声兮:解析上传的图片资源 ${keys.size}", System.currentTimeMillis())
        val imgArrays = arrayOfNulls<UploadData>(keys.size)
        for (a in keys.indices) {
            imgArrays[a] = UploadData(keys[a], imgData[a])
        }
        LocalLogUtils.writeLog("发声兮:解析上传的图片资源 ${imgArrays.size}", System.currentTimeMillis())
        return imgArrays
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUESTALBUM) {
                val list = data?.getSerializableExtra("result") as ArrayList<String>
                for (path in list) {
                    imgData.add(imgData.size - 1, path)
                }
                if (imgData.size >= 4) {
                    imgData.removeAt(3)
                }
                adapter.notifyDataSetChanged()
            } else if (requestCode == REQUESTTAG) {
                data?.getParcelableExtra<SearchTopicData.SearchTopicBean>("topicBean")?.let {
                    tvTopic.text = "#${it.topic_name}# X"
                    topicName = it.topic_name
                    topicId = it.topic_id
                    tv_AddTopic.visibility = View.GONE
                }
            } else if (requestCode == REQUEST_RECORD) {
                data?.let {
                    voiceLenth = it.getStringExtra("voiceLength")
                    original = it.getStringExtra("original")//原始本地路径
                    tv_Record.visibility = View.GONE
                    voiceAnimProgress.visibility = View.VISIBLE
                    voiceAnimProgress.data = BaseAnimBean(voiceLenth)
                    when (sendType) {
                        2 -> {//影评
                            if (!TextUtils.isEmpty(selectorScore.score)) {
                                tvPush.isEnabled = true
                                tvPush.isSelected = true
//                                tv_Commit.setTextColor(Color.WHITE)
                            }
                        }
                        5 -> {
                            if (!TextUtils.isEmpty(selectorScore.score)) {
                                tvPush.isEnabled = true
                                tvPush.isSelected = true
//                                tv_Commit.setTextColor(Color.WHITE)
                            }
                        }
                        4 -> {
                            if (!TextUtils.isEmpty(selectorMusic.getScore())) {
                                tvPush.isEnabled = true
                                tvPush.isSelected = true
//                                tv_Commit.setTextColor(Color.WHITE)
                            }
                        }
                        else -> {
                            tvPush.isEnabled = true
                            tvPush.isSelected = true
                        }
                    }
                }
            }
        } else {
            if (requestCode == REQUEST_RECORD) {
                if (resultCode == 100) {
                    voiceAnimProgress.visibility = View.GONE
                    tv_Record.visibility = View.VISIBLE
                    voiceLenth = null
                    original = null
                    tvPush.isEnabled = false
                    tvPush.isSelected = false
                }
            }
            if (resultCode == 999) {
                if (intent.getBooleanExtra("isOpenRecord", false)) {
                    this.resultCode = 999
                    finish()
                }
            }
        }
    }

    private var resultCode = -1

    override fun finish() {
        super.finish()
        if (resultCode != 999)
            overridePendingTransition(0, R.anim.operate_exit)
        else {
            overridePendingTransition(0, 0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            original?.let {
                DialogCancelCommit(this).setOnClickListener(View.OnClickListener {
                    if (!TextUtils.isEmpty(original)) {
                        val deleteFile = File(original)
                        if (deleteFile.exists()) {
                            deleteFile.delete()
                        }
                    }
                    finish()
                }).show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    override fun canBeSlideBack(): Boolean {
        return false
    }

}