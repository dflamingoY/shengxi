package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_character_test.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.PersonalityGroupData
import org.xiaoxingqi.shengxi.model.PersonnalityDescData
import org.xiaoxingqi.shengxi.model.TestEnjoyCountData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.FileUtils
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import java.io.File
import java.util.concurrent.ExecutionException

/**
 * 用户测一测界面
 */
class CharacterTestActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<PersonalityGroupData.PersonalityGroupBean>
    private val mData by lazy { ArrayList<PersonalityGroupData.PersonalityGroupBean>() }
    private var currentShreData: PersonnalityDescData? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_character_test
    }

    override fun initView() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val sta = if (loginBean != null) {
            SPUtils.getString(this, IConstant.LANGUAGE + loginBean.user_id, "")
        } else {
            SPUtils.getString(this, IConstant.LANGUAGE, "")
        }
        if (IConstant.HK.equals(sta, true) || IConstant.TW.equals(sta, true)) {
            iv_test.setImageResource(R.drawable.draw_test_test_big_hk)
            StrokeTextView.setImageResource(R.drawable.draw_test_test_little_hk)
            ivTestHint.setImageResource(R.mipmap.icon_test_hint_hk)
        } else {
            StrokeTextView.setImageResource(R.drawable.draw_test_test_little)
            iv_test.setImageResource(R.drawable.draw_test_test_big)
            ivTestHint.setImageResource(R.mipmap.icon_test_hint)
        }
    }

    override fun onResume() {
        super.onResume()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
            tv_character_test_hint_1.text = resources.getString(R.string.string_character_test_e_1)
        } else {
            tv_character_test_hint_1.text = resources.getString(R.string.string_character_test_i_1)
        }
    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val result = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", "")
        if (TextUtils.isEmpty(result)) {//没有做过测试, 或者切换账号
            relativeEmptyTest.visibility = View.VISIBLE
            nestedResult.visibility = View.GONE
            iv_TestBg.visibility = View.GONE
            request(2)
        } else {//先展示, 再去请求网络, 获取最新的result
            relativeEmptyTest.visibility = View.GONE
            nestedResult.visibility = View.VISIBLE
            iv_TestBg.visibility = View.VISIBLE
            request(0)
        }
        initArrays()
        adapter = object : QuickAdapter<PersonalityGroupData.PersonalityGroupBean>(this, R.layout.item_test_avatar, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: PersonalityGroupData.PersonalityGroupBean?) {
                helper!!.getTextView(R.id.tv_Name).text = item!!.personality_title
                if (!TextUtils.isEmpty(item.role_pic_url))
                    glideUtil.loadGlide(item.role_pic_url, helper.getImageView(R.id.iv_Avatar), R.drawable.drawable_default_tmpry, glideUtil.getLastModified(item.role_pic_url))
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.isFocusable = false
        recyclerView.isFocusableInTouchMode = false
    }

    /**
     * 初始化假数据, 避免界面弹跳突兀
     */
    private fun initArrays() {
        val groupData = PreferenceTools.getObj(this, IConstant.PERSONALITYRESULT, PersonalityGroupData::class.java)
        if (null != groupData && groupData.data != null) {
            mData.addAll(groupData.data)
        } else {
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
            mData.add(PersonalityGroupData.PersonalityGroupBean())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val result = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", "")
        if (TextUtils.isEmpty(result)) {//没有做过测试, 或者切换账号
            relativeEmptyTest.visibility = View.VISIBLE
            nestedResult.visibility = View.GONE
            iv_TestBg.visibility = View.GONE
        } else {//先展示, 再去请求网络, 获取最新的result
            relativeEmptyTest.visibility = View.GONE
            nestedResult.visibility = View.VISIBLE
            iv_TestBg.visibility = View.VISIBLE
            request(0)
            initArrays()
        }
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            startActivity(Intent(this@CharacterTestActivity, PersonalityResultActivity::class.java)
                    .putExtra("personal_no", currentShreData?.data?.personality_no)
                    .putExtra("personal_title", currentShreData?.data?.personality_title)
                    .putExtra("personality", mData[position].personality_id))
        }
        cardReTest.setOnClickListener {
            startActivity(Intent(this, PersonalityActivity::class.java))
        }
        iv_Start.setOnClickListener {
            startActivity(Intent(this, PersonalityActivity::class.java))
        }
        btn_Back.setOnClickListener { finish() }
        cardShare.setOnClickListener {
            /**
             * 下载图片
             */
            currentShreData?.let {
                if (!TextUtils.isEmpty(it.data.img_url)) {
                    save(it.data.img_url)
                }
            }
        }
        cardResult.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            val id = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", "")
            startActivity(Intent(this, PersonalityResultActivity::class.java)
                    .putExtra("personal_no", currentShreData?.data?.personality_no)
                    .putExtra("personal_title", currentShreData?.data?.personality_title)
                    .putExtra("personality", id))
        }
        cardNewCount.setOnClickListener {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            val id = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", "")
            startActivity(Intent(this, PersonalityResultActivity::class.java)
                    .putExtra("personal_no", currentShreData?.data?.personality_no)
                    .putExtra("personal_title", currentShreData?.data?.personality_title)
                    .putExtra("isComment", true)
                    .putExtra("personality", id))
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(url: String) {
        transLayout.showProgress()
        var path: String = url
        if (url.contains("?")) {
            path = url.substring(0, url.lastIndexOf("?"))
        }
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg") && !suffix.contains(".png") && !suffix.contains(".gif")) {
            suffix = "$suffix.jpg"
        }
        task = object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(this@CharacterTestActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(System.currentTimeMillis().toString())))
                            .load(path)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(s: File?) {
                if (s == null) {
                    showToast("保存失败")
                } else {
                    val bootfile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                    if (!bootfile.exists()) {
                        bootfile.mkdirs()
                    }
                    val file = File(bootfile.absolutePath + "/" + suffix)
                    FileUtils.copyFile(s, file)
                    //在手机相册中显示刚拍摄的图片
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(file)
                    mediaScanIntent.data = contentUri
                    sendBroadcast(mediaScanIntent)
                    showToast("截图已保存到手机相册")
                }
                transLayout.showContent()
            }
        }.execute()
    }

    private var task: AsyncTask<Void, Void, File>? = null
    override fun request(flag: Int) {
        when (flag) {
            0 -> {//获取用户测试结果
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                val result = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", "")
                OkClientHelper.get(this, "personality/$result", PersonnalityDescData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as PersonnalityDescData
                        if (result.code == 0) {
                            currentShreData = result
                            tv_Desc.text = result.data.personality_title
                            tv_JoinCount.text = result.data.today_join.toString() + "名"
                            request(1)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
            }
            1 -> {//获取代表角色
                OkClientHelper.get(this, "personalityRole", PersonalityGroupData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PersonalityGroupData
                        if (result.code == 0) {
                            PreferenceTools.saveObj(this@CharacterTestActivity, IConstant.PERSONALITYRESULT, result)
                            mData.clear()
                            adapter.notifyDataSetChanged()
                            for (bean in result.data) {
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
            }
            2 -> {
                OkClientHelper.get(this, "personalityTest/dailyStatistics", TestEnjoyCountData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as TestEnjoyCountData
                        if (result.code == 0) {
                            result.data?.let {
                                if (it.num > 0) {
                                    tv_toady_enjoy_count.text = Html.fromHtml(String.format(resources.getString(R.string.string_character_testers), result.data.num.toString()))
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.0")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel(true)
    }

}