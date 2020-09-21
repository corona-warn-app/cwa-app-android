package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingNotificationsBinding
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * This fragment ask the user if he wants to get notifications and finishes the onboarding afterwards.
 *
 * @see NotificationManagerCompat
 * @see AlertDialog
 */
class OnboardingNotificationsFragment : Fragment(R.layout.fragment_onboarding_notifications) {

    private val binding: FragmentOnboardingNotificationsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            navigateToMain()
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }

    private fun navigateToMain() {
        (requireActivity() as OnboardingActivity).completeOnboarding()
    }
}
