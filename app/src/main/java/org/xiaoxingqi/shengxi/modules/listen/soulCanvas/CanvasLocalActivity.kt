package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.AnimatorSet
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_local_canvas.*
import kotlinx.android.synthetic.main.activity_local_canvas.btn_Back
import kotlinx.android.synthetic.main.activity_local_canvas.iv_Close
import kotlinx.android.synthetic.main.activity_local_canvas.relativeShowShareHint
import kotlinx.android.synthetic.main.activity_local_canvas.transLayout
import kotlinx.android.synthetic.main.activity_local_canvas.tvTopic
import kotlinx.android.synthetic.main.activity_local_canvas.tv_AddTopic
import kotlinx.android.synthetic.main.activity_local_canvas.tv_Send_HintText
import kotlinx.android.synthetic.main.activity_local_canvas.tv_Title
import kotlinx.android.synthetic.main.activity_send.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.ImpUpdatePaint
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.impl.SendMsgEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.SearchTopicData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.EditTopicSearchActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.paintView.BrushDrawingView
import org.xiaoxingqi.shengxi.wedgit.paintView.BrushViewChangeListener
import skin.support.SkinCompatManager
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException

class CanvasLocalActivity : BaseThemeNoSwipeActivity() {
    companion object {
        private const val REQUEST_PERMISSION_FILE = 0x00
        private const val REQUEST_TOPIC = 0x01
    }

