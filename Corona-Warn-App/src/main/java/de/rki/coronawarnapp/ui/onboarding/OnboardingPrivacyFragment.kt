package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingPrivacyBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This fragment informs the user regarding privacy.
 */
class OnboardingPrivacyFragment : Fragment(R.layout.fragment_onboarding_privacy), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingPrivacyViewModel by cwaViewModels { viewModelFactory }
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

                is OnboardingNavigationEvents.NavigateToMainActivity -> {
                    (requireActivity() as OnboardingActivity).completeOnboarding()
                }

                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
