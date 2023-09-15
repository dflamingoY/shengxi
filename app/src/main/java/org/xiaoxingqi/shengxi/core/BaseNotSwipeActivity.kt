package org.xiaoxingqi.shengxi.core

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils

abstract class BaseNotSwipeActivity : SuperActivity() {

    protected lateinit var glideUtil: GlideJudeUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystem()
        glideUtil = GlideJudeUtils(this)
        setContentView(getLayoutId())
        initView()
        initData()
        initEvent()
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initData()
    abstract fun initEvent()
    open fun request(flag: Int) {}

    fun initSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//魅族6.0 的状态栏顶格,没有夜间
                try {
                    if (Build.DISPLAY.contains("flyme", true)) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * 弹土司
     */
    fun showToast(text: String) {
        if (!TextUtils.isEmpty(text))
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        glideUtil.cancelAll()
    }
}