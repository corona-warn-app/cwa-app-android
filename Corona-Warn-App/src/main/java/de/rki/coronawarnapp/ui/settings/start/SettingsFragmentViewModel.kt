package de.rki.coronawarnapp.ui.settings.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    notificationSettings: NotificationSettings,
    backgroundModeStatus: BackgroundModeStatus,
    analytics: Analytics
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val tracingState: LiveData<SettingsTracingState> = tracingStatus.generalStatus
        .map { it.toSettingsTracingState() }
        .asLiveData(dispatcherProvider.Default)

    val notificationSettingsState: LiveData<SettingsNotificationState> = notificationSettings
        .isNotificationsEnabled
        .map { SettingsNotificationState(it) }
        .asLiveData(dispatcherProvider.Default)

    val backgroundPriorityState: LiveData<SettingsBackgroundState> =
        backgroundModeStatus.isIgnoringBatteryOptimizations
            .map { SettingsBackgroundState(it) }
            .asLiveData(dispatcherProvider.Default)

    var analyticsState: LiveData<SettingsPrivacyPreservingAnalyticsState> =
        analytics.isAnalyticsEnabledFlow()
            .map { SettingsPrivacyPreservingAnalyticsState(it) }
            .asLiveData(dispatcherProvider.Default)
}
