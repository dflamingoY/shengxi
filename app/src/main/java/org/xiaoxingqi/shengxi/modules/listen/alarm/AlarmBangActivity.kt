package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_alarm_bang.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseDecorationAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.AlarmBangDubbingData
import org.xiaoxingqi.shengxi.model.DataTitleBean
import org.xiaoxingqi.shengxi.model.alarm.LoaderBoardData
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.ColorArraysTextView
import skin.support.SkinCompatManager
import kotlin.collections.ArrayList

class AlarmBangActivity : BaseAct() {
    private lateinit var adapter: BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>
    private val mData by lazy { ArrayList<DataTitleBean>() }
    private lateinit var monthAdapter: BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>
    private val monthData by lazy { ArrayList<DataTitleBean>() }
    private var allBoardData: LoaderBoardData.LoaderBoardBean? = null
    private var monthBoardData: LoaderBoardData.LoaderBoardBean? = null
    private lateinit var dayAdapter: BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>
    private val dayData by lazy { ArrayList<DataTitleBean>() }
    private var dayBoardData: LoaderBoardData.LoaderBoardBean? = null
    private lateinit var weekAdapter: BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>
    private val weekData by lazy { ArrayList<DataTitleBean>() }
    private var weekBoardData: LoaderBoardData.LoaderBoardBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_alarm_bang
    }

    override fun initView() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators), ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
        swipeRefresh.isEnabled = false
        recyclerViewDay.isNestedScrollingEnabled = false
        recyclerViewWeek.isNestedScrollingEnabled = false
        recyclerViewMonth.isNestedScrollingEnabled = false
        recyclerView.isNestedScrollingEnabled = false

        recyclerViewDay.layoutManager = LinearLayoutManager(this)
        recyclerViewWeek.layoutManager = LinearLayoutManager(this)
        recyclerViewMonth.layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        viewDays.setDividingVisible(View.VISIBLE)
        viewWeeks.setDividingVisible(View.VISIBLE)
        viewMonths.setDividingVisible(View.VISIBLE)
    }

    override fun initData() {
        dayAdapter = object : BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>(this, R.layout.item_text_title, R.layout.layout_alarm_leader_board, dayData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: DataTitleBean?) {
                val position = helper!!.itemView.tag as Int
                if (!item!!.isSelect) {
                    helper.getImageView(R.id.iv_flag).setImageResource(parseMattch(position))
                    dayBoardData?.let { bean ->
                        if (bean.top1 != null && bean.top1.size > position - 1) {
                            glideUtil.loadGlide(bean.top1[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_angel), 0, glideUtil.getLastModified(bean.top1[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_angel_name).text = bean.top1[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerAngel).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_1).apply {
                                visibility = if (bean.top1[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top1[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_angel_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_1).visibility = View.GONE
                            helper.getImageView(R.id.iv_angel).setImageDrawable(null)
                            helper.getTextView(R.id.tv_angel_name).text = ""
                            helper.getView(R.id.viewLayerAngel).visibility = View.GONE
                        }
                        if (bean.top2 != null && bean.top2.size > position - 1) {
                            glideUtil.loadGlide(bean.top2[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_monster), 0, glideUtil.getLastModified(bean.top2[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_monster_name).text = bean.top2[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerMonster).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_2).apply {
                                visibility = if (bean.top2[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top2[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_monster_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_2).visibility = View.GONE
                            helper.getImageView(R.id.iv_monster).setImageDrawable(null)
                            helper.getTextView(R.id.tv_monster_name).text = ""
                            helper.getView(R.id.viewLayerMonster).visibility = View.GONE
                        }
                        if (bean.top3 != null && bean.top3.size > position - 1) {
                            glideUtil.loadGlide(bean.top3[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_god), 0, glideUtil.getLastModified(bean.top3[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_god_name).text = bean.top3[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerGod).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_3).apply {
                                visibility = if (bean.top3[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top3[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_god_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_3).visibility = View.GONE
                            helper.getImageView(R.id.iv_god).setImageDrawable(null)
                            helper.getTextView(R.id.tv_god_name).text = ""
                            helper.getView(R.id.viewLayerGod).visibility = View.GONE
                        }
                        if (bean.top4 != null && bean.top4.size > position - 1) {
                            try {
                                glideUtil.loadGlide(bean.top4[position - 1].user_info.avatar_url, helper.getImageView(R.id.ivWordUser), 0, glideUtil.getLastModified(bean.top4[position - 1].user_info.avatar_url))
                                helper.getTextView(R.id.tvWordCount).text = bean.top4[position - 1].dubbing_num + "配"
                                helper.getView(R.id.viewLayerWord).visibility = View.VISIBLE
                                helper.getView(R.id.ivWordType).apply {
                                    visibility = if (bean.top4[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                    isSelected = bean.top4[position - 1].user_info.identity_type == 1
                                }
                                (helper.getTextView(R.id.tvWordCount) as ColorArraysTextView).setCurrentLevel(position)
                            } catch (e: Exception) {
                            }
                        } else {
                            helper.getView(R.id.ivWordType).visibility = View.GONE
                            helper.getImageView(R.id.ivWordUser).setImageDrawable(null)
                            helper.getTextView(R.id.tvWordCount).text = ""
                            helper.getView(R.id.viewLayerWord).visibility = View.GONE
                        }

                        helper.getView(R.id.linear_angel).setOnClickListener {
                            if (bean.top1 != null && bean.top1.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "1")
                                        .putExtra("type", 3)
                                        .putExtra("id", bean.top1[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_monster).setOnClickListener {
                            if (bean.top2 != null && bean.top2.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "2")
                                        .putExtra("type", 3)
                                        .putExtra("id", bean.top2[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_god).setOnClickListener {
                            if (bean.top3 != null && bean.top3.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "3")
                                        .putExtra("type", 3)
                                        .putExtra("id", bean.top3[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linearWord).setOnClickListener {
                            try {
                                startActivity<TopWordActivity>("type" to 0, "uid" to bean.top4[position - 1].user_info.id)
                            } catch (e: Exception) {
                            }
                        }
                    }
                } else {
                    helper.getTextView(R.id.tv_board_title).text = "今日"
                }
            }
        }
        recyclerViewDay.adapter = dayAdapter
        weekAdapter = object : BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>(this, R.layout.item_text_title, R.layout.layout_alarm_leader_board, weekData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: DataTitleBean?) {
                val position = helper!!.itemView.tag as Int
                if (!item!!.isSelect) {
                    helper.getImageView(R.id.iv_flag).setImageResource(parseMattch(position))
                    weekBoardData?.let { bean ->
                        if (bean.top1 != null && bean.top1.size > position - 1) {
                            glideUtil.loadGlide(bean.top1[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_angel), 0, glideUtil.getLastModified(bean.top1[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_angel_name).text = bean.top1[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerAngel).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_1).apply {
                                visibility = if (bean.top1[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top1[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_angel_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_1).visibility = View.GONE
                            helper.getImageView(R.id.iv_angel).setImageDrawable(null)
                            helper.getTextView(R.id.tv_angel_name).text = ""
                            helper.getView(R.id.viewLayerAngel).visibility = View.GONE
                        }
                        if (bean.top2 != null && bean.top2.size > position - 1) {
                            glideUtil.loadGlide(bean.top2[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_monster), 0, glideUtil.getLastModified(bean.top2[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_monster_name).text = bean.top2[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerMonster).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_2).apply {
                                visibility = if (bean.top2[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top2[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_monster_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getImageView(R.id.iv_monster).setImageDrawable(null)
                            helper.getTextView(R.id.tv_monster_name).text = ""
                            helper.getView(R.id.viewLayerMonster).visibility = View.GONE
                            helper.getView(R.id.iv_user_type_2).visibility = View.GONE
                        }
                        if (bean.top3 != null && bean.top3.size > position - 1) {
                            glideUtil.loadGlide(bean.top3[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_god), 0, glideUtil.getLastModified(bean.top3[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_god_name).text = bean.top3[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerGod).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_3).apply {
                                visibility = if (bean.top3[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top3[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_god_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_3).visibility = View.GONE
                            helper.getImageView(R.id.iv_god).setImageDrawable(null)
                            helper.getTextView(R.id.tv_god_name).text = ""
                            helper.getView(R.id.viewLayerGod).visibility = View.GONE
                        }
                        if (bean.top4 != null && bean.top4.size > position - 1) {
                            try {
                                glideUtil.loadGlide(bean.top4[position - 1].user_info.avatar_url, helper.getImageView(R.id.ivWordUser), 0, glideUtil.getLastModified(bean.top4[position - 1].user_info.avatar_url))
                                helper.getTextView(R.id.tvWordCount).text = bean.top4[position - 1].dubbing_num + "配"
                                helper.getView(R.id.viewLayerWord).visibility = View.VISIBLE
                                helper.getView(R.id.ivWordType).apply {
                                    visibility = if (bean.top4[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                    isSelected = bean.top4[position - 1].user_info.identity_type == 1
                                }
                                (helper.getTextView(R.id.tvWordCount) as ColorArraysTextView).setCurrentLevel(position)
                            } catch (e: java.lang.Exception) {
                            }
                        } else {
                            helper.getView(R.id.ivWordType).visibility = View.GONE
                            helper.getImageView(R.id.ivWordUser).setImageDrawable(null)
                            helper.getTextView(R.id.tvWordCount).text = ""
                            helper.getView(R.id.viewLayerWord).visibility = View.GONE
                        }
                        helper.getView(R.id.linear_angel).setOnClickListener {
                            if (bean.top1 != null && bean.top1.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "1")
                                        .putExtra("type", 4)
                                        .putExtra("id", bean.top1[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_monster).setOnClickListener {
                            if (bean.top2 != null && bean.top2.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "2")
                                        .putExtra("type", 4)
                                        .putExtra("id", bean.top2[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_god).setOnClickListener {
                            if (bean.top3 != null && bean.top3.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "3")
                                        .putExtra("type", 4)
                                        .putExtra("id", bean.top3[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linearWord).setOnClickListener {
                            try {
                                startActivity<TopWordActivity>("type" to 1, "uid" to bean.top4[position - 1].user_info.id)
                            } catch (e: Exception) {
                            }
                        }
                    }
                } else {
                    helper.getTextView(R.id.tv_board_title).text = "本周"
                }
            }
        }
        recyclerViewWeek.adapter = weekAdapter
        monthAdapter = object : BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>(this, R.layout.item_text_title, R.layout.layout_alarm_leader_board, monthData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: DataTitleBean?) {
                val position = helper!!.itemView.tag as Int
                if (!item!!.isSelect) {
                    helper.getImageView(R.id.iv_flag).setImageResource(parseMattch(position))
                    monthBoardData?.let { bean ->
                        if (bean.top1 != null && bean.top1.size > position - 1) {
                            glideUtil.loadGlide(bean.top1[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_angel), 0, glideUtil.getLastModified(bean.top1[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_angel_name).text = bean.top1[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerAngel).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_1).apply {
                                visibility = if (bean.top1[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top1[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_angel_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_1).visibility = View.GONE
                            helper.getImageView(R.id.iv_angel).setImageDrawable(null)
                            helper.getTextView(R.id.tv_angel_name).text = ""
                            helper.getView(R.id.viewLayerAngel).visibility = View.GONE
                        }
                        if (bean.top2 != null && bean.top2.size > position - 1) {
                            glideUtil.loadGlide(bean.top2[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_monster), 0, glideUtil.getLastModified(bean.top2[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_monster_name).text = bean.top2[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerMonster).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_2).apply {
                                visibility = if (bean.top2[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top2[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_monster_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getImageView(R.id.iv_monster).setImageDrawable(null)
                            helper.getTextView(R.id.tv_monster_name).text = ""
                            helper.getView(R.id.viewLayerMonster).visibility = View.GONE
                            helper.getView(R.id.iv_user_type_2).visibility = View.GONE
                        }
                        if (bean.top3 != null && bean.top3.size > position - 1) {
                            glideUtil.loadGlide(bean.top3[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_god), 0, glideUtil.getLastModified(bean.top3[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_god_name).text = bean.top3[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerGod).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_3).apply {
                                visibility = if (bean.top3[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top3[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_god_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_3).visibility = View.GONE
                            helper.getImageView(R.id.iv_god).setImageDrawable(null)
                            helper.getTextView(R.id.tv_god_name).text = ""
                            helper.getView(R.id.viewLayerGod).visibility = View.GONE
                        }
                        if (bean.top4 != null && bean.top4.size > position - 1) {
                            try {
                                glideUtil.loadGlide(bean.top4[position - 1].user_info.avatar_url, helper.getImageView(R.id.ivWordUser), 0, glideUtil.getLastModified(bean.top4[position - 1].user_info.avatar_url))
                                helper.getTextView(R.id.tvWordCount).text = bean.top4[position - 1].dubbing_num + "配"
                                helper.getView(R.id.viewLayerWord).visibility = View.VISIBLE
                                helper.getView(R.id.ivWordType).apply {
                                    visibility = if (bean.top4[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                    isSelected = bean.top4[position - 1].user_info.identity_type == 1
                                }
                                (helper.getTextView(R.id.tvWordCount) as ColorArraysTextView).setCurrentLevel(position)
                            } catch (e: Exception) {
                            }
                        } else {
                            helper.getView(R.id.ivWordType).visibility = View.GONE
                            helper.getImageView(R.id.ivWordUser).setImageDrawable(null)
                            helper.getTextView(R.id.tvWordCount).text = ""
                            helper.getView(R.id.viewLayerWord).visibility = View.GONE
                        }
                        helper.getView(R.id.linear_angel).setOnClickListener {
                            if (bean.top1 != null && bean.top1.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "1")
                                        .putExtra("type", 2)
                                        .putExtra("id", bean.top1[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_monster).setOnClickListener {
                            if (bean.top2 != null && bean.top2.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "2")
                                        .putExtra("type", 2)
                                        .putExtra("id", bean.top2[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_god).setOnClickListener {
                            if (bean.top3 != null && bean.top3.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "3")
                                        .putExtra("type", 2)
                                        .putExtra("id", bean.top3[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linearWord).setOnClickListener {
                            try {
                                startActivity<TopWordActivity>("type" to 2, "uid" to bean.top4[position - 1].user_info.id)
                            } catch (e: Exception) {
                            }
                        }
                    }
                } else {
                    helper.getTextView(R.id.tv_board_title).text = "本月"
                }
            }
        }
        recyclerViewMonth.adapter = monthAdapter
        adapter = object : BaseDecorationAdapter<DataTitleBean, BaseAdapterHelper>(this, R.layout.item_text_title, R.layout.layout_alarm_leader_board, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: DataTitleBean?) {
                val position = helper!!.itemView.tag as Int
                if (!item!!.isSelect) {
                    helper.getImageView(R.id.iv_flag).setImageResource(parseMattch(position))
                    allBoardData?.let { bean ->
                        if (bean.top1 != null && bean.top1.size > position - 1) {
                            glideUtil.loadGlide(bean.top1[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_angel), 0, glideUtil.getLastModified(bean.top1[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_angel_name).text = bean.top1[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerAngel).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_1).apply {
                                visibility = if (bean.top1[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top1[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_angel_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_1).visibility = View.GONE
                            helper.getImageView(R.id.iv_angel).setImageDrawable(null)
                            helper.getTextView(R.id.tv_angel_name).text = ""
                            helper.getView(R.id.viewLayerAngel).visibility = View.GONE
                        }
                        if (bean.top2 != null && bean.top2.size > position - 1) {
                            glideUtil.loadGlide(bean.top2[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_monster), 0, glideUtil.getLastModified(bean.top2[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_monster_name).text = bean.top2[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerMonster).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_2).apply {
                                visibility = if (bean.top2[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top2[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_monster_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_2).visibility = View.GONE
                            helper.getImageView(R.id.iv_monster).setImageDrawable(null)
                            helper.getTextView(R.id.tv_monster_name).text = ""
                            helper.getView(R.id.viewLayerMonster).visibility = View.GONE
                        }
                        if (bean.top3 != null && bean.top3.size > position - 1) {
                            glideUtil.loadGlide(bean.top3[position - 1].user_info.avatar_url, helper.getImageView(R.id.iv_god), 0, glideUtil.getLastModified(bean.top3[position - 1].user_info.avatar_url))
                            helper.getTextView(R.id.tv_god_name).text = bean.top3[position - 1].vote_num + "票"
                            helper.getView(R.id.viewLayerGod).visibility = View.VISIBLE
                            helper.getView(R.id.iv_user_type_3).apply {
                                visibility = if (bean.top3[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                isSelected = bean.top3[position - 1].user_info.identity_type == 1
                            }
                            (helper.getTextView(R.id.tv_god_name) as ColorArraysTextView).setCurrentLevel(position)
                        } else {
                            helper.getView(R.id.iv_user_type_3).visibility = View.GONE
                            helper.getImageView(R.id.iv_god).setImageDrawable(null)
                            helper.getTextView(R.id.tv_god_name).text = ""
                            helper.getView(R.id.viewLayerGod).visibility = View.GONE
                        }
                        if (bean.top4 != null && bean.top4.size > position - 1) {
                            try {
                                glideUtil.loadGlide(bean.top4[position - 1].user_info.avatar_url, helper.getImageView(R.id.ivWordUser), 0, glideUtil.getLastModified(bean.top4[position - 1].user_info.avatar_url))
                                helper.getTextView(R.id.tvWordCount).text = bean.top4[position - 1].dubbing_num + "配"
                                helper.getView(R.id.viewLayerWord).visibility = View.VISIBLE
                                helper.getView(R.id.ivWordType).apply {
                                    visibility = if (bean.top4[position - 1].user_info.identity_type == 0) View.GONE else View.VISIBLE
                                    isSelected = bean.top4[position - 1].user_info.identity_type == 1
                                }
                                (helper.getTextView(R.id.tvWordCount) as ColorArraysTextView).setCurrentLevel(position)
                            } catch (e: Exception) {
                            }
                        } else {
                            helper.getView(R.id.ivWordType).visibility = View.GONE
                            helper.getImageView(R.id.ivWordUser).setImageDrawable(null)
                            helper.getTextView(R.id.tvWordCount).text = ""
                            helper.getView(R.id.viewLayerWord).visibility = View.GONE
                        }
                        helper.getView(R.id.linear_angel).setOnClickListener {
                            if (bean.top1 != null && bean.top1.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "1")
                                        .putExtra("type", 1)
                                        .putExtra("id", bean.top1[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_monster).setOnClickListener {
                            if (bean.top2 != null && bean.top2.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "2")
                                        .putExtra("type", 1)
                                        .putExtra("id", bean.top2[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linear_god).setOnClickListener {
                            if (bean.top3 != null && bean.top3.size > position - 1) {
                                startActivity(Intent(this@AlarmBangActivity, TopTenAlarmActivity::class.java)
                                        .putExtra("voteOption", "3")
                                        .putExtra("type", 1)
                                        .putExtra("id", bean.top3[position - 1].user_info.id))
                            }
                        }
                        helper.getView(R.id.linearWord).setOnClickListener {
                            try {
                                startActivity<TopWordActivity>("type" to 3, "uid" to bean.top4[position - 1].user_info.id)
                            } catch (e: Exception) {
                            }
                        }
                    }
                } else {
                    helper.getTextView(R.id.tv_board_title).text = resources.getString(R.string.string_alarm_11)
                }
            }
        }
        recyclerView.adapter = adapter
        request(0)
        request(5)
    }

    override fun initEvent() {
        swipeRefresh.setOnRefreshListener {
            allBoardData = null
            monthBoardData = null
            dayBoardData = null
            request(0)
        }
        relativeBestDubbing.setOnClickListener {
            startActivity<TodayBestWorkActivity>("type" to 1)
        }
        relativeBestWord.setOnClickListener {
            startActivity<TodayBestWorkActivity>()
        }
        btn_Back.setOnClickListener { finish() }
    }

    private fun parseMattch(position: Int): Int {
        return when (position.rem(6)) {
            1 -> if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.mipmap.icon_alarm_board_1 else R.mipmap.icon_alarm_board_1_night
            2 -> if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.mipmap.icon_alarm_board_2 else R.mipmap.icon_alarm_board_2_night
            3 -> if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.mipmap.icon_alarm_board_3 else R.mipmap.icon_alarm_board_3_night
            4 -> if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.mipmap.icon_alarm_board_4 else R.mipmap.icon_alarm_board_4_night
            5 -> if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.mipmap.icon_alarm_board_5 else R.mipmap.icon_alarm_board_5_night
            else -> R.mipmap.icon_alarm_board_1
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "leaderboard/dubbings", LoaderBoardData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        swipeRefresh.isRefreshing = false
                        mData.clear()
                        adapter.notifyDataSetChanged()
                        result as LoaderBoardData
                        if (result.code == 0) {
                            if (result.data != null) {
                                allBoardData = result.data
                                repeat((0 until if (result.data.top1 != null) {
                                    result.data.top1.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top2 != null) {
                                    result.data.top2.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top3 != null) {
                                    result.data.top3.size
                                } else {
                                    0
                                }))).count()) {
                                    mData.add(DataTitleBean())
                                }
                                if (mData.size > 0) {
                                    mData.add(0, DataTitleBean().apply {
                                        isSelect = true
                                    })
                                }
                            }
                            adapter.notifyDataSetChanged()
                        }
                        request(4)
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
                OkClientHelper.get(this, "leaderboard/dubbings?type=month", LoaderBoardData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as LoaderBoardData
                        swipeRefresh.isRefreshing = false
                        monthData.clear()
                        monthAdapter.notifyDataSetChanged()
                        if (result.code == 0) {
                            if (result.data != null) {
                                monthBoardData = result.data
                                (0 until if (result.data.top1 != null) {
                                    result.data.top1.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top2 != null) {
                                    result.data.top2.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top3 != null) {
                                    result.data.top3.size
                                } else {
                                    0
                                }))).forEach { _ ->
                                    monthData.add(DataTitleBean())
                                }
                                if (monthData.size > 0) {
                                    monthData.add(0, DataTitleBean().apply {
                                        isSelect = true
                                    })
                                }
                            }
                            monthAdapter.notifyDataSetChanged()
                        }
                        request(3)
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
                OkClientHelper.get(this, "leaderboard/dubbings?type=week", LoaderBoardData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as LoaderBoardData
                        swipeRefresh.isRefreshing = false
                        weekData.clear()
                        weekAdapter.notifyDataSetChanged()
                        if (result.code == 0) {
                            if (result.data != null) {
                                weekBoardData = result.data
                                (0 until if (result.data.top1 != null) {
                                    result.data.top1.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top2 != null) {
                                    result.data.top2.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top3 != null) {
                                    result.data.top3.size
                                } else {
                                    0
                                }))).forEach { _ ->
                                    weekData.add(DataTitleBean())
                                }
                                if (weekData.size > 0) {
                                    weekData.add(0, DataTitleBean().apply {
                                        isSelect = true
                                    })
                                }
                            }
                            weekAdapter.notifyDataSetChanged()
                        }
                        request(2)
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
                OkClientHelper.get(this, "leaderboard/dubbings?type=day", LoaderBoardData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as LoaderBoardData
                        swipeRefresh.isRefreshing = false
                        dayData.clear()
                        dayAdapter.notifyDataSetChanged()
                        if (result.code == 0) {
                            if (result.data != null) {
                                dayBoardData = result.data
                                (0 until if (result.data.top1 != null) {
                                    result.data.top1.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top2 != null) {
                                    result.data.top2.size
                                } else {
                                    0
                                }.coerceAtLeast(if (result.data.top3 != null) {
                                    result.data.top3.size
                                } else {
                                    0
                                }))).forEach { _ ->
                                    dayData.add(DataTitleBean())
                                }
                                if (dayData.size > 0) {
                                    dayData.add(0, DataTitleBean().apply {
                                        isSelect = true
                                    })
                                }
                            }
                            dayAdapter.notifyDataSetChanged()
                        }
                        request(1)
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
            }
            1 -> {//日台词榜单
                OkClientHelper.get(this, "leaderboard/lines?type=day", AlarmBangDubbingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AlarmBangDubbingData
                        result.data?.let {
                            if (dayData.size == 0) {
                                dayData.addAll(Array(it.size - dayData.size) {//填充数据
                                    DataTitleBean()
                                })
                                dayData.add(0, DataTitleBean().apply {
                                    isSelect = true
                                })
                            } else {
                                if (dayData.size < it.size) {
                                    dayData.addAll(Array(it.size - dayData.size + 1) {//填充数据
                                        DataTitleBean()
                                    })
                                } else if (dayData.size == it.size) {
                                    dayData.add(DataTitleBean())
                                }
                            }
                            if (dayBoardData == null) {
                                dayBoardData = LoaderBoardData.LoaderBoardBean()
                            }
                            dayBoardData?.top4 = it
                            dayAdapter.notifyDataSetChanged()
                            //展示今日台词最佳
                        }
                        if (dayData.size == 0) linearDays.visibility = View.GONE else linearDays.visibility = View.VISIBLE
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            2 -> {
                OkClientHelper.get(this, "leaderboard/lines?type=week", AlarmBangDubbingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AlarmBangDubbingData
                        result.data?.let {
                            if (weekData.size == 0) {
                                weekData.addAll(Array(it.size - weekData.size) {//填充数据
                                    DataTitleBean()
                                })
                                weekData.add(0, DataTitleBean().apply {
                                    isSelect = true
                                })
                            } else {
                                if (weekData.size < it.size) {
                                    weekData.addAll(Array(it.size - weekData.size + 1) {//填充数据
                                        DataTitleBean()
                                    })
                                } else if (weekData.size == it.size) {
                                    weekData.add(DataTitleBean())
                                }
                            }
                            /* if (weekData.size < it.size) {
                                 weekData.addAll(Array(it.size - weekData.size) {//填充数据
                                     DataTitleBean()
                                 })
                             }
                             if (weekData.size == 1) {
                                 weekData.add(0, DataTitleBean().apply {
                                     isSelect = true
                                 })
                             }*/
                            if (weekBoardData == null) {
                                weekBoardData = LoaderBoardData.LoaderBoardBean()
                            }
                            weekBoardData?.top4 = it
                            weekAdapter.notifyDataSetChanged()
                        }
                        if (weekData.size == 0) linearWeeks.visibility = View.GONE else linearWeeks.visibility = View.VISIBLE
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            3 -> {
                OkClientHelper.get(this, "leaderboard/lines?type=month", AlarmBangDubbingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AlarmBangDubbingData
                        result.data?.let {
                            if (monthData.size == 0) {
                                monthData.addAll(Array(it.size - monthData.size) {//填充数据
                                    DataTitleBean()
                                })
                                monthData.add(0, DataTitleBean().apply {
                                    isSelect = true
                                })
                            } else {
                                if (monthData.size < it.size) {
                                    monthData.addAll(Array(it.size - monthData.size + 1) {//填充数据
                                        DataTitleBean()
                                    })
                                } else if (monthData.size == it.size) {
                                    monthData.add(DataTitleBean())
                                }
                            }
                            /*if (monthData.size < it.size) {
                                monthData.addAll(Array(it.size - monthData.size) {//填充数据
                                    DataTitleBean()
                                })
                            }
                            if (monthData.size == 1) {
                                monthData.add(0, DataTitleBean().apply {
                                    isSelect = true
                                })
                            }*/
                            if (monthBoardData == null) {
                                monthBoardData = LoaderBoardData.LoaderBoardBean()
                            }
                            monthBoardData?.top4 = it
                            monthAdapter.notifyDataSetChanged()
                        }
                        if (monthData.size == 0) linearMonths.visibility = View.GONE else linearMonths.visibility = View.VISIBLE
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            4 -> {
                OkClientHelper.get(this, "leaderboard/lines", AlarmBangDubbingData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AlarmBangDubbingData
                        result.data?.let {
                            if (mData.size == 0) {
                                mData.addAll(Array(it.size - mData.size) {//填充数据
                                    DataTitleBean()
                                })
                                mData.add(0, DataTitleBean().apply {
                                    isSelect = true
                                })
                            } else {
                                if (mData.size < it.size) {
                                    mData.addAll(Array(it.size - mData.size + 1) {//填充数据
                                        DataTitleBean()
                                    })
                                } else if (mData.size == it.size) {
                                    mData.add(DataTitleBean())
                                }
                            }
                            /* if (mData.size < it.size && mData.size < 6) {
                                 //最多允许6条记录
                                 mData.addAll(Array(if (it.size > 6) 6 else {
                                     it.size
                                 } - mData.size) {//填充数据
                                     DataTitleBean()
                                 })
                             }
                             if (mData.size == 1) {
                                 mData.add(0, DataTitleBean().apply {
                                     isSelect = true
                                 })
                             }*/
                            if (allBoardData == null) {
                                allBoardData = LoaderBoardData.LoaderBoardBean()
                            }
                            allBoardData?.top4 = it
                            adapter.notifyDataSetChanged()
                        }
                        if (mData.size == 0) linearAll.visibility = View.GONE else linearAll.visibility = View.VISIBLE
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V4.3")
            }
            5 -> {
                val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                getBangUserDubbing(obj.user_id, "month") { result ->
                    result.data?.let {
                        viewMonths.setAngel(it.num1)
                        viewMonths.setMonster(it.num2)
                        viewMonths.setGod(it.num3)
                    }
                }
                getBangUserDubbing(obj.user_id, "week") { result ->
                    result.data?.let {
                        viewWeeks.setAngel(it.num1)
                        viewWeeks.setMonster(it.num2)
                        viewWeeks.setGod(it.num3)
                    }
                }
                getBangUserDubbing(obj.user_id, "day") { result ->
                    result.data?.let {
                        viewDays.setAngel(it.num1)
                        viewDays.setMonster(it.num2)
                        viewDays.setGod(it.num3)
                    }
                }
                getBangUserDubbing(obj.user_id, "") { result ->
                    result.data?.let {
                        viewAll.setAngel(it.num1)
                        viewAll.setMonster(it.num2)
                        viewAll.setGod(it.num3)
                    }
                }
                getBangWord(obj.user_id, "") { result ->
                    result.data?.let {
                        viewAll.setDubbing(it.dubbing_num)
                    }
                }
                getBangWord(obj.user_id, "day") { result ->
                    result.data?.let {
                        viewDays.setDubbing(it.dubbing_num)
                    }
                }
                getBangWord(obj.user_id, "week") { result ->
                    result.data?.let {
                        viewWeeks.setDubbing(it.dubbing_num)
                    }
                }
                getBangWord(obj.user_id, "month") { result ->
                    result.data?.let {
                        viewMonths.setDubbing(it.dubbing_num)
                    }
                }
                //获取今日最佳台词
                executeRequest("leaderboard/lines/optimal?type=day") {
                    try {
                        if (it != null) {
                            it as WordingData
                            it.data?.let { bean ->
                                glideUtil.loadGlide(bean[0].user.avatar_url, ivWordBest, 0, glideUtil.getLastModified(bean[0].user.avatar_url))
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
                //后去今日最佳台词
                executeRequest("leaderboard/dubbings/optimal?type=day") {
                    try {
                        if (it != null) {
                            it as WordingData
                            it.data?.let { data ->
                                glideUtil.loadGlide(data[0].from_user_info.avatar_url, viDubbingBest, 0, glideUtil.getLastModified(data[0].from_user_info.avatar_url))
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}