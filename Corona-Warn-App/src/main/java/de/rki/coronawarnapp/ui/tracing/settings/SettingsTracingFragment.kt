package de.rki.coronawarnapp.ui.tracing.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsTracingBinding
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.tracing.ui.TracingConsentDialog
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.tracing.settings.SettingsTracingFragmentViewModel.Event
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The user can start/stop tracing and is informed about tracing.
 *
 * @see SettingsViewModel
 * @see InternalExposureNotificationClient
 */
class SettingsTracingFragment : Fragment(R.layout.fragment_settings_tracing), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsTracingFragmentViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val binding: FragmentSettingsTracingBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingDetailsState.observe2(this) {
            binding.tracingDetails = it
        }
        vm.tracingSettingsState.observe2(this) { state ->
            binding.settingsTracingState = state

            binding.settingsTracingSwitchRow.settingsSwitchRow.apply {
                when (state) {
                    TracingSettingsState.BluetoothDisabled,
                    TracingSettingsState.LocationDisabled -> setOnClickListener(null)
                    TracingSettingsState.TracingInactive,
                    TracingSettingsState.TracingActive -> setOnClickListener {
                        binding.settingsTracingSwitchRow.settingsSwitchRowSwitch.performClick()
                    }
                }
            }
        }

        vm.events.observe2(this) {
            when (it) {
                is Event.RequestPermissions -> it.permissionRequest.invoke(requireActivity())
                Event.ManualCheckingDialog -> showManualCheckingRequiredDialog()
                is Event.TracingConsentDialog -> {
                    TracingConsentDialog(requireContext()).show(
                        onConsentGiven = { it.onConsentResult(true) },
                        onConsentDeclined = { it.onConsentResult(false) }
                    )
                }
            }
        }

        vm.isTracingSwitchChecked.observe2(this) { checked ->
            binding.settingsTracingSwitchRow.settingsSwitchRowSwitch.isChecked = checked
        }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsTracingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        vm.handleActivityResult(requestCode, resultCode, data)
    }

    private fun setButtonOnClickListener() {
        val switch = binding.settingsTracingSwitchRow.settingsSwitchRowSwitch
        val back = binding.settingsTracingHeader.headerButtonBack.buttonIcon
        val bluetooth = binding.settingsTracingStatusBluetooth.tracingStatusCardButton
        val location = binding.settingsTracingStatusLocation.tracingStatusCardButton
        val interoperability = binding.settingsInteroperabilityRow.settingsPlainRow
        val row = binding.settingsTracingSwitchRow.settingsSwitchRow

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (switch.isPressed || row.isPressed) {
                onTracingToggled(isChecked)
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

    private fun onTracingToggled(isChecked: Boolean) {
        // Focus on the body text after to announce the tracing status for accessibility reasons
        binding.settingsTracingSwitchRow.settingsSwitchRowHeaderBody
            .sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
        vm.onTracingToggled(isChecked)
    }

    private fun navigateToInteroperability() {
        doNavigate(
            SettingsTracingFragmentDirections.actionSettingsTracingFragmentToInteropCountryConfigurationFragment()
        )
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

    companion object {
        internal val TAG: String? = SettingsTracingFragment::class.simpleName
    }
}
