package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingNotificationsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * This fragment ask the user if he wants to get notifications and finishes the onboarding afterwards.
 *
 * @see NotificationManagerCompat
 * @see AlertDialog
 */
@AndroidEntryPoint
class OnboardingNotificationsFragment : Fragment(R.layout.fragment_onboarding_notifications) {

    private val vm: OnboardingNotificationsViewModel by viewModels()
    private val binding: FragmentOnboardingNotificationsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
            onboardingNotificationsToolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        }
        vm.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingAnalytics ->
                    findNavController().navigate(
                        OnboardingNotificationsFragmentDirections
                            .actionOnboardingNotificationsFragmentToOnboardingAnalyticsFragment()
                    )

                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
