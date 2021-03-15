package de.rki.coronawarnapp.ui.eventregistration.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import de.rki.coronawarnapp.R

class TraceLocationCardHighlightView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val captionView: TextView by lazy { findViewById(R.id.caption) }
    private val containerView: ConstraintLayout by lazy { findViewById(R.id.container) }

    init {
        LayoutInflater.from(context).inflate(R.layout.trace_location_view_cardhighlight, this, true)

        background = ContextCompat.getDrawable(context, R.drawable.trace_location_view_cardhighlight_background)

        context.withStyledAttributes(attrs, R.styleable.TraceLocationHighlightView) {
            val captionText = getText(R.styleable.TraceLocationHighlightView_android_text) ?: ""
            captionView.text = captionText
        }
    }

    override fun onFinishInflate() {
        children
            .filter { it != captionView && it != containerView }
            .forEach {
                removeView(it)
                containerView.addView(it)
            }
        super.onFinishInflate()
    }

    fun setCaption(caption: String) {
        captionView.text = caption
    }
}