    private var currentPenSize = 1
    private var currentEraserSize = 1
    private var colorDialog: DialogShowColors? = null
    private var artId = -1
    private var resourceType = "14"
    private var userId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_local_canvas
    }

    override fun initView() {
        view_black.isSelected = true
        iv_pen.isSelected = true
        val set = AnimatorSet()
        set.playTogether(ObjectAnimator.ofFloat(iv_eraser, "TranslationY", AppTools.dp2px(this, 10).toFloat())
                , ObjectAnimator.ofFloat(iv_eraser, "scaleX", 0.7f)
                , ObjectAnimator.ofFloat(iv_eraser, "scaleY", 0.7f)
        )
        set.duration = 0
        set.start()
        drawView.brushDrawingMode = true
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M && (OsUtil.isOppo() || OsUtil.isVivo())) || OsUtil.isMeizu()) {
            tv_Send_HintText.setPadding(0, AppTools.dp2px(this, 8), 0, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val artworkUrl = intent.getStringExtra("artworkUrl")
        topicId = intent.getStringExtra("topicId")
        topicName = intent.getStringExtra("topicName")
        /*if (!TextUtils.isEmpty(topicName)) {
            tv_AddTopic.visibility = View.GONE
            tvTopic.text = "#$topicName#X"
        }*/
        artId = intent.getIntExtra("artId", -1)
        if (!TextUtils.isEmpty(artworkUrl)) {
            tv_Title.text = "涂鸦"
            tv_send.text = resources.getString(R.string.string_23)
            userId = intent.getStringExtra("uid")
            val topicName = intent.getStringExtra("topicName")
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(artworkUrl)))
                    .load(artworkUrl)
                    .into(ivCanvasBg)
            tv_Send_HintText.text = resources.getString(R.string.string_graffiti_hint)
            tvTopic.isEnabled = false
            tv_AddTopic.visibility = View.INVISIBLE
            tv_AddTopic.isEnabled = false
            if (!TextUtils.isEmpty(topicName)) {
                tvTopic.isSelected = true
                tvTopic.text = "#$topicName#"
            }
            /* val infoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
             if (infoData.data.created_at + 86400 > System.currentTimeMillis() / 1000) {
                 val dtime = infoData.data.created_at + 86400 - System.currentTimeMillis() / 1000
                 DialogLimitCanvas(this).setTitle(resources.getString(R.string.string_limit_graffiti))
                         .setOnClickListenern(View.OnClickListener { finish() })
                         .setLimitInfo(dtime).show()
             }*/
            resourceType = "11"
        }
        if (artId != -1) {
            if (!SPUtils.getBoolean(this, IConstant.GRAFFITI_HINT + loginBean.user_id, false)) {
                relativeShowShareHint.visibility = View.VISIBLE
                linear_canvas.gravity = Gravity.CENTER
            } else {
                linear_canvas.gravity = Gravity.TOP
            }
        } else {
            if (!SPUtils.getBoolean(this, IConstant.ISPAINSENDHINT + loginBean.user_id, false)) {
                relativeShowShareHint.visibility = View.VISIBLE
                linear_canvas.gravity = Gravity.CENTER
            } else {
                linear_canvas.gravity = Gravity.TOP
            }
        }
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            drawView.brushColor = Color.BLACK
        } else {
            drawView.brushColor = Color.parseColor("#72727f")
        }
    }

    private fun anim(view: View, isAnim: Boolean) {
        val set = AnimatorSet()
        set.playTogether(ObjectAnimator.ofFloat(view, "TranslationY", if (isAnim) AppTools.dp2px(this, 10).toFloat() else 0f)
                , ObjectAnimator.ofFloat(view, "scaleX", if (isAnim) 0.7f else 1f)
                , ObjectAnimator.ofFloat(view, "scaleY", if (isAnim) 0.7f else 1f))
        set.duration = 200
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.isSelected = !view.isSelected
            }
        })
        set.start()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            DialogCancelCommit(this).setOnClickListener(View.OnClickListener {
                finish()
            }).show()
        }
        iv_Close.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (artId == -1) {
                SPUtils.setBoolean(this, IConstant.ISPAINSENDHINT + loginBean.user_id, true)
            } else {
                SPUtils.setBoolean(this, IConstant.GRAFFITI_HINT + loginBean.user_id, true)
            }
            relativeShowShareHint.visibility = View.GONE
            linear_canvas.gravity = Gravity.TOP
            linear_canvas.setPadding(0, AppTools.dp2px(this, 20), 0, 0)
        }
        frame_pen.setOnClickListener {
            setPenType()
            drawView.brushDrawingMode = true
            if (iv_pen.isSelected) {
//                anim(iv_pen, false)
//                anim(iv_eraser, true)
//            } else {
                val location = IntArray(2)
                it.getLocationOnScreen(location)
                DialogSelectorPenSize(this).setCurrentPenSize(currentPenSize).setOnClickListener(View.OnClickListener { view ->
                    when (view.id) {
                        R.id.iv_pen_size_1 -> {
                            currentPenSize = 1
                        }
                        R.id.iv_pen_size_2 -> {
                            currentPenSize = 2
                        }
                        R.id.iv_pen_size_3 -> {
                            currentPenSize = 3
                        }
                        R.id.iv_pen_size_4 -> {
                            currentPenSize = 4
                        }
                        R.id.iv_pen_size_5 -> {
                            currentPenSize = 5
                        }
                    }
                    drawView.brushSize = currentPenSize * 8f
                }).setLocation(location[0], location[1]).show()
            }
        }
        frame_eraser.setOnClickListener {
//            drawView.brushEraser()
//            drawView.brushColor = Color.parseColor("#f7f7f7")
            drawView.setEraseWidth(currentEraserSize * 15f)
            drawView.brushEraser()
            if (!iv_eraser.isSelected) {
                anim(iv_eraser, false)
                anim(iv_pen, true)
            }
            val location = IntArray(2)
            it.getLocationOnScreen(location)
            DialogSelectorEraserSize(this).setCurrentPenSize(currentEraserSize).setOnClickListener(View.OnClickListener { view ->
                when (view.id) {
                    R.id.iv_eraser_size_1 -> currentEraserSize = 1
                    R.id.iv_eraser_size_2 -> currentEraserSize = 2
                    R.id.iv_eraser_size_3 -> currentEraserSize = 3
                }
                drawView.setEraseWidth(currentEraserSize * 15f)
                drawView.brushEraser()
            }).setLocation(location[0], location[1]).show()
        }
        view_black.setOnClickListener {
            setPenType()
            if (!it.isSelected) {
                if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
                    drawView.brushColor = Color.BLACK
                } else {
                    drawView.brushColor = Color.parseColor("#72727f")
                }
                drawView.opacity = 255
                clearStatus()
                it.isSelected = !it.isSelected
            }
        }
        view_blue.setOnClickListener {
            setPenType()
            if (!it.isSelected) {
                clearStatus()
                it.isSelected = !it.isSelected
            }
            drawView.opacity = 255
            drawView.brushColor = Color.parseColor("#0B80FA")
        }
        view_green.setOnClickListener {
            setPenType()
            if (!it.isSelected) {
                clearStatus()
                it.isSelected = !it.isSelected
            }
            drawView.opacity = 255
            drawView.brushColor = Color.parseColor("#52D768")
        }
        view_yellow.setOnClickListener {
            setPenType()
            if (!it.isSelected) {
                clearStatus()
                it.isSelected = !it.isSelected
            }
            drawView.opacity = 255
            drawView.brushColor = Color.parseColor("#FFD12A")
        }
        view_red.setOnClickListener {
            setPenType()
            if (!it.isSelected) {
                clearStatus()
                it.isSelected = !it.isSelected
            }
            drawView.opacity = 255
            drawView.brushColor = Color.parseColor("#FD2D3F")
        }
        view_colors.setOnClickListener {
            setPenType()
            if (!it.isSelected) {
                clearStatus()
                it.isSelected = true
            }
            if (colorDialog == null)
                colorDialog = DialogShowColors(this).setOnResultListener(object : DialogShowColors.OnResultColorListener {
                    override fun resultColor(color: Int, alpha: Int) {
                        drawView.opacity = alpha
                        drawView.brushColor = color
                    }
                })
            colorDialog?.show()
        }
        tv_send.setOnClickListener {
            checkPermission()
        }
        tv_AddTopic.setOnClickListener {
            startActivityForResult(Intent(this, EditTopicSearchActivity::class.java)
                    .putExtra("topicType", 1)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    , REQUEST_TOPIC)
        }
        tvTopic.setOnClickListener {
            tvTopic.text = ""
            topicName = ""
            topicId = ""
            tv_AddTopic.visibility = View.VISIBLE
        }
        ivUndo.setOnClickListener {
            if (ivUndo.isSelected)
                drawView.undo()
        }
        ivRedo.setOnClickListener {
            if (ivRedo.isSelected)
                drawView.redo()
        }
        drawView.setBrushViewChangeListener(object : BrushViewChangeListener {
            override fun onViewRemoved(brushDrawingView: BrushDrawingView?) {
                ivRedo.isSelected = drawView.canRedo()
                ivUndo.isSelected = drawView.canUndo()
            }

            override fun onStartDrawing() {

            }

            override fun onStopDrawing() {
                ivRedo.isSelected = false
                ivUndo.isSelected = true
            }

            override fun onViewAdd(brushDrawingView: BrushDrawingView?) {
                ivRedo.isSelected = drawView.canRedo()
                ivUndo.isSelected = drawView.canUndo()
            }
        })
    }

    /**
     * 设置画笔的type
     */
    private fun setPenType() {
        if (!iv_pen.isSelected) {
            anim(iv_pen, false)
            anim(iv_eraser, true)
        }
    }

    /**
     * 检测是否有权限
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_FILE)
            } else {
                saveCanvas()
            }
        } else {
            saveCanvas()
        }
    }

    private fun clearStatus() {
        view_black.isSelected = false
        view_blue.isSelected = false
        view_green.isSelected = false
        view_yellow.isSelected = false
        view_red.isSelected = false
        view_colors.isSelected = false
    }

    /**
     * 上传文件
     */
    private fun upload(path: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceType", resourceType)
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(path))}${System.currentTimeMillis()}.jpg")
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                val data = result as QiniuStringData
                if (result.code == 0) {
                    result.data.bucket_id?.let {
                        bucketId = it
                    }
                    paintUri = result.data.resource_content
                    LocalLogUtils.writeLog("SoulPainter 上传截图", System.currentTimeMillis())
                    AliLoadFactory(this@CanvasLocalActivity, result.data.end_point, data.data.bucket, data.data.oss, object : LoadStateListener {
                        override fun progress(current: Long) {

                        }

                        override fun success() {
                            if (artId != -1) {
                                pushGraffiti()
                            } else
                                request(0)
                        }

                        override fun fail() {
                            File(path).delete()
                            LocalLogUtils.writeLog("SoulPainter :oss 上传错误", System.currentTimeMillis())
                            runOnUiThread {
                                showToast("上传失败，请稍后重试")
                                transLayout.showContent()
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {
                            File(path).delete()
                        }
                    }, UploadData(data.data.resource_content, path))
                } else {
                    LocalLogUtils.writeLog("SoulPainter : ${result.data} ", System.currentTimeMillis())
                    transLayout.showContent()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                LocalLogUtils.writeLog("SoulPainter=>Token获取错误 : ${any.toString()} ${if (AppTools.isNetOk(this@CanvasLocalActivity)) "网络正常" else "网络异常"}", System.currentTimeMillis())
                if (any is Exception) {
                    if (any is ConnectException) {
                        showToast("网络连接异常")
                    }
                }
                transLayout.showContent()
            }
        })
    }

    /**
     * 图片缓存至本地文件, 上传之后删除缓存图片
     */
    @SuppressLint("StaticFieldLeak")
    private fun saveCanvas() {
        transLayout.showProgress()
        squre_relative.buildDrawingCache()
        squre_relative.isDrawingCacheEnabled = true
        val createBitmap = Bitmap.createBitmap(squre_relative.drawingCache, 0, 0, squre_relative.width, squre_relative.height)
        squre_relative.destroyDrawingCache()
        if (createBitmap != null) {
            object : AsyncTask<Void, Void, String>() {
                override fun doInBackground(vararg params: Void?): String {
                    /**
                     * 保存图片到本地
                     */
                    val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.IMG_CACHE).let {
                        if (!it.exists()) {
                            it.mkdirs()
                        }
                        File("${it.absolutePath}/${System.currentTimeMillis()}.jpg")
                    }
                    val stream = FileOutputStream(file)
                    createBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.close()
                    return file.absolutePath
                }

                override fun onPostExecute(result: String?) {
                    val file = File(result)
                    if (file.exists()) {
                        upload(result!!)
                    } else {
                        showToast("发布失败")
                        transLayout.showContent()
                    }
                }
            }.execute()
        }
    }

    private var bucketId: String? = "0"
    private var topicName: String? = null
    private var topicId: String? = null
    private var paintUri: String? = null
    override fun request(flag: Int) {
        LocalLogUtils.writeLog("SoulPainter :发布: 上传数据到服务器", System.currentTimeMillis())
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val builder = FormBody.Builder()
                .add("bucketId", bucketId)
                .add("artworkUri", paintUri)
        if (!TextUtils.isEmpty(topicName)) {
            builder.add("topicName", topicName)
        }
        if (!TextUtils.isEmpty(topicId))
            builder.add("topicid", topicId)
        OkClientHelper.post(this, "users/${loginBean.user_id}/artwork", builder.build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                LocalLogUtils.writeLog("SoulPainter :发布:code:${result.code} msg:${result.msg}", System.currentTimeMillis())
                if (result.code == 0) {
                    EventBus.getDefault().post(ImpUpdatePaint(4, 0))
                    showToast("发布成功")
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                LocalLogUtils.writeLog("SoulPainter :发布: 发布错误 ${any.toString()}", System.currentTimeMillis())
            }
        }, "V3.6")
    }

    /**
     * 发布涂鸦作品
     */
    private fun pushGraffiti() {
        EventBus.getDefault().post(SendMsgEvent(AppTools.fastJson(userId, paintUri, artId.toString())))
        finish()
        /*val formBody = FormBody.Builder()
                .add("artworkId", artId.toString())
                .add("bucketId", bucketId)
                .add("graffitiUri", paintUri).build()
        OkClientHelper.post(this, "graffitis", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
//                      需要更新的地方
//                      1.大家的作品的角标
//                      2.作品的涂鸦列表
//                      3.自己的涂鸦足迹列表
                    EventBus.getDefault().post(ImpUpdatePaint(7, artId))
                    LocalLogUtils.writeLog("SoulPainter:发布涂鸦作品成功", System.currentTimeMillis())
                    finish()
                    showToast("发布成功")
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                LocalLogUtils.writeLog("SoulPainter:涂鸦作品异常 ${if (any is java.lang.Exception) any.message else any.toString()}", System.currentTimeMillis())
                transLayout.showContent()
            }
        }, "V4.1")*/
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TOPIC) {
                data?.let {
                    val topicBean = it.getParcelableExtra<SearchTopicData.SearchTopicBean>("topicBean")
                    tvTopic.text = "#${topicBean.topic_name}# X"
                    tv_AddTopic.visibility = View.GONE
                    topicName = topicBean.topic_name
                    topicId = topicBean.topic_id
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveCanvas()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogCancelCommit(this).setOnClickListener(View.OnClickListener {
                finish()
            }).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}