package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This fragment ask the user if he wants to enable tracing.
 */
class OnboardingTracingFragment : Fragment(R.layout.fragment_onboarding_tracing), AutoInject {

    private val binding: FragmentOnboardingTracingBinding by viewBinding()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingTracingFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe(viewLifecycleOwner) {
            binding.countryList.setCountryList(it)
        }
        vm.saveInteroperabilityUsed()
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onActivateTracingClicked() }
            onboardingButtonDisable.setOnClickListener { vm.showCancelDialog() }
            onboardingTracingToolbar.setNavigationOnClickListener { vm.onBackButtonPress() }
        }
        vm.routeToScreen.observe(viewLifecycleOwner) {
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
        vm.permissionRequestEvent.observe(viewLifecycleOwner) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        vm.ensErrorEvents.observe(viewLifecycleOwner) { error -> displayDialog { setError(error) } }
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
