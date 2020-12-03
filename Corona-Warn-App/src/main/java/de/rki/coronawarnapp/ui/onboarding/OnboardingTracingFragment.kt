package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This fragment ask the user if he wants to enable tracing.
 */
class OnboardingTracingFragment : Fragment(R.layout.fragment_onboarding_tracing), AutoInject {

    private val binding: FragmentOnboardingTracingBinding by viewBindingLazy()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingTracingFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe2(this) {
            binding.countryData = it
        }
        vm.saveInteroperabilityUsed()
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onActivateTracingClicked() }
            onboardingButtonDisable.setOnClickListener { vm.showCancelDialog() }
            onboardingButtonBack.buttonIcon.setOnClickListener { vm.onBackButtonPress() }
        }
        vm.routeToScreen.observe2(this) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingTest -> navigateToOnboardingTestFragment()
                is OnboardingNavigationEvents.ShowCancelDialog ->
                    DialogHelper.showDialog(DialogHelper.DialogInstance(
                        context = requireActivity(),
                        title = R.string.onboarding_tracing_dialog_headline,
                        message = R.string.onboarding_tracing_dialog_body,
                        positiveButton = R.string.onboarding_tracing_dialog_button_positive,
                        negativeButton = R.string.onboarding_tracing_dialog_button_negative,
                        cancelable = true,
                        positiveButtonFunction = {
                            navigateToOnboardingTestFragment()
                        }
                    ))
                is OnboardingNavigationEvents.NavigateToOnboardingPrivacy ->
                    (requireActivity() as OnboardingActivity).goBack()
            }
        }
        vm.permissionRequestEvent.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        vm.resetTracing()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        vm.handleActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToOnboardingTestFragment() {
        findNavController().doNavigate(
            OnboardingTracingFragmentDirections.actionOnboardingTracingFragmentToOnboardingTestFragment()
        )
    }
}
