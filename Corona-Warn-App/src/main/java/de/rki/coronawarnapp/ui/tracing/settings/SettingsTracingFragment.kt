package de.rki.coronawarnapp.ui.tracing.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsTracingBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * The user can start/stop tracing and is informed about tracing.
 *
 * @see SettingsViewModel
 * @see InternalExposureNotificationClient
 * @see InternalExposureNotificationPermissionHelper
 */
class SettingsTracingFragment : Fragment(R.layout.fragment_settings_tracing),
    InternalExposureNotificationPermissionHelper.Callback, AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsTracingFragmentViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val binding: FragmentSettingsTracingBinding by viewBindingLazy()

    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingDetailsState.observe2(this) {
            binding.tracingDetails = it
        }
        vm.tracingSettingsState.observe2(this) {
            binding.settingsTracingState = it

            binding.settingsTracingSwitchRow.settingsSwitchRow.apply {
                when (it) {
                    TracingSettingsState.BluetoothDisabled,
                    TracingSettingsState.LocationDisabled -> setOnClickListener(null)
                    TracingSettingsState.TracingInActive,
                    TracingSettingsState.TracingActive -> setOnClickListener { startStopTracing() }
                }
            }
        }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    override fun onStartPermissionGranted() {
        BackgroundWorkScheduler.startWorkScheduler()
    }

    override fun onFailure(exception: Exception?) {
        Timber.w(exception, "onPermissionFaliure")
    }

    private fun setButtonOnClickListener() {
        val switch = binding.settingsTracingSwitchRow.settingsSwitchRowSwitch
        val back = binding.settingsTracingHeader.headerButtonBack.buttonIcon
        val bluetooth = binding.settingsTracingStatusBluetooth.tracingStatusCardButton
        val location = binding.settingsTracingStatusLocation.tracingStatusCardButton
        val interoperability = binding.settingsInteroperabilityRow.settingsPlainRow

        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        switch.setOnCheckedChangeListener { view, _ ->

            // Make sure that listener is called by user interaction
            if (view.isPressed) {
                startStopTracing()
                // Focus on the body text after to announce the tracing status for accessibility reasons
                binding.settingsTracingSwitchRow.settingsSwitchRowHeaderBody
                    .sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
            }
        }
        back.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        bluetooth.setOnClickListener {
            ExternalActionHelper.toMainSettings(requireContext())
        }
        location.setOnClickListener {
            ExternalActionHelper.toMainSettings(requireContext())
        }
        interoperability.setOnClickListener {
            navigateToInteroperability()
        }
    }

    private fun navigateToInteroperability() {
        findNavController()
            .doNavigate(
                SettingsTracingFragmentDirections.actionSettingsTracingFragmentToInteropCountryConfigurationFragment()
            )
    }

    private fun startStopTracing() {
        // if tracing is enabled when listener is activated it should be disabled
        lifecycleScope.launch {
            try {
                if (InternalExposureNotificationClient.asyncIsEnabled()) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                } else {
                    // tracing was already activated
                    if (LocalData.initialTracingActivationTimestamp() != null) {
                        internalExposureNotificationPermissionHelper.requestPermissionToStartTracing()
                    } else {
                        // tracing was never activated
                        // ask for consent via dialog for initial tracing activation when tracing was not
                        // activated during onboarding
                        showConsentDialog()
                        // check if background processing is switched off, if it is, show the manual calculation dialog explanation before turning on.
                        val activity = requireActivity() as MainActivity
                        if (!activity.backgroundPrioritization.isBackgroundActivityPrioritized) {
                            showManualCheckingRequiredDialog()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.report(
                    ExceptionCategory.EXPOSURENOTIFICATION,
                    TAG,
                    null
                )
            }
        }
    }

    private fun showManualCheckingRequiredDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_manual_required_dialog_headline,
            R.string.onboarding_manual_required_dialog_body,
            R.string.onboarding_manual_required_dialog_button,
            null,
            false, {
                // close dialog
            }
        )
        DialogHelper.showDialog(dialog)
    }

    private fun showConsentDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_tracing_headline_consent,
            R.string.onboarding_tracing_body_consent,
            R.string.onboarding_button_enable,
            R.string.onboarding_button_cancel,
            true, {
                internalExposureNotificationPermissionHelper.requestPermissionToStartTracing()
            }, {
                // Declined
            })
        DialogHelper.showDialog(dialog)
    }

    companion object {
        private val TAG: String? = SettingsTracingFragment::class.simpleName
    }
}
