package de.rki.coronawarnapp.ui.settings

import com.google.android.gms.common.api.ApiException
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.util.DataReset
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SettingsResetViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dataReset: DataReset,
    private val shortcutsHelper: AppShortcutsHelper,
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
                // TODO Remove static access
                val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
                // only stop tracing if it is currently enabled
                if (isTracingEnabled) {
                    InternalExposureNotificationClient.asyncStop()
                }
            } catch (apiException: ApiException) {
                apiException.report(
                    ExceptionCategory.EXPOSURENOTIFICATION,
                    TAG,
                    null
                )
            }

            dataReset.clearAllLocalData()
            shortcutsHelper.removeAppShortcut()
            clickEvent.postValue(SettingsEvents.GoToOnboarding)
        }
    }

    companion object {
        private val TAG: String? = SettingsResetFragment::class.simpleName
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SettingsResetViewModel>
}
