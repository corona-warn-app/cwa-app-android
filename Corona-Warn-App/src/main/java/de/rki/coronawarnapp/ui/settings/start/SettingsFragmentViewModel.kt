package de.rki.coronawarnapp.ui.settings.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SettingsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    settingsRepository: SettingsRepository,
    notificationSettings: NotificationSettings
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val tracingState: LiveData<SettingsTracingState> = tracingStatus.generalStatus
        .map { it.toSettingsTracingState() }
        .asLiveData(dispatcherProvider.Default)

    // (settingsViewModel.isNotificationsEnabled(), settingsViewModel.isNotificationsRiskEnabled(), settingsViewModel.isNotificationsTestEnabled())}"
    val notificationState: LiveData<SettingsNotificationState> = combine(
        notificationSettings.isNotificationsEnabled,
        notificationSettings.isNotificationsRiskEnabled,
        notificationSettings.isNotificationsTestEnabled
    ) { args ->
        SettingsNotificationState(
            isNotificationsEnabled = args[0],
            isNotificationsRiskEnabled = args[1],
            isNotificationsTestEnabled = args[2]
        )
    }.asLiveData(dispatcherProvider.Default)

    val backgroundPrioritystate: LiveData<SettingsBackgroundState> =
        settingsRepository.isBackgroundPriorityEnabledFlow
            .map { SettingsBackgroundState((it)) }
            .asLiveData(dispatcherProvider.Default)

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsFragmentViewModel>
}
