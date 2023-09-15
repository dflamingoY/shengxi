package org.xiaoxingqi.shengxi.modules.user.set

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_privacy_select.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

/**
 * 心情簿-关于浏览我的电影评论设置
 */
class MovieScanPrivacyActivity : BaseAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {
        tv_Content.text = resources.getString(R.string.string_movies_privacy_1)
    }

    override fun initData() {
        when (intent.getIntExtra("type", 0)) {
            0 -> {
                tv_hint_2.text = resources.getString(R.string.string_privacy_14)
                iv_hint_drawable.setImageResource(R.drawable.drawable_privacy_movies)
            }
            1 -> {
                tv_hint_2.text = resources.getString(R.string.string_privacy_15)
                iv_hint_drawable.setImageResource(R.drawable.drawable_privacy_books)
            }
            2 -> {
                tv_hint_2.text = resources.getString(R.string.string_privacy_16)
                iv_hint_drawable.setImageResource(R.drawable.drawable_privacy_songs)
            }
            else -> {
                tv_hint_2.text = resources.getString(R.string.string_privacy_17)
            }
        }
        val title = intent.getStringExtra("title")
        if (!TextUtils.isEmpty(title)) {
            tv_Content.text = title
        }
        val group = intent.getIntExtra("group", 0)
        if (0 == group) {
            viewAll.isSelected = true
            viewFriend.isSelected = false
        } else {
            viewAll.isSelected = false
            viewFriend.isSelected = true
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        viewAll.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("group", 0))
            finish()
        }
        viewFriend.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("group", 1))
            finish()
        }
    }
}