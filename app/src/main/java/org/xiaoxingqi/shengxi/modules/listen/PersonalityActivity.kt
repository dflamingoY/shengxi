package org.xiaoxingqi.shengxi.modules.listen

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import com.alibaba.fastjson.JSON
import kotlinx.android.synthetic.main.activity_personality.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.model.TestBean
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelTest
import org.xiaoxingqi.shengxi.model.PersonnalityData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils


/**
 * 性格测试题
 */
class PersonalityActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<TestBean.AnswerBean>
    private val allData = ArrayList<TestBean.QuestionBean>()
    private var current = 0
    private var isShack = false
    private val mData by lazy {
        ArrayList<TestBean.AnswerBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_personality
    }

    override fun initView() {
        tv_Next.isEnabled = false
        tv_Next.alpha = 0.6f
    }

    override fun initData() {
        isShack = intent.getBooleanExtra("isShack", false)
        adapter = object : QuickAdapter<TestBean.AnswerBean>(this, R.layout.item_test_personality, mData) {
            /**
             * 添加item载入动画。
             */
            private var lastPosition = -1
            private var map: HashMap<Int, Animation> = HashMap()

            private fun setAnimation(viewToAnimate: View, position: Int) {
                val animation: Animation
                if (position > lastPosition) {
                    if (map[position] != null) {
                        animation = map[position]!!
                    } else {
                        animation = AnimationUtils.loadAnimation(viewToAnimate.context, R
                                .anim.item_animation)
                        map[position] = animation
                    }
                    animation.startOffset = position * 30L
                    viewToAnimate.startAnimation(animation)
                    lastPosition = position
                }
            }

            override fun onViewDetachedFromWindow(holder: BaseAdapterHelper) {
                super.onViewDetachedFromWindow(holder)
                try {
                    val view = holder.itemView
                    view?.clearAnimation()
                } catch (e: Exception) {

                }
            }

            override fun convert(helper: BaseAdapterHelper?, item: TestBean.AnswerBean?) {
                helper!!.getTextView(R.id.tv_Title).text = item!!.desc
                helper.getView(R.id.relativeTop).isSelected = item.isSelect
//                setAnimation(helper.itemView, helper.itemView.tag as Int)
                helper.getView(R.id.tv_Title).setOnClickListener {
                    clearSelect()
                    item.isSelect = !item.isSelect
                    tv_Next.isEnabled = true
                    tv_Next.alpha = 1f
                    notifyDataSetChanged()
                }
            }

            override fun changeStatue(isSelect: Boolean) {
                super.changeStatue(isSelect)
                lastPosition = -1
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        loadData()
    }

    private fun clearSelect() {
        for (bean in mData) {
            bean.isSelect = false
        }
    }

    override fun initEvent() {
        tv_Next.setOnClickListener {
            if (current == allData.size - 1) {
                /**
                 * 输出结果
                 */
                val arrays = JSONArray()
                for (index in allData.indices) {
                    val objec = JSONObject()
                    objec["id"] = allData[index].id
                    if (index == 0 || index == 3 || index == 11 || index == 22 || index == 8) {
                        for (i in allData[index].answer.indices) {
                            if (allData[index].answer[i].isSelect) {
                                objec["result"] = "${i + 1}"
                                break
                            }
                        }
                    } else {
                        for (bean in allData[index].answer) {
                            if (bean.isSelect) {
                                objec["result"] = bean.value
                                break
                            }
                        }
                    }
                    arrays.add(objec)
                }
                transLayout.showProgress()
                loadProfile(arrays.toJSONString())
            } else {
                current++
                adapter.changeStatue(true)
                mData.clear()
                adapter.notifyDataSetChanged()
//                ObjectAnimator.ofFloat(tv_TopicName, "translationY", AppTools.dp2px(this, 50).toFloat(), 0F).setDuration(320).start()
                tv_TopicName.text = allData[current].title
                for (item in allData[current].answer) {
                    mData.add(item)
                    adapter.notifyItemInserted(adapter.itemCount - 1)
                }
                tv_Index.text = "${current + 1}/${allData.size}"
                if (current == allData.size - 1) {
                    tv_Next.text = "完成"
                }
                tv_Next.isEnabled = false
                tv_Next.alpha = 0.6f
            }
        }
        btn_Back.setOnClickListener {
            DialogCancelTest(this).setOnClickListener(View.OnClickListener {
                finish()
            }).show()
        }
    }

    private fun loadData() {
        val data = JSON.parseObject(resources.getString(R.string.string_word), TestBean::class.java)
        allData.addAll(data.data)
        tv_TopicName.text = allData[0].title
        for (item in allData[0].answer) {
            mData.add(item)
            adapter.notifyItemInserted(adapter.itemCount - 1)
        }
    }


    private fun loadProfile(result: String) {
        OkClientHelper.post(this, "personalityTest/result", FormBody.Builder().add("personalityResult", result).build(), PersonnalityData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as PersonnalityData
                if (result.code == 0) {
                    val loginBean = PreferenceTools.getObj(this@PersonalityActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    SPUtils.setString(this@PersonalityActivity, IConstant.USERCHARACTERTYPE + loginBean.user_id, result.data.interface_type)
                    SPUtils.setString(this@PersonalityActivity, "${IConstant.PERSONALITYRESULT}_${loginBean.user_id}", result.data.personality_id)
                    startActivity(Intent(this@PersonalityActivity, PersonalityResultActivity::class.java)
                            .putExtra("personality", result.data.personality_id)
                            .putExtra("isNewIntent", true)
                            .putExtra("isShack", isShack)
                    )
                    finish()
                } else {
                    transLayout.showContent()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.0")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {

            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogCancelTest(this).setOnClickListener(View.OnClickListener {
                finish()
            }).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun canBeSlideBack(): Boolean {
        return false
    }

    override fun supportSlideBack(): Boolean {
        return false
    }
}