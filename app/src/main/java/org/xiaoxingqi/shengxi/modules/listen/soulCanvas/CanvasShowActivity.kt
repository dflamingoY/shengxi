package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.gw.swipeback.SwipeBackLayout
import kotlinx.android.synthetic.main.activity_canvas_show.*
import kotlinx.android.synthetic.main.activity_canvas_show.swipeBackLayout
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.utils.FileUtils
import org.xiaoxingqi.shengxi.utils.IConstant
import java.io.File

class CanvasShowActivity : BaseNormalActivity() {
    private lateinit var artUrl: String
    private lateinit var resourceId: String
    override fun getLayoutId(): Int {
        return R.layout.activity_canvas_show
    }

    override fun initView() {
        swipeBackLayout.directionMode = SwipeBackLayout.FROM_TOP
    }

    override fun initData() {
        artUrl = intent.getStringExtra("artworkUrl")
        resourceId = intent.getStringExtra("resourceId")
        if (intent.getBooleanExtra("isSend", false)) {
            frameOperator.visibility = View.VISIBLE
        }
        Glide.with(this)
                .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(artUrl)))
                .load(artUrl)
                .into(showImgView)
    }

    override fun initEvent() {
        tvSend.setOnClickListener {
            //发送给好友
            startActivity<SelectFriendActivity>("artworkUrl" to artUrl, "resourceId" to resourceId)
        }
        swipeBackLayout.setSwipeBackListener(object : SwipeBackLayout.OnSwipeBackListener {
            override fun onViewPositionChanged(mView: View?, swipeBackFraction: Float, swipeBackFactor: Float) {
                val alpha = (255 * (1 - swipeBackFraction)).toInt()
                swipeBackLayout.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
            }

            override fun onViewSwipeFinished(mView: View?, isEnd: Boolean) {
                if (isEnd) {
                    finish()
                }
            }
        })
        showImgView.setOnClickListener { finish() }
        showImgView.setOnLongClickListener {
            DialogSelectPhoto(this).hideAction(true).hindOther(false).setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                override fun itemView(view: View) {
                    when (view.id) {
                        R.id.tv_Save -> {
                            save()
                        }
                    }
                }
            }).show()
            false
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save() {
        var suffix = artUrl.substring(artUrl.lastIndexOf("/") + 1)
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
                    Glide.with(this@CanvasShowActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(artUrl)))
                            .downloadOnly()
                            .load(artUrl)
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
                    sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.act_exit_alpha)
    }
}