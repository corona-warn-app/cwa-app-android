package de.rki.coronawarnapp.ui.settings

import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.tracing.disableTracingIfEnabled
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.reset.DataReset
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsResetViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dataReset: DataReset,
    private val shortcutsHelper: AppShortcutsHelper,
    private val enfClient: ENFClient,
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
                enfClient.disableTracingIfEnabled()
            } catch (apiException: ApiException) {
                apiException.report(ExceptionCategory.EXPOSURENOTIFICATION, TAG, null)
            }

            dataReset.clearAllLocalData()
            shortcutsHelper.disableAllShortcuts()
            clickEvent.postValue(SettingsEvents.GoToOnboarding)
        }
    }

    companion object {
        private val TAG = tag<SettingsResetFragment>()
    }
}
