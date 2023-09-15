package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_edit_send.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelEditVoice
import org.xiaoxingqi.shengxi.impl.ChangeTopicInfoEvent
import org.xiaoxingqi.shengxi.model.BaseBean
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.SearchTopicData
import org.xiaoxingqi.shengxi.modules.listen.EditTopicSearchActivity

//只可以编辑普通心情, 非书音影
class EditSendAct : BaseAct() {
    private var bean: BaseBean? = null
    private lateinit var adapter: QuickAdapter<String>
    private var topicName: String? = null
    private var topicId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_edit_send
    }

    override fun initView() {
        imgRecycler.layoutManager = GridLayoutManager(this, 3)
        tvPush.isSelected = true
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        bean = intent.getParcelableExtra("data")
        if (bean == null) {
            showToast("数据异常")
            super.finish()
        } else {
            if (bean!!.img_list == null) {
                imgRecycler.visibility = View.GONE
            }
            adapter = object : QuickAdapter<String>(this, R.layout.item_img, bean!!.img_list) {
                override fun convert(helper: BaseAdapterHelper?, item: String?) {
                    helper!!.getView(R.id.view_Stroke)?.visibility = View.GONE
                    Glide.with(this@EditSendAct)
                            .asBitmap()
                            .load(item)
                            .apply(RequestOptions().centerCrop().error(R.drawable.drawable_default_tmpry))
                            .into(helper.getImageView(R.id.iv_img))
                    helper.getView(R.id.iv_Delete).visibility = View.GONE
                    helper.getView(R.id.viewLayer).visibility = View.VISIBLE
                }
            }
            imgRecycler.adapter = adapter
            voiceAnimProgress.setData(bean!!)
            if (!bean!!.topic_name.isNullOrEmpty()) {
                topicName = bean!!.topic_name
                topicId = bean!!.topic_id.toString()
                tv_AddTopic.visibility = View.GONE
                tvTopic.text = "#${bean!!.topic_name}#X"
            }
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvTopic.setOnClickListener {
            tv_AddTopic.visibility = View.VISIBLE
            tvTopic.visibility = View.GONE
            topicName = ""
            topicId = ""
        }
        tv_AddTopic.setOnClickListener {
            startActivityForResult(Intent(this, EditTopicSearchActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    , 0x00)
        }
        tvPush.setOnClickListener {
            if (TextUtils.isEmpty(topicName)) {
                showToast("话题不能为空")
                return@setOnClickListener
            }
            if (topicName == bean!!.topic_name) {
                showToast("还没有编辑话题")
                return@setOnClickListener
            }
            request(0)
        }

    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
        if (!TextUtils.isEmpty(topicName)) {
            formBody.add("topicName", topicName).add("topicId", topicId)
        }
        OkClientHelper.patch(this, "users/${bean!!.user_id}/voices/${bean!!.voice_id}", formBody.build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    //发送修改话题的
                    EventBus.getDefault().post(ChangeTopicInfoEvent(bean!!.voice_id, topicName!!, topicId!!))
                    dismiss()
                } else
                    showToast(result.msg)
            }
        }, "V4.3")
    }

    private fun dismiss() = super.finish()

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0x00) {
                data?.getParcelableExtra<SearchTopicData.SearchTopicBean>("topicBean")?.let {
                    tvTopic.visibility = View.VISIBLE
                    tvTopic.text = "#${it.topic_name}# X"
                    topicName = it.topic_name
                    topicId = it.topic_id
                    tv_AddTopic.visibility = View.GONE
                }
            }
        }
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    override fun canBeSlideBack(): Boolean {
        return false
    }

    override fun finish() {
        DialogCancelEditVoice(this).setOnClickListener(View.OnClickListener {
            super.finish()
        }).show()
    }

}