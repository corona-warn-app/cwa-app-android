package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * This fragment ask the user if he wants to enable tracing.
 */

@AndroidEntryPoint
class OnboardingTracingFragment : Fragment(R.layout.fragment_onboarding_tracing) {

    private val binding: FragmentOnboardingTracingBinding by viewBinding()

    private val vm: OnboardingTracingFragmentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe2(this) {
            binding.countryList.setCountryList(it)
        }
        vm.saveInteroperabilityUsed()
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onActivateTracingClicked() }
            onboardingButtonDisable.setOnClickListener { vm.showCancelDialog() }
            onboardingTracingToolbar.setNavigationOnClickListener { vm.onBackButtonPress() }
        }
        vm.routeToScreen.observe2(this) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingTest -> navigateToOnboardingTestFragment()
                is OnboardingNavigationEvents.ShowCancelDialog ->
                    displayDialog {
                        title(R.string.onboarding_tracing_dialog_headline)
                        message(R.string.onboarding_tracing_dialog_body)
                        positiveButton(R.string.onboarding_tracing_dialog_button_positive) {
                            vm.disableTracingIfEnabled()
                            navigateToOnboardingTestFragment()
                        }
                        negativeButton(R.string.onboarding_tracing_dialog_button_negative)
                    }

                is OnboardingNavigationEvents.NavigateToOnboardingPrivacy -> popBackStack()

                else -> Unit
            }
        }
        vm.permissionRequestEvent.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        vm.ensErrorEvents.observe2(this) { error -> displayDialog { setError(error) } }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        vm.handleActivityResult(requestCode, resultCode, data)
    }

    private fun navigateToOnboardingTestFragment() {
        findNavController().navigate(
            OnboardingTracingFragmentDirections.actionOnboardingTracingFragmentToOnboardingTestFragment()
        )
    }
}
