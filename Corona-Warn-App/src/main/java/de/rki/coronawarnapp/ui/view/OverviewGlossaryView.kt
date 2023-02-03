package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.OverviewGlossaryLayoutBinding

class OverviewGlossaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: OverviewGlossaryLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.overview_glossary_layout, this, true)
        binding = OverviewGlossaryLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.OverviewGlossaryView) {
            val title = getText(R.styleable.OverviewGlossaryView_android_title) ?: ""
            binding.mainOverviewGlossaryTitle.isVisible = title.isNotEmpty()
            binding.mainOverviewGlossaryTitle.text = title

            val subtitle = getText(R.styleable.OverviewGlossaryView_android_subtitle) ?: ""
            binding.mainOverviewGlossarySubtitle.isVisible = subtitle.isNotEmpty()
            binding.mainOverviewGlossarySubtitle.text = subtitle
        }
    }
}
