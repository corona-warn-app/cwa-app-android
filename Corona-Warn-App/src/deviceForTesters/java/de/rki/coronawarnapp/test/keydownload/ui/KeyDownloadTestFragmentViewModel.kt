package de.rki.coronawarnapp.test.keydownload.ui

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.diagnosiskeys.download.KeyPackageSyncSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class KeyDownloadTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val syncSettings: KeyPackageSyncSettings,
    private val appConfigProvider: AppConfigProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val allowMeteredConnections = syncSettings.allowMeteredConnections.flow.asLiveData()

    fun toggleAllowMeteredConnections() {
        syncSettings.allowMeteredConnections.update { !it }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<KeyDownloadTestFragmentViewModel>
}
