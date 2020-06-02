package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewCircleProgressBinding
import de.rki.coronawarnapp.risk.TimeVariables

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
    private val circlePaint: Paint
    private val progressPaint: Paint
    private val rect = RectF()

    // var
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var progressWidth: Float = 0f
    private var disableText: Boolean = false

    var progress: Int = 0
        set(value) {
            field = value
            val body = binding.circleProgressBody
            val icon = binding.circleProgressIcon
            if (value == maxProgress) {
                body.visibility = View.GONE
                icon.visibility = View.VISIBLE
            } else {
                body.visibility = View.VISIBLE
                icon.visibility = View.GONE
            }
            if (disableText) {
                body.visibility = View.GONE
            } else {
                body.visibility = View.VISIBLE
                body.text = value.toString()
            }
            invalidate()
        }


    private var binding: ViewCircleProgressBinding

    init {
        setWillNotDraw(false)
        binding = ViewCircleProgressBinding.inflate(LayoutInflater.from(context), this)

        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress)

        val circleColor = styleAttrs.getColor(
            R.styleable.CircleProgress_secondaryColor,
            ContextCompat.getColor(context, R.color.colorGreyLight)
        )

        val progressColor = styleAttrs.getColor(R.styleable.CircleProgress_progressColor,
            ContextCompat.getColor(context, R.color.colorPrimary))

        val textColor = styleAttrs.getColor(R.styleable.CircleProgress_textColor,
            ContextCompat.getColor(context, R.color.colorGrey))

        disableText = styleAttrs.getBoolean(R.styleable.CircleProgress_disableText, true)

        progressWidth = styleAttrs.getFloat(R.styleable.CircleProgress_circleWidth, DEFAULT_WIDTH)

        progress = styleAttrs.getInt(R.styleable.CircleProgress_progress, 0)

        val body = binding.circleProgressBody
        body.setTextColor(textColor)
        if (disableText) {
            body.visibility = View.GONE
        } else {
            body.visibility = View.VISIBLE
        }

        circlePaint = Paint().apply {
            color = circleColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }

        progressPaint = Paint().apply {
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
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
        canvas?.drawCircle(centerX, centerY, radius, circlePaint)
        canvas?.drawArc(
            rect,
            START_ANGLE,
            FULL_CIRCLE.times(progress).div(maxProgress),
            false,
            progressPaint)
    }
}
