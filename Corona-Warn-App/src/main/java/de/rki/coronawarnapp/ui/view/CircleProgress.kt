package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R

class CircleProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val START_ANGLE = 270f
        private const val FULL_CIRCLE = 360f
        private const val DEFAULT_WIDTH = 20f
        private const val DEFAULT_MAX_PROGRESS = 14
    }
    private val secondaryPaint: Paint
    private val progressPaint: Paint
    private val rect = RectF()
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f

    val progressWidth = DEFAULT_WIDTH
    val maxProgress = DEFAULT_MAX_PROGRESS

    init {
        setWillNotDraw(false)
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress)
        inflate(context, R.layout.view_circle_progress, this)
        val secondaryColor = styleAttrs.getColor(R.styleable.CircleProgress_secondaryColor,
            ContextCompat.getColor(context, R.color.colorGrey))
        val progressColor = styleAttrs.getColor(R.styleable.CircleProgress_progressColor,
            ContextCompat.getColor(context, R.color.colorPrimary))
        val textColor = styleAttrs.getColor(R.styleable.CircleProgress_textColor,
            ContextCompat.getColor(context, R.color.colorGrey))
        val textEnabled = styleAttrs.getBoolean(R.styleable.CircleProgress_enableText, true)
        val textView = findViewById<TextView>(R.id.circle_text_view)
        textView.setTextColor(textColor)
        textView.isEnabled = textEnabled
        textView.visibility = if (textEnabled) View.VISIBLE else View.INVISIBLE
        secondaryPaint = Paint().apply {
            color = secondaryColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }
        progressPaint = Paint().apply {
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }
        styleAttrs.recycle()
    }

    var progress: Int = 0
        set(value) {
            findViewById<TextView>(R.id.circle_text_view).text = value.toString()
            field = value
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w.toFloat().div(2)
        centerY = h.toFloat().div(2)
        radius = w.toFloat().div(2).minus(progressWidth)
        rect.set(
            centerX.minus(radius),
            centerY.minus(radius),
            centerX.plus(radius),
            centerY.plus(radius))
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(centerX, centerY, radius, secondaryPaint)
        canvas?.drawArc(
            rect,
            START_ANGLE,
            FULL_CIRCLE.times(progress).div(maxProgress),
            false,
            progressPaint)
    }
}
