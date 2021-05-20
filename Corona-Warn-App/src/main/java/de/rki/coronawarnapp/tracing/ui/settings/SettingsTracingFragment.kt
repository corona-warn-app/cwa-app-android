package de.rki.coronawarnapp.tracing.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.FragmentSettingsTracingBinding
import de.rki.coronawarnapp.tracing.ui.TracingConsentDialog
import de.rki.coronawarnapp.tracing.ui.settings.SettingsTracingFragmentViewModel.Event
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper.openDeviceSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The user can start/stop tracing and is informed about tracing.
 */
class SettingsTracingFragment : Fragment(R.layout.fragment_settings_tracing), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SettingsTracingFragmentViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val binding: FragmentSettingsTracingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loggingPeriod.observe2(this) {
            binding.loggedPeriod = it
        }
        viewModel.tracingSettingsState.observe2(this) { state ->
            binding.settingsTracingState = state

            binding.switchRow.apply {
                when (state) {
                    TracingSettingsState.BluetoothDisabled,
                    TracingSettingsState.LocationDisabled -> setOnClickListener(null)
                    TracingSettingsState.TracingInactive,
                    TracingSettingsState.TracingActive -> setOnClickListener {
                        onTracingToggled(!binding.switchRow.isChecked)
                    }
                }
            }
        }

        viewModel.events.observe2(this) {
            when (it) {
                is Event.RequestPermissions -> it.permissionRequest.invoke(requireActivity())
                is Event.ManualCheckingDialog -> showManualCheckingRequiredDialog()
                is Event.TracingConsentDialog -> TracingConsentDialog(requireContext()).show(
                    onConsentGiven = { it.onConsentResult(true) },
                    onConsentDeclined = { it.onConsentResult(false) }
                )
            }
        }

        viewModel.isTracingSwitchChecked.observe2(this) { checked ->
            binding.switchRow.setChecked(checked)
        }

        viewModel.ensErrorEvents.observe2(this) { error ->
            error.toErrorDialogBuilder(requireContext()).show()
        }

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

        settingsTracingStatusBluetooth.tracingStatusCardButton.setOnClickListener {
            openDeviceSettings()
        }

        settingsTracingStatusLocation.tracingStatusCardButton.setOnClickListener {
            openDeviceSettings()
        }

        settingsInteroperabilityRow.setOnClickListener {
            doNavigate(
                SettingsTracingFragmentDirections.actionSettingsTracingFragmentToInteropCountryConfigurationFragment()
            )
        }
    }

    private fun onTracingToggled(isChecked: Boolean) = with(viewModel) {
        if (isChecked) turnTracingOn() else turnTracingOff()
    }

    private fun showManualCheckingRequiredDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_manual_required_dialog_headline,
            R.string.onboarding_manual_required_dialog_body,
            R.string.onboarding_manual_required_dialog_button,
            null,
            false,
            {
                // close dialog
            }
        )
        DialogHelper.showDialog(dialog)
    }

    companion object {
        internal val TAG: String? = SettingsTracingFragment::class.simpleName
    }
}
