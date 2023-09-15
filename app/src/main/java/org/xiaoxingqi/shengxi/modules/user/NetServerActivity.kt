package org.xiaoxingqi.shengxi.modules.user

import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_net_server.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class NetServerActivity : BaseAct() {
    private var originalBucketId = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_net_server
    }

    override fun initView() {
    }

    override fun initData() {
        recycle()
        request(1)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            if (originalBucketId == if (!tv_net_china.isSelected) 0 else if (!tv_net_us.isSelected) 1 else 2) {
                finish()
            } else {
                transLayout.showProgress()
                updateInfo(if (!tv_net_china.isSelected) 0 else if (!tv_net_us.isSelected) 1 else 2)
            }
        }
        tv_net_china.setOnClickListener {
            recycle()
            tv_net_china.isSelected = false
        }
        tv_net_us.setOnClickListener {
            recycle()
            tv_net_us.isSelected = false
        }
        tv_net_jp.setOnClickListener {
            recycle()
            tv_net_jp.isSelected = false
        }
        tv_save.setOnClickListener {

        }
    }

    private fun recycle() {
        tv_net_us.isSelected = true
        tv_net_jp.isSelected = true
        tv_net_china.isSelected = true
    }

    private fun updateInfo(bucketId: Int) {
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${obj.user_id}/setting", FormBody.Builder().add("bucketId", bucketId.toString()).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("保存成功")
                    finish()
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/" + loginBean.user_id + "/setting", PatchData::class.java, object : OkResponse {
            override fun success(result: Any) {
                result as PatchData
                if (result.code == 0) {
                    recycle()
                    originalBucketId = result.data.bucket_id
                    when {
                        result.data.bucket_id == 0 -> tv_net_china.isSelected = false
                        result.data.bucket_id == 1 -> tv_net_us.isSelected = false
                        result.data.bucket_id == 2 -> tv_net_jp.isSelected = false
                    }
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any) {
                transLayout.showContent()
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (originalBucketId != if (!tv_net_china.isSelected) 0 else if (!tv_net_us.isSelected) 1 else 2) {
                transLayout.showProgress()
                updateInfo(if (!tv_net_china.isSelected) 0 else if (!tv_net_us.isSelected) 1 else 2)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}