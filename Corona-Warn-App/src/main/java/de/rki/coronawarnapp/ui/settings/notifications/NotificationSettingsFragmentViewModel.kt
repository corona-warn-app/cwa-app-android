package de.rki.coronawarnapp.ui.settings.notifications

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.common.PresenceTracingNotifications
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.hasAPILevel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class NotificationSettingsFragmentViewModel @AssistedInject constructor(
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
        if (BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.O)) {
            generalNotifications.setupNotificationChannel()
            presenceTracingNotifications.setupChannel()
            digitalCovidCertificateNotifications.setupChannel()
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<NotificationSettingsFragmentViewModel>
}
