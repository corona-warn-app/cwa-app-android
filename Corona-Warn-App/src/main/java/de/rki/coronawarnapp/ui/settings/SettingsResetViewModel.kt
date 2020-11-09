package de.rki.coronawarnapp.ui.settings

import com.google.android.gms.common.api.ApiException
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.notification.NotificationConstants.POSITIVE_RESULT_NOTIFICATION_ID
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.DataReset
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler

class SettingsResetViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dataReset: DataReset
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val clickEvent: SingleLiveEvent<SettingsEvents> = SingleLiveEvent()

    fun resetAllData() {
        clickEvent.postValue(SettingsEvents.ResetApp)
    }

    fun goBack() {
        clickEvent.postValue(SettingsEvents.GoBack)
    }

    fun deleteAllAppContent() {
        launch {
            try {
                val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
                // only stop tracing if it is currently enabled
                if (isTracingEnabled) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                }
            } catch (apiException: ApiException) {
                apiException.report(
                    ExceptionCategory.EXPOSURENOTIFICATION, TAG, null
                )
            }
            resetPositiveTestResultNotification()

            dataReset.clearAllLocalData()
            clickEvent.postValue(SettingsEvents.GoToOnboarding)
        }
    }

    private fun resetPositiveTestResultNotification() {
        NotificationHelper.cancelFutureNotifications(POSITIVE_RESULT_NOTIFICATION_ID)
        LocalData.numberOfRemainingPositiveTestResultReminders = Int.MIN_VALUE
    }

    companion object {
        private val TAG: String? = SettingsResetFragment::class.simpleName
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsResetViewModel>
}
