package org.xiaoxingqi.shengxi.wedgit.flowereffects.effective

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant

/**
 * 屏幕密度有关系 如果是 density <=2 偏移量要更低 否则正常展示
 * 雨滴下落速度不受此因素影响
 */
class SnowView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val arrays = arrayListOf<SnowModel>()
    private var stated = false
    private var speedMin = 0
    private var speedMax = 0
    private var currentModel: String? = ""

    companion object {
        private val map by lazy { HashMap<Int, Bitmap>() }
    }

    init {
        //添加数据
        val bean = SnowModel.SnowBean(AppTools.getWindowsWidth(context),
                AppTools.getWindowsHeight(context) + 300,
                arrayOf(),
                180,
                255,
                AppTools.dp2px(context, 15),
                AppTools.dp2px(context, 30),
                4,
                10,
                2
        )
        (0..20).forEach { _ ->
            arrays.add(SnowModel(bean))
        }
        val dp = context.resources.displayMetrics.density
        if (dp <= 2) {
            speedMin = (dp * 0.5 + 0.5f).toInt()
            speedMax = (dp * 2 + 0.5f).toInt()
        } else {
            speedMin = (dp * 1 + 0.5f).toInt()
            speedMax = (dp * 2 + 0.5f).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (stated) {
            arrays.forEach {
                it.onDraw(canvas)
            }
            postInvalidateOnAnimation()
            arrays.forEach {
                it.update()
            }
        }
    }

    /**
     *  雨 树叶 枫叶 花  固定8个
     *  雪 25个
     */
    fun startView(model: String?) {
        if (TextUtils.isEmpty(model)) {
            return
        }
        try {
//            arrays[0].bean.images.forEach {
//                it.recycle()
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (currentModel == model)
            return
        currentModel = model
        stated = true
        when (model) {
            IConstant.THEME_RAIN -> {
                arrays[0].bean.apply {
                    images = arrayOf(map[R.mipmap.icon_secens_rain]
                            ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_secens_rain).let {
                                map[R.mipmap.icon_secens_rain] = it
                                it
                            })
                    angle = 2
                    speedMin = 20
                    speedMax = 30
                }
            }
            IConstant.THEME_SNOW -> {
                arrays[0].bean.apply {
                    images = arrayOf(
                            map[R.mipmap.icon_effects_snow_1]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effects_snow_1).let {
                                        map[R.mipmap.icon_effects_snow_1] = it
                                        it
                                    },
                            map[R.mipmap.icon_effects_snow_2]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effects_snow_2).let {
                                        map[R.mipmap.icon_effects_snow_2] = it
                                        it
                                    }
                    )
                    angle = 10
                    speedMin = this@SnowView.speedMin
                    speedMax = this@SnowView.speedMax
                }
            }
            IConstant.THEME_FLOWER -> {
                arrays[0].bean.apply {
                    images = arrayOf(
                            map[R.mipmap.icon_effedts_flower_3]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_flower_3).let {
                                        map[R.mipmap.icon_effedts_flower_3] = it
                                        it
                                    },
                            map[R.mipmap.icon_effedts_flower_1]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_flower_1).let {
                                        map[R.mipmap.icon_effedts_flower_1] = it
                                        it
                                    },
                            map[R.mipmap.icon_effedts_flower_2]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_flower_2).let {
                                        map[R.mipmap.icon_effedts_flower_2] = it
                                        it
                                    },
                            map[R.mipmap.icon_effedts_flower_4]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_flower_4).let {
                                        map[R.mipmap.icon_effedts_flower_4] = it
                                        it
                                    }
                    )
                    angle = 10
                    speedMin = this@SnowView.speedMin
                    speedMax = this@SnowView.speedMax
                }
            }
            IConstant.THEME_LEAVES -> {
                arrays[0].bean.apply {
                    images = arrayOf(
                            map[R.mipmap.icon_effedts_leaves_1]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_leaves_1).let {
                                        map[R.mipmap.icon_effedts_leaves_1] = it
                                        it
                                    },
                            map[R.mipmap.icon_effedts_leaves_2]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_leaves_2).let {
                                        map[R.mipmap.icon_effedts_leaves_2] = it
                                        it
                                    },
                            map[R.mipmap.icon_effedts_leaves_3]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_leaves_3).let {
                                        map[R.mipmap.icon_effedts_leaves_3] = it
                                        it
                                    },
                            map[R.mipmap.icon_effedts_leaves_4]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effedts_leaves_4).let {
                                        map[R.mipmap.icon_effedts_leaves_4] = it
                                        it
                                    }
                    )
                    angle = 10
                    speedMin = this@SnowView.speedMin
                    speedMax = this@SnowView.speedMax
                }
            }
            IConstant.THEME_MAPLE -> {
                arrays[0].bean.apply {
                    images = arrayOf(
                            map[R.mipmap.icon_effects_maple]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effects_maple).let {
                                        map[R.mipmap.icon_effects_maple] = it
                                        it
                                    },
                            map[R.mipmap.icon_effects_maple_1]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effects_maple_1).let {
                                        map[R.mipmap.icon_effects_maple_1] = it
                                        it
                                    },
                            map[R.mipmap.icon_effects_maple_2]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_effects_maple_2).let {
                                        map[R.mipmap.icon_effects_maple_2] = it
                                        it
                                    })
                    angle = 10
                    speedMin = this@SnowView.speedMin
                    speedMax = this@SnowView.speedMax
                }
            }
        }
        postInvalidateOnAnimation()
    }

    fun stopView() {
        currentModel = ""
        stated = false
    }

}