package org.xiaoxingqi.shengxi.modules.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.text.TextUtils
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_user_details.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNotSwipeActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.listen.alarm.downPlay
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.ArtUserRelationDelegate
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.showAchieve
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.overScroll.IOverScrollState
import org.xiaoxingqi.shengxi.wedgit.overScroll.OverScrollDecoratorHelper
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.math.pow
import kotlin.math.sqrt

class UserDetailsActivity : BaseNotSwipeActivity() {
    private var userInfoData: UserInfoData? = null
    private var uid: String? = null
    private var originalUser: UserInfoData? = null
    private var bottomData: DetailsBottomData? = null
    private var isGuide = false
    private lateinit var audioplayer: AudioPlayer
    private var dialog: DialogSelectPhoto? = null
    private var animatorButton: ObjectAnimator? = null
    private var relation = 0
    private var playViewWave = true
    private var isPrivacyRandomPlay = 2

    override fun getLayoutId(): Int {
        return R.layout.activity_user_details
    }

    override fun writeHeadSet(): Boolean {
        return try {
            val headsetOn = audioplayer.audioManager.isWiredHeadsetOn
            val a2dpOn = audioplayer.audioManager.isBluetoothA2dpOn
            val scoOn = audioplayer.audioManager.isBluetoothScoOn
            headsetOn || a2dpOn || scoOn
        } catch (e: Exception) {
            e.printStackTrace()
            super.writeHeadSet()
        }
    }

    override fun changSpeakModel(type: Int) {
        if (type == 1) {
            if (audioplayer.isPlaying) {
                val currentPosition = audioplayer.currentPosition
                audioplayer.stop()
                audioplayer.start(AudioManager.STREAM_MUSIC)
                audioplayer.seekTo(currentPosition.toInt())
            }
        } else {
            if (audioplayer.isPlaying) {
                val currentPosition = audioplayer.currentPosition
                audioplayer.stop()
                audioplayer.start(AudioManager.STREAM_VOICE_CALL)
                audioplayer.seekTo(currentPosition.toInt())
            }
        }
    }

    override fun initView() {
        animatorButton = ObjectAnimator.ofFloat(iv_Home, "translationX", AppTools.dp2px(this, 15).toFloat())
        animatorButton?.duration = 2000
        animatorButton?.repeatMode = ObjectAnimator.REVERSE
        animatorButton?.repeatCount = ObjectAnimator.INFINITE
        val interpolator = BounceInterpolator()
        animatorButton?.interpolator = interpolator
    }

    override fun initData() {
        originalUser = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        uid = intent.getStringExtra("id")
        request(0)
        if (uid != originalUser?.data?.user_id) {
            iv_Home.text = resources.getString(R.string.string_open_voice_gallery).replace("我", "ta")
            linear_Bottom.visibility = View.VISIBLE
            guideView.setBackgroundResource(R.drawable.draw_other_center_bg)
            guideView.setHintText(resources.getString(R.string.string_empty_desc_me_5))
            iv_setting.setImageResource(R.mipmap.icon_home_user_other)
            tv_Set.visibility = View.GONE
            request(2)
        } else {
            tv_Set.visibility = View.VISIBLE
            tv_OtherHint.visibility = View.GONE
            guideView.setBackgroundResource(R.drawable.draw_self_center_bg)
            linear_Bottom.visibility = View.GONE
            iv_setting.setImageResource(R.mipmap.icon_user_setting)
            SPUtils.getString(this, IConstant.USER_CACHE_COVER_LIST + uid, null).let {
                if (!TextUtils.isEmpty(it)) {
                    it.split(",").filter { predicate ->
                        predicate != "," && predicate != ""
                    }.let { list ->
                        if (list.isEmpty()) {
                            if (!TextUtils.isEmpty(originalUser!!.data.main_cover_photo_url)) {
                                glideUtil.loadGlide(originalUser!!.data.main_cover_photo_url, iv_Cover, 0, glideUtil.getLastModified(originalUser!!.data.main_cover_photo_url))
                                iv_Cover.setImageUrl(originalUser!!.data.main_cover_photo_url)
                            } else
                                Glide.with(this)
                                        .load(R.drawable.drawable_default_bg_1)
                                        .apply(RequestOptions().centerCrop())
                                        .into(iv_Cover)
                        } else {
                            val url = list[Random().nextInt(list.size)]
                            Glide.with(this)
                                    .load(url)
                                    .apply(RequestOptions().centerCrop().error(R.drawable.drawable_default_bg_1))
                                    .into(iv_Cover)
                            iv_Cover.setImageUrl(url)
                        }
                    }
                } else {
                    Glide.with(this)
                            .load(R.drawable.drawable_default_bg_1)
                            .apply(RequestOptions().centerCrop())
                            .into(iv_Cover)
                }
            }
        }
        request(11)
        audioplayer = AudioPlayer(this, null, object : OnPlayListenAdapter() {
            override fun onPrepared() {
                isPlayed = true
                anim(if (playViewWave) iv_Play else ivRandomPlayer)
                userInfoData!!.data.isPlaying = true
            }

            override fun onCompletion() {
                animator?.end()
                animator = null
                userInfoData!!.data.isPlaying = false
            }

            override fun onInterrupt() {
                animator?.end()
                animator = null
                userInfoData!!.data.isPlaying = false
            }
        })
    }

