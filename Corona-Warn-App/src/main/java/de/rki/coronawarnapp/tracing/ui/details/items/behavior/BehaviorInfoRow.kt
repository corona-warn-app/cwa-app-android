package de.rki.coronawarnapp.tracing.ui.details.items.behavior

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.hasAPILevel
import setTextWithUrl

class BehaviorInfoRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val body by lazy { findViewById<TextView>(R.id.body) }
    private val icon by lazy { findViewById<ImageView>(R.id.icon) }
    private val iconBackground by lazy { findViewById<View>(R.id.icon_background) }
    private val infoButton by lazy { findViewById<View>(R.id.info_icon) }

    init {
        LayoutInflater.from(context).inflate(R.layout.tracing_details_item_behavior_row_view, this, true)

        context.withStyledAttributes(attrs, R.styleable.TracingDetailsBehaviorRow) {
            getResourceId(R.styleable.TracingDetailsBehaviorRow_android_icon, 0).let {
                if (it != 0) icon.setImageResource(it)
            }
            getResourceId(R.styleable.TracingDetailsBehaviorRow_android_foregroundTint, 0).let {
                if (it != 0) setForegroundTint(ContextCompat.getColor(context, it))
            }

            getResourceId(R.styleable.TracingDetailsBehaviorRow_android_backgroundTint, 0).let {
                if (it != 0) setBackgroundTint(ContextCompat.getColor(context, it))
            }

            getResourceId(R.styleable.TracingDetailsBehaviorRow_android_text, 0).let {
                body.text = if (it != 0) resources.getString(it)
                else getString(R.styleable.TracingDetailsBehaviorRow_android_text)
            }
            infoButton.isGone = true
        }

        if (body.text == context.getString(R.string.risk_details_behavior_body_health_department)) {
            body.setTextWithUrl(
                R.string.risk_details_behavior_body_health_department,
                R.string.risk_details_behavior_body_health_department_label,
                R.string.risk_details_behavior_body_health_department_link
            )
        }
    }

    fun setText(text: String) {
        this.body.text = text
    }

    fun setTextWithUrl(@StringRes textRes: Int, @StringRes labelRes: Int, @StringRes urlRes: Int) {
        this.body.setTextWithUrl(textRes, labelRes, urlRes)
    }

    fun setBackgroundTint(@ColorInt color: Int) {
        @Suppress("DEPRECATION")
        when {
            BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.LOLLIPOP_MR1) ->
                ViewCompat.setBackgroundTintList(iconBackground, ColorStateList.valueOf(color))
            else ->
                iconBackground.background.setColorFilter(color, PorterDuff.Mode.SRC_OVER)
        }
    }

    fun setForegroundTint(@ColorInt color: Int) {
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(color))
    }
}

@BindingAdapter("infoButtonCallback")
fun BehaviorInfoRow.infoButtonCallback(callback: () -> Unit) {
    findViewById<View>(R.id.info_icon).apply {
        isVisible = true
        setOnClickListener { callback() }
    }
}
