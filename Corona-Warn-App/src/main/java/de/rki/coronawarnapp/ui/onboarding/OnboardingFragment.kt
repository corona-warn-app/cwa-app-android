package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * Onboarding starting point.
 */
@AndroidEntryPoint
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val viewModel: OnboardingViewModel by viewModels()

    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { goToOnboardingPrivacyFragment() }
            // only show link for German
            onboardingEasyLanguage.setOnClickListener { openEasyLanguageLink() }
            viewModel.maxEncounterAgeInDays.observe(viewLifecycleOwner) { setExposureLoggingPeriod(it) }
        }
    }

    private fun goToOnboardingPrivacyFragment() {
        findNavController().navigate(
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

    private fun FragmentOnboardingBinding.setExposureLoggingPeriod(maxEncounterAgeInDays: Long) {
        onboardingBody2.text = getString(R.string.onboarding_body_2, maxEncounterAgeInDays)
    }
}
