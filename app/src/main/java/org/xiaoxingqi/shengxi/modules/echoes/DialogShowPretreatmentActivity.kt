package org.xiaoxingqi.shengxi.modules.echoes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_dialog_pretreatment.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseDialogAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.PretreatmentData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogShowPretreatmentActivity : BaseDialogAct() {
    private lateinit var adapter: QuickAdapter<PretreatmentData.PretreatmentBean>
    private val mData by lazy {
        ArrayList<PretreatmentData.PretreatmentBean>()
    }
    private var lastId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_dialog_pretreatment
    }

    override fun initView() {
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.BOTTOM)
    }

    override fun initData() {
        adapter = object : QuickAdapter<PretreatmentData.PretreatmentBean>(this, R.layout.item_gravaty_text, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: PretreatmentData.PretreatmentBean?) {
                helper!!.getTextView(R.id.tv_title).text = item!!.reply_remark
            }
        }
        recycler_dialog.layoutManager = LinearLayoutManager(this)
        recycler_dialog.adapter = adapter
        adapter.setLoadMoreEnable(recycler_dialog, recycler_dialog.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_white, recycler_dialog, false))
        request(0)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            setResult(Activity.RESULT_OK, Intent().putExtra("voiceUri", mData[position].resource_uri).putExtra("voiceLength", mData[position].resource_len))
            finish()
        }
        adapter.setOnLoadListener {
            request(0)
        }
        tv_cancel.setOnClickListener {
            finish()
        }
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "admin/${loginBean.user_id}/defaultReply?lastId=$lastId", PretreatmentData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as PretreatmentData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.data != null) {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                        mData.addAll(result.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        for (bean in result.data) {
                            mData.add(bean)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    lastId = result.data[result.data.size - 1].id
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }

            override fun onFailure(any: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.operate_exit)
    }
}