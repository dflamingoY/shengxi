package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ProgressBar
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.impl.ProgressBindTrackStateListener
import org.xiaoxingqi.shengxi.impl.ProgressTrackListener

class ViewSeekProgress : ProgressBar {
    private val mPaint: Paint = Paint()
    private val mPaint1: Paint = Paint()
    private lateinit var detector: GestureDetector
    private var mask: Drawable? = null
    private var radius = 0f
    private var color = Color.parseColor("#46cdcf")
    private var startColor = Color.parseColor("#33ffffff")
    private var endColor = Color.parseColor("#4dffffff")
    private val linePaint = Paint()

    //是否拦截点击事件
    private var isInterceptClick = false

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ViewSeekProgress)
        radius = array.getDimension(R.styleable.ViewSeekProgress_wrap_radius, 0f)
        color = array.getColor(R.styleable.ViewSeekProgress_mask_color, 0)
        isInterceptClick = array.getBoolean(R.styleable.ViewSeekProgress_progress_intercept, false)
        array.recycle()
        init()
    }

    private var isDraging = false

    private fun init() {
        linePaint.color = Color.parseColor("#7cffffff")
        linePaint.isDither = true
        linePaint.isAntiAlias = true
        mask = GradientDrawable()
        if (mask is GradientDrawable) {
            (mask as GradientDrawable)?.cornerRadius = radius
            (mask as GradientDrawable)?.setColor(color)
        }

        mPaint.isDither = true
        mPaint.isAntiAlias = true
        mPaint.isFilterBitmap = true
        mPaint1.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                isDraging = true
                trackListener?.startTrack()
                isPressed = true
                parent.requestDisallowInterceptTouchEvent(true)
                var current = progress / 1000f * width.toFloat()
                current -= distanceX
                setProgress((current / width * 1000).toInt(), true)
                trackStateListener?.trackStateStart(current / width)
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                return if (!isInterceptClick) {
                    performClick()
                } else
                    false
            }

            override fun onLongPress(e: MotionEvent?) {
                performLongClick()
            }
        })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val linear = LinearGradient(0f, 0f,
                w.toFloat(), h.toFloat(), startColor, endColor, Shader.TileMode.MIRROR)
        mPaint.shader = linear
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null)
        if (mask != null) {
            mask?.setBounds(0, 0, measuredWidth, measuredHeight)
            mask?.draw(canvas)
        }
        canvas?.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), mPaint1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas?.drawRoundRect(0f, 0f, progress.toFloat() / 1000 * measuredWidth, measuredHeight.toFloat(), 0f, 0f, mPaint)
            if (isInterceptClick)
                canvas?.drawRect(progress.toFloat() / 1000 * measuredWidth - 3, 0f, progress.toFloat() / 1000 * measuredWidth, measuredHeight.toFloat(), linePaint)
        }
        canvas?.restore()
        canvas?.restore()
    }

    /**
     * 填充物的背景色
     */
    fun setMaskColor(isSelf: Drawable, color: Int, startColor: Int, endColor: Int) {
        this.color = color
        mask = isSelf
//        if (isSelf) {
//            mask?.setStroke(0, Color.WHITE)
//        } else {
//            mask?.setStroke(AppTools.dp2px(context, 1), Color.parseColor("#AEAEAE"))
//        }
//        mask?.setColor(color)
        this.startColor = startColor
        this.endColor = endColor
        val linear = LinearGradient(0f, 0f,
                width.toFloat(), height.toFloat(), startColor, endColor, Shader.TileMode.MIRROR)
        mPaint.shader = linear
        invalidate()
    }

    override fun setProgress(progress: Int) {
        if (!isDraging)
            super.setProgress(progress)
    }

    override fun setProgress(progress: Int, update: Boolean) {
        if (update)
            super.setProgress(progress)
    }

    private var isMoved = false
    fun setIsMove(isMoved: Boolean) {
        if (!isDraging)
            this.isMoved = isMoved
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isMoved) {
            return super.onTouchEvent(event)
        }
        detector.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_CANCEL) {
            //点击取消之后, 回调到主界面当前进度
            if (isDraging) {
                trackListener?.endTrack(progress / 1000f)
            } else {
                if (isInterceptClick) {
                    //计算当前点击的距离
                    val percent = event.x / width
                    setProgress((percent * 1000).toInt(), true)
                    trackListener?.endTrack(percent)
                }
            }
            isDraging = false
            isPressed = false
        }
        return true
    }

    private var trackListener: ProgressTrackListener? = null

    fun setOnTrackListener(trackListener: ProgressTrackListener) {
        this.trackListener = trackListener
    }

    //绑定到ViewProgress
    private var trackStateListener: ProgressBindTrackStateListener? = null

    fun setOnTrackStateListener(trackStateListener: ProgressBindTrackStateListener) {
        this.trackStateListener = trackStateListener
    }

}