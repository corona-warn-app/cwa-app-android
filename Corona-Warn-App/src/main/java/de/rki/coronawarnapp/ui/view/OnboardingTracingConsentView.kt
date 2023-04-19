package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.OnboardingTracingConsentLayoutBinding

class OnboardingTracingConsentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: OnboardingTracingConsentLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.onboarding_tracing_consent_layout, this, true)
        binding = OnboardingTracingConsentLayoutBinding.bind(this)
    }
}
