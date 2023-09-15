package org.xiaoxingqi.shengxi.modules.listen.movies

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_one_user_details_list.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

class OneUserMoviesDetailsListActivity : BaseAct() {

    override fun getLayoutId(): Int {
        return R.layout.activity_one_user_details_list
    }

    override fun initView() {

    }

    override fun initData() {
        val title = intent.getStringExtra("nickName")
        tv_Title.text = title + "的" + resources.getString(R.string.string_Movies)
        val fragment = UserMovieFragment()
        val bundle = Bundle()
        bundle.putString("score", "")
        bundle.putString("uid", intent.getStringExtra("uid"))
        bundle.putString("title", title)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.frameContainer, fragment, "").commit()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }
}