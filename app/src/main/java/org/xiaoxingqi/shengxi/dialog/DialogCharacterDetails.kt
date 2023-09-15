package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.widget.NestedScrollView
import android.text.TextUtils
import android.view.ViewTreeObserver
import android.widget.SeekBar
import kotlinx.android.synthetic.main.dialog_character.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.model.PersonalityGroupData
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.SkinCompatManager

class DialogCharacterDetails(context: Context) : BaseDialog(context) {
    private var bean: PersonalityGroupData.PersonalityGroupBean? = null
    private var glideUtil: GlideJudeUtils? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_character
    }

    override fun initView() {
        bean?.let {
            if (null != glideUtil) {
                glideUtil!!.loadGlide(it.role_pic_url, iv_Avatar, 0, glideUtil!!.getLastModified(it.role_pic_url))
            }
            tv_Name.text = it.role_name
            tv_Desc.text = "《${it.role_from}》"
            tvContent.text = it.role_intro//用户简介
        }
        relative_dismiss.setOnClickListener {
            dismiss()
        }
        tvContent.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                tvContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val height = linearContainer.height
                seekBar.max = height - AppTools.dp2px(context, 249)
            }
        })
        seekBar.thumb = context.resources.getDrawable(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.shape_test_slide else R.drawable.shape_test_slide_night, null)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    nestedScroll.scrollTo(0, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        nestedScroll.setOnScrollChangeListener { v: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (!seekBar.isPressed)
                seekBar.progress = scrollY
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
        window.attributes.windowAnimations = R.style.AlphaDialogAnim
    }

    fun setGlide(glideUtil: GlideJudeUtils): DialogCharacterDetails {
        this.glideUtil = glideUtil

        return this
    }

    fun setData(bean: PersonalityGroupData.PersonalityGroupBean): DialogCharacterDetails {
        this.bean = bean
        return this
    }
}