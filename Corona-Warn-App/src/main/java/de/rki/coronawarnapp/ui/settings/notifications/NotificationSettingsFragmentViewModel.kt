package de.rki.coronawarnapp.ui.settings.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.common.PresenceTracingNotifications
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    notificationSettings: NotificationSettings,
    private val generalNotifications: GeneralNotifications,
    private val presenceTracingNotifications: PresenceTracingNotifications,
    private val digitalCovidCertificateNotifications: DigitalCovidCertificateNotifications,
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider
) {

    val notificationSettingsState: LiveData<NotificationSettingsState> = notificationSettings
        .isNotificationsEnabled
        .map { NotificationSettingsState(it) }
        .asLiveData(dispatcherProvider.Default)

    fun createNotificationChannels() {
        generalNotifications.setupNotificationChannel()
        presenceTracingNotifications.setupChannel()
        digitalCovidCertificateNotifications.setupChannel()
    }
}
