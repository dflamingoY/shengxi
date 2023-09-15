package org.xiaoxingqi.shengxi.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.dialog_user_achievement.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.modules.user.frag.TimeMachineActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogUserAchievement(val mcontext: Context) : BaseDialog(mcontext, R.style.DialogAnimated) {
    private var recordStatue = -1
    private var recordDays = 0
    private var travelStatue = -1
    private var travelDays = 0

    override fun getLayoutId(): Int {
        return R.layout.dialog_user_achievement
    }

    override fun initView() {
        when (recordStatue) {
            1 -> {
                ivRecordStatue.visibility = View.VISIBLE
                linearRecordStatus.isSelected = true
                tvCurrentRecord.text = "Day $recordDays"
                tvToadyTarget.text = "Day ${recordDays + 1}"
                tvRecordHint.text = context.resources.getString(R.string.string_after_tmorrow_reached)
                goRecord.isSelected = true
                linearRecordNextStatus.isSelected = true
                tvRecordCurrentStatus.text = context.resources.getString(R.string.string_today_reached)
            }
            0 -> {
                tvCurrentRecord.text = "Day $recordDays"
                tvToadyTarget.text = "Day ${recordDays + 1}"
            }
            else -> {
                tvCurrentRecord.text = "Day 0"
                tvToadyTarget.text = "Day 1"
            }
        }
        when (travelStatue) {
            1 -> {
                ivGoMachine.isSelected = true
                tvTravelCurrentStatus.text = context.resources.getString(R.string.string_today_reached)
                ivTravelStatus.visibility = View.VISIBLE
                tvTravelHint.text = context.resources.getString(R.string.string_after_tmorrow_reached)
                linearTravelCurrent.isSelected = true
                tvToadyReach.text = "Day $travelDays"
                linearTravelNext.isSelected = true
                tvTomorrowReach.text = "Day ${travelDays + 1}"
            }
            0 -> {
                tvToadyReach.text = "Day $travelDays"
                tvTomorrowReach.text = "Day ${travelDays + 1}"
            }
            else -> {
                tvToadyReach.text = "Day 0"
                tvTomorrowReach.text = "Day 1"
            }
        }
        goRecord.setOnClickListener {
            if (!it.isSelected) {
                mcontext.startActivity(Intent(mcontext, SendAct::class.java)
                        .putExtra("isHome", true)
                        .putExtra("type", 1))
                (mcontext as Activity).overridePendingTransition(R.anim.operate_enter, 0)
                dismiss()
            }
        }
        ivGoMachine.setOnClickListener {
            if (!ivGoMachine.isSelected) {
                val loginBean = PreferenceTools.getObj(mcontext, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                mcontext.startActivity(Intent(mcontext, TimeMachineActivity::class.java).putExtra("userId", loginBean.user_id))
                dismiss()
            }
        }
        fillWidth()
    }

    //记录状态
    fun setRecordInfo(statue: Int, recordDays: Int): DialogUserAchievement {
        recordStatue = statue
        this.recordDays = recordDays
        return this
    }

    //穿越状态
    fun setTravelInfo(statue: Int, travelDays: Int): DialogUserAchievement {
        travelStatue = statue
        this.travelDays = travelDays
        return this
    }
}