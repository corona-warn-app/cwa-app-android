package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingNotificationsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This fragment ask the user if he wants to get notifications and finishes the onboarding afterwards.
 *
 * @see NotificationManagerCompat
 * @see AlertDialog
 */
class OnboardingNotificationsFragment : Fragment(R.layout.fragment_onboarding_notifications), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingNotificationsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingNotificationsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        vm.routeToScreen.observe2(this) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToMainActivity ->
                    navigateToMain()
                is OnboardingNavigationEvents.NavigateToOnboardingTest ->
                    navigateToOnboardingTestFragment()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener { vm.onBackButtonClick() }
    }

    private fun navigateToMain() {
        (requireActivity() as OnboardingActivity).completeOnboarding()
    }

    private fun navigateToOnboardingTestFragment() {
        (activity as OnboardingActivity).goBack()
    }
}
