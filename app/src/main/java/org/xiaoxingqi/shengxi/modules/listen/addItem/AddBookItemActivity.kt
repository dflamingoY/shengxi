package org.xiaoxingqi.shengxi.modules.listen.addItem

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_add_item.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCommitPushItem
import org.xiaoxingqi.shengxi.dialog.DialogDeleteResource
import org.xiaoxingqi.shengxi.impl.AddResourceEvent
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.book.OneBookDetailsActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.zxing.activity.ScanActivity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * 添加书籍词条
 */
class AddBookItemActivity : BaseAct() {
    companion object {
        private const val REQUEST_ALBUM = 0x00
        private const val REQUEST_NAME = 0x01
        private const val REQUEST_AUTHOR = 0x02
        private const val REQUEST_CROP = 0x03
        private const val REQUEST_PERMISSION = 0x04
        private const val REQUEST_BOOK_DESC = 0x06
    }

    private var coverPath: String? = null
    private lateinit var imgName: String
    private var bookName: String? = null
    private var bookAuthor: String? = null
    private var cacheData: BaseSearchBean? = null
    private var netCover: String? = null
    private var desc: String? = null
    private var isbn: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_add_item
    }

    override fun initView() {
        tv_push.isSelected = false
        val params = cardLayout.layoutParams
        params.width = AppTools.dp2px(this, 29)
        cardLayout.layoutParams = params
        tv_cover_title.text = resources.getString(R.string.string_book_add_cover)
        view_music.setTitle(resources.getString(R.string.string_book_add_name))
        view_singer.setTitle("作者")
        view_album.visibility = View.GONE
        linearScan.visibility = View.VISIBLE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        isbn = intent?.getStringExtra("isbn")
        if (!TextUtils.isEmpty(isbn)) {
            request(2)
        }
        setIntent(intent)
    }

    override fun initData() {
        cacheData = intent.getParcelableExtra("data")
        isbn = intent.getStringExtra("isbn")
        imgName = UUID.randomUUID().toString()
        cacheData?.let {
            linearScan.visibility = View.GONE
            view_Desc.visibility = View.VISIBLE
            cardLayout.visibility = View.VISIBLE
            tv_un_add.visibility = View.GONE
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .signature(ObjectKey(it.book_cover)))
                    .load(it.book_cover)
                    .into(iv_img)
            netCover = it.book_cover
            bookName = it.book_name
            bookAuthor = it.book_author
            desc = it.book_intro
            view_music.setMsgCount(bookName)
            view_singer.setMsgCount(bookAuthor)
            view_Desc.setMsgCount(desc)
            view_music.setChildTextStatus(true)
            view_Desc.setChildTextStatus(true)
            view_singer.setChildTextStatus(true)

            tvDelete.visibility = View.VISIBLE
            tv_push.isSelected = true
            request(0)
        }
        if (!TextUtils.isEmpty(isbn)) {
            request(2)
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        relative_cover.setOnClickListener {
            checkPermission()
        }
        view_music.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", bookName)
                    .putExtra("count", 100)
                    .putExtra("title", "添加" + resources.getString(R.string.string_book_add_name)), REQUEST_NAME)
        }
        view_singer.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", bookAuthor)
                    .putExtra("title", "添加作者"), REQUEST_AUTHOR)
        }
        tv_push.setOnClickListener {
            if (it.isSelected) {
                if (cacheData != null) {
                    if (TextUtils.isEmpty(coverPath)) {
                        transLayout.showProgress()
                        adminUpload("", "")
                    } else {
                        request(1)
                    }
                } else
                    DialogCommitPushItem(this).setOnClickListener(View.OnClickListener {
                        request(1)
                    }).show()
            }
        }
        view_Desc.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", desc)
                    .putExtra("count", 500)
                    .putExtra("title", "编辑简介"), REQUEST_BOOK_DESC)
        }
        tvDelete.setOnClickListener {
            DialogDeleteResource(this).setOnClickListener(View.OnClickListener {
                deleteItem()
            }).show()
        }
        linearScan.setOnClickListener {
            startActivity<ScanActivity>()
        }
    }

    private fun jude(): Boolean {
        return (!TextUtils.isEmpty(coverPath) || !TextUtils.isEmpty(netCover)) && !TextUtils.isEmpty(bookName) && !TextUtils.isEmpty(bookAuthor)
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
        startActivityForResult(Intent(this, AlbumActivity::class.java).putExtra("count", 1), REQUEST_ALBUM)
    }

    //删除词条资源类型，1=影片  2 书籍  3 唱歌
    private fun deleteItem() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/resource/2/${cacheData?.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    showToast("操作成功")
                } else {
                    showToast(result.msg)
                }
            }
        })
    }

    /**
     * admin
     */
    private fun adminUpload(imgCover: String, baseUri: String) {
        val formBody = FormBody.Builder()
        if (!TextUtils.isEmpty(imgCover)) {
            formBody.add("bookCoverUri", imgCover)
        }
        if (bookName != cacheData?.book_name)
            formBody.add("bookName", bookName)
        if (bookAuthor != cacheData?.book_author)
            formBody.add("bookAuthor", bookAuthor)
        if (desc != cacheData?.book_intro)
            formBody.add("bookIntro", desc)
        if (formBody.build().size() == 0) {
            finish()
            return
        }
        formBody.add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
        OkClientHelper.patch(this, "admin/resource/2/${cacheData?.id}", formBody.build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showContent()
                if (result.code == 0) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun upload(imgCover: String, baseUri: String) {
        val formBody = FormBody.Builder()
                .add("bookName", bookName)
                .add("bookCoverUri", imgCover)
                .add("bookAuthor", bookAuthor)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "user/${loginBean.user_id}/entries/2", formBody, ConfessionsData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as ConfessionsData
                if (result.code == 0) {
                    File(coverPath).delete()
                    showToast("添加完成")
                    val searchBean = BaseSearchBean()
                    searchBean.id = result.data.resource_id
                    searchBean.book_author = bookAuthor
                    searchBean.book_name = bookName
                    searchBean.book_cover = baseUri + imgCover
                    EventBus.getDefault().post(AddResourceEvent(2, searchBean))
                    startActivity(Intent(this@AddBookItemActivity, OneBookDetailsActivity::class.java).putExtra("id", result.data.resource_id))
                    finish()
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.7")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "resources/2/${cacheData?.id}", OneDetailsData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as OneDetailsData
                        if (result.code == 0) {
                            if (result.data != null) {
                                cacheData = result.data
                                Glide.with(this@AddBookItemActivity)
                                        .applyDefaultRequestOptions(RequestOptions()
                                                .signature(ObjectKey(result.data.book_cover)))
                                        .load(result.data.book_cover)
                                        .into(iv_img)
                                netCover = result.data.book_cover
                                bookName = result.data.book_name
                                bookAuthor = result.data.book_author
                                desc = result.data.book_intro
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.2")
            }
            1 -> {
                if (File(coverPath).length() == 0L) {
                    showToast("封面图已被删除")
                    return
                }
                transLayout.showProgress()
                val formBody = FormBody.Builder()
                        .add("resourceType", "16")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(coverPath))}.png")
                        .add("needBaseUri", "1")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            LocalLogUtils.writeLog("Add book :上传book cover:${result.data}", System.currentTimeMillis())
                            AliLoadFactory(this@AddBookItemActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    if (cacheData != null) {
                                        adminUpload(result.data.resource_content, result.data.baseUri)
                                    } else
                                        upload(result.data.resource_content, result.data.baseUri)
                                }

                                override fun fail() {//oss 异常
                                    runOnUiThread {
                                        transLayout.showContent()
                                        LocalLogUtils.writeLog("Add book:传音封面失败 oss error", System.currentTimeMillis())
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
                        LocalLogUtils.writeLog("Add book:传音封面失败 ${any.toString()}", System.currentTimeMillis())
                        transLayout.showContent()
                    }
                })
            }
            2 -> {
                transLayout.showProgress()
                OkClientHelper.getIsbn(this, "https://isbn.market.alicloudapi.com/ISBN?is_info=0&isbn=$isbn", ISBNDetailsBean::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        if (any is String)
                            showToast(any)
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as ISBNDetailsBean
                        result.result?.let {
                            cardLayout.visibility = View.VISIBLE
                            tv_un_add.visibility = View.GONE
                            Glide.with(this@AddBookItemActivity)
                                    .asFile()
                                    .load(it.images_large)
                                    .listener(object : RequestListener<File> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?, isFirstResource: Boolean): Boolean {
                                            return false
                                        }

                                        override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            Glide.with(this@AddBookItemActivity)
                                                    .load(resource)
                                                    .into(iv_img)
                                            coverPath = resource!!.absolutePath
                                            return false
                                        }
                                    }).preload()
                            bookAuthor = it.author
                            bookName = it.title
                            view_music.setMsgCount(bookName)
                            view_music.setChildTextStatus(true)
                            view_singer.setMsgCount(bookAuthor)
                            view_singer.setChildTextStatus(true)
                            tv_push.isSelected = true
                        }
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_ALBUM -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<String>
                        if (result != null && result.size > 0) {
                            startActivityForResult(Intent(this, AddItemCoverActivity::class.java)
                                    .putExtra("rate", 0.72f)
                                    .putExtra("name", imgName)
                                    .putExtra("path", result[0]), REQUEST_CROP)
                        }
                    }
                }
                REQUEST_NAME -> {
                    bookName = data?.getStringExtra("name")
//                    view_music.setMsgCount("已添加")
                    view_music.setMsgCount(bookName)
                    view_music.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_AUTHOR -> {
//                    view_singer.setMsgCount("已添加")
                    bookAuthor = data?.getStringExtra("name")
                    view_singer.setMsgCount(bookAuthor)
                    view_singer.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_CROP -> {
                    cardLayout.visibility = View.VISIBLE
                    tv_un_add.visibility = View.GONE
                    data?.let {
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
                REQUEST_BOOK_DESC -> {
                    data?.let {
                        view_Desc.setMsgCount("已添加")
                        desc = it.getStringExtra("name")
                    }
                }
            }
        }
    }
}