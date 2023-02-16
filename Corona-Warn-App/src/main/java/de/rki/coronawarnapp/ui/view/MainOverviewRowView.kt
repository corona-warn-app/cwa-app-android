package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.MainOverviewRowLayoutBinding

class MainOverviewRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: MainOverviewRowLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.main_overview_row_layout, this, true)
        binding = MainOverviewRowLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.MainOverviewRowView) {

            val icon = getResourceId(R.styleable.MainOverviewRowView_android_icon, 0)
            binding.mainOverviewIcon.isVisible = icon != 0
            if (icon != 0) {
                val drawable = getDrawable(context, icon)
                binding.mainOverviewIcon.setImageDrawable(drawable)
            }

            val iconTint = getColor(
                R.styleable.MainOverviewRowView_android_iconTint,
                resources.getColor(R.color.colorAccentTintIcon, resources.newTheme())
            )
            binding.mainOverviewIcon.setColorFilter(iconTint)

            val subtitle = getText(R.styleable.MainOverviewRowView_android_subtitle) ?: ""
            binding.mainOverviewRowSubtitle.isVisible = subtitle.isNotEmpty()
            binding.mainOverviewRowSubtitle.text = subtitle
        }
    }
}
