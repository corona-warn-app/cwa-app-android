package de.rki.coronawarnapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsTracingBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.IGNORE_CHANGE_TAG
import de.rki.coronawarnapp.util.PowerManagementHelper
import de.rki.coronawarnapp.util.formatter.formatTracingSwitchEnabled
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.launch

/**
 * The user can start/stop tracing and is informed about tracing.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 * @see InternalExposureNotificationClient
 * @see InternalExposureNotificationPermissionHelper
 */
class SettingsTracingFragment : Fragment(),
    InternalExposureNotificationPermissionHelper.Callback {

    companion object {
        private val TAG: String? = SettingsTracingFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentSettingsTracingBinding? = null
    private val binding: FragmentSettingsTracingBinding get() = _binding!!

    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsTracingBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshIsTracingEnabled()
        binding.settingsTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }

    override fun onStartPermissionGranted() {
        tracingViewModel.refreshIsTracingEnabled()
        BackgroundWorkScheduler.startWorkScheduler()
    }

    override fun onFailure(exception: Exception?) {
        tracingViewModel.refreshIsTracingEnabled()
    }

    private fun setButtonOnClickListener() {
        val row = binding.settingsTracingSwitchRow.settingsSwitchRow
        val switch = binding.settingsTracingSwitchRow.settingsSwitchRowSwitch
        val back = binding.settingsTracingHeader.headerButtonBack.buttonIcon
        val bluetooth = binding.settingsTracingStatusBluetooth.tracingStatusCardButton
        val connection = binding.settingsTracingStatusConnection.tracingStatusCardButton
        val location = binding.settingsTracingStatusLocation.tracingStatusCardButton
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)
        switch.setOnCheckedChangeListener { _, _ ->
            // Make sure that listener is called by user interaction
            if (switch.tag != IGNORE_CHANGE_TAG) {
                startStopTracing()
                // Focus on the body text after to announce the tracing status for accessibility reasons
                binding.settingsTracingSwitchRow.settingsSwitchRowHeaderBody
                    .sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
            }
        }
        row.setOnClickListener {
            val isTracingEnabled =
                tracingViewModel.isTracingEnabled.value ?: throw IllegalArgumentException()
            val isBluetoothEnabled =
                settingsViewModel.isBluetoothEnabled.value ?: throw IllegalArgumentException()
            val isConnectionEnabled =
                settingsViewModel.isConnectionEnabled.value ?: throw IllegalArgumentException()
            val isLocationEnabled =
                settingsViewModel.isLocationEnabled.value ?: throw IllegalArgumentException()
            // check if the row is clickable, this adds the switch behaviour
            val isEnabled = formatTracingSwitchEnabled(
                isTracingEnabled,
                isBluetoothEnabled,
                isConnectionEnabled,
                isLocationEnabled
            )
            if (isEnabled) startStopTracing()
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
        connection.setOnClickListener {
            ExternalActionHelper.toConnections(requireContext())
        }
    }

    private fun startStopTracing() {
        // if tracing is enabled when listener is activated it should be disabled
        lifecycleScope.launch {
            try {
                if (InternalExposureNotificationClient.asyncIsEnabled()) {
                    InternalExposureNotificationClient.asyncStop()
                    tracingViewModel.refreshIsTracingEnabled()
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
                        if (!PowerManagementHelper.isIgnoringBatteryOptimizations(requireActivity())) {
                            showManualCheckingRequiredDialog()
                        }
                    }
                }
            } catch (exception: Exception) {
                tracingViewModel.refreshIsTracingEnabled()
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
                tracingViewModel.refreshIsTracingEnabled()
            })
        DialogHelper.showDialog(dialog)
    }
}
