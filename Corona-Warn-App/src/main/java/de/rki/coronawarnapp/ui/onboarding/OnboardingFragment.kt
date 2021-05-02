package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import java.util.Locale

/**
 * Onboarding starting point.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val binding: FragmentOnboardingBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { goToOnboardingPrivacyFragment() }
            // only show link for German
            if (showEasyLanguageLink()) {
                onboardingEasyLanguage.visibility = View.VISIBLE
                onboardingEasyLanguage.setOnClickListener { openEasyLanguageLink() }
            } else {
                onboardingEasyLanguage.visibility = View.GONE
            }
        }
    }

    private fun goToOnboardingPrivacyFragment() {
        doNavigate(
            OnboardingFragmentDirections
                .actionOnboardingFragmentToOnboardingPrivacyFragment()
        )
    }

    private fun openEasyLanguageLink() {
        openUrl(getString(R.string.onboarding_tracing_easy_language_explanation_url))
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}

@VisibleForTesting
fun showEasyLanguageLink(): Boolean = Locale.getDefault().language == Locale.GERMAN.language
