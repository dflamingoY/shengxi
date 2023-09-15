package org.xiaoxingqi.shengxi.modules.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_recycler.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.NormalDecoration
import org.xiaoxingqi.shengxi.model.login.NationalData
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.SkinCompatManager

class CountryActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<NationalData.NationalEntity>
    private lateinit var normalDecoration: NormalDecoration
    private val mData = ArrayList<NationalData.NationalEntity>()
    private var dividingIndex = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_recycler
    }

    override fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        relativeFriendsInfo.visibility = View.GONE
    }

    override fun initData() {
        tv_Title.text = resources.getString(R.string.string_country_1)
        adapter = object : QuickAdapter<NationalData.NationalEntity>(this, R.layout.item_text, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: NationalData.NationalEntity?) {
                helper!!.getTextView(R.id.tv_country).text = item!!.area_name
                helper.getTextView(R.id.tv_National).text = "+${item.phone_code}"
                helper.getView(R.id.viewLine).visibility = if (dividingIndex - 1 == helper.itemView.tag) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
        normalDecoration = object : NormalDecoration(this) {
            override fun getHeaderName(pos: Int): String {
                return when {
                    pos < dividingIndex -> resources.getString(R.string.string_country_2)
                    else -> {
                        resources.getString(R.string.string_country_3)
                    }
                }
            }
        }
        if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {//白天模式
            normalDecoration.setHeaderContentColor(resources.getColor(R.color.colorGrayContent))
            normalDecoration.setTextColor(resources.getColor(R.color.colorTextGray))
        } else {
            normalDecoration.setHeaderContentColor(Color.parseColor("#12121f"))
            normalDecoration.setTextColor(Color.parseColor("#3E3E4A"))
        }
        normalDecoration.setHeaderHeight(AppTools.dp2px(this, 44))
        normalDecoration.setTextPaddingLeft(AppTools.dp2px(this, 15))
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(normalDecoration)
        request(0)
    }

    override fun initEvent() {
        adapter.setOnItemClickListener { _, position ->
            setResult(Activity.RESULT_OK, Intent().putExtra("national", mData[position]))
            finish()
        }
        btn_Back.setOnClickListener { finish() }
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "nation", NationalData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NationalData
                result.data.hot?.let {
                    mData.addAll(it)
                }
                dividingIndex = mData.size
                result.data.other?.let {
                    mData.addAll(it)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        normalDecoration.onDestory()
    }

}