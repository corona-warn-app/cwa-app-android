package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTestBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * This fragment informs the user about test results.
 */
class OnboardingTestFragment : Fragment(R.layout.fragment_onboarding_test) {
    private val binding: FragmentOnboardingTestBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingTestContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            findNavController().doNavigate(
                OnboardingTestFragmentDirections.actionOnboardingTestFragmentToOnboardingNotificationsFragment()
            )
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }
}
