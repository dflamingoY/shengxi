package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.activity_push_wording.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogAlbumHowOperator
import org.xiaoxingqi.shengxi.dialog.DialogCancelCommit
import org.xiaoxingqi.shengxi.impl.AlarmUpdateEvent
import org.xiaoxingqi.shengxi.impl.OnAlarmItemClickListener
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import skin.support.SkinCompatManager
import java.io.File
import java.util.regex.Pattern

class PushWordingActivity : BaseNormalActivity() {
    //本次交互, 查看是否有下载过
    private var isDownloadSensitive = false

    override fun getLayoutId(): Int {
        return R.layout.activity_push_wording
    }

    override fun initView() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (SPUtils.getBoolean(this, IConstant.PUSH_WORD_HINT_BANNER + loginBean.user_id, false)) {
            relativeShowShareHint.visibility = View.GONE
        }
        val params = viewStatus.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        viewStatus.layoutParams = params
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                viewStatus.setBackgroundColor(Color.WHITE)
            } else {//夜间模式
                viewStatus.setBackgroundColor(Color.parseColor("#181828"))
            }
        } else {
            if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
                viewStatus.setBackgroundColor(Color.parseColor("#cccccc"))
            } else {//夜间模式
                viewStatus.setBackgroundColor(Color.parseColor("#181828"))
            }
        }
    }

    override fun initData() {

    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            if (!TextUtils.isEmpty(etWord.text.toString().trim())) {
                DialogCancelCommit(this).setOnClickListener(View.OnClickListener {
                    finish()
                }).show()
                return@setOnClickListener
            }
            finish()
        }
        etWord.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                tv_Commit.isSelected = s!!.isNotEmpty() && !alarmTagView.getSelectedTabType().isNullOrEmpty()
                if (s.length > 100) {
                    tvCount.text = Html.fromHtml(String.format(resources.getString(R.string.string_count_10).replace("10", "100"), s?.length.toString()))
                } else
                    tvCount.text = "${s?.length}/100"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        tvAnonymous.setOnClickListener {
            it.isSelected = !it.isSelected
            tvAnonymous.text = if (it.isSelected) resources.getString(R.string.string_alarm_17) else resources.getString(R.string.string_alarm_16)
        }
        iv_Close.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            relativeShowShareHint.visibility = View.GONE
            SPUtils.setBoolean(this, IConstant.PUSH_WORD_HINT_BANNER + loginBean.user_id, true)
        }
        tv_Commit.setOnClickListener {
            if (alarmTagView.getSelectedTabType().isNullOrEmpty()) {
                showToastInLocation("请选择台词标签")
                return@setOnClickListener
            }
            if (it.isSelected) {
                //检测是否超过10行
                if (etWord.lineCount > 10) {
                    showToast("台词最多可发布10行的内容")
                } else
                    request(1)
            }
        }
        etWord.setOnEditorActionListener { _, actionId, _ ->
            //此段文本中超过10个换行符, 则换行按钮不在有效
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                matcherCount() > 9
            } else {
                false
            }
        }
        alarmTagView.setOnItemClick(object : OnAlarmItemClickListener {
            override fun itemClick(type: Int) {
                tv_Commit.isSelected = !AppTools.isEmptyEt(etWord, 0)
            }
        })
    }

    /*
    匹配换行符的数量
     */
    private fun matcherCount(): Int {
        var count = 0
        try {
            val matcher = Pattern.compile("\\n").matcher(etWord.text)
            while (matcher.find()) {
                count++
            }
        } catch (e: Exception) {

        }
        return count
    }

    /**
     * 匹配敏感字
     */
    private fun matchSensitive() {
        //读取数据
        File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.LOCALSENSITIVE}").apply {
            if (exists()) {
                readText().replace("，", ",").split(",").filter {
                    it != "," && it != "，" && !TextUtils.isEmpty(it)
                }.forEach { tag ->
                    //敏感词匹配
                    if (etWord.text.toString().contains(tag, true)) {
                        DialogAlbumHowOperator(this@PushWordingActivity).setTitle("当前文本存在敏感词\n  请修改后再发布").show()
                        etWord.setSelection(etWord.text.indexOf(tag), etWord.text.indexOf(tag) + tag.length)
                        transLayout.showContent()
                        return
                    }
                }
            }
        }
        //发布台词
        request(0)
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {
                OkClientHelper.post(this, "lines", FormBody.Builder()
                        .add("lineContent", etWord.text.toString().let {
                            if (it.length > 100)
                                it.substring(0, 100)
                            else it
                        })
                        .add("tagId", alarmTagView.getSelectedTabType())
                        .add("isAnonymous", if (tvAnonymous.isSelected) "1" else "0").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        transLayout.showContent()
                        result as BaseRepData
                        if (result.code == 0) {
                            /*
                             *刷新台词界面
                             */
                            EventBus.getDefault().post(AlarmUpdateEvent(2, 2))
                            finish()
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.3")
            }
            1 -> {//下载敏感词文件夹
                OkClientHelper.get(this, "admin/lines/sensitive", IntegerRespData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as IntegerRespData
                        //下载文件
                        if (result.code == 0) {
                            if (isDownloadSensitive) {
                                matchSensitive()
                            } else {
                                downSensitive(result.data.sensitive_url)
                            }
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            2 -> {
                OkClientHelper.get(this, "lineTag/top", BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {

                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
        }
    }

    //下载敏感字文件
    private fun downSensitive(url: String) {
        OkClientHelper.downByPath(this, url, File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.LOCALSENSITIVE}").absolutePath, {
            isDownloadSensitive = true
            //读取数据
            matchSensitive()
        }, {
            if (it.message == "0") {
                request(0)
            }
            transLayout.showContent()
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!TextUtils.isEmpty(etWord.text.toString().trim())) {
                DialogCancelCommit(this).setOnClickListener(View.OnClickListener {
                    finish()
                }).show()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.operate_exit)
    }
}