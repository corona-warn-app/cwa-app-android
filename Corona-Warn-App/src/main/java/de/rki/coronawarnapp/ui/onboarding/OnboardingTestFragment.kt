package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTestBinding
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * This fragment informs the user about test results.
 */

@AndroidEntryPoint
class OnboardingTestFragment : Fragment(R.layout.fragment_onboarding_test) {

    private val vm: OnboardingTestViewModel by viewModels()
    private val binding: FragmentOnboardingTestBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
            onboardingTestToolbar.setNavigationOnClickListener { vm.onBackButtonClick() }
        }
        vm.routeToScreen.observe2(this) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingNotifications ->
                    findNavController().navigate(
                        OnboardingTestFragmentDirections
                            .actionOnboardingTestFragmentToOnboardingNotificationsFragment()
                    )

                is OnboardingNavigationEvents.NavigateToOnboardingTracing -> popBackStack()

                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingTestContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
