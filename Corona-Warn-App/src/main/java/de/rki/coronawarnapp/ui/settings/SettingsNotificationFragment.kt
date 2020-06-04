package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentSettingsNotificationsBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.SettingsNavigationHelper

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
    private lateinit var binding: FragmentSettingsNotificationsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsNotificationsBinding.inflate(inflater)
        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        settingsViewModel.refreshNotificationsEnabled(requireContext())
        settingsViewModel.refreshNotificationsRiskEnabled()
        settingsViewModel.refreshNotificationsTestEnabled()
    }

    private fun setButtonOnClickListener() {
        // Notifications about risk status
        val updateRiskNotificationSwitch =
            binding.settingsSwitchRowNotificationsRisk.settingsSwitchRowSwitch
        // Notifications about test status
        val updateTestNotificationSwitch =
            binding.settingsSwitchRowNotificationsTest.settingsSwitchRowSwitch
        // Settings
        val settingsRow = binding.settingsNavigationRowSystem.navigationRow
        val goBack =
            binding.settingsNotificationsHeader.headerButtonBack.buttonIcon
        // Update Risk
        updateRiskNotificationSwitch.setOnCheckedChangeListener { _, _ ->
            // android calls this listener also on start, so it has to be verified if the user pressed the switch
            if (updateRiskNotificationSwitch.isPressed) {
                settingsViewModel.toggleNotificationsRiskEnabled()
            }
        }
        // Update Test
        updateTestNotificationSwitch.setOnCheckedChangeListener { _, _ ->
            // android calls this listener also on start, so it has to be verified if the user pressed the switch
            if (updateTestNotificationSwitch.isPressed) {
                settingsViewModel.toggleNotificationsTestEnabled()
            }
        }
        goBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        // System Settings
        settingsRow.setOnClickListener {
            SettingsNavigationHelper.toNotifications(requireContext())
        }
    }
}
