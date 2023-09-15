package org.xiaoxingqi.shengxi.modules.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_crop.*
import kotlinx.android.synthetic.main.view_progress_speed.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.LocalLogUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils
import java.io.File
import java.io.FileOutputStream

class CropActivity : BaseNormalActivity() {
    companion object {
        const val INSTALL_PACKAGES_REQUESTCODE = 0x00
    }

    private var path: String? = null
    private var resourceType = "6"
    override fun getLayoutId(): Int {
        return R.layout.activity_crop
    }

    override fun initView() {

    }

    override fun initData() {
        ImageLoader.getInstance().displayImage("file://${intent.getStringExtra("path")}", touchImg, AppTools.options)
        intent.getStringExtra("resourceType")?.let {
            resourceType = it
        }
    }

    override fun initEvent() {
        tv_Cancel.setOnClickListener { finish() }
        tv_Commit.setOnClickListener {
            checkPermission()
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), INSTALL_PACKAGES_REQUESTCODE)
            } else {
                save2Cache()
            }
        } else {
            save2Cache()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save2Cache() {
        transLayout.showProgress()
        object : AsyncTask<Void, Void, String>() {
            @SuppressLint("WrongThread")
            override fun doInBackground(vararg voids: Void): String? {
                val bitmap = Bitmap.createBitmap(cropView.width, cropView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                cropView.draw(canvas)
                val booFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.IMG_CACHE)
                if (!booFile.exists()) {
                    booFile.mkdirs()
                }
                val file = File(booFile.absolutePath + "/" + System.currentTimeMillis() + ".jpg")
                try {
                    if (bitmap != null) {
                        val fos = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        fos.close()
                    }
                    bitmap.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                    bitmap?.recycle()
                    return null
                }

                return file.absolutePath
            }

            override fun onPostExecute(s: String) {
                super.onPostExecute(s)
                /**
                 * 上传图片
                 */
                path = s
                request(1)
            }
        }.execute()
    }

    private var allLength = 0L
    private var startTime = 0L
    private var aliLoad: AliLoadFactory? = null

    override fun request(flag: Int) {
        transLayout.showProgress()
        startTime = System.currentTimeMillis()
        allLength = File(path).length()
        val formBody = FormBody.Builder()
                .add("resourceType", resourceType)
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(path))}.jpg")
                .add("needBaseUri", "1")
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as QiniuStringData
                if (result.code == 0) {
                    result.data.bucket_id?.let {
                        AppTools.bucketId = it
                    }
                    aliLoad = AliLoadFactory(this@CropActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {

                        override fun progress(current: Long) {
                            if (System.currentTimeMillis() - startTime > 30000) {//超过30s 放弃所有上传 直接缓存到本地
                                LocalLogUtils.writeLog("裁剪图片:上传图片:more than 30s cancel all task", System.currentTimeMillis())
                                aliLoad?.cancel()
                            } else if (System.currentTimeMillis() - startTime >= 10000) {
                                if (tv_progress.visibility != View.VISIBLE) {
                                    tv_progress.post {
                                        tv_progress.visibility = View.VISIBLE
                                    }
                                }
                                tv_progress.post {
                                    tv_progress.text = "${((current * 1f / allLength) * 100).toInt()}%"
                                }

                            }
                        }

                        override fun success() {
                            val intent = Intent()
                            intent.putExtra("result", result.data.resource_content)
                                    .putExtra("originalPath", path)
                                    .putExtra("url", result.data.baseUri + result.data.resource_content)
                            setResult(RESULT_OK, intent)
                            try {
                                File(path).delete()
                            } catch (e: Exception) {
                            }
                            finish()
                        }

                        override fun fail() {
                            runOnUiThread {
                                tv_progress.visibility = View.GONE
                                showToast("上传失败,当前网络不太稳定")
                                transLayout.showContent()
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {

                        }
                    }, UploadData(result.data.resource_content, path))
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                showToast("上传失败,当前网络不太稳定")
                transLayout.showContent()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == INSTALL_PACKAGES_REQUESTCODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                save2Cache()
            }
        }
    }
}