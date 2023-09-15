package org.xiaoxingqi.shengxi.modules.home

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_anim_guide.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.dialog.DialogGuideHint
import org.xiaoxingqi.shengxi.impl.UpdateCartoonStatus
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import skin.support.SkinCompatManager

//DialogGuideHint
class AnimGuideActivity : BaseNormalActivity() {
    //图片集合
    private lateinit var cacheData: Array<Int>
    private var currentIndex = 0
    private var isGuide = false
    private lateinit var name: String
    private lateinit var loginBean: LoginData.LoginBean
    override fun getLayoutId(): Int {
        return R.layout.activity_anim_guide
    }

    override fun initView() {
        val params = view_status_bar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                view_status_bar.setBackgroundColor(Color.WHITE)
            } else {//夜间模式
                view_status_bar.setBackgroundColor(Color.parseColor("#181828"))
            }
        } else {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                view_status_bar.setBackgroundColor(Color.parseColor("#cccccc"))
            } else {//夜间模式
                view_status_bar.setBackgroundColor(Color.parseColor("#181828"))
            }
        }
        setStatusBarFontIconDark(TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        name = intent.getStringExtra("name")
        isGuide = intent.getBooleanExtra("isGuide", false)
        generator(name)
        if (isGuide) {
            frameActionBar.visibility = View.GONE
        }
        if (cacheData.isNotEmpty()) {
            Glide.with(this)
                    .load(cacheData[0])
                    .into(ivContent)
        }
        if (cacheData.size == 1) {
            ivNext.setImageResource(R.drawable.icon_guide_anim_finish)
        }
    }

    private fun generator(name: String) {
        cacheData = when (name) {
            "guide" -> {
                arrayOf(R.drawable.icon_anim_guide_in, R.drawable.draw_world_guide_anim_4, R.drawable.draw_world_guide_anim_1, R.drawable.draw_world_guide_anim_2, R.drawable.draw_world_guide_anim_3)
            }
            "voiceVisible" -> {
                arrayOf(R.drawable.icon_anim_guide_operator)
            }
            else -> {
                arrayOf()
            }
        }
    }

    override fun initEvent() {
        ivOver.setOnClickListener { DialogGuideHint(this).show() }
        ivNext.setOnClickListener {
            if (currentIndex == cacheData.size - 1) {
                if (isGuide) {
                    //发送通知关闭 操场界面的引导按钮
                    val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    SPUtils.setBoolean(this, IConstant.IS_GUIDE_USER_HOME + login.user_id, false)
                    setResult(Activity.RESULT_OK)
                }
                exit()
            } else {
                currentIndex++
                ivNext.setImageResource(R.drawable.icon_guide_anim_next)
                ivContent.setImageResource(cacheData[currentIndex])
                nestedView.scrollTo(0, 0)
            }
            if (currentIndex == cacheData.size - 1) {
                ivNext.setImageResource(R.drawable.icon_guide_anim_finish)
            }
        }
    }

    private fun exit() {
        val readName = SPUtils.getString(this, IConstant.ANIM_READ_STATUS + loginBean.user_id, "")
        if (!readName.contains(name, true)) {
            SPUtils.setString(this, IConstant.ANIM_READ_STATUS + loginBean.user_id, "$readName,$name")
            EventBus.getDefault().post(UpdateCartoonStatus(name))
        }
        finish()
    }

    override fun onBackPressed() {
    }
}