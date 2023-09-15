package org.xiaoxingqi.shengxi.modules.user

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_dialog_user_theme.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.IUpdateUserCoverEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.CropBgActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

class DialogUserThemeActivity : BaseAct() {
    companion object {
        private const val REQUEST_GALLERY = 0x00
        private const val REQUEST_CROP = 0x01
    }

    private var defaultLight = 1
    private var defaultEffects = 0
    private var defaultMachine = 1

    private lateinit var adapter: QuickAdapter<AddCoverData.AddCoverBean>
    private val mData by lazy {
        ArrayList<AddCoverData.AddCoverBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_dialog_user_theme
    }

    override fun initView() {
        recyclerCover.layoutManager = GridLayoutManager(this, 4)
        recyclerCover.isNestedScrollingEnabled = false
    }

    override fun initData() {
        adapter = object : QuickAdapter<AddCoverData.AddCoverBean>(this, R.layout.item_user_theme_cove, mData) {
            //66:100
            val screenWidth = AppTools.getWindowsWidth(this@DialogUserThemeActivity) - AppTools.dp2px(this@DialogUserThemeActivity, 64)
            val width = ((screenWidth - AppTools.dp2px(this@DialogUserThemeActivity, 30)) / 4f + 0.5f).toInt()
            override fun convert(helper: BaseAdapterHelper?, item: AddCoverData.AddCoverBean?) {
                val params = helper!!.getView(R.id.cardLayout).layoutParams as RelativeLayout.LayoutParams
                params.width = width
                params.height = (width * 90f / 65 + 0.5f).toInt()
                helper.getView(R.id.cardLayout).layoutParams = params
                if ("default" == item!!.coverUrl) {
                    helper.getView(R.id.iv_Delete).visibility = View.GONE
                    helper.getView(R.id.iv_img).visibility = View.GONE
                    helper.getView(R.id.view_Stroke).visibility = View.VISIBLE
                } else {
                    helper.getView(R.id.iv_Delete).visibility = View.VISIBLE
                    helper.getView(R.id.iv_img).visibility = View.VISIBLE
                    helper.getView(R.id.view_Stroke).visibility = View.GONE
                    Glide.with(this@DialogUserThemeActivity)
                            .applyDefaultRequestOptions(RequestOptions().centerCrop())
                            .load(item.coverUrl)
                            .into(helper.getImageView(R.id.iv_img))
                    helper.getView(R.id.iv_Delete).setOnClickListener {
                        //删除封面图
                        deleteCover(item)
                    }
                }
            }
        }
        recyclerCover.adapter = adapter
        request(0)
    }

    private var arrays = Array(3) { "" }
    override fun initEvent() {
        tvSave.setOnClickListener {
            transLayout.showProgress()
            //获取默认设置
            if (defaultLight != if (circleLayoutGray.isSelected) 1 else 2) {
                saveSettings("cover_brightness", if (circleLayoutGray.isSelected) "1" else "2")
                arrays[0] = "cover_brightness"
            }
            if (defaultEffects != getEffects()) {
                saveSettings("cover_efficacy", "${getEffects()}")
                arrays[1] = "cover_efficacy"
            }
            if (defaultMachine != if (circleMiniMachineSelf.isSelected) 1 else 2) {
                saveSettings("minimachine_visibility", if (circleMiniMachineSelf.isSelected) "1" else "2")
                arrays[2] = "minimachine_visibility"
            }
            if (TextUtils.isEmpty(arrays.joinToString(separator = ""))) {
                finish()
            }
        }
        circleLayoutGray.setOnClickListener {
            clearFocus(linearCoverLight)
            it.isSelected = true
        }
        circleLayoutOriginal.setOnClickListener {
            clearFocus(linearCoverLight)
            it.isSelected = true
        }
        circleEmpty.setOnClickListener {
            clearFocus(linearScenes1)
            clearFocus(linearScenes2)
            it.isSelected = true
        }
        circleRain.setOnClickListener {
            clearFocus(linearScenes1)
            clearFocus(linearScenes2)
            it.isSelected = true
        }
        circleSnow.setOnClickListener {
            clearFocus(linearScenes1)
            clearFocus(linearScenes2)
            it.isSelected = true
        }
        circleLeaves.setOnClickListener {
            clearFocus(linearScenes1)
            clearFocus(linearScenes2)
            it.isSelected = true
        }
        circleFlowers.setOnClickListener {
            clearFocus(linearScenes1)
            clearFocus(linearScenes2)
            it.isSelected = true
        }
        circleMaple.setOnClickListener {
            clearFocus(linearScenes1)
            clearFocus(linearScenes2)
            it.isSelected = true
        }
        circleMiniMachineSelf.setOnClickListener {
            clearFocus(linearMiniMachine)
            it.isSelected = true
        }
        circleMiniMachineOpen.setOnClickListener {
            clearFocus(linearMiniMachine)
            it.isSelected = true
        }
        adapter.setOnItemClickListener { _, position ->
            if (mData[position].coverUrl == "default") {
                startActivityForResult(Intent(this, AlbumActivity::class.java)
                        .putExtra("isChat", true)
                        .putExtra("count", 1), REQUEST_GALLERY)
            } else {
                startActivity(Intent(this, ShowPicActivity::class.java).putExtra("path", mData[position].coverUrl))
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun getEffects(): Int {
        return if (circleRain.isSelected) 1 else if (circleSnow.isSelected) 2 else if (circleLeaves.isSelected) 3 else if (circleFlowers.isSelected) 4 else if (circleMaple.isSelected) 5 else 0
    }

    private fun deleteCover(item: AddCoverData.AddCoverBean) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(this, "users/${loginBean.user_id}/covers/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    mData.remove(item)
                    if ((mData.filter {
                                it.coverUrl == "default"
                            }).isEmpty()) {
                        mData.add(AddCoverData.AddCoverBean("default"))
                    }
                    SPUtils.setString(this@DialogUserThemeActivity, IConstant.USER_CACHE_COVER_LIST + loginBean.user_id, mData.filter {
                        it.coverUrl != "default"
                    }.joinToString(",") { bean -> bean.coverUrl })
                    adapter.notifyDataSetChanged()
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }
        }, "V4.2")
    }

    /**
     * 保存设置
     */
    private fun saveSettings(name: String, value: String) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("settingName", name)
                .add("settingTag", "moodbook")
                .add("settingValue", value).build()
        OkClientHelper.patch(this, "users/${loginBean.user_id}/settings", formBody, BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                if ((result as BaseRepData).code != 0) {
                    showToast(result.msg)
                    transLayout.showContent()
                } else {
                    if ("cover_brightness" == name) {
                        //主界面更改
                        SPUtils.setString(this@DialogUserThemeActivity, IConstant.USER_THEME_LIGHT_MODEL + loginBean.user_id, value)
                    } else if ("cover_efficacy" == name) {
                        //保存封面效果到本地
                        SPUtils.setString(this@DialogUserThemeActivity, IConstant.USER_THEME_COVER_MODEL + loginBean.user_id, value)
                    }
                    arrays.indices.forEach {
                        if (arrays[it] == name) {
                            arrays[it] = ""
                        }
                    }
                    if (TextUtils.isEmpty(arrays.joinToString(separator = ""))) {
                        transLayout.showContent()
                        finish()
                    }
                }
            }
        }, "V4.2")
    }

    /**
     * 清除焦点
     */
    private fun clearFocus(groupView: ViewGroup) {
        (0 until groupView.childCount).forEach {
            groupView.getChildAt(it).isSelected = false
        }
    }

    private fun setEffectsStatus(status: Int) {
        circleEmpty.isSelected = status == 0
        circleRain.isSelected = status == 1
        circleSnow.isSelected = status == 2
        circleLeaves.isSelected = status == 3
        circleFlowers.isSelected = status == 4
        circleMaple.isSelected = status == 5
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}/settings?settingName=&settingTag=moodbook", NewVersionSetData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NewVersionSetData
                result.data?.let { list ->
                    list.forEach {
                        when (it.setting_name) {
                            "cover_brightness" -> {
                                circleLayoutGray.isSelected = it.setting_value == 1
                                circleLayoutOriginal.isSelected = it.setting_value == 2
                                defaultLight = it.setting_value
                            }
                            "cover_efficacy" -> {
                                setEffectsStatus(it.setting_value)
                                defaultEffects = it.setting_value
                            }
                            "minimachine_visibility" -> {
                                circleMiniMachineSelf.isSelected = it.setting_value == 1
                                circleMiniMachineOpen.isSelected = it.setting_value == 2
                                defaultMachine = it.setting_value
                            }
                        }
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.2")
        OkClientHelper.get(this, "users/${loginBean.user_id}/covers?coverName=moodbook_cover", NewVersionCoverData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NewVersionCoverData
                if (result.code == 0) {
                    result.data?.let {
                        mData.addAll(it)
                    }
                }
                if (mData.size < 8) {
                    mData.add(AddCoverData.AddCoverBean("default"))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(any: Any?) {
                mData.add(AddCoverData.AddCoverBean("default"))
                adapter.notifyDataSetChanged()
            }
        }, "V4.2")
    }

    private fun addCover(path: String) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder().add("coverName", "moodbook_cover")
                .add("bucketId", AppTools.bucketId)
                .add("coverUri", path)
                .build()
        OkClientHelper.post(this, "users/${loginBean.user_id}/covers", formBody, AddCoverData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AddCoverData
                if (result.code == 0) {
                    mData.add(mData.size - 1, result.data)
                    if (mData.size > 8) {
                        mData.removeAt(8)
                    }
                    adapter.notifyDataSetChanged()
                    SPUtils.setString(this@DialogUserThemeActivity, IConstant.USER_CACHE_COVER_LIST + loginBean.user_id, mData.filter {
                        it.coverUrl != "default"
                    }.joinToString(",") { bean -> bean.coverUrl })
                    //保存封面之后, 更新我的界面
                    EventBus.getDefault().post(IUpdateUserCoverEvent())
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.2")
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.operate_exit)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                data?.let {
                    val result = it.getSerializableExtra("result") as java.util.ArrayList<String>
                    if (result.size > 0) {
                        startActivityForResult(Intent(this, CropBgActivity::class.java)
                                .putExtra("isSend", true)
                                .putExtra("path", result[0]), REQUEST_CROP)
                    }
                }
            } else if (requestCode == REQUEST_CROP) {
                data?.let {
                    val netPath = it.getStringExtra("result")
                    addCover(netPath)
                }
            }
        }
    }
}