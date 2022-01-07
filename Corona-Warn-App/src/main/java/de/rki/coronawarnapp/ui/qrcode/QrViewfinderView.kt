package de.rki.coronawarnapp.ui.qrcode

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import de.rki.coronawarnapp.R
import kotlin.math.roundToInt

class QrViewfinderView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPosition: Float
    private val maskSize: Float

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.QrViewfinderView, 0, 0).apply {
            try {
                paint.color = getColor(R.styleable.QrViewfinderView_maskColor, Color.BLACK)
                maskPosition = getFloat(R.styleable.QrViewfinderView_maskPosition, 0.5f)
                maskSize = getDimension(R.styleable.QrViewfinderView_maskSize, 300f)
            } finally {
                recycle()
            }
        }
    }

    private val frame by lazy {
        Rect(
            (width / 2 - maskSize / 2).roundToInt(),
            (height * maskPosition - maskSize / 2).roundToInt(),
            (width / 2 + maskSize / 2).roundToInt(),
            (height * maskPosition + maskSize / 2).roundToInt()
        )
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(0f, frame.top.toFloat(), frame.left.toFloat(), (frame.bottom + 1).toFloat(), paint)
        canvas.drawRect(
            (frame.right + 1).toFloat(),
            frame.top.toFloat(),
            width.toFloat(),
            (frame.bottom + 1).toFloat(),
            paint
        )
        canvas.drawRect(0f, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)
    }
}
