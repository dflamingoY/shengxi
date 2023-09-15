package org.xiaoxingqi.shengxi.wedgit.starview

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import java.util.*

class StarBean {
    var startX = 0f
    var startY = 0f
    var duration = 0L
    var endX = 0f
    var endY = 0f
    var bitmap: Bitmap? = null
    var currentX = 0f
    var currentY = 0f
    var alpha = 255
    var scale = 0f
    val random: Random = Random()
    val matrix: Matrix = Matrix()
    var offsetY = 0f
    var wHeight = 0
    val paint: Paint = Paint()
}