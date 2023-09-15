package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.fastjson.JSONArray
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.EchoTypesData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.wedgit.SwitchButton

/**
 * tips 默认数值是 1 2 3 4
 *hobbies 默认数值:   1 2
可能存在 没获取到数据
 */
class DialogEchoSet(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_echo_set
    }

    private var toggleStatus = false
    private var map: Map<String, List<EchoTypesData.EchoTypesBean>>? = null
    override fun initView() {
        var toggleButton = findViewById<SwitchButton>(R.id.toggle_Button)
        toggleButton.isChecked = toggleStatus
        toggleButton.setOnCheckedChangeListener { view, isChecked ->
            /*
             *是否选中
             */
            toggleStatus = isChecked
        }
        if (map != null) {
            try {
                map!!["hobbies"].let {
                    if (it != null) {
                        for (index in it.indices) {
                            if (index == 0) {
                                findViewById<TextView>(R.id.tv_Short).text = it[index].name
                                findViewById<TextView>(R.id.tv_Short).isSelected = it[index].isSelected

                            } else if (index == 1) {
                                findViewById<TextView>(R.id.tv_Long).text = it[index].name
                                findViewById<TextView>(R.id.tv_Long).isSelected = it[index].isSelected
                            }
                        }
                    }
                }
                map!!["tips"].let {
                    if (it != null) {
                        if (it.size >= findViewById<LinearLayout>(R.id.linear_tips_container).childCount)
                            for (index in it.indices) {
                                (findViewById<LinearLayout>(R.id.linear_tips_container).getChildAt(index) as TextView).text = it[index].name
                                findViewById<LinearLayout>(R.id.linear_tips_container).getChildAt(index).isSelected = it[index].isSelected
                            }
                    }
                }
            } catch (e: Exception) {
            }
        }
        findViewById<View>(R.id.tv_Short).setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                findViewById<View>(R.id.tv_Long).isSelected = !it.isSelected
            }
        }
        findViewById<View>(R.id.tv_Long).setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                findViewById<View>(R.id.tv_Short).isSelected = !it.isSelected
            }
        }
        findViewById<View>(R.id.tv_Wechat).setOnClickListener {
            it.isSelected = !it.isSelected
        }
        findViewById<View>(R.id.tv_in).setOnClickListener {
            it.isSelected = !it.isSelected
        }
        findViewById<View>(R.id.tv_Self).setOnClickListener {
            it.isSelected = !it.isSelected
        }
        findViewById<View>(R.id.tv_Qrcode).setOnClickListener {
            it.isSelected = !it.isSelected
        }
        val p = window!!.attributes
        p.windowAnimations = R.style.dialog_echo_anim
        p.gravity = Gravity.TOP
        p.width = AppTools.getWindowsWidth(context)
        window!!.attributes = p
    }

    fun setIsBusy(isBusy: Boolean, map: Map<String, List<EchoTypesData.EchoTypesBean>>?): DialogEchoSet {
        toggleStatus = isBusy
        this.map = map
        return this
    }

    /**
     *设置用户的私聊时,隐私设置
     */
    fun setChatStyle(map: Map<String, List<EchoTypesData.EchoTypesBean>>): DialogEchoSet {
        this.map = map
        return this
    }

    private fun parseTips(): String {
        val json = JSONArray()
        if (map != null || map?.get("tips") == null) {//有数据
            for (count in 0 until findViewById<LinearLayout>(R.id.linear_tips_container).childCount) {
                if (findViewById<LinearLayout>(R.id.linear_tips_container).getChildAt(count).isSelected) {
                    json.add(map?.get("tips")?.get(count)?.id ?: count + 1)
                }
            }
        } else {
            for (count in 0 until findViewById<LinearLayout>(R.id.linear_tips_container).childCount) {
                if (findViewById<LinearLayout>(R.id.linear_tips_container).getChildAt(count).isSelected) {
                    json.add("${count + 1}")
                }
            }
        }
        return json.toJSONString()
    }

    private fun getHobbies(): Int {
        var hobbies = 0
        if (map != null || map?.get("hobbies") == null) {//有数据
            if (findViewById<View>(R.id.tv_Short).isSelected) {
                hobbies = map?.get("hobbies")?.get(0)?.id ?: 1
            } else if (findViewById<View>(R.id.tv_Long).isSelected) {
                hobbies = map?.get("hobbies")?.get(1)?.id ?: 2
            }
        } else {
            if (findViewById<View>(R.id.tv_Short).isSelected) {
                hobbies = 1
            } else if (findViewById<View>(R.id.tv_Long).isSelected) {
                hobbies = 1
            }
        }
        return hobbies
    }

    override fun dismiss() {
        super.dismiss()
//        EventBus.getDefault().post(EchoSetEvent(toggleStatus, parseTips(), getHobbies()))
    }

}