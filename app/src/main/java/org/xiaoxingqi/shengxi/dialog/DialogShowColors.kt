package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.dialog_show_colors.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.wedgit.canvas.CircleColorSelectorView
import org.xiaoxingqi.shengxi.wedgit.canvas.HorizontalColorsView
import org.xiaoxingqi.shengxi.wedgit.ProgressSeekBar
import skin.support.SkinCompatManager

class DialogShowColors(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var color = 0
    override fun getLayoutId(): Int {
        return R.layout.dialog_show_colors
    }

    override fun initView() {
        val selectorView = findViewById<CircleColorSelectorView>(R.id.view_show_colors)
        findViewById<View>(R.id.iv_commit).setOnClickListener {
            dismiss()
        }
//        val horizontalColorsView = findViewById<HorizontalColorsView>(R.id.colorsView)
        val seekBar = findViewById<ProgressSeekBar>(R.id.seekBar)
        if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            seekBar.thumb = context.resources.getDrawable(R.drawable.shape_seekbar_thumb_night)
            seekBar.progressDrawable = context.resources.getDrawable(R.drawable.shape_colors_progress_night)
        }
//        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
////                colorPicker.setColorAlpha(progress)
////                color = horizontalColorsView.getColor(progress)
//                color = colorPicker.color
//                selectorView.setColor(color)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//        })
        colorPicker.setOnColorChangedListener {
            this.color = it
            selectorView.setColor(it)
        }
        colorPicker.setColor(Color.RED, true)
        selectorView.setColor(color)
//        color = horizontalColorsView.getColor(255)
//        horizontalColorsView.setOnColorResult(object : HorizontalColorsView.OnColorResultListener {
//            override fun colorResult(color: Int) {
//                this@DialogShowColors.color = color
//                selectorView.setColor(color)
//            }
//        })
        window.setBackgroundDrawable(ColorDrawable(0))
        initSystem()
    }

    private var onResultColorListener: OnResultColorListener? = null

    interface OnResultColorListener {
        fun resultColor(color: Int,alpha:Int)
    }

    /**
     * 设置结果
     */
    fun setOnResultListener(onResultColorListener: OnResultColorListener): DialogShowColors {
        this.onResultColorListener = onResultColorListener
        return this
    }

    override fun dismiss() {
        onResultColorListener?.resultColor(color,seekBar.progress)
        super.dismiss()
    }
}