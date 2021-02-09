package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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

/**
 * Used on the tracing details fragment without text and also on the risk card with the progress
 * number in the circle.
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class CircleProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val START_ANGLE = 270f
        private const val FULL_CIRCLE = 360f
        private const val DEFAULT_WIDTH = 10f
        private val DEFAULT_MAX_PROGRESS = TimeVariables.getDefaultRetentionPeriodInDays().toFloat()
    }

    private val circlePaint: Paint
    private var progressPaint: Paint
    private val rect = RectF()
    private var binding: ViewCircleProgressBinding
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var progressWidth: Float = 0f
    private var disableText: Boolean = false

    /**
     * Setter for progress. Text and icon depend on the progress value.
     * The visibility is also influenced by the disableText attribute.
     */
    var progress: Float = 0F
        set(value) {
            field = value
            val body = binding.circleProgressBody
            val icon = binding.circleProgressIcon
            // text visibility
            if (disableText || value == DEFAULT_MAX_PROGRESS) {
                body.visibility = View.GONE
            } else {
                body.visibility = View.VISIBLE
                body.text = context.getString(R.string.risk_details_information_active_tracing_days_circle_progress)
                    .format(value.toInt())
            }
            // icon visibility
            if (value == DEFAULT_MAX_PROGRESS) {
                icon.visibility = View.VISIBLE
            } else {
                icon.visibility = View.GONE
            }
            invalidate()
        }

    /**
     * Setter for the progress circle color.
     * The progress bar also needs to be repainted when the
     * color changes (ex: when the risk calculation gets turned on/off)
     */
    var progressColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            binding.circleProgressIcon.setColorFilter(value, android.graphics.PorterDuff.Mode.SRC_IN)
            progressPaint = paintProgressCircle()
            invalidate()
        }

    /**
     * Initialise the view with the following attributes or some default values:
     * - circleColor
     * - textColor
     * - disableText
     * - progressWidth
     */
    init {
        setWillNotDraw(false)
        binding = ViewCircleProgressBinding.inflate(LayoutInflater.from(context), this)
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress)
        val circleColor = styleAttrs.getColor(
            R.styleable.CircleProgress_circleColor,
            ContextCompat.getColor(context, R.color.colorSurface2)
        )
        // attribute progressColor; default = colorAccentTintIcon
        val progressColor = styleAttrs.getColor(
            R.styleable.CircleProgress_progressColor,
            ContextCompat.getColor(context, R.color.colorAccentTintIcon)
        )
        // attribute textColor; default = colorTextPrimary2
        val textColor = styleAttrs.getColor(
            R.styleable.CircleProgress_textColor,
            ContextCompat.getColor(context, R.color.colorTextPrimary2)
        )
        // attribute disableText; default = true
        disableText = styleAttrs.getBoolean(R.styleable.CircleProgress_disableText, false)
        // attribute progressWidth; default = DEFAULT_WIDTH
        progressWidth = styleAttrs.getFloat(R.styleable.CircleProgress_circleWidth, DEFAULT_WIDTH)
        // attribute progress; default = 0
        progress = styleAttrs.getFloat(R.styleable.CircleProgress_progress, 0F)
        // set textColor
        val body = binding.circleProgressBody
        body.setTextColor(textColor)
        // set icon color
        val icon = binding.circleProgressIcon
        icon.setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN)
        // circlePaint based on the attributes and default value
        circlePaint = Paint().apply {
            color = circleColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }
        // progressPaint based on the attributes and default value
        progressPaint = paintProgressCircle()
        styleAttrs.recycle()
    }

    private fun paintProgressCircle() =
        Paint().apply {
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w.toFloat().div(2)
        centerY = h.toFloat().div(2)
        radius = w.toFloat().div(2).minus(progressWidth)
        rect.set(
            centerX.minus(radius),
            centerY.minus(radius),
            centerX.plus(radius),
            centerY.plus(radius)
        )
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(centerX, centerY, radius, circlePaint)
        canvas?.drawArc(
            rect,
            START_ANGLE,
            FULL_CIRCLE.times(progress).div(DEFAULT_MAX_PROGRESS),
            false,
            progressPaint
        )
    }
}
