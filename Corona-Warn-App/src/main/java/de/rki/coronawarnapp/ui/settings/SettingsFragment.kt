package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * This is the setting overview page.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val binding: FragmentSettingsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshIsTracingEnabled()
        settingsViewModel.refreshNotificationsEnabled()
        settingsViewModel.refreshNotificationsRiskEnabled()
        settingsViewModel.refreshNotificationsTestEnabled()

        binding.settingsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        val tracingRow = binding.settingsTracing.settingsRow
        val notificationRow = binding.settingsNotifications.settingsRow
        val resetRow = binding.settingsReset
        val goBack = binding.settingsHeader.headerButtonBack.buttonIcon
        resetRow.setOnClickListener {
            findNavController().doNavigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsResetFragment()
            )
        }
        tracingRow.setOnClickListener {
            findNavController().doNavigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsTracingFragment()
            )
        }
        notificationRow.setOnClickListener {
            findNavController().doNavigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsNotificationFragment()
            )
        }
        goBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
