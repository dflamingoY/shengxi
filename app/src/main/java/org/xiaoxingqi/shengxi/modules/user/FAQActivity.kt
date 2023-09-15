package org.xiaoxingqi.shengxi.modules.user

import android.content.Intent
import kotlinx.android.synthetic.main.activity_faq.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.ListenData
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity

class FAQActivity : BaseAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_faq
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        moreQuestion.setOnClickListener {
//            request(1)
            startActivity(Intent(this@FAQActivity, ActionActivity::class.java)
                    .putExtra("title", "声昔宿舍诞生记")
                    .putExtra("url", "28")
                    .putExtra("isHtml", true)
                    .putExtra("isVersion", false)
            )
        }
        moreAbout.setOnClickListener {
            request(0)
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "h5/type?htmlType=1", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                startActivity(Intent(this@FAQActivity, ActionActivity::class.java)
                                        .putExtra("title", result.data[0].html_title)
                                        .putExtra("url", result.data[0].html_id)
                                        .putExtra("isHtml", true)
                                        .putExtra("isVersion", true)
                                )
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            1 -> {
                OkClientHelper.get(this, "h5/type?htmlType=2", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                startActivity(Intent(this@FAQActivity, ActionActivity::class.java)
                                        .putExtra("title", result.data[0].html_title)
                                        .putExtra("url", result.data[0].html_id)
                                        .putExtra("isHtml", true)
                                )
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
        }
    }


}