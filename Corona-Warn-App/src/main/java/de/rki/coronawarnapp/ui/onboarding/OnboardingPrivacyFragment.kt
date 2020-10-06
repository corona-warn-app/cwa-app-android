package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingPrivacyBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This fragment informs the user regarding privacy.
 */
class OnboardingPrivacyFragment : Fragment(R.layout.fragment_onboarding_privacy), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingPrivacyViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingPrivacyBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingPrivacyViewModel = vm
        setButtonOnClickListener()
        vm.routeToScreen.observe2(this) {
            when(it){
                is OnboardingNavigationEvents.NavigateToOnboardingTracing ->
                    navigateToOnboardingTracing()
                is OnboardingNavigationEvents.NavigateToOnboardingFragment ->
                    navigateToOnboardingFragment()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener { vm.onBackButtonClick() }
    }

    private fun navigateToOnboardingTracing() {
        findNavController().doNavigate(
            OnboardingPrivacyFragmentDirections.actionOnboardingPrivacyFragmentToOnboardingTracingFragment()
        )
    }

    private fun navigateToOnboardingFragment() {
        (activity as OnboardingActivity).goBack()
    }
}
