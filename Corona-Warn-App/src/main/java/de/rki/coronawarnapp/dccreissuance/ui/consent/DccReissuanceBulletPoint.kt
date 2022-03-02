package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R

class DccReissuanceBulletPoint @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val bulletPointText by lazy { findViewById<TextView>(R.id.bulletpoint_text) }

    init {
        LayoutInflater.from(context).inflate(R.layout.dcc_reissuance_bullet_point, this, true)

        context.withStyledAttributes(
            attrs,
            R.styleable.DccReissuanceBulletPoint,
        ) {
            getResourceId(R.styleable.DccReissuanceBulletPoint_android_text, 0).let {
                bulletPointText.text = if (it != 0) {
                    resources.getString(it)
                } else {
                    getString(R.styleable.DccReissuanceBulletPoint_android_text)
                }
            }
            if (getBoolean(R.styleable.DccReissuanceBulletPoint_bold, false))
                bulletPointText.setTypeface(bulletPointText.typeface, Typeface.BOLD)
        }
    }
}
