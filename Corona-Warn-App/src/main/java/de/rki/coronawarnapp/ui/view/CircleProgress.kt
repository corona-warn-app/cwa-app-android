package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.TimeVariables
import kotlinx.android.synthetic.main.view_circle_progress.view.circle_progress_text

class CircleProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val START_ANGLE = 270f
        private const val FULL_CIRCLE = 360f
        private const val DEFAULT_WIDTH = 10f
        private val DEFAULT_MAX_PROGRESS = TimeVariables.getDefaultRetentionPeriodInDays()
    }

    //val
    private val maxProgress = DEFAULT_MAX_PROGRESS
    private val secondaryPaint: Paint
    private val progressPaint: Paint
    private val rect = RectF()

    // var
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var progressWidth: Float = 0f

    private var progress: Int = 0
        set(value) {
            circle_progress_text.text = value.toString()
            field = value
            invalidate()
        }

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
        progressWidth = styleAttrs.getFloat(R.styleable.CircleProgress_circleWidth, DEFAULT_WIDTH)
        progress = styleAttrs.getInt(R.styleable.CircleProgress_progress, 0)
        val progressText = progress.toString()
        val textView = circle_progress_text
        textView.text = progressText
        textView.setTextColor(textColor)
        // textView.visibility = if (progressText == "") View.VISIBLE else View.INVISIBLE
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
