package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsBackgroundPriorityBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * This is the setting background priority page. Here the user sees the background priority setting status.
 * If background priority is disabled it can be activated.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class SettingsBackgroundPriorityFragment :
    Fragment(R.layout.fragment_settings_background_priority) {

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val binding: FragmentSettingsBackgroundPriorityBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsViewModel = settingsViewModel
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsBackgroundPriorityContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        // refresh required data
        settingsViewModel.refreshBackgroundPriorityEnabled()
    }

    private fun setButtonOnClickListener() {
        val settingsRow = binding.settingsRowBackgroundPriority

        // enable background priority
        settingsRow.setOnClickListener {
            (requireActivity() as MainActivity).apply {
                startActivity(powerManagement.toBatteryOptimizationSettingsIntent)
            }
        }

        // explanatory card
        binding.settingsTracingStatusConnection.tracingStatusCardButton.setOnClickListener {
            (requireActivity() as MainActivity).apply {
                startActivity(powerManagement.toBatteryOptimizationSettingsIntent)
            }
        }

        // back navigation
        binding.settingsBackgroundPriorityHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
