package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.widget.ImageViewCompat
import de.rki.coronawarnapp.R

class TracingCardInfoRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val body by lazy { findViewById<TextView>(R.id.body) }
    private val icon by lazy { findViewById<ImageView>(R.id.icon) }

    init {
        LayoutInflater.from(context).inflate(R.layout.tracing_card_info_row_layout, this, true)

        context.withStyledAttributes(attrs, R.styleable.TracingCardInfoRow) {
            getResourceId(R.styleable.TracingCardInfoRow_android_icon, 0).let {
                if (it != 0) icon.setImageResource(it)
            }
            getResourceId(R.styleable.TracingCardInfoRow_compatIconTint, 0).let {
                if (it != 0) {
                    ImageViewCompat.setImageTintList(
                        icon,
                        ColorStateList.valueOf(ContextCompat.getColor(context, it))
                    )
                }
            }

            getResourceId(R.styleable.TracingCardInfoRow_android_text, 0).let {
                body.text = if (it != 0) resources.getString(it)
                else getString(R.styleable.TracingCardInfoRow_android_text)
            }
            getResourceId(R.styleable.TracingCardInfoRow_android_textColor, 0).let {
                if (it != 0) body.setTextColor(ContextCompat.getColor(context, it))
            }
        }
    }

    fun setText(text: String) {
        this.body.text = text
    }
}
