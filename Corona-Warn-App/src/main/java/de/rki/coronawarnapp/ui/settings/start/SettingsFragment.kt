package de.rki.coronawarnapp.ui.settings.start

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
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This is the setting overview page.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class SettingsFragment : Fragment(R.layout.fragment_settings), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val binding: FragmentSettingsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.tracingState.observe2(this) {
            binding.tracingState = it
        }
        vm.notificationState.observe2(this) {
            binding.notificationState = it
        }
        vm.backgroundPrioritystate.observe2(this) {
            binding.backgroundState = it
        }
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        settingsViewModel.refreshNotificationsEnabled()
        settingsViewModel.refreshNotificationsRiskEnabled()
        settingsViewModel.refreshNotificationsTestEnabled()
        settingsViewModel.refreshBackgroundPriorityEnabled()

        binding.settingsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        val tracingRow = binding.settingsTracing.settingsRow
        val notificationRow = binding.settingsNotifications.settingsRow
        val backgroundPriorityRow = binding.settingsBackgroundPriority.settingsRow
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
        backgroundPriorityRow.setOnClickListener {
            findNavController().doNavigate(
                SettingsFragmentDirections.actionSettingsFragmentToSettingsBackgroundPriorityFragment()
            )
        }
        goBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
