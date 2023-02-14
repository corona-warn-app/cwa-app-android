package de.rki.coronawarnapp.tracing.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTracingSettingsBinding
import de.rki.coronawarnapp.tracing.ui.settings.TracingSettingsFragmentViewModel.Event
import de.rki.coronawarnapp.tracing.ui.tracingConsentDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openDeviceSettings
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * The user can start/stop tracing and is informed about tracing.
 */
@AndroidEntryPoint
class TracingSettingsFragment : Fragment(R.layout.fragment_tracing_settings) {

    private val viewModel: TracingSettingsFragmentViewModel by activityViewModels()
    private val binding: FragmentTracingSettingsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loggingPeriod.observe(viewLifecycleOwner) {
            with(binding) {
                riskDetailsPeriodLoggedBodyNotice.text = it.getExposureLoggingPeriod(requireContext())
                riskDetailsPeriodLoggedDays.text = it.getInstallTimePeriodLogged(requireContext())
            }
        }
        viewModel.tracingSettingsState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                illustration.apply {
                    contentDescription = state.getTracingIllustrationText(requireContext())
                    setImageDrawable(state.getTracingStatusImage(requireContext()))
                }

                switchRow.apply {
                    setChecked(state.isTracingSwitchChecked())
                    setSubtitle(state.getTracingStatusText(requireContext()))
                    setSwitchEnabled(state.isTracingSwitchEnabled())
                }
                settingsTracingStatusLocation.isVisible = state.isLocationCardVisible()
                settingsTracingStatusBluetooth.isVisible = state.isBluetoothCardVisible()
                riskDetailsPeriodLoggedHeadline.isVisible = state.isTracingStatusTextVisible()
                riskDetailsPeriodLoggedSubtitle.isVisible = state.isTracingStatusTextVisible()
                riskDetailsPeriodLoggedBodyNotice.isVisible = state.isTracingStatusTextVisible()
                riskDetailsPeriodLoggedDays.isVisible = state.isTracingStatusTextVisible()

                switchRow.apply {
                    when (state) {
                        TracingSettingsState.BluetoothDisabled,
                        TracingSettingsState.LocationDisabled -> setOnClickListener(null)

                        TracingSettingsState.TracingInactive,
                        TracingSettingsState.TracingActive -> setOnClickListener {
                            onTracingToggled(!switchRow.isChecked)
                        }
                    }
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                is Event.RequestPermissions -> it.permissionRequest.invoke(requireActivity())
                is Event.ManualCheckingDialog -> showManualCheckingRequiredDialog()
                is Event.TracingConsentDialog -> tracingConsentDialog(
                    positiveButton = { it.onConsentResult(true) },
                    negativeButton = { it.onConsentResult(false) }
                )
            }
        }

        viewModel.isTracingSwitchChecked.observe(viewLifecycleOwner) { checked ->
            binding.switchRow.setChecked(checked)
        }

        viewModel.ensErrorEvents.observe(viewLifecycleOwner) { error -> displayDialog { setError(error) } }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.handleActivityResult(requestCode, resultCode, data)
    }

    private fun setButtonOnClickListener() = with(binding) {
        toolbar.setNavigationOnClickListener {
            popBackStack()
        }

        settingsTracingStatusBluetooth.setOnClickListener {
            openDeviceSettings()
        }

        settingsTracingStatusLocation.setOnClickListener {
            openDeviceSettings()
        }

        settingsInteroperabilityRow.setOnClickListener {
            findNavController().navigate(
                TracingSettingsFragmentDirections.actionSettingsTracingFragmentToInteropCountryConfigurationFragment()
            )
        }
    }

    private fun onTracingToggled(isChecked: Boolean) = with(viewModel) {
        if (isChecked) turnTracingOn() else turnTracingOff()
    }

    private fun showManualCheckingRequiredDialog() = displayDialog {
        title(R.string.onboarding_manual_required_dialog_headline)
        message(R.string.onboarding_manual_required_dialog_body)
        positiveButton(R.string.onboarding_manual_required_dialog_button)
    }

    companion object {
        internal val TAG: String? = TracingSettingsFragment::class.simpleName
    }
}
