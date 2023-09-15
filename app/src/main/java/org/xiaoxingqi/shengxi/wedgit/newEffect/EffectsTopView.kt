package org.xiaoxingqi.shengxi.wedgit.newEffect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant

class EffectsTopView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val arrays = arrayListOf<EffectsModel>()
    private var started = false
    private var speedMin = 0
    private var speedMax = 0
    private var currentModel: String? = ""

    companion object {
        private val map by lazy { HashMap<Int, Bitmap>() }
    }

    init {
        //添加数据
        val dp = context.resources.displayMetrics.density
        val bean = EffectsModel.EffectsBean(AppTools.getWindowsWidth(context),
                (dp * 330).toInt(),
                arrayOf(),
                200,
                255,
                AppTools.dp2px(context, 15),
                AppTools.dp2px(context, 30),
                4,
                10,
                2
        )
        (0..20).forEach { _ ->
            arrays.add(EffectsModel(bean))
        }
        if (dp <= 2) {
            speedMin = (dp * 0.5 + 0.5f).toInt()
            speedMax = (dp * 2 + 0.5f).toInt()
        } else {
            speedMin = (dp * 0.3 + 0.5f).toInt()
            speedMax = (dp * 1.2 + 0.5f).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (started) {
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
     *
     */
    fun start(model: String) {
        if (TextUtils.isEmpty(model)) {
            return
        }
        if (currentModel == model)
            return
        currentModel = model
        started = true
        when (model) {
            IConstant.THEME_RAIN -> {
                arrays[0].bean.apply {
                    images = arrayOf(map[R.mipmap.icon_secens_rain]
                            ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_secens_rain).let {
                                map[R.mipmap.icon_secens_rain] = it
                                it
                            })
                    angle = 2
                    speedMin = 10
                    speedMax = 16
                }
            }
            IConstant.THEME_SNOW -> {
                arrays[0].bean.apply {
                    images = arrayOf(
                            map[R.mipmap.icon_snow_home1]
                                    ?: BitmapFactory.decodeResource(resources, R.mipmap.icon_snow_home1).let {
                                        map[R.mipmap.icon_snow_home1] = it
                                        it
                                    }
                    )
                    angle = 10
                    speedMin = this@EffectsTopView.speedMin
                    speedMax = this@EffectsTopView.speedMax
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
                    speedMin = this@EffectsTopView.speedMin
                    speedMax = this@EffectsTopView.speedMax
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
                    speedMin = this@EffectsTopView.speedMin
                    speedMax = this@EffectsTopView.speedMax
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
                    speedMin = this@EffectsTopView.speedMin
                    speedMax = this@EffectsTopView.speedMax
                }
            }
        }
        postInvalidateOnAnimation()
    }

    fun stop() {
        currentModel = ""
        started = false
        /*重置所有点的位置*/
        arrays.forEach {
            it.restore()
        }

        postInvalidateOnAnimation()
    }
}