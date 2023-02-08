package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.MainOverviewSegmentLayoutBinding

class MainOverviewSegmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: MainOverviewSegmentLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.main_overview_segment_layout, this, true)
        binding = MainOverviewSegmentLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.MainOverviewSegmentView) {

            val icon = getResourceId(R.styleable.MainOverviewSegmentView_android_icon, 0)
            binding.mainOverviewSegmentIcon.isVisible = icon != 0
            if (icon != 0) {
                val drawable = getDrawable(context, icon)
                binding.mainOverviewSegmentIcon.setImageDrawable(drawable)
            }

            val title = getText(R.styleable.MainOverviewSegmentView_android_title) ?: ""
            binding.mainOverviewSegmentTitle.isVisible = title.isNotEmpty()
            binding.mainOverviewSegmentTitle.text = title

            val subtitle = getText(R.styleable.MainOverviewSegmentView_android_subtitle) ?: ""
            binding.mainOverviewSegmentSubtitle.isVisible = subtitle.isNotEmpty()
            binding.mainOverviewSegmentSubtitle.text = subtitle
        }
    }

    fun setSubtitleText(text: String) {
        binding.mainOverviewSegmentSubtitle.text = text
    }
}
