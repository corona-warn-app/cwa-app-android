package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTracingBinding
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This fragment ask the user if he wants to enable tracing.
 *
 * @see InternalExposureNotificationPermissionHelper
 * @see AlertDialog
 */
class OnboardingTracingFragment : Fragment(R.layout.fragment_onboarding_tracing),
    InternalExposureNotificationPermissionHelper.Callback, AutoInject {

    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper
    private val binding: FragmentOnboardingTracingBinding by viewBindingLazy()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingTracingFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.countryList.observe2(this) {
            binding.countryData = it
        }
        vm.saveInteroperabilityUsed()
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
            onboardingButtonDisable.setOnClickListener { vm.showCancelDialog() }
            onboardingButtonBack.buttonIcon.setOnClickListener { vm.onBackButtonPress() }
        }
        vm.routeToScreen.observe2(this) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingTest ->
                    internalExposureNotificationPermissionHelper.requestPermissionToStartTracing()
                is OnboardingNavigationEvents.ShowCancelDialog ->
                    DialogHelper.showDialog(DialogHelper.DialogInstance(
                        requireActivity(),
                        R.string.onboarding_tracing_dialog_headline,
                        R.string.onboarding_tracing_dialog_body,
                        R.string.onboarding_tracing_dialog_button_positive,
                        R.string.onboarding_tracing_dialog_button_negative,
                        true,
                        {
                            navigateToOnboardingTestFragment()
                        }
                    ))
                is OnboardingNavigationEvents.NavigateToOnboardingPrivacy ->
                    (requireActivity() as OnboardingActivity).goBack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        vm.resetTracing()
    }

    override fun onStartPermissionGranted() {
        navigateToOnboardingTestFragment()
    }

    override fun onFailure(exception: Exception?) {
        // dialog closed, user has to explicitly allow or deny the tracing permission
    }

    private fun navigateToOnboardingTestFragment() {
        findNavController().doNavigate(
            OnboardingTracingFragmentDirections.actionOnboardingTracingFragmentToOnboardingTestFragment()
        )
    }
}
