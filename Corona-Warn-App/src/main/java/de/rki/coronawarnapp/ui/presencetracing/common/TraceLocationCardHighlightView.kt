package de.rki.coronawarnapp.ui.presencetracing.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import timber.log.Timber

class TraceLocationCardHighlightView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val captionView: TextView by lazy { findViewById(R.id.caption) }
    private val containerView: ViewGroup by lazy { findViewById(R.id.container) }

    init {
        LayoutInflater.from(context).inflate(R.layout.trace_location_view_cardhighlight, this, true)

        context.withStyledAttributes(attrs, R.styleable.TraceLocationHighlightView) {
            val captionText = getText(R.styleable.TraceLocationHighlightView_android_text) ?: ""
            setCaption(captionText.toString())
        }
    }

    override fun onFinishInflate() {
        children
            .toList()
            .filter {
                val filtered = it != captionView && it != containerView
                Timber.v("filtered($filtered): %s", it)
                filtered
            }
            .forEach {
                removeView(it)
                containerView.addView(it)
            }
        super.onFinishInflate()
    }

    fun setCaption(caption: String?) {
        captionView.text = caption

        captionView.isGone = caption.isNullOrBlank()
        containerView.background = ContextCompat.getDrawable(
            context,
            if (captionView.isGone) R.drawable.trace_location_view_cardhighlight_gradient_all_corners
            else R.drawable.trace_location_view_cardhighlight_gradient_top_corners

        )
    }
}
