package de.rki.coronawarnapp.appconfig.devicetime

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.devicetime.ui.IncorrectDeviceTimeNotification
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTimeHandler @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val appConfigProvider: AppConfigProvider,
    private val notification: IncorrectDeviceTimeNotification,
    private val cwaSettings: CWASettings
) {

    fun launch() {
        appConfigProvider.currentConfig
            .onStart { Timber.tag(TAG).d("Observing device time.") }
            // If we don't delay emissions, we will consume the event before the UI "wasDeviceTimeIncorrectAcknowledged"
            // ForegroundState.isInForeground has a day when opening the app.
            .sample(5000)
            .onEach {
                Timber.tag(TAG).v("Current device time offset is: %dms", it.localOffset.millis)
                if (it.isDeviceTimeCorrect) {
                    Timber.v("Dismissing any notification, device time is correct again.")
                    notification.dismiss()
                } else {
                    if (cwaSettings.wasDeviceTimeIncorrectAcknowledged) {
                        Timber.d("Device time is incorrect, but user has already acknowledged it.")
                    } else {
                        Timber.i("Showing notification, device time is incorrect.")
                        // Notificaiton may not show if in foreground, then we don't want to consume the flag
                        // It could race the UI logic that wants to display a dialog.
                        if (notification.show()) {
                            cwaSettings.wasDeviceTimeIncorrectAcknowledged = true
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    companion object {
        private const val TAG = "DeviceTimeHandler"
    }
}
