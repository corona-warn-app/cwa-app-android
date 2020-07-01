package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentSettingsNotificationsBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.IGNORE_CHANGE_TAG

/**
 * This is the setting notification page. Here the user sees his os notifications settings status.
 * If os notifications are disabled he can navigate to them with one click. And if the os is enabled
 * the user can decide which notifications he wants to get: risk updates and/or test results.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class SettingsNotificationFragment : Fragment() {
    companion object {
        private val TAG: String? = SettingsNotificationFragment::class.simpleName
    }

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentSettingsNotificationsBinding? = null
    private val binding: FragmentSettingsNotificationsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsNotificationsBinding.inflate(inflater)
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
        binding.settingsNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        // refresh required data
        settingsViewModel.refreshNotificationsEnabled(requireContext())
        settingsViewModel.refreshNotificationsRiskEnabled()
        settingsViewModel.refreshNotificationsTestEnabled()
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
        updateRiskNotificationSwitch.setOnCheckedChangeListener { _, _ ->
            // Make sure that listener is called by user interaction
            if (updateRiskNotificationSwitch.tag != IGNORE_CHANGE_TAG) {
                settingsViewModel.toggleNotificationsRiskEnabled()
            }
        }
        // Additional click target to toggle switch
        updateRiskNotificationRow.setOnClickListener {
            if (updateRiskNotificationRow.isEnabled) settingsViewModel.toggleNotificationsRiskEnabled()
        }
        // Update Test
        updateTestNotificationSwitch.setOnCheckedChangeListener { _, _ ->
            // Make sure that listener is called by user interaction
            if (updateTestNotificationSwitch.tag != IGNORE_CHANGE_TAG) {
                settingsViewModel.toggleNotificationsTestEnabled()
            }
        }
        // Additional click target to toggle switch
        updateTestNotificationRow.setOnClickListener {
            if (updateTestNotificationRow.isEnabled) settingsViewModel.toggleNotificationsTestEnabled()
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
