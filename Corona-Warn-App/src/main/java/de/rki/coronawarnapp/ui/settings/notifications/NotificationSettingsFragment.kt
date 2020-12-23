package de.rki.coronawarnapp.ui.settings.notifications

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsNotificationsBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This is the setting notification page. Here the user sees his os notifications settings status.
 * If os notifications are disabled he can navigate to them with one click. And if the os is enabled
 * the user can decide which notifications he wants to get: risk updates and/or test results.
 */
class NotificationSettingsFragment : Fragment(R.layout.fragment_settings_notifications),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: NotificationSettingsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSettingsNotificationsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.notificationSettingsState.observe2(this) {
            binding.state = it
        }
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        // Notifications about risk status
        val updateRiskNotificationSwitch =
            binding.settingsSwitchRowNotificationsRisk.settingsSwitchRowSwitch
        // Additional click target to toggle switch
        val updateRiskNotificationRow =
            binding.settingsSwitchRowNotificationsRisk.settingsSwitchRow
        // Notifications about test status
        val updateTestNotificationSwitch =
            binding.settingsSwitchRowNotificationsTest.settingsSwitchRowSwitch
        // Additional click target to toggle switch
        val updateTestNotificationRow =
            binding.settingsSwitchRowNotificationsTest.settingsSwitchRow
        // Settings
        val settingsRow = binding.settingsNotificationsCard.tracingStatusCardButton
        val goBack =
            binding.settingsNotificationsHeader.headerButtonBack.buttonIcon
        // Update Risk
        updateRiskNotificationSwitch.setOnCheckedChangeListener { view, _ ->
            // Make sure that listener is called by user interaction
            if (!view.isPressed) return@setOnCheckedChangeListener

            vm.toggleNotificationsRiskEnabled()
        }
        // Additional click target to toggle switch
        updateRiskNotificationRow.setOnClickListener {
            if (updateRiskNotificationRow.isEnabled) vm.toggleNotificationsRiskEnabled()
        }
        // Update Test
        updateTestNotificationSwitch.setOnCheckedChangeListener { view, _ ->
            // Make sure that listener is called by user interaction
            if (!view.isPressed) return@setOnCheckedChangeListener

            vm.toggleNotificationsTestEnabled()
        }
        // Additional click target to toggle switch
        updateTestNotificationRow.setOnClickListener {
            if (updateTestNotificationRow.isEnabled) vm.toggleNotificationsTestEnabled()
        }
        goBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        // System Settings
        settingsRow.setOnClickListener {
            ExternalActionHelper.toNotifications(requireContext())
        }
    }
}
