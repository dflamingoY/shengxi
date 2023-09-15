package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.gw.swipeback.SwipeBackLayout
import kotlinx.android.synthetic.main.activity_showpic.*
import kotlinx.android.synthetic.main.layout_show_pic.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.DataHelper
import org.xiaoxingqi.shengxi.utils.FileUtils
import org.xiaoxingqi.shengxi.utils.IConstant
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException

class ShowPicActivity : BaseNormalActivity() {
    var mImgs: ArrayList<String>? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_showpic
    }

    override fun initView() {
        swipeBackLayout.directionMode = SwipeBackLayout.FROM_TOP
//        swipeBackLayout.isSwipeFromEdge = false
    }

    override fun initData() {
        intent.getSerializableExtra("data")?.let {
            mImgs = it as ArrayList<String>
        }
        intent.getStringExtra("path")?.let {
            if (mImgs == null) {
                mImgs = ArrayList()
                mImgs!!.add(it)
            }
        }
        if (intent.getBooleanExtra("bigData", false)) {//共享数据
            mImgs = DataHelper.getInstance().imgBeans as ArrayList<String>?
        }
        viewPager.adapter = ImagePagerAdapter()
        viewPager.currentItem = intent.getIntExtra("index", 0)
        if (intent.getBooleanExtra("isVoice", true))
            indicator.attachPager(viewPager)
    }

    override fun initEvent() {
        /**
         * 保存图片
         */
        hintSave.setOnClickListener {
            save()
            hintSave.show()
        }
        swipeBackLayout.setSwipeBackListener(object : SwipeBackLayout.OnSwipeBackListener {
            override fun onViewPositionChanged(mView: View?, swipeBackFraction: Float, swipeBackFactor: Float) {
                val alpha = (255 * (1 - swipeBackFraction)).toInt()
                viewPager.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
            }

            override fun onViewSwipeFinished(mView: View?, isEnd: Boolean) {
                if (isEnd) {
                    finish()
                }
            }
        })
    }

    @SuppressLint("StaticFieldLeak")
    private fun save() {
        val baseImg = mImgs!![viewPager.currentItem]
        var path: String = baseImg
        if (baseImg.contains("?")) {
            path = baseImg.substring(0, baseImg.lastIndexOf("?"))
        }
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg") && !suffix.contains(".png") && !suffix.contains(".gif")) {
            suffix = "$suffix.jpg"
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        if (file.exists()) {
            showToast("图片已存在")
            return
        }
        object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(this@ShowPicActivity)
                            .load(path)
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
            }
        }.execute()
    }

    inner class ImagePagerAdapter : PagerAdapter() {
        internal var width = AppTools.getWindowsWidth(this@ShowPicActivity)
        internal var height = AppTools.getWindowsHeight(this@ShowPicActivity)
        internal var lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = View.inflate(this@ShowPicActivity, R.layout.layout_show_pic, null) as RelativeLayout
            val progressBar = view.findViewById<ProgressBar>(R.id.progress)
            val scaleImageView = view.findViewById<SubsamplingScaleImageView>(R.id.imageView)
            scaleImageView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
            scaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
            val ivPic = view.showIamgeView
            ivPic.scaleType = ImageView.ScaleType.FIT_CENTER
            val imgBean = mImgs?.get(position)
            var url = ""
            if (imgBean?.endsWith("gif")!! || !imgBean.contains("?")) {
                url = imgBean
            } else {
                if (imgBean.contains("?"))
                    url = imgBean.substring(0, imgBean.lastIndexOf("?"))
            }
            if (url.endsWith(".gif")) {
                Glide.with(this@ShowPicActivity)
                        .applyDefaultRequestOptions(RequestOptions()
                                .error(R.drawable.drawable_default_tmpry)
                                .signature(ObjectKey(url))
                        )
                        .load(url)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                progressBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                progressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(ivPic)
            } else {
                Glide.with(this@ShowPicActivity)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(imgBean)))
                        .downloadOnly().load(imgBean).listener(object : RequestListener<File> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?, isFirstResource: Boolean): Boolean {
                                progressBar.visibility = View.GONE
                                if (isFirstResource) {
                                    if (imgBean.contains("?")) {
                                        val path = imgBean.substring(0, imgBean.indexOf("?"))
                                        /*Glide.with(this@ShowPicActivity)
                                                .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(path)))
                                                .load(path)
                                                .into(ivPic)*/
                                        Glide.with(this@ShowPicActivity)
                                                .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(path)))
                                                .downloadOnly()
                                                .load(path).listener(object : RequestListener<File> {
                                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<File>?, isFirstResource: Boolean): Boolean {
                                                        return false
                                                    }

                                                    override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                                        val options = BitmapFactory.Options()
                                                        options.inJustDecodeBounds = true
                                                        BitmapFactory.decodeFile(resource?.absolutePath, options)
                                                        if (options.outWidth < AppTools.getWindowsWidth(this@ShowPicActivity) * AppTools.getPhoneDensity(this@ShowPicActivity)
                                                                && options.outHeight < AppTools.getWindowsHeight(this@ShowPicActivity) * 2) {
                                                            ivPic.visibility = View.VISIBLE
                                                            scaleImageView.visibility = View.GONE
                                                            Glide.with(this@ShowPicActivity).load(resource)
                                                                    .error(Glide.with(this@ShowPicActivity)
                                                                            .applyDefaultRequestOptions(RequestOptions()
                                                                                    .signature(ObjectKey(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url)))
                                                                            .load(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url))
                                                                    .into(ivPic)
                                                        } else {
                                                            scaleImageView.visibility = View.VISIBLE
                                                            ivPic.visibility = View.GONE
                                                            scaleImageView.setImage(ImageSource.uri(resource?.absolutePath).tiling(true), ImageViewState(1f, PointF(), 0))
                                                        }
                                                        return false
                                                    }
                                                }).preload()
                                    }
                                }
                                return false
                            }

                            override fun onResourceReady(resource: File?, model: Any?, target: Target<File>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                progressBar.visibility = View.GONE
                                val options = BitmapFactory.Options()
                                /**
                                 * 最关键在此，把options.inJustDecodeBounds = true;
                                 * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
                                 * 判断图片是用什么展示
                                 * 获取当前手机的分辨率
                                 */
                                options.inJustDecodeBounds = true
                                BitmapFactory.decodeFile(resource?.absolutePath, options)
                                if (options.outWidth < AppTools.getWindowsWidth(this@ShowPicActivity) * AppTools.getPhoneDensity(this@ShowPicActivity)
                                        && options.outHeight < AppTools.getWindowsHeight(this@ShowPicActivity) * 2) {
                                    ivPic.visibility = View.VISIBLE
                                    scaleImageView.visibility = View.GONE
                                    Glide.with(this@ShowPicActivity).load(resource)
                                            .error(Glide.with(this@ShowPicActivity)
                                                    .applyDefaultRequestOptions(RequestOptions()
                                                            .signature(ObjectKey(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url)))
                                                    .load(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url))
                                            .into(ivPic)
                                } else {
                                    scaleImageView.visibility = View.VISIBLE
                                    ivPic.visibility = View.GONE
                                    //判断图片是长图,还是宽图
//                                    val widthRate = options.outWidth / AppTools.getWindowsWidth(this@ShowPicActivity).toFloat()
//                                    val heightRate = options.outHeight / AppTools.getWindowsHeight(this@ShowPicActivity).toFloat()
//                                    Log.d("Mozator", "widthRate $widthRate heightRate  $heightRate")
                                    scaleImageView.setImage(ImageSource.uri(resource?.absolutePath).tiling(true), ImageViewState(0f, PointF(), 0))
                                }
                                return false
                            }
                        }).preload()
            }
//            ivPic.layoutParams = lp
            ivPic.setOnClickListener { finish() }
            ivPic.setOnLongClickListener {
                hintSave.show()
                false
            }
            scaleImageView.setOnLongClickListener {
                hintSave.show()
                false
            }
            scaleImageView.setOnClickListener { finish() }
            view.setOnClickListener { finish() }
            view.requestLayout()
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int {
            if (mImgs.isNullOrEmpty()) {
                return 0
            }
            return mImgs!!.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.act_exit_alpha)
    }

}