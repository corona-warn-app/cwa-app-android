package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * Onboarding starting point.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OnboardingViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentOnboardingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { goToOnboardingPrivacyFragment() }
            // only show link for German
            onboardingEasyLanguage.setOnClickListener { openEasyLanguageLink() }
            viewModel.maxEncounterAgeInDays.observe2(this@OnboardingFragment) {
                setExposureLoggingPeriod(it)
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

    private fun FragmentOnboardingBinding.setExposureLoggingPeriod(maxEncounterAgeInDays: Long) {
        onboardingBody2.text = getString(R.string.onboarding_body_2, maxEncounterAgeInDays)
    }
}