    private var animator: ObjectAnimator? = null
    private fun anim(view: View) {
        animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        animator?.duration = 15000
        animator?.interpolator = LinearInterpolator()
        animator?.repeatCount = ObjectAnimator.INFINITE
        animator?.start()
    }

    override fun onResume() {
        super.onResume()
        isShowDialog = true
    }

    override fun onStart() {
        super.onStart()
        animatorButton?.start()
    }

    override fun onStop() {
        super.onStop()
        animatorButton?.end()
    }

    private var isOpen = false
    private var isShowDialog = true
    private var clickTime = 0L

    override fun initEvent() {
        val scrollDecor = OverScrollDecoratorHelper.setUpStaticOverScroll(iv_Cover, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
        scrollDecor.setOverScrollUpdateListener { _, state, offset ->
            if (state == IOverScrollState.STATE_BOUNCE_BACK) {//空闲状态
                if (offset <= -50 && !isOpen) {
                    isOpen = true
                    isShowDialog = false
                    when (relation) {
                        3 -> //被对方拉黑
                            showToast(resources.getString(R.string.string_user_details_blacked))
                        4 -> //自己拉黑对方
                            showToast(resources.getString(R.string.string_user_details_blacks))
                        else -> startActivity(Intent(this, UserHomeActivity::class.java)
                                .putExtra("friendCount", userInfoData?.data?.friend_num)
                                .putExtra("totalLength", userInfoData?.data?.voice_total_len)
                                .putExtra("relation", relation)
                                .putExtra("id", uid))
                    }
                }
            }
            if (offset == 0f) {
                isOpen = false
            }
        }
        /**
         * 1 判断是否是自己
         * 2 判断是否在对方的黑名单
         * 3 判断是否是好友
         * 4 判断是否设置访问条件
         */
        iv_Play.setOnClickListener {
            sendObserver()
            if (userInfoData == null)
                return@setOnClickListener
            animator?.end()
            animator?.cancel()
            animator = null
            if (audioplayer.isPlaying && playViewWave) {
                audioplayer.stop()
                return@setOnClickListener
            } else
                audioplayer.stop()
            if (userInfoData == null) {
                return@setOnClickListener
            }
            try {
                if (TextUtils.isEmpty(userInfoData!!.data.wave_url)) {
                    /*if (uid == originalUser?.data?.user_id) {
                        if ((System.currentTimeMillis() - clickTime) < 500) {
                            return@setOnClickListener
                        }
                        clickTime = System.currentTimeMillis()
                        startActivityForResult(Intent(this, RecordVoiceActivity::class.java)
                                .putExtra("resourceType", "1")
                                .putExtra("recordType", 2), REQUEST_EDIT_VOICE)
                        overridePendingTransition(0, 0)

                    } else {
                    }*/
                    showToast(resources.getString(R.string.string_empty_voice_1))
                    return@setOnClickListener
                }
                OkClientHelper.downWave(this, userInfoData!!.data.wave_url, { o ->
                    try {
                        if (null == o) {
                            showToast(resources.getString(R.string.string_error_file))
                            return@downWave
                        }
                        if (!audioplayer.isPlaying) {
                            audioplayer.setDataSource(o.toString())
                            if (SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)) {
                                audioplayer.start(AudioManager.STREAM_VOICE_CALL)
                            } else {
                                audioplayer.start(AudioManager.STREAM_MUSIC)
                            }
                            playViewWave = true
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }, { volleyError -> showToast(VolleyErrorHelper.getMessage(volleyError)) })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        btn_Back.setOnClickListener { finish() }
        linearLike.setOnClickListener {
            request(9)
        }
        linear_Chat.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (loginBean.user_id == "1") {
                userInfoData?.let {
                    startActivity(Intent(this@UserDetailsActivity, ChatActivity::class.java)
                            .putExtra("userName", userInfoData?.data?.nick_name)
                            .putExtra("chatId", userInfoData?.data?.chat_pri_id)
                            .putExtra("uid", userInfoData?.data?.user_id))
                }
            } else
                if (!linear_Chat.isSelected)
                    bottomData?.let {
                        if (it.data.chatPri.status == 0)
                            userInfoData?.let {
                                /**
                                 * 添加聊天标记到服务器
                                 */
                                request(8)
                            }
                    }
        }
        /**
         * 1 判断是否在自己黑名单
         * 2 判断是否在对方黑名单
         */
        linear_MkFriend.setOnClickListener {
            //加好友
            if (!linear_MkFriend.isSelected)
                addFriend()
        }
        linearSet.setOnClickListener {
            //如果是自己  如果是非好友
            if (uid == originalUser?.data?.user_id) {
                startActivity(Intent(this, SettingActivity::class.java))
            } else {
                userInfoData?.let {
                    DialogFriendsOperator(this).setInWhiteList(it.data.in_whitelist).setRelation(it.data.relation_status, it.data.user_status, it.data.flag, it.data.released_at).setOnClickListener(View.OnClickListener { dialog ->
                        when (dialog.id) {
                            R.id.tv_Report -> {
                                if (userInfoData?.data?.relation_status == 2) {//已拉黑
                                    removeBlack()
                                } else {
                                    DialogReportContent(this).setOnResultListener(OnReportItemListener { report ->
                                        report(report)
                                    }).show()
                                }
                            }
                            R.id.tv_Remark -> {
                                startActivityForResult(Intent(this, EditUserNameActivity::class.java)
                                        .putExtra("originName", it.data.nick_name_true)
                                        .putExtra("isName", false)
                                        .putExtra("name", userInfoData!!.data.nick_name), REQUEST_EDIT_NAME)
                            }
                            R.id.tv_Delete -> {
                                /**
                                 * 删除好友
                                 */
                                DialogDeleteFriend(this).setOnClickListener(View.OnClickListener { deleteFriend() }).show()
                            }
                            R.id.tv_admin_warn -> {//警告
                                operatorAdmin("warn", "1")
                            }
                            R.id.tv_admin_limit -> {//限制交流
                                operatorAdmin("confine", if (System.currentTimeMillis() / 1000 < it.data.released_at) "0" else "1")
                            }
                            R.id.tv_admin_injury -> {//仙人掌
                                operatorAdmin("flag", if ("1" == it.data.flag) "0" else "1")
                            }
                            R.id.tv_admin_close -> {//封号
                                operatorAdmin("userStatus", if ("3" == it.data.user_status) "1" else "3")
                            }
                            R.id.tv_add_white_list -> {
                                if (it.data.in_whitelist == 0) {
                                    DialogAddWhiteList(this@UserDetailsActivity).setOnClickListener(View.OnClickListener {
                                        addWhiteList()
                                    }).show()
                                } else {
                                    deleteWhiteList()
                                }
                            }
                            R.id.tv_report_normal -> {
                                DialogNormalReport(this).setOnClickListener(View.OnClickListener { report ->
                                    when (report.id) {
                                        R.id.tv_Attach -> {
                                            reportNormalItem("1")
                                        }
                                        R.id.tv_Porn -> {
                                            reportNormalItem("2")
                                        }
                                        R.id.tv_Junk -> {
                                            reportNormalItem("3")
                                        }
                                        R.id.tv_illegal -> {
                                            reportNormalItem("4")
                                        }
                                    }
                                }).show()

                            }
                        }
                    }).show()
                }
            }
        }
        iv_Home.setOnClickListener {
            userInfoData?.let {
                when (relation) {
                    3 -> //被对方拉黑
                        showToast(resources.getString(R.string.string_user_details_blacked))
                    4 -> //自己拉黑对方
                        showToast(resources.getString(R.string.string_user_details_blacks))
                    else -> {
                        if (System.currentTimeMillis() - clickTime < 1000) {
                            return@setOnClickListener
                        }
                        clickTime = System.currentTimeMillis()
                        startActivity(Intent(this, UserHomeActivity::class.java)
                                .putExtra("friendCount", it.data.friend_num)
                                .putExtra("totalLength", it.data.voice_total_len)
                                .putExtra("relation", relation)
                                .putExtra("id", it.data.user_id))
                    }

                }
            }
        }
        iv_Cover.setOnLongClickListener {
            userInfoData?.let {
                if (null != dialog) {
                    if (dialog?.isShowing!!) {
                        dialog!!.dismiss()
                    }
                }
                dialog = DialogSelectPhoto(this).hideAction(true).hindOther(false).setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                    override fun itemView(view: View) {
                        when (view.id) {
                            R.id.tv_Save -> {
                                save(iv_Cover.url)
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
        iv_Cover.setOnClickListener {
            animBottom()
        }
        tvUserName.setOnClickListener {
            userInfoData?.let {
                if (originalUser?.data?.user_id == uid) {
                    startActivity(Intent(this, UserInfoActivity::class.java))
                }
            }
        }
        tv_Desc.setOnClickListener {
            userInfoData?.let {
                if (originalUser?.data?.user_id == uid) {
                    startActivity(Intent(this, UserInfoActivity::class.java))
                }
            }
        }
        ivRandomPlayer.setOnClickListener {
            if (it.visibility != View.VISIBLE)
                return@setOnClickListener
            sendObserver()
            if (audioplayer.isPlaying && !playViewWave) {
                audioplayer.stop()
                return@setOnClickListener
            } else
                audioplayer.stop()
            animator?.let { anim ->
                //停止播放
                anim.end()
                anim.cancel()
            }
            animator = null
            request(10)
        }
    }

    private var dialogPwd: DialogCommitPwd? = null

    private fun operatorAdmin(key: String, value: String) {
        dialogPwd = DialogCommitPwd(this).setOperator(key, value).setOnResultListener(DialogAdminPwdListener { key_, value_, pwd ->
            adminUpdate(FormBody.Builder().add(key_, value_).add("confirmPasswd", pwd).build())
        })
        dialogPwd?.show()
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(url: String?) {
        if (TextUtils.isEmpty(url)) {
            showToast(resources.getString(R.string.string_save_cover_fail_1))
            return
        }
        transLayout.showProgress()
        var suffix = System.currentTimeMillis().toString() + url!!.substring(url.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg") && !suffix.contains(".png") && !suffix.contains(".gif")) {
            suffix = "$suffix.png"
        }
        object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(this@UserDetailsActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(System.currentTimeMillis().toString())))
                            .load(url)
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
                transLayout.showContent()
            }
        }.execute()
    }

    private fun animBottom() {
        if (isPlaying) {
            return
        }
        if (isShow) {
            val anim = ValueAnimator.ofInt(AppTools.dp2px(this, 44), 0)
            anim.addUpdateListener {
                val value = it.animatedValue as Int
                val params = linear_Bottom.layoutParams
                params.height = value
                linear_Bottom.layoutParams = params
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isPlaying = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isPlaying = false
                    isShow = false
                }
            })
            anim.duration = 400
            anim.start()
        } else {
            val anim = ValueAnimator.ofInt(0, AppTools.dp2px(this, 44))
            anim.addUpdateListener {
                val value = it.animatedValue as Int
                val params = linear_Bottom.layoutParams
                params.height = value
                linear_Bottom.layoutParams = params
            }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isPlaying = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isPlaying = false
                    isShow = true
                }
            })
            anim.duration = 400
            anim.start()
        }
    }

    private var isPlaying = false
    private var isShow = true
    private var isFirstAnim = false

    private fun endAnim() {
        try {
            if (!isFirstAnim) {
                val local = IntArray(2)
                iv_Play.getLocationInWindow(local)
                val reveal = ViewAnimationUtils.createCircularReveal(iv_Loading, local[0] + iv_Play.width / 2,
                        local[1] + iv_Play.height / 2, sqrt(AppTools.getWindowsWidth(this).toDouble().pow(2.0)
                        + AppTools.getWindowsHeight(this).toDouble().pow(2.0)).toFloat(), 0f)
                reveal.duration = 600
                reveal.start()
                reveal.addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: android.animation.Animator?) {

                    }

                    override fun onAnimationEnd(animation: android.animation.Animator?) {
                        /**
                         * 有部分机型出现 动画结束在非UI 线程, 使用handler
                         */
                        runOnUiThread {
                            iv_Loading.visibility = View.GONE
                            isFirstAnim = true
                        }
                    }

                    override fun onAnimationCancel(animation: android.animation.Animator?) {

                    }

                    override fun onAnimationStart(animation: android.animation.Animator?) {

                    }
                })

            }
        } catch (e: Exception) {
            iv_Loading.visibility = View.GONE
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                /**
                 * 查询基本信息
                 */
                transLayout.showProgress()
                OkClientHelper.get(this, "users/$uid", UserInfoData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        userInfoData = result as UserInfoData
                        request(12)
                        tvUserName.text = result.data.nick_name
                        tv_Desc.text = if (TextUtils.isEmpty(result.data.self_intro)) {
                            resources.getString(R.string.string_empty_desc_me_1)
                        } else {
                            result.data.self_intro
                        }
                        if (originalUser!!.data.user_id != result.data.user_id) {
                            request(1)
                            if (result.data.relation_status == 2) {
                                //已拉黑
                                ivRandomPlayer.visibility = View.INVISIBLE
                            } else if (result.data.relation_status == 1) {
                                relation = 2
                                ivRandomPlayer.visibility = View.VISIBLE
                            } else {
                                if (isPrivacyRandomPlay == 1) {
                                    ivRandomPlayer.visibility = View.INVISIBLE
                                }
                            }
                        }
                        if (result.data.user_id == originalUser?.data?.user_id) {//自己
                            viewCoverLayer.visibility = View.GONE
                            if (!isGuide) {
                                guideView.startGuide()
                                isGuide = true
                            }
                            if (TextUtils.isEmpty(iv_Cover.url)) {
                                glideUtil.loadGlide(result.data.main_cover_photo_url, iv_Cover, 0, glideUtil.getLastModified(result.data.main_cover_photo_url))
                                iv_Cover.setImageUrl(result.data.main_cover_photo_url)
                            }
                        }
                        endAnim()
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showOffline()
                    }
                }
                )
            }
            1 -> {
                /**
                 * 查询底部显示的按钮样式
                 */
                transLayout.showProgress()
                OkClientHelper.get(this, "users/$uid/bottom", DetailsBottomData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        bottomData = result as DetailsBottomData
                        if (result.code == 0) {
                            linear_MkFriend.text = when (result.data.friend.status) {
                                0 -> "加室友"
                                1 -> resources.getString(R.string.string_pending)
                                else -> "已是室友"
                            }
                            linear_MkFriend.isSelected = result.data.friend.status != 0
                            linear_Chat.isSelected = result.data.chatPri.status == 1
                            if (!TextUtils.isEmpty(result.data.chatPri.tipsName)) {
                                linear_Chat.text = result.data.chatPri.tipsName
                            }
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            2 -> {
                OkClientHelper.get(this, "relations/$uid", RelationData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 查询用户关系, 是否是好友
                         */
                        result as RelationData
                        if (result.code == 0) {
                            relation = result.data.friend_status
                            if (result.data.friend_status == 3 || result.data.friend_status == 4) {
                                ivRandomPlayer.visibility = View.INVISIBLE
                            } else {
                                if (relation != 2)
                                    ivRandomPlayer.visibility = if (isPrivacyRandomPlay == 1) View.INVISIBLE else View.VISIBLE
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            8 -> {
                //能否聊天
                transLayout.showProgress()
                OkClientHelper.get(this, "chats/check/$uid/0", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            startActivity(Intent(this@UserDetailsActivity, ChatActivity::class.java)
                                    .putExtra("userName", userInfoData?.data?.nick_name)
                                    .putExtra("chatId", userInfoData?.data?.chat_pri_id)
                                    .putExtra("uid", userInfoData?.data?.user_id))
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
            9 -> {
                //检测用户是否表白过
                transLayout.showProgress()
                OkClientHelper.get(this, "confessions?toUserId=$uid", ConfessionsData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as ConfessionsData
                        if (result.code == 0) {
                            if (result.data.confess_id == 0)
                                DialogLikeHint(this@UserDetailsActivity).setOnClickListener(View.OnClickListener {
                                    when (it.id) {
                                        R.id.tv_Cancel -> {
                                            like(0)
                                        }
                                        R.id.tv_Submit -> {
                                            like(7)
                                        }
                                        R.id.tv_right -> {
                                            like(-1)
                                        }
                                    }
                                }).show()
                            else {
                                showToast("每天可以表白一次，今天已经表白过了")
                            }
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            10 -> {
                //获取随机的播放源
                transLayout.showProgress()
                OkClientHelper.get(this, "users/$uid/voices/random", DynamicDatailData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as DynamicDatailData
                        if (result.code == 0 && result.data != null) {
                            if (!TextUtils.isEmpty(result.data.voice_url)) {
                                downPlay(result.data.voice_url) { it, _ ->
                                    playViewWave = false
                                    audioplayer.setDataSource(it)
                                    audioplayer.start(if (SPUtils.getBoolean(this@UserDetailsActivity, IConstant.ISEARPIECE, false)) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC)
                                    /*if (uid == originalUser?.data?.user_id) {
                                        if (TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()) != SPUtils.getString(this@UserDetailsActivity, IConstant.FIRST_PUSH_ACHIEVEMENT + uid, "")) {
                                            //提交成就播放
                                            pushTravel(uid!!)
                                        }
                                    }*/
                                }
                            }
                        } else
                            showToast("ta还没发过心情")
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.2")
            }
            11 -> {
                //非自己查看是否允许显示miniMachine
                OkClientHelper.get(this, "users/${uid}/settings?settingName=&settingTag=moodbook", NewVersionSetData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetData
                        //默认可见
                        result.data?.let { data ->
                            data.forEach {
                                when (it.setting_name) {
                                    "cover_brightness" -> {
                                        viewCoverLayer.visibility = if (it.setting_value == 2) View.GONE else View.VISIBLE
                                    }
                                    "cover_efficacy" -> {
                                        flowView.startView(when (it.setting_value) {
                                            1 -> IConstant.THEME_RAIN
                                            2 -> IConstant.THEME_SNOW
                                            3 -> IConstant.THEME_LEAVES
                                            4 -> IConstant.THEME_FLOWER
                                            5 -> IConstant.THEME_MAPLE
                                            else -> null
                                        })
                                    }
                                    "minimachine_visibility" -> {
                                        //非同步的请求
                                        isPrivacyRandomPlay = it.setting_value
                                        if (uid != originalUser?.data?.user_id) {
                                            if (relation != 3 && relation != 4 && relation != 2) {
                                                ivRandomPlayer.visibility = if (it.setting_value == 1) View.INVISIBLE else View.VISIBLE
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            12 -> {
                OkClientHelper.get(this, "users/${uid}/covers?coverName=moodbook_cover", NewVersionCoverData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionCoverData
                        if (result.code == 0) {
                            result.data?.let {
                                //随机展示图片
                                val url = it[Random().nextInt(it.size)].coverUrl
                                Glide.with(this@UserDetailsActivity)
                                        .load(url)
                                        .apply(RequestOptions().centerCrop().error(R.drawable.drawable_default_bg_1))
                                        .into(iv_Cover)
                                iv_Cover.setImageUrl(url)
                            }
                            if (result.data == null || result.data.isEmpty()) {
                                if (userInfoData?.let {
                                            if (!TextUtils.isEmpty(it.data.main_cover_photo_url)) {
                                                glideUtil.loadGlide(it.data.main_cover_photo_url, iv_Cover, 0, glideUtil.getLastModified(it.data.main_cover_photo_url))
                                                iv_Cover.setImageUrl(it.data.main_cover_photo_url)
                                            } else {
                                                Glide.with(this@UserDetailsActivity)
                                                        .load(R.drawable.drawable_default_bg_1)
                                                        .apply(RequestOptions().centerCrop())
                                                        .into(iv_Cover)
                                            }
                                            it
                                        } == null) {
                                    Glide.with(this@UserDetailsActivity)
                                            .load(R.drawable.drawable_default_bg_1)
                                            .apply(RequestOptions().centerCrop())
                                            .into(iv_Cover)
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.2")
            }
        }
    }

    /**
     * 加入白名单
     */
    private fun addWhiteList() {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/whitelist", FormBody.Builder().add("toUserId", uid).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
//                    showToast("已经对方加入白名单")
                    userInfoData?.data?.in_whitelist = 1
                    DialogAddWhiteHint(this@UserDetailsActivity).show()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun reportNormalItem(type: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceId", uid)
                .add("reportType", type)
                .add("resourceType", "4")
                .build()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0)
                    showToast(resources.getString(R.string.string_report_success))
                else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 举报
     */
    private fun report(type: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceType", "4")
                .add("reasonType", type)
                .add("resourceId", uid)
                .add("toUserId", uid)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/shield", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    EventBus.getDefault().post(IDeleteFriends(uid))
                    EventBus.getDefault().post(INotifyFriendStatus(0, uid))
                    DialogPBBlack(this@UserDetailsActivity).setType(type).show()
                    //4.3.7 加入判断是否动态拉黑对方
                    ArtUserRelationDelegate.getInstance().plus(uid!!, 4)
                    request(0)
                    request(1)
                    request(2)
                } else {
                    showToast(result.msg)
                }

            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.4")
    }

    /**
     * 移除黑名单
     */
    private fun removeBlack() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "users/${originalUser?.data?.user_id}/blacklist/$uid", FormBody.Builder().add("blackType", "0")
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    EventBus.getDefault().post(INotifyFriendStatus(5, uid))
                    showToast("取消屏蔽成功")
                    //4.3.7 加入判断是否动态拉黑对方
                    ArtUserRelationDelegate.getInstance().plus(uid!!, 0)
                    request(0)
                    request(1)
                    request(2)
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

    /**
     * 加入黑名单
     */
    private fun addBlack() {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("toUserId", uid)
                .build()
        OkClientHelper.post(this, "users/${originalUser?.data?.user_id}/blacklist", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    showToast("拉黑成功")
                    EventBus.getDefault().post(IDeleteFriends(uid))
                    request(0)
                    request(1)
                    request(2)
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

    /**
     * 移除白名单
     */
    private fun deleteWhiteList() {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(this, "users/${loginBean.user_id}/whitelist/$uid", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已经对方移除白名单")
                    userInfoData?.data?.in_whitelist = 0
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun addFriend() {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("toUserId", uid)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    linear_MkFriend.isSelected = true
                    linear_MkFriend.text = resources.getString(R.string.string_pending)
                    /*if (SPUtils.getInt(this@UserDetailsActivity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        DialogCreateVoice(this@UserDetailsActivity).show()
                    } else*/ if (SPUtils.getBoolean(this@UserDetailsActivity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        DialogUserSet(this@UserDetailsActivity).setOnClickListener(View.OnClickListener {
                            addWhiteList()
                        }).show()
                    }
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

    /**
     * 匿名表白
     */
    private fun like(isOpen: Int) {
        transLayout.showProgress()
        userInfoData?.let {
            val formBody = FormBody.Builder()
                    .add("toUserId", uid)
                    .add("openDay", isOpen.toString())
                    .build()
            OkClientHelper.post(this, "confessions", formBody, BaseRepData::class.java, object : OkResponse {
                override fun success(result: Any?) {
                    result as BaseRepData
                    if (result.code == 0) {
                        when (isOpen) {
                            -1 -> {
                                showToast("表白成功")
                            }
                            0 -> {
                                showToast(resources.getString(R.string.string_admire_Succes))
                            }
                            7 -> {
                                showToast(resources.getString(R.string.string_admire_Succes))
                            }
                        }
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
    }

    /**
     * 删除好友
     */
    private fun deleteFriend() {
        OkClientHelper.delete(this, "users/${originalUser?.data?.user_id}/friends/$uid", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    EventBus.getDefault().post(INotifyFriendStatus(0, uid))
                    EventBus.getDefault().post(IDeleteFriends(uid))
                    request(0)
                    request(1)
                }
            }

            override fun onFailure(any: Any?) {


            }
        })
    }

    /**
     *更新备注
     */
    private fun updateRemark(remark: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("nickName", remark)
                .build()
        OkClientHelper.patch(this, "users/${originalUser?.data?.user_id}/friends/$uid", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    userInfoData!!.data.nick_name = remark
                    tvUserName.text = remark
                    EventBus.getDefault().post(UpdateFriendInfoEvent(uid, 0, remark, null, null))
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

    /**
     * 更新声波
     */
    private fun updateWave(formBody: FormBody) {
        OkClientHelper.patch(this, "users/$uid", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0)
                    request(0)
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }


    private fun adminUpdate(formBody: FormBody) {
        OkClientHelper.patch(this, "admin/users/$uid", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code != 0) {
                    dialogPwd?.setCallBack()
                } else {
                    dialogPwd?.dismiss()
                    showToast("操作已执行")
                    request(0)
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    fun pushTravel(userId: String) {
        OkClientHelper.post(this, "achievement", FormBody.Builder().add("achievementType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SPUtils.setString(this@UserDetailsActivity, IConstant.FIRST_PUSH_ACHIEVEMENT + userId, TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()))
                    showAchieve(type = 1)
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        }, "V4.3")

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateFriendEvent(event: UpdateFriendInfoEvent) {
        try {
            if (!isVisibleActivity) {
                if (event.type == 1) {
                    if (uid == event.userId) {
                        tvUserName.text = event.nickName
                    }
                } else if (event.type == 2) {
                    if (uid == event.userId) {
                        tv_Desc.text = event.desc
                    }
                }
            }
        } catch (e: Exception) {

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_EDIT_NAME) {
                data?.let {
                    updateRemark(it.getStringExtra("name"))
                }
            } else if (requestCode == REQUEST_EDIT_VOICE) {
                val voicePath = data?.getStringExtra("voice")
                val voiceLength = data?.getStringExtra("voiceLength")
                val formBody = FormBody.Builder()
                        .add("waveUri", voicePath)
                        .add("waveLen", voiceLength)
                        .build()
                //TODO 更新声波
                transLayout.showProgress()
                updateWave(formBody)
            }
        }
        if (requestCode == REQUEST_EDIT_INFO) {
            request(0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun stopEvent(event: StopPlayInterFace) {
        /**
         * 焦点不在当前Fragment 或者不在当前Activity时,需要clear所有状态
         */
        if (!isVisibleActivity) {
            if (userInfoData != null) {
                if (userInfoData!!.data.isPlaying) {
                    userInfoData!!.data.isPlaying = false
                }
            }
            animator?.end()
            animator?.cancel()
            animator = null
            audioplayer.stop()
        }
    }

    /**
     * 更新好友请求的状态,
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 2) {
            if (uid == event.userId) {//是当前用户的状态变更
                request(0)
                request(1)
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Activity, url: String?, id: String?, imageView: View) {
            val intent = Intent(context, UserDetailsActivity::class.java)
            intent.putExtra("id", id)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                    imageView, "")//与xml文件对应
            ActivityCompat.startActivity(context, intent, null)
        }

        const val REQUEST_EDIT_NAME = 0x99
        const val REQUEST_EDIT_INFO = 0x98
        const val REQUEST_EDIT_VOICE = 0x97
    }

    override fun finish() {
        super.finish()
        audioplayer.stop()
    }

    override fun onDestroy() {
        animatorButton?.let {
            if (it.isRunning) {
                it.end()
                it.cancel()
            }
        }
        super.onDestroy()
        iv_Cover.background = null
        animatorButton = null
    }

}