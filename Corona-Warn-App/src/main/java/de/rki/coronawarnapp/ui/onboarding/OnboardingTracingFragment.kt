package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
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
                    displayDialog { dialog ->
                        setTitle(R.string.onboarding_tracing_dialog_headline)
                        setMessage(R.string.onboarding_tracing_dialog_body)
                        setPositiveButton(R.string.onboarding_tracing_dialog_button_positive) { _, _ ->
                            vm.disableTracingIfEnabled()
                            navigateToOnboardingTestFragment()
                            dialog.dismiss()
                        }
                        setNegativeButton(R.string.onboarding_tracing_dialog_button_negative) { _, _ -> }
                    }
                is OnboardingNavigationEvents.NavigateToOnboardingPrivacy -> popBackStack()

                else -> Unit
            }
        }
        vm.permissionRequestEvent.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        vm.ensErrorEvents.observe2(this) { error ->
            error.toErrorDialogBuilder(requireContext()).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
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
