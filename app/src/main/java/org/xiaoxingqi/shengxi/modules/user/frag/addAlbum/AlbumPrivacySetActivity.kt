package org.xiaoxingqi.shengxi.modules.user.frag.addAlbum

import android.app.Activity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_album_privacy_set.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

class AlbumPrivacySetActivity : BaseAct() {
    private var privacy = "1"
    override fun getLayoutId(): Int {
        return R.layout.activity_album_privacy_set
    }

    override fun initView() {

    }

    override fun initData() {
        privacy = intent.getStringExtra("privacy")
        when (privacy) {
            "1" -> {
                view_public.changeStatus(true)
            }
            "2" -> {
                view_friend.changeStatus(true)
            }
            else -> {
                view_privacy.changeStatus(true)
            }
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        view_privacy.setOnClickListener {
            clearStatus()
            view_privacy.changeStatus(true)
            privacy = "3"
            finish()
        }
        view_friend.setOnClickListener {
            clearStatus()
            view_friend.changeStatus(true)
            privacy = "2"
            finish()
        }
        view_public.setOnClickListener {
            clearStatus()
            view_public.changeStatus(true)
            privacy = "1"
            finish()
        }
    }

    private fun clearStatus() {
        view_privacy.changeStatus(false)
        view_friend.changeStatus(false)
        view_public.changeStatus(false)
    }

    override fun finish() {
        setResult(Activity.RESULT_OK, Intent().putExtra("privacy", privacy))
        super.finish()
    }

}