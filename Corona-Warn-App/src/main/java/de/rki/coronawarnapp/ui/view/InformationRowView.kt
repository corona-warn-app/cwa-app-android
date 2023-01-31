package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.InformationRowLayoutBinding

class InformationRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: InformationRowLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.information_row_layout, this, true)
        binding = InformationRowLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.InformationRowView) {

            val description = getText(R.styleable.InformationRowView_android_description) ?: ""
            binding.mainRowItemSubtitle.isVisible = description.isNotEmpty()
            binding.mainRowItemSubtitle.text = description

            val icon = getResourceId(R.styleable.InformationRowView_android_src, 0)
            if (icon == 0) {
                binding.mainRowItemIcon.isVisible = false
            } else {
                val drawable = getDrawable(context, icon)
                binding.mainRowItemIcon.setImageDrawable(drawable)
            }
        }
    }
}
