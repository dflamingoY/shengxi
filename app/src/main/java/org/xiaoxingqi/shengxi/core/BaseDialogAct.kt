package org.xiaoxingqi.shengxi.core

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils

abstract class BaseDialogAct : BaseObjectActivity() {
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

    fun initSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            //透明导航栏
            //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        }
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initData()
    abstract fun initEvent()
    open fun request(flag: Int) {}

    /**
     * 弹土司
     */
    fun showToast(text: String) {
        if (!TextUtils.isEmpty(text))
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}