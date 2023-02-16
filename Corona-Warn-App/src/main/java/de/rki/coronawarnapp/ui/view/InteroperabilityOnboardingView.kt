package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.InteroperabilityOnboardingLayoutBinding

class InteroperabilityOnboardingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: InteroperabilityOnboardingLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.interoperability_onboarding_layout, this, true)
        binding = InteroperabilityOnboardingLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.InteroperabilityOnboardingView) {
            with(binding) {
                val title = getText(R.styleable.InteroperabilityOnboardingView_android_title) ?: ""
                interoperabilityOnboardingContainer.contentDescription = title
                interopOnboardingTitle.isVisible = title.isNotEmpty()
                interopOnboardingTitle.text = title

                val firstSection = getText(R.styleable.InteroperabilityOnboardingView_firstSection) ?: ""
                interopFirstSection.text = firstSection

                val secondSection = getText(R.styleable.InteroperabilityOnboardingView_secondSection) ?: ""
                interopSecondSection.text = secondSection

                val thirdSection = getText(R.styleable.InteroperabilityOnboardingView_thirdSection) ?: ""
                interopThirdSection.text = thirdSection
            }
        }
    }
}
