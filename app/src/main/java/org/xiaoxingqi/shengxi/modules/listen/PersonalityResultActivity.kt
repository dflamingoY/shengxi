package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_personality_result.*
import kotlinx.android.synthetic.main.head_personality_view.view.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCharacterDetails
import org.xiaoxingqi.shengxi.dialog.DialogDeleteTestMsg
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.AutoSplitTextView
import org.xiaoxingqi.shengxi.wedgit.LoadContentWebView
import skin.support.SkinCompatManager
import java.io.File
import java.util.concurrent.ExecutionException

/**
 * 测试结果展示
 */
class PersonalityResultActivity : BaseNormalActivity() {
    private var personalityId: String? = null
    private var currentShreData: PersonnalityDescData? = null
    private lateinit var adapter: QuickAdapter<PersonalityGroupData.PersonalityGroupBean>
    private val mData by lazy { ArrayList<PersonalityGroupData.PersonalityGroupBean>() }
    private lateinit var commentAdapter: QuickAdapter<PersonalityCommentData.PersonalityCommentBean>
    private val commentData by lazy { ArrayList<PersonalityCommentData.PersonalityCommentBean>() }
    private var isNewIntent = false
    private var lastId: String = ""
    private var current = 0
    private var isSelf = false
    private lateinit var manager: SoftkeyBoardManager
    private var isFirstScroll = true
    private var loginBean: LoginData.LoginBean? = null
    private var personalNo: String? = ""
    private var personalTitle: String? = ""
    private lateinit var headView: View
    override fun getLayoutId(): Int {
        return R.layout.activity_personality_result
    }

    @SuppressLint("InflateParams")
    override fun initView() {
        setStatusBarFontIconDark(true)
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || OsUtil.isMIUI() || OsUtil.isFlyme()) {

            } else {
                view_status_bar.setBackgroundColor(Color.parseColor("#cccccc"))
            }
        }
        val params = view_status_bar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        recyclerComment.layoutManager = LinearLayoutManager(this)
        headView = LayoutInflater.from(this).inflate(R.layout.head_personality_view, null, false)
        recyclerComment.isFocusable = false
        recyclerComment.isFocusableInTouchMode = false
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initData() {
        commentAdapter = object : QuickAdapter<PersonalityCommentData.PersonalityCommentBean>(this, R.layout.item_personality_comment, commentData, headView) {

            override fun convert(helper: BaseAdapterHelper?, item: PersonalityCommentData.PersonalityCommentBean?) {
                (helper!!.getView(R.id.tv_Content) as AutoSplitTextView).setString(AppTools.getWindowsWidth(this@PersonalityResultActivity) - AppTools.dp2px(this@PersonalityResultActivity, 86 + 33), item!!.content)
//                helper!!.getTextView(R.id.tv_Content).text = item!!.content
                glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.iv_Avatar), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                helper.getView(R.id.iv_Avatar).setOnClickListener {
                    startActivity(Intent(this@PersonalityResultActivity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
                }
                helper.getTextView(R.id.tv_Delete).visibility = if (item.user_id == loginBean?.user_id) View.VISIBLE else View.GONE
                helper.getTextView(R.id.tv_Name).text = item.nick_name
                helper.getTextView(R.id.tv_character_type).text = item.personality_title
                try {
                    helper.getView(R.id.tv_character_type).isSelected=item.personality_no.contains("T", true)
                    helper.getView(R.id.tv_character_type).visibility = View.VISIBLE
                } catch (e: Exception) {
                    helper.getView(R.id.tv_character_type).visibility = View.GONE
                }
                helper.getView(R.id.tv_Delete).setOnClickListener {
                    /**
                     * 删除
                     */
                    DialogDeleteTestMsg(this@PersonalityResultActivity).setOnClickListener(View.OnClickListener {
                        deleteComment(item)
                    }).show()
                }
            }
        }
        recyclerComment.setHasFixedSize(true)
        recyclerComment.adapter = commentAdapter
        personalNo = intent.getStringExtra("personal_no")
        personalTitle = intent.getStringExtra("personal_title")
        personalityId = intent.getStringExtra("personality")
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean?.user_id, "I")
        if (IConstant.USER_EXTROVERT.equals(character, true)) {
//            headView.tv_Share.text = resources.getString(R.string.string_character_test_e_1)
        }
        val id = SPUtils.getString(this, "${IConstant.PERSONALITYRESULT}_${loginBean?.user_id}", "")
        if (!TextUtils.isEmpty(personalityId) && personalityId == id) {//是自己
            headView.tv_Empty_Hint.text = resources.getString(R.string.string_test_3)
            isSelf = true
        } else {
            headView.tv_Empty_Hint.text = resources.getString(R.string.string_test_4)
            isSelf = false
            headView.tv_Comment.text = resources.getString(R.string.string_character_comment_other)
        }
        mData.add(PersonalityGroupData.PersonalityGroupBean())
        isNewIntent = intent.getBooleanExtra("isNewIntent", false)
        adapter = object : QuickAdapter<PersonalityGroupData.PersonalityGroupBean>(this, R.layout.item_horizontal_group, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: PersonalityGroupData.PersonalityGroupBean?) {
                helper!!.getTextView(R.id.tv_Name).text = item!!.role_name
                if (!TextUtils.isEmpty(item.role_pic_url))
                    glideUtil.loadGlide(item.role_pic_url, helper.getImageView(R.id.iv_Avatar),0, glideUtil.getLastModified(item.role_pic_url))
                item.role_from?.let {
                    helper.getTextView(R.id.tv_From).text = item.role_from
                }
            }
        }
        headView.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        headView.recyclerView.adapter = adapter
        headView.webView.setBackgroundColor(0)
        val settings = headView.webView.settings
        settings.builtInZoomControls = false
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.setSupportZoom(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        manager = SoftkeyBoardManager(window.decorView, false)
        request(0)
        transLayout.showProgress()
        request(2)
    }

    override fun onPause() {
        super.onPause()
        closeMenu()
    }

    private var isExpend = true
    override fun initEvent() {
        manager.addSoftKeyboardStateListener(keyboardListener)
        adapter.setOnItemClickListener { _, position ->
            DialogCharacterDetails(this).setGlide(glideUtil).setData(mData[position]).show()
        }
        headView.tv_Expend.setOnClickListener {
            val params = headView.webView.layoutParams as LinearLayout.LayoutParams
            if (isExpend) {
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT
                headView.tv_Expend.text = resources.getString(R.string.string_test_6)
            } else {
                params.height = AppTools.dp2px(this, 320)
                headView.tv_Expend.text = resources.getString(R.string.string_test_5)
                recyclerComment.scrollToPosition(0)
            }
            isExpend = !isExpend
            headView.webView.layoutParams = params
            filingBg()
        }
        btn_Back.setOnClickListener {
            if (!intent.getBooleanExtra("isShack", false)) {//需要返回 摇一摇
                if (isNewIntent) {
                    startActivity(Intent(this, CharacterTestActivity::class.java))
                }
            }
            finish()
        }
        headView.tv_Share.setOnClickListener {
            /**
             * 保存截图
             */
            currentShreData?.let {
                if (!TextUtils.isEmpty(it.data.img_url)) {
                    save(it.data.img_url)
                }
            }
        }
        headView.tv_Comment.setOnClickListener {
            linear_Comment.visibility = View.VISIBLE
            et_Content.requestFocus()
        }
        et_Content.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                tv_Count.text = "${s?.length}/100"
                tv_Send.isSelected = s?.length!! > 0
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        et_Content.setOnEditorActionListener { _, _, event -> (event?.keyCode == KeyEvent.KEYCODE_ENTER) }
        tv_Send.setOnClickListener {
            if (tv_Send.isSelected) {
                push(et_Content.text.toString().trim())
            }
        }
        recyclerComment.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (linear_Comment.visibility == View.VISIBLE) {
                    linear_Comment.visibility = View.GONE
                }
            }
        })
        headView.webView.setWebClientListener(object : LoadContentWebView.onWebClientListener {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            }

            override fun onPageFinished(view: WebView?, url: String?) {
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            }

            override fun contentFinish(value: String?) {
//                headView.ivBlueCircle.visibility = View.VISIBLE
            }
        })
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
                    return Glide.with(this@PersonalityResultActivity)
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
                try {
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
                } catch (e: Exception) {
                    showToast("保存失败")
                }
                transLayout.showContent()
            }
        }.execute()
    }

    private var task: AsyncTask<Void, Void, File>? = null


    private fun filingBg() {
        if (isFirstScroll) {
            isFirstScroll = false
            if (intent.getBooleanExtra("isComment", false)) {
//                recyclerComment.scrollToPosition(1)
                val scroller = TopSmoothScroller(this)
                scroller.targetPosition = 1
                recyclerComment.layoutManager?.startSmoothScroll(scroller)

            }
        }
    }


    private class TopSmoothScroller(context: Context?) : LinearSmoothScroller(context) {

        override fun getHorizontalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return 10f / displayMetrics?.densityDpi!!
        }
    }


    private fun closeMenu() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(et_Content.windowToken, 0)
    }

    private val keyboardListener = object : SoftkeyBoardManager.SoftKeyboardStateListener {
        override fun onSoftKeyboardOpened(keyboardHeightInPx: Int) {
            var navigation = AppTools.getNavigationBarHeight(this@PersonalityResultActivity)
            val statusBarHeight = AppTools.getStatusBarHeight(this@PersonalityResultActivity)
            if (OsUtil.isMIUI()) {//小米系统判断虚拟键是否显示方法
                if (Settings.Global.getInt(contentResolver, "force_fsg_nav_bar", 0) != 0) {
                    //开启手势，不显示虚拟键  则导航栏高度为0
                    navigation = 0
                }
            }
            val keyboardHeight = keyboardHeightInPx - navigation - statusBarHeight
            val params = linear_Comment.layoutParams as FrameLayout.LayoutParams
            if(linear_Comment.visibility!=View.VISIBLE){
                linear_Comment.visibility=View.VISIBLE
            }
            params.setMargins(0, 0, 0, keyboardHeight)
            linear_Comment.layoutParams = params
            et_Content.requestFocus()
            et_Content.isFocusableInTouchMode = true
        }

        override fun onSoftKeyboardClosed() {
            val params = linear_Comment.layoutParams as FrameLayout.LayoutParams
            params.setMargins(0, 0, 0, 0)
            linear_Comment.layoutParams = params
        }
    }

    /**
     * 删除留言
     */
    private fun deleteComment(item: PersonalityCommentData.PersonalityCommentBean?) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "messageCover/1/$personalityId/${item!!.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    commentData.remove(item)
                    commentAdapter.notifyDataSetChanged()
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.0")

    }

    /**
     * 提交留言
     */
    private fun push(comment: String) {
        transLayout.showProgress()
        OkClientHelper.post(this, "messageCover/1/$personalityId", FormBody.Builder().add("content", comment).build(), PersonnalityDescData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as PersonnalityDescData
                if (result.code == 0) {
                    et_Content.setText("")
                    showToast("留言成功")
                    headView.tv_Empty_Hint.visibility = View.GONE
                    val infoData = PreferenceTools.getObj(this@PersonalityResultActivity, IConstant.USERCACHE, UserInfoData::class.java)
                    val bean = PersonalityCommentData.PersonalityCommentBean(infoData.data.avatar_url, comment)
                    bean.id = result.data.id
                    bean.user_id = infoData.data.user_id
                    bean.nick_name = infoData.data.nick_name
                    bean.personality_no = personalNo
                    bean.personality_title = personalTitle
                    commentData.add(0, bean)
                    commentAdapter.notifyDataSetChanged()
                    linear_Comment.visibility = View.GONE
                    closeMenu()
                    filingBg()
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.0")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//获取用户测试结果
                OkClientHelper.get(this, "personality/$personalityId", PersonnalityDescData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as PersonnalityDescData
                        if (result.code == 0) {
                            currentShreData = result
                            headView.tv_Example1.text = result.data.personality_title + resources.getString(R.string.string_charact_test_10)
                            headView.tv_Comment_Type.text = result.data.personality_title + "留言板"
                            headView.tv_personal_type.text = String.format(resources.getString(R.string.string_personal_match_type), result.data.personality_title)
                            if (isSelf) {
                                headView.tv_characterType.text = "你是${result.data.personality_title}"
                            } else {
                                headView.tv_characterType.text = result.data.personality_title
//                                headView.tv_characterType.setTextColor(Color.parseColor("#FFECE02C"))
                            }
                            headView.webView.setHtml(result.data.personality_desc_long)
                            request(1)
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V3.0")
            }
            1 -> {//获取代表角色
                OkClientHelper.get(this, "personalityRole/$personalityId", PersonalityGroupData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PersonalityGroupData
                        mData.clear()
                        adapter.notifyDataSetChanged()
                        if (result.code == 0) {
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
            2 -> {//获取同族留言
//                transLayout.showProgress()
                OkClientHelper.get(this, "messageCover/1/$personalityId?lastId=$lastId&page=$current", PersonalityCommentData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PersonalityCommentData
                        if (result.code == 0) {
                            try {
                                if (null != result.data) {
                                    for (bean in result.data) {
                                        commentData.add(bean)
                                        commentAdapter.notifyItemInserted(commentAdapter.itemCount - 1)
                                    }
                                    if (TextUtils.isEmpty(lastId) && current == 0) {
                                        filingBg()
                                    }
                                    if (result.data.size >= 10) {
                                        current++
                                        lastId = result.data[result.data.size - 1].id.toString()
                                        transLayout.showContent()
                                        request(2)
                                    } else {
                                        if (commentData.size > 0) {
                                            transLayout.showContent()
                                        }
                                    }
                                } else {
                                    if (commentData.size > 0) {
                                        filingBg()
                                        transLayout.showContent()
                                    }
                                }
                                if (commentData.size == 0) {
                                    headView.tv_Empty_Hint.visibility = View.VISIBLE
                                    filingBg()
                                    transLayout.showContent()
                                } else {
                                    headView.tv_Empty_Hint.visibility = View.GONE
                                }
                            } catch (e: Exception) {

                            }
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.0")
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (linear_Comment.visibility == View.VISIBLE) {
                linear_Comment.visibility = View.GONE
                return true
            }
            if (!intent.getBooleanExtra("isShack", false))
                if (isNewIntent) {
                    startActivity(Intent(this, CharacterTestActivity::class.java))
                    finish()
                    return true
                }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        task?.cancel(true)
    }
}