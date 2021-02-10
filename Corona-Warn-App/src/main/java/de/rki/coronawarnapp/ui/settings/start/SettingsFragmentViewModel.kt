package de.rki.coronawarnapp.ui.settings.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SettingsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    notificationSettings: NotificationSettings,
    backgroundModeStatus: BackgroundModeStatus
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val tracingState: LiveData<SettingsTracingState> = tracingStatus.generalStatus
        .map { it.toSettingsTracingState() }
        .asLiveData(dispatcherProvider.Default)

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

    val data: Boolean = true

    val backgroundPriorityState: LiveData<SettingsBackgroundState> =
        backgroundModeStatus.isIgnoringBatteryOptimizations
            .map { SettingsBackgroundState((it)) }
            .asLiveData(dispatcherProvider.Default)

    // TODO Will be changed by @kolyaophale
    var analyticsEnabled = false

    var analyticsState: LiveData<SettingsPrivacyPreservingAnalyticsState> = MutableStateFlow(analyticsEnabled)
        .map { SettingsPrivacyPreservingAnalyticsState((it)) }
        .asLiveData(dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SettingsFragmentViewModel>
}
