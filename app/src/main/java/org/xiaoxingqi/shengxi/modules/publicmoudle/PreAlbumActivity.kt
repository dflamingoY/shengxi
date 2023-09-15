package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_pre_album.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.DataHelper

class PreAlbumActivity : BaseNormalActivity() {
    private val mData by lazy {
        ArrayList<String>()
    }
    private var selectes: ArrayList<String>? = null
    private var allowCount = 1
    private val imageViews: List<PhotoView> by lazy {
        arrayListOf<PhotoView>().apply {
            for (index in 1..4) {
                add(PhotoView(this@PreAlbumActivity))
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_pre_album
    }

    override fun initView() {
        val params = linear_operator.layoutParams as RelativeLayout.LayoutParams
        AppTools.getStatusBarHeight(this)
        params.setMargins(0, AppTools.getStatusBarHeight(this), 0, 0)
        linear_operator.layoutParams = params
    }

    override fun initData() {
        val current = intent.getIntExtra("current", 0)
        selectes = intent.getSerializableExtra("selects") as ArrayList<String>
        allowCount = intent.getIntExtra("all", 1)
        mData.addAll(DataHelper.getInstance().imgBeans as ArrayList<String>)
        if (mData.size > 0 && mData[0] == "相机") {
            mData.removeAt(0)
        }
        viewPager.adapter = ImgAdapter()
        if (current < mData.size)
            viewPager.setCurrentItem(current, false)
        selectes?.let {
            if (mData.size > current && it.contains(mData[current])) {
                checkAlbum.isSelected = true
            }
            if (it.size > 0) {
                tv_Commit.text = "${resources.getString(R.string.string_Next_step)}（" + it.size + "）"
                tv_Commit.isSelected = true
                tv_Commit.setTextColor(Color.WHITE)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (selectes != null) {
                    checkAlbum.isSelected = selectes!!.contains(mData[position])
                } else {
                    checkAlbum.isSelected = false
                }
            }
        })
        checkAlbum.setOnClickListener {
            if (null == selectes) {
                selectes = ArrayList()
            }
            if (it.isSelected) {
                if (selectes!!.contains(mData[viewPager.currentItem])) {
                    selectes!!.remove(mData[viewPager.currentItem])
                }
                it.isSelected = false
            } else {
                if (null != selectes) {
                    if (selectes!!.size == allowCount) {
                        return@setOnClickListener
                    }
                }
                if (!selectes!!.contains(mData[viewPager.currentItem])) {
                    selectes!!.add(mData[viewPager.currentItem])
                }
                it.isSelected = true
            }
            if (selectes!!.size <= 0) {
                tv_Commit.text = resources.getString(R.string.string_Next_step)
                tv_Commit.isSelected = false
                tv_Commit.setTextColor(resources.getColor(R.color.colorTextGray))
            } else {
                tv_Commit.text = "${resources.getString(R.string.string_Next_step)}（" + selectes!!.size + "）"
                tv_Commit.isSelected = true
                tv_Commit.setTextColor(Color.WHITE)
            }
        }
        tv_Commit.setOnClickListener {
            if (selectes != null && selectes!!.size > 0) {
                setResult(Activity.RESULT_OK, Intent().putExtra("selects", selectes))
                finish()
            }
        }
    }

    private var isOpen = true
    private var isRunning = false
    private fun animOperator() {
        if (isRunning) {
            return
        }
        isRunning = true
        val duration = ObjectAnimator.ofFloat(linear_operator, "translationX", if (isOpen) linear_operator.width.toFloat() else 0f).setDuration(200)
        duration.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                isRunning = false
                isOpen = !isOpen
            }
        })
        duration.start()
    }

    private inner class ImgAdapter : PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return mData.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = imageViews[position.rem(4)]
            Glide.with(this@PreAlbumActivity)
                    .load(mData!![position])
                    .into(imageView)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            container.addView(imageView, params)
            imageView.setOnClickListener {
                animOperator()
            }
            return imageView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val i = position % 4
            val imageView = imageViews[i]
            container.removeView(imageView)
            Glide.with(this@PreAlbumActivity).clear(imageView)
        }
    }

    /* override fun finish() {
         setResult(Activity.RESULT_CANCELED, Intent().putExtra("selects", selectes))
         super.finish()
     }*/

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent().putExtra("selects", selectes))
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}