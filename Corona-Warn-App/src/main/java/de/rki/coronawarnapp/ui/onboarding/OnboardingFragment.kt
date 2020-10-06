package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragment.OnboardingFragmentDirections
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentDirections
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * Onboarding starting point.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingFragmentViewModel = vm
        vm.routeToScreen.observe2(this) {
            when(it) {
                is OnboardingNavigationEvents.NavigateToOnboardingPrivacy ->
                    navigateToOnboardingPrivacyFragment()
                is OnboardingNavigationEvents.NavigateToEasyLanguageUrl ->
                    navigateToEasyLanguageUrl()
            }
        }
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener { vm.onNextButtonClick()}
        binding.onboardingInclude.onboardingEasyLanguage.setOnClickListener { vm.onEasyLanguageClick() }
    }

    private fun navigateToOnboardingPrivacyFragment() {
        findNavController().doNavigate(
            OnboardingFragmentDirections.actionOnboardingFragmentToOnboardingPrivacyFragment()
        )
    }

    private fun navigateToEasyLanguageUrl() {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.onboarding_tracing_easy_language_explanation_url))
        )
        startActivity(browserIntent)
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
