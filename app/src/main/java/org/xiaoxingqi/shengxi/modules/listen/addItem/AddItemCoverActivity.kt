package org.xiaoxingqi.shengxi.modules.listen.addItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.AsyncTask
import android.os.Environment
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_add_item_cover.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import java.io.File
import java.io.FileOutputStream

/**
 * 不做上传图片保留到 提交时上传
 */
class AddItemCoverActivity : BaseNormalActivity() {
    private lateinit var name: String
    override fun getLayoutId(): Int {
        return R.layout.activity_add_item_cover
    }

    override fun initView() {

    }

    override fun initData() {
        name = intent.getStringExtra("name")
        val path = intent.getStringExtra("path")
        ImageLoader.getInstance().displayImage("file://$path", touchImg, AppTools.options)
        val rate = intent.getFloatExtra("rate", 1.0f)
        layerView.setScaleRate(rate)
        cropView.setScaleRate(rate)
    }

    override fun initEvent() {
        tv_Cancel.setOnClickListener { finish() }
        tv_Commit.setOnClickListener {
            savePic()
        }
    }

    /**
     * 保存图片到本地
     */
    @SuppressLint("StaticFieldLeak")
    private fun savePic() {
        transLayout.showProgress()
        val rect = layerView.getRect()
        touchImg.isDrawingCacheEnabled = true
        val cacheBitmap = touchImg.drawingCache
        object : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {
                val saveBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565)
                val canvas = Canvas(saveBitmap)
                canvas.drawBitmap(cacheBitmap, Rect(rect.left, 0, rect.right, rect.height()), Rect(0, 0, rect.width(), rect.height()), null)
                val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.IMG_CACHE)
                if (!file.exists()) {
                    file.mkdirs()
                }
                val imgFile = File(file, "/$name.png")
                val fos = FileOutputStream(imgFile, false)
                saveBitmap.compress(Bitmap.CompressFormat.PNG, 60, fos)
                fos.close()
                return imgFile.absolutePath
            }

            override fun onPostExecute(result: String?) {
                setResult(Activity.RESULT_OK, Intent().putExtra("result", result))
                finish()
                transLayout.showContent()
            }
        }.execute()
    }

}