package org.xiaoxingqi.shengxi.wedgit.flowereffects.effective

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class SnowModel(val bean: SnowBean) {
    private var bitmap: Bitmap? = null
    private var positionX = 0.0
    private var positionY = 0.0
    var shouldRecycle = true//是否继续循环anim
    private var speedX: Double = 0.0
    private var speedY: Double = 0.0
    private var stop = false
    private var size = 0
    private var alpha: Int = 255

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(255, 255, 255)
    }
    private val random = Random()

    init {
        reset()
    }

    //计算当前
    internal fun reset(positionY: Double? = null) {
        try {
            shouldRecycle = true
            size = bean.sizeMin + random.nextInt(bean.sizeMax - bean.sizeMin)
            if (bean.images.isNotEmpty()) {
                var tempBitmap = bean.images[random.nextInt(bean.images.size)]
                val tempW = tempBitmap.width
                val tempH = tempBitmap.height
                val scale = if (size > tempW || size > tempH) {
                    if (tempW > tempH) {
                        tempW.toFloat() / size
                    } else {
                        tempH.toFloat() / size
                    }
                } else {
                    if (tempW > tempH) {
                        size.toFloat() / tempW
                    } else {
                        size.toFloat() / tempH
                    }
                }
                if (!tempBitmap.isRecycled) {
                    bitmap = Bitmap.createScaledBitmap(tempBitmap, (tempW / 2f).toInt(), (tempH / 2f).toInt(), false)
                }
            }
            val speed = (size - bean.sizeMin).toFloat() / (bean.sizeMax - bean.sizeMin) * (bean.speedMax - bean.speedMin) + bean.speedMin
            val angle = Math.toRadians(random.nextDouble() * (bean.angle + 1)) * if (random.nextBoolean()) 1 else -1
            speedY = speed * cos(angle)
            speedX = speed * sin(angle)
            positionX = random.nextDouble() * (bean.parentWidth)
            alpha = random.nextInt(bean.alphaMax - bean.alphaMin) + bean.alphaMin
//        paint.alpha = alpha
            if (positionY != null) {
                this.positionY = positionY
            } else {
                this.positionY = random.nextDouble() * bean.parentHeight
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 继续在屏幕中滑行
     */
    fun isStillFalling(): Boolean {
        return shouldRecycle || (positionY > 0 && positionY < bean.parentHeight)
    }

    fun update() {
        positionX += speedX
        positionY += speedY
        if (positionY > bean.parentHeight) {
            if (shouldRecycle) {
                if (stop) {
                    stop = false
                    reset()
                } else {
                    reset(-size.toDouble())
                }
            } else {
                stop = true
            }
        }
        //计算出当前的
//        paint.alpha = (alpha * ((bean.parentHeight - positionY).toFloat() / bean.parentHeight)).toInt()
    }

    fun onDraw(canvas: Canvas) {
        bitmap?.let {
            canvas.drawBitmap(it, positionX.toFloat(), positionY.toFloat() - 300, paint)
        }
    }

    data class SnowBean(val parentWidth: Int,
                        val parentHeight: Int,
                        var images: Array<Bitmap>,
                        val alphaMin: Int,
                        val alphaMax: Int,
                        val sizeMin: Int,
                        val sizeMax: Int,
                        var speedMin: Int,
                        var speedMax: Int,
                        var angle: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SnowBean) return false

            if (parentWidth != other.parentWidth) return false
            if (parentHeight != other.parentHeight) return false
            if (!images.contentEquals(other.images)) return false
            if (alphaMin != other.alphaMin) return false
            if (alphaMax != other.alphaMax) return false
            if (sizeMin != other.sizeMin) return false
            if (sizeMax != other.sizeMax) return false
            if (speedMin != other.speedMin) return false
            if (speedMax != other.speedMax) return false
            if (angle != other.angle) return false

            return true
        }

        override fun hashCode(): Int {
            var result = parentWidth
            result = 31 * result + parentHeight
            result = 31 * result + images.contentHashCode()
            result = 31 * result + alphaMin
            result = 31 * result + alphaMax
            result = 31 * result + sizeMin
            result = 31 * result + sizeMax
            result = 31 * result + speedMin
            result = 31 * result + speedMax
            result = 31 * result + angle
            return result
        }
    }

}
