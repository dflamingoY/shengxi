package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.Log
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class ColorArraysTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), SkinCompatSupportable {
    private var colorResource1: Int = 0
    private var colorResource2: Int = 0
    private var colorResource3: Int = 0
    private var colorResource4: Int = 0
    private var colorResource5: Int = 0
    private var currentLevel = 1

    private var color1: Int = 0
    private var color2: Int = 0
    private var color3: Int = 0
    private var color4: Int = 0
    private var color5: Int = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ColorArraysTextView)
        if (array.hasValue(R.styleable.ColorArraysTextView_colorArrays_1)) {
            colorResource1 = array.getResourceId(R.styleable.ColorArraysTextView_colorArrays_1, 0)
        }
        if (array.hasValue(R.styleable.ColorArraysTextView_colorArrays_2)) {
            colorResource2 = array.getResourceId(R.styleable.ColorArraysTextView_colorArrays_2, 0)
        }
        if (array.hasValue(R.styleable.ColorArraysTextView_colorArrays_3)) {
            colorResource3 = array.getResourceId(R.styleable.ColorArraysTextView_colorArrays_3, 0)
        }
        if (array.hasValue(R.styleable.ColorArraysTextView_colorArrays_4)) {
            colorResource4 = array.getResourceId(R.styleable.ColorArraysTextView_colorArrays_4, 0)
        }
        if (array.hasValue(R.styleable.ColorArraysTextView_colorArrays_5)) {
            colorResource5 = array.getResourceId(R.styleable.ColorArraysTextView_colorArrays_5, 0)
        }
        array.recycle()
        SkinCompatResources.init(getContext())
        applySkin()
    }

    fun setCurrentLevel(level: Int) {
        currentLevel = level
        setTextColor(when (level) {
            1 -> color1
            2 -> color2
            3 -> color3
            4 -> color4
            5 -> color5
            else -> color1
        })
    }

    override fun applySkin() {
        try {
            if (colorResource1 != 0)
                color1 = SkinCompatResources.getInstance().getColorStateList(colorResource1).defaultColor
            if (colorResource2 != 0)
                color2 = SkinCompatResources.getInstance().getColorStateList(colorResource2).defaultColor
            if (colorResource3 != 0)
                color3 = SkinCompatResources.getInstance().getColorStateList(colorResource3).defaultColor
            if (colorResource4 != 0)
                color4 = SkinCompatResources.getInstance().getColorStateList(colorResource4).defaultColor
            if (colorResource5 != 0)
                color5 = SkinCompatResources.getInstance().getColorStateList(colorResource5).defaultColor
        } catch (e: Exception) {

        }
    }
}