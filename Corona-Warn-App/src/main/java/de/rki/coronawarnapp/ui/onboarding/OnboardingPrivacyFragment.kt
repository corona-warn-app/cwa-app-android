package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingPrivacyBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * This fragment informs the user regarding privacy.
 */
@AndroidEntryPoint
class OnboardingPrivacyFragment : Fragment(R.layout.fragment_onboarding_privacy) {

    private val vm: OnboardingPrivacyViewModel by viewModels()
    private val binding: FragmentOnboardingPrivacyBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
            onboardingPrivacyToolbar.setNavigationOnClickListener { vm.onBackButtonClick() }
            privacyView.getOnboardingHtmlText()
        }
        vm.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingTracing ->
                    findNavController().navigate(
                        OnboardingPrivacyFragmentDirections
                            .actionOnboardingPrivacyFragmentToOnboardingTracingFragment()
                    )

                is OnboardingNavigationEvents.NavigateToOnboardingFragment -> popBackStack()
                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
