package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PointF
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_crop_bg.*
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

class CropBgActivity : BaseNormalActivity() {
    private var path: String? = null
    private var isSend = true
    override fun getLayoutId(): Int {
        return R.layout.activity_crop_bg
    }

    override fun initView() {

    }

    override fun initData() {
        isSend = intent.getBooleanExtra("isSend", true)
        //判断图片是否是超大长图
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(intent.getStringExtra("path"), options)
        if (options.outWidth < AppTools.getWindowsWidth(this@CropBgActivity) * AppTools.getPhoneDensity(this@CropBgActivity)
                && options.outHeight < AppTools.getWindowsHeight(this@CropBgActivity) * 2) {
            ImageLoader.getInstance().displayImage("file://${intent.getStringExtra("path")}", touchImg, AppTools.options)
        } else {
            //获取需要缩放的比例
            imageView.setImage(ImageSource.uri(intent.getStringExtra("path")).tiling(true), ImageViewState(0f, PointF(), 0))
            imageView.visibility = View.VISIBLE
        }
    }

    override fun initEvent() {
        touchImg.setOnClickListener {
            openAnim()
        }
        tv_Cancel.setOnClickListener {
            finish()
        }
        tv_Commit.setOnClickListener {
            save2Cache()
        }
        imageView.setOnClickListener {
            openAnim()
        }
    }

    private var isRunning = false
    private var isShow = true
    /**
     * 执行动画
     */
    private fun openAnim() {
        if (isRunning)
            return
        if (isShow) {
            val animator = ObjectAnimator.ofFloat(relative_Bottom, "translationY", AppTools.dp2px(this, 60).toFloat())
            animator.duration = 320
            animator.start()
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isRunning = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isRunning = false
                    isShow = false
                }
            })
        } else {
            val animator = ObjectAnimator.ofFloat(relative_Bottom, "translationY", 0f)
            animator.duration = 320
            animator.start()
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isRunning = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isRunning = false
                    isShow = true
                }
            })
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
                    val fos = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                    fos.close()
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
                if (!isSend) {
                    setResult(Activity.RESULT_OK, Intent().putExtra("originalPath", path))//源文件
                    finish()
                } else
                    request(1)
            }
        }.execute()
    }

    private var allLength = 0L
    private var startTime = 0L
    private var aliLoad: AliLoadFactory? = null

    override fun request(flag: Int) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("resourceType", "23")
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(path))}.jpg")
                .add("needBaseUri", "1")
                .build()
        allLength = File(path).length()
        startTime = System.currentTimeMillis()

        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                val data = result as QiniuStringData
                if (result.code == 0) {
                    result.data.bucket_id?.let {
                        AppTools.bucketId = it
                    }
                    aliLoad = AliLoadFactory(this@CropBgActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
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
                                    .putExtra("originalPath", "${data.data.baseUri}${data.data.resource_content}")
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
                    transLayout.showContent()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                showToast("上传失败,当前网络不太稳定")
            }
        })
    }

}