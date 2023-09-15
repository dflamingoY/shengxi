package org.xiaoxingqi.shengxi.modules.home

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewTreeObserver
import com.bumptech.glide.Glide
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.activity_cartoon.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.impl.UpdateCartoonStatus
import org.xiaoxingqi.shengxi.model.AnimBean
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class CartoonListActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<AnimBean>
    private val mData by lazy { ArrayList<AnimBean>() }
    private var isAnimation = false
    private var readCount = 0//已读
    override fun getLayoutId(): Int {
        return R.layout.activity_cartoon
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        adapter = object : QuickAdapter<AnimBean>(this, R.layout.item_anim_list, mData) {
            override fun convert(helper: BaseAdapterHelper, item: AnimBean) {
                Glide.with(this@CartoonListActivity)
                        .load(item.resourceId)
                        .into(helper.getImageView(R.id.ivAnim))
                helper.getView(R.id.tvUnRead).visibility = if (item.status == 0) View.VISIBLE else View.GONE
                helper.getView(R.id.tvRead).visibility = if (item.status != 0) View.VISIBLE else View.GONE
            }
        }
        recyclerView.adapter = adapter
        generator()
    }

    private fun generator() {
        mData.add(AnimBean(R.drawable.drawable_anim_1, "guide"))
        mData.add(AnimBean(R.drawable.drawable_anim_2, "voiceVisible"))
        val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val result = SPUtils.getString(this, IConstant.ANIM_READ_STATUS + login.user_id, "")
        result.split(",").filter {
            it.isNotEmpty()
        }.forEach { name ->
            mData.loop { bean ->
                name == bean.name
            }?.let {
                readCount++
                it.status = 1
            }
        }
        adapter.notifyDataSetChanged()
        if (readCount > 0) {
            anim()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun anim(isGlobal: Boolean = true) {
        isAnimation = false
        if (isGlobal) {
            ivProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    ivProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val anim = ObjectAnimator.ofFloat(ivProgress, "alpha", 0f, 1f).setDuration(220)
                    anim.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            runOnUiThread {
                                progress.setProgress(readCount / mData.size.toFloat())
                            }
                        }
                    })
                    anim.start()
                }
            })
        } else {
            ivProgress.alpha = 1f
            progress.setProgress(readCount / mData.size.toFloat())
        }
        tvRead.text = "已读:$readCount"
    }

    override fun onStart() {
        super.onStart()
        if (isAnimation) {
            ivProgress.postDelayed({
                anim(false)
            }, 300)
        }
    }


    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            startActivity<AnimGuideActivity>("name" to mData[position].name)
        }
        progress.setOnValueChange {
            val value = progress.width * it
            val dx = value - AppTools.dp2pxFloat(this, 28)
            ivProgress.x = if (dx >= 0) dx else 0f
            if (it >= 1f) {
                val anim = ValueAnimator.ofFloat(0f, 1f).setDuration(320)
                anim.addUpdateListener { animation ->
                    val animValue = animation.animatedValue as Float
                    ivProgress.alpha = 1 - animValue
                    ivFinish.alpha = animValue
                }
                anim.start()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateCartoon(event: UpdateCartoonStatus) {
        mData.loop {
            event.name == it.name
        }?.let {
            readCount++
            isAnimation = true
            it.status = 1
            adapter.notifyDataSetChanged()
        }
    }
}