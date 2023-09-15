package org.xiaoxingqi.shengxi.modules.user.frag.talkAlbum

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_add_item.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogConfirmCancelAlbum
import org.xiaoxingqi.shengxi.impl.IUpdateAlbumEvent
import org.xiaoxingqi.shengxi.impl.IUpdateTalkAlbumEvent
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.impl.OnTalkAddAlbumEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddItemCoverActivity
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddItemNameActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.DialogAddTalkAlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TalkCreateAlbumActivity : BaseThemeNoSwipeActivity() {
    companion object {
        private const val REQUEST_PERMISSION = 0x00
        private const val REQUEST_GALLERY = 0x01
        private const val REQUEST_CROP = 0x02
        private const val REQUEST_NAME = 0x03
    }

    private var talkId: String? = null
    private var coverPath: String? = null
    private var albumName: String? = null
    private lateinit var imgName: String
    private var originalAlbumId: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_add_item
    }

    override fun initView() {
        tv_title.text = "添加对话专辑"
        tv_push.text = "完成"
        relative_album.visibility = View.VISIBLE
        tv_cover_title.text = "封面"
        view_singer.setTitle("专辑名")
        view_singer.visibility = View.GONE
        view_music.visibility = View.GONE
        view_album.visibility = View.GONE
    }

    override fun initData() {
        talkId = intent.getStringExtra("talkId")
        originalAlbumId = intent.getStringExtra("originalAlbumId")
        imgName = UUID.randomUUID().toString()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            if (!TextUtils.isEmpty(coverPath) || !TextUtils.isEmpty(albumName)) {
                DialogConfirmCancelAlbum(this).setOnClickListener(View.OnClickListener {
                    finish()
                }).show()
            } else
                finish()
        }
        relative_cover.setOnClickListener {
            checkPermission()
        }
        relative_album.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("count", 20)
                    .putExtra("name", albumName)
                    .putExtra("title", resources.getString(R.string.string_add_album_name)), REQUEST_NAME)
        }
        tv_push.setOnClickListener {
            if (originalAlbumId == null) {
                request(0)
            } else {
                delete()
            }
        }
    }

    private fun delete() {
        if (!TextUtils.isEmpty(originalAlbumId)) {
            transLayout.showProgress()
            OkClientHelper.delete(this, "albumDialogs/${originalAlbumId}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                override fun onFailure(any: Any?) {
                    request(0)
                }

                override fun success(result: Any?) {
                    request(0)
                }
            }, "V4.3")
        }
    }

    override fun request(flag: Int) {
        if (TextUtils.isEmpty(coverPath)) {
            return
        }
        if (File(coverPath).length() == 0L) {
            showToast("封面图已被删除")
            return
        }
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceType", "23")
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(coverPath))}.png")
                .add("needBaseUri", "1")
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as QiniuStringData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(result.data.bucket_id))
                        AppTools.bucketId = result.data.bucket_id
                    LocalLogUtils.writeLog("Add 专辑 :上传book cover:${result.data}", System.currentTimeMillis())
                    AliLoadFactory(this@TalkCreateAlbumActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                        override fun progress(current: Long) {

                        }

                        override fun success() {
                            upload(result.data.resource_content)
                        }

                        override fun fail() {//oss 异常
                            runOnUiThread {
                                transLayout.showContent()
                                LocalLogUtils.writeLog("Add 专辑:传音封面失败 oss error", System.currentTimeMillis())
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {

                        }
                    }, UploadData(result.data.resource_content, coverPath))
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                LocalLogUtils.writeLog("Add 专辑:传音封面失败 ${any.toString()}", System.currentTimeMillis())
                transLayout.showContent()
            }
        })
    }

    private fun upload(uri: String) {
        val builder = FormBody.Builder()
                .add("albumCoverUri", uri)
                .add("albumName", albumName)
                .add("bucketId", AppTools.bucketId)
        if (!TextUtils.isEmpty(talkId)) {
            builder.add("dialogId", talkId)
        }
        OkClientHelper.post(this, "dialogAlbums", builder.build(), IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as IntegerRespData
                if (result.code == 0) {
                    if (!TextUtils.isEmpty(talkId)) {
                        showToast("已加入专辑")
                        if (DialogAddTalkAlbumActivity.instances != null) {
                            DialogAddTalkAlbumActivity.instances!!.finish()
                        }
                    }
                    if (originalAlbumId != null)
                        EventBus.getDefault().post(OnTalkAddAlbumEvent(originalAlbumId!!))
                    EventBus.getDefault().post(IUpdateTalkAlbumEvent(3, result.data.id.toString()))
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.3")
    }


    /**
     * 检测是否有权限
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
            } else {
                openGallery()
            }
        } else {
            openGallery()
        }
    }

    /**
     * 选取图片
     */
    private fun openGallery() {
        startActivityForResult(Intent(this, AlbumActivity::class.java).putExtra("count", 1), REQUEST_GALLERY)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!TextUtils.isEmpty(coverPath) || !TextUtils.isEmpty(albumName)) {
                DialogConfirmCancelAlbum(this).setOnClickListener(View.OnClickListener {
                    finish()
                }).show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        try {
            if (File(coverPath).exists()) {
                File(coverPath).delete()
            }
        } catch (e: Exception) {
        }
    }

    private fun jude(): Boolean {
        return !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(albumName)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<*>
                        if (result.size > 0) {
                            startActivityForResult(Intent(this, AddItemCoverActivity::class.java)
                                    .putExtra("rate", 1f)
                                    .putExtra("name", imgName)
                                    .putExtra("path", result[0] as String), REQUEST_CROP)
                        }
                    }
                }
                REQUEST_NAME -> {
                    data?.let {
                        albumName = data.getStringExtra("name")
                        tv_album_name.text = albumName
                        tv_push.isSelected = jude()
                    }
                }
                REQUEST_CROP -> {
                    data?.let {
                        cardLayout.visibility = View.VISIBLE
                        Glide.with(this)
                                .applyDefaultRequestOptions(RequestOptions()
                                        .skipMemoryCache(true)
                                        .signature(ObjectKey(System.currentTimeMillis().toString())))
                                .load(it.getStringExtra("result"))
                                .into(iv_img)
                        coverPath = it.getStringExtra("result")
                        tv_push.isSelected = jude()
                    }
                }
            }
        }
    }
}