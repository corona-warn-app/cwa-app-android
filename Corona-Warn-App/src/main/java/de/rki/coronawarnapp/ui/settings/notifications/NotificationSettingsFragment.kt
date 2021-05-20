package de.rki.coronawarnapp.ui.settings.notifications

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsNotificationsBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppNotificationSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This is the setting notification page. Here the user sees his os notifications settings status.
 * If os notifications are disabled he can navigate to them with one click. And if the os is enabled
 * the user can decide which notifications he wants to get: risk updates and/or test results.
 */
class NotificationSettingsFragment :
    Fragment(R.layout.fragment_settings_notifications),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: NotificationSettingsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSettingsNotificationsBinding by viewBinding()

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
        binding.settingsSwitchRowNotificationsRisk.setOnClickListener {
            vm.toggleNotificationsRiskEnabled()
        }
        binding.settingsSwitchRowNotificationsTest.setOnClickListener {
            vm.toggleNotificationsTestEnabled()
        }
        binding.settingsNotificationsHeader.setNavigationOnClickListener {
            popBackStack()
        }
        binding.settingsNotificationsCard.tracingStatusCardButton.setOnClickListener {
            requireContext().openAppNotificationSettings()
        }
    }
}
