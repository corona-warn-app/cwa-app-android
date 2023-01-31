package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.BulletPointTextLayoutBinding

class BulletPointTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: BulletPointTextLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.bullet_point_text_layout, this, true)
        binding = BulletPointTextLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.BulletPointTextView) {

            val description = getText(R.styleable.BulletPointTextView_android_description) ?: ""
            binding.bulletpointText.isVisible = description.isNotEmpty()
            binding.bulletpointText.text = description

            if (getBoolean(R.styleable.DccReissuanceBulletPoint_bold, false))
                binding.bulletpointText.setTypeface(binding.bulletpointText.typeface, Typeface.BOLD)
        }
    }

    fun setBulletPointText(bulletpointText: CharSequence) {
        binding.bulletpointText.isVisible = bulletpointText.isNotEmpty()
        binding.bulletpointText.text = bulletpointText
    }
}
