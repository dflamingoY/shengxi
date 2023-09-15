package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.os.Environment
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_crop_friend_bg.*
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
import org.xiaoxingqi.shengxi.utils.TimeUtils
import java.io.File
import java.io.FileOutputStream

class CropFriendBgActivity : BaseNormalActivity() {
    private var path: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_crop_friend_bg
    }

    override fun initView() {

    }

    override fun initData() {
        ImageLoader.getInstance().displayImage("file://${intent.getStringExtra("path")}", touchImg, AppTools.options)
    }

    override fun initEvent() {
        tv_Cancel.setOnClickListener {
            finish()
        }

        tv_Commit.setOnClickListener {
            save2Cache()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save2Cache() {
        transLayout.showProgress()
        val bitmap = Bitmap.createBitmap(cropView.width, cropView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)
        cropView.draw(canvas)
        object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg voids: Void): String? {
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

    override fun request(flag: Int) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceType", "12")
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
                    AliLoadFactory(this@CropFriendBgActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                        override fun progress(current: Long) {

                        }

                        override fun success() {
                            val intent = Intent()
                            intent.putExtra("result", result.data.resource_content)
                                    .putExtra("originalPath", "${result.data.baseUri}${result.data.resource_content}")
                            setResult(RESULT_OK, intent)
                            try {
                                File(path).delete()
                            } catch (e: Exception) {
                            }
                            finish()
                        }

                        override fun fail() {
                            runOnUiThread {
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
                transLayout.showContent()
            }
        })
    }


}