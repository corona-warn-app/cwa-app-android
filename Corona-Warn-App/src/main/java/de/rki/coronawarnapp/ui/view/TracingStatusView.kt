package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingStatusLayoutBinding
import setTextWithUrl

class TracingStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: TracingStatusLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.tracing_status_layout, this, true)
        binding = TracingStatusLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.TracingStatusView) {
            with(binding) {
                val title = getText(R.styleable.TracingStatusView_android_title) ?: ""
                tracingStatusTitle.text = title

                val icon = getResourceId(R.styleable.TracingStatusView_android_src, 0)
                if (icon == 0) {
                    tracingStatusIcon.isVisible = false
                } else {
                    val drawable = getDrawable(context, icon)
                    tracingStatusIcon.setImageDrawable(drawable)
                }

                val body = getText(R.styleable.TracingStatusView_tracingStatusBody) ?: ""
                tracingStatusBody.isVisible = body.isNotEmpty()
                tracingStatusBody.text = body

                val url = getText(R.styleable.TracingStatusView_tracingStatusUrl) ?: ""
                val urlLabel = getText(R.styleable.TracingStatusView_tracingStatusLabel) ?: ""
                if (url.isNotEmpty() && urlLabel.isNotEmpty()) {
                    tracingStatusBody.setTextWithUrl(body.toString(), urlLabel.toString(), url.toString())
                }

                val buttonText = getText(R.styleable.TracingStatusView_tracingStatusButton) ?: ""
                tracingStatusButton.isGone = buttonText.isEmpty()
                tracingStatusButton.text = buttonText

            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.tracingStatusButton.setOnClickListener(l)
    }
}
