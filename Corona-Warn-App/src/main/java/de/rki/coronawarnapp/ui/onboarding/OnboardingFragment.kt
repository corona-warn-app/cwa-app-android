package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBinding
import setTextWithUrl
import java.util.Locale

/**
 * Onboarding starting point.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { goToOnboardingPrivacyFragment() }
            onboardingEasyLanguage.isVisible = showEasyLanguageLink() // only show link for German
            onboardingEasyLanguage.setTextWithUrl(
                R.string.easy_language_title,
                R.string.easy_language_title,
                R.string.easy_language_url
            )
        }
    }

    private fun goToOnboardingPrivacyFragment() {
        doNavigate(
            OnboardingFragmentDirections
                .actionOnboardingFragmentToOnboardingPrivacyFragment()
        )
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}

fun showEasyLanguageLink(): Boolean = Locale.getDefault().language == Locale.GERMAN.language
