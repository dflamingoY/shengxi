package org.xiaoxingqi.shengxi.wedgit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.layout_cheers_view.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.UserCheersData
import org.xiaoxingqi.shengxi.modules.listen.cheers.REQUEST_CHEERS_VOICE
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity

class CheersLayoutView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {
    override fun getLayoutId(): Int {
        return R.layout.layout_cheers_view
    }

    init {
        linearRecord.setOnClickListener {
            (context as Activity).startActivityForResult(Intent(context, RecordVoiceActivity::class.java).putExtra("recordType", 7)
                    .putExtra("resourceType", "24")
                    .putExtra("isSend", true), REQUEST_CHEERS_VOICE)
            context.overridePendingTransition(0, 0)
        }
        ivClose.setOnClickListener {
            closeAnim()
        }
        /*ivConfirm.setOnClickListener {
            //  do not anything
            closeAnim()
        }*/
    }

    private fun closeAnim() {
        val anim = ObjectAnimator.ofFloat(this@CheersLayoutView, "translationY", 600f).setDuration(220)
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                this@CheersLayoutView.visibility = View.GONE
                this@CheersLayoutView.alpha = 1f
                this@CheersLayoutView.y = 0f
            }
        })
        anim.start()
        ObjectAnimator.ofFloat(this@CheersLayoutView, "alpha", 1f, 0f).setDuration(220).start()
    }

    fun setData(data: UserCheersData.UserCheersBean?) {
        if (data == null) {
            voiceProgress.visibility = View.GONE
            linearOperatorVoice.visibility = View.GONE
            linearEmptyVoice.visibility = View.VISIBLE
            ivClose.visibility = View.VISIBLE
        } else {
            voiceProgress.visibility = View.VISIBLE
            linearOperatorVoice.visibility = View.VISIBLE
            linearEmptyVoice.visibility = View.GONE
            ivClose.visibility = View.GONE
            voiceProgress.data = data
        }
    }
}