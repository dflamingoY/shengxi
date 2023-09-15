package org.xiaoxingqi.shengxi.wedgit

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable
import java.lang.Deprecated

@Deprecated
class LinearStatusText : LinearLayout, SkinCompatSupportable {

    private var defaultColor = 0
    private var selectedColor = 0
    private var text: TextView? = null
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper? = null
    private var normalTextColorId = 0
    private var selectedTextColorId = 0
    private var imgId = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        val array = context.obtainStyledAttributes(attributeSet, R.styleable.LinearStatusText)
        defaultColor = array.getColor(R.styleable.LinearStatusText_textNormalColor, defaultColor)
        selectedColor = array.getColor(R.styleable.LinearStatusText_textSelectedColor, selectedColor)
        array.recycle()
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LinearStatusText)
        if (typedArray.hasValue(R.styleable.LinearStatusText_textNormalColor)) {
            normalTextColorId = typedArray.getResourceId(R.styleable.LinearStatusText_textNormalColor, 0)
        }
        if (typedArray.hasValue(R.styleable.LinearStatusText_textSelectedColor)) {
            selectedTextColorId = typedArray.getResourceId(R.styleable.LinearStatusText_textSelectedColor, 0)
        }
        if (typedArray.hasValue(R.styleable.LinearStatusText_textImgId)) {
            imgId = typedArray.getResourceId(R.styleable.LinearStatusText_textImgId, 0)
        }
        typedArray.recycle()
        applySkin()
        SkinCompatResources.init(context)
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attributeSet, defStyleAttr)
        init()
    }

    fun init() {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        text = TextView(context)
        text?.textSize = 9F
        text?.includeFontPadding = false
        text?.text = resources.getString(R.string.string_friend)
        text?.setTextColor(selectedColor)
        addView(text)
    }

    @SuppressLint("SetTextI18n")
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            text?.text = "+" + resources.getString(R.string.string_friend)
            text?.setTextColor(selectedColor)
        } else {
            text?.text = resources.getString(R.string.string_pending)
            text?.setTextColor(defaultColor)
        }
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        mBackgroundTintHelper?.onSetBackgroundResource(resId)
    }

    override fun applySkin() {
        mBackgroundTintHelper?.applySkin()
        try {
            val color = SkinCompatResources.getInstance().getColorStateList(normalTextColorId)
            defaultColor = color.defaultColor
            val color1 = SkinCompatResources.getInstance().getColorStateList(selectedTextColorId)
            selectedColor = color1.defaultColor
            isSelected = isSelected
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setVisibility(visibility: Int) {
//        super.setVisibility(visibility)
    }
}