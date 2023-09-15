package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import com.alibaba.fastjson.JSONArray
import com.bumptech.glide.Glide
import com.nineoldandroids.animation.*
import kotlinx.android.synthetic.main.activity_find_friends.*
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogFind1
import org.xiaoxingqi.shengxi.dialog.DialogFind2
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.ViewFindWord

class FindFriendsActivity : BaseNormalActivity() {
    companion object {
        private const val REQUEST_ADD = 0x00
        private const val REQUEST_EDIT = 0x01
    }

    private var userBean: BaseUserBean? = null
    private var animator: ValueAnimator? = null
    private val wordList by lazy { ArrayList<String>() }

    override fun getLayoutId(): Int {
        return R.layout.activity_find_friends
    }

    override fun initView() {
        val params = view_status_bar.layoutParams
        params.height = AppTools.getStatusBarHeight(this)
        view_status_bar.layoutParams = params
    }

    override fun initData() {
        request(0)
        request(2)
        //1080 2340
//        animate() 1440 2960
//        Log.d("Mozator", "${AppTools.getWindowsHeight(this)}  ${AppTools.getWindowsHeight(this)}")
//        Log.d("Mozator", "dpi : ${resources.displayMetrics.densityDpi}")
//        Log.d("Mozator", "dpi : ${resources.displayMetrics.scaledDensity}")
    }

    /**
     * 执行动画
     */
    private fun animate() {
        animator = ValueAnimator.ofFloat(360f, 0f).setDuration(1000)
        animator?.repeatCount = ValueAnimator.INFINITE
        animator?.repeatMode = ValueAnimator.RESTART
        animator?.interpolator = LinearInterpolator()
        animator?.addUpdateListener {
            val value = it.animatedValue as Float
            cardLayout.rotationY = value
        }
        animator?.start()
    }

    private fun stopAnimator() {
        animator?.let {
            it.end()
            it.cancel()
            animator = null
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        toggle_Button.setOnCheckedChangeListener { _, _ ->
            setOnCheck()
        }
        iv_search.setOnClickListener {
            if (userBean != null) {
                startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", userBean?.id))
            }
        }
        ivSearchHint.setOnClickListener {
            DialogFind2(this).show()
        }
        ivAddWord.setOnClickListener {
            if (it.isSelected)
                startActivityForResult<FindWordActivity>(REQUEST_ADD, "data" to wordList.joinToString(" "))
        }
        tvFind.setOnClickListener {
            if (wordList.isEmpty()) {
                DialogFind1(this@FindFriendsActivity).show()
                return@setOnClickListener
            }
            iv_search.setImageResource(R.drawable.shape_circle_orange)
            animate()
            request(1)
        }
        linearNameInfo.setOnClickListener {
            userBean?.let {
                startActivity<UserDetailsActivity>("id" to it.id)
            }
        }
    }

    private fun showTitleAnim() {
        tvSmiler.visibility = View.VISIBLE
        linearNameInfo.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(tvSmiler, "translationY", -AppTools.getWindowsHeight(this).toFloat(), 0f).setDuration(420).start()
        ObjectAnimator.ofFloat(linearNameInfo, "translationY", -AppTools.getWindowsHeight(this).toFloat(), 0f).setDuration(520).start()
    }

    private fun setOnCheck() {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${loginBean.user_id}/setting", FormBody.Builder().add("lookingFor", if (toggle_Button.isChecked) "1" else "0").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                toggle_Button.isChecked = !toggle_Button.isChecked
                transLayout.showContent()
            }
        })
    }

    private fun attachView() {
        if (linearContainer.childCount > 1) {
            (0..linearContainer.childCount - 2).forEach { _ ->
                linearContainer.removeViewAt(0)
            }
        }
        if (wordList.isNotEmpty()) {
            linearContainer.gravity = Gravity.START
        } else
            linearContainer.gravity = Gravity.CENTER
        wordList.forEach { name ->
            val child = ViewFindWord(this@FindFriendsActivity)
            child.setTitle(name)
            linearContainer.addView(child, linearContainer.childCount - 1)
            child.setOnClickListener {
                wordList.remove(name)
                val array = JSONArray()
                wordList.forEach {
                    array.add(it)
                }
                addWord(array.toJSONString())
                linearContainer.removeView(it)
                if (linearContainer.childCount == 1) {
                    linearContainer.gravity = Gravity.CENTER
                }
            }
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//请求用户设置
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}/setting", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            toggle_Button.isChecked = result.data.looking_for == 1
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            1 -> {//找寻
                transLayout.showProgress()
                OkClientHelper.get(this, "experience/search", FindUserData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as FindUserData
                        if (result.data == null) {
                            Glide.with(this@FindFriendsActivity)
                                    .load(R.mipmap.icon_find_empty)
                                    .into(iv_search)
                            tvEmptyFind.visibility = View.VISIBLE
                            tvSmiler.visibility = View.GONE
                            linearNameInfo.visibility = View.GONE
                            userBean = null
                        } else {
                            tvEmptyFind.visibility = View.GONE
                            linearNameInfo.visibility = View.VISIBLE
                            //缓存查询的关键字  第一次请求成功之后才保存. 其他时间不保存
                            userBean = result.data.user
                            glideUtil.loadGlide(result.data.user.avatar_url, iv_search, 0, glideUtil.getLastModified(result.data.user.avatar_url))
                            iv_user_type.visibility = if (result.data.user.identity_type == 0) View.GONE else View.VISIBLE
                            iv_user_type.isSelected = result.data.user.identity_type == 1
                            tvUserName.text = result.data.user.nick_name
                            if (!result.data.user_tag.isNullOrEmpty()) {
                                var count = 0
                                var matchResult = ""
                                result.data.user_tag.forEach { otherTag ->
                                    for (i in (0 until wordList.size)) {
                                        if (otherTag == wordList[i]) {
                                            count++
                                            matchResult += "$otherTag "
                                            break
                                        }
                                    }
                                }
                                tvSmiler.text = String.format(resources.getString(R.string.string_8), count) + matchResult
                            }
                            showTitleAnim()
                        }
                        stopAnimator()
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        userBean = null
                        Glide.with(this@FindFriendsActivity)
                                .load(R.mipmap.icon_find_empty)
                                .into(iv_search)
                        tvEmptyFind.visibility = View.VISIBLE
                        tvSmiler.visibility = View.GONE
                        linearNameInfo.visibility = View.GONE
                        transLayout.showContent()
                    }
                }, "V4.3")
            }
            2 -> {//获取关键字
                OkClientHelper.get(this, "experience/tags", FindResultData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as FindResultData
                        ivAddWord.isSelected = true
                        if (result.data != null) {
                            wordList.addAll(result.data.tag)
                            attachView()
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V4.3")
            }
        }
    }

    private fun addWord(word: String) {
        transLayout.showProgress()
        OkClientHelper.post(this, "experience/tags", FormBody.Builder().add("tag", word).build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
            }
        }, "V4.3")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD) {
                data?.let {
                    val name = it.getStringExtra("name")
                    wordList.add(name)
                    attachView()
                    val array = JSONArray()
                    wordList.forEach {
                        array.add(it)
                    }
                    addWord(array.toJSONString())
                }
            } else if (requestCode == REQUEST_EDIT) {
                data?.let {
                    val name = it.getStringExtra("name")
                    it.getStringExtra("wordList").split(" ").filter { filter ->
                        filter.isNotEmpty()
                    }.let { list ->
                        wordList.clear()
                        wordList.addAll(list)
                    }
                    val array = JSONArray()
                    wordList.forEach {
                        array.add(it)
                    }
                    addWord(array.toJSONString())
                    attachView()
                }
            }
        }
    }

    override fun onDestroy() {
        stopAnimator()
        super.onDestroy()
    }
}