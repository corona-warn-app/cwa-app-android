package de.rki.coronawarnapp.test.appconfig.ui

import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AppConfigTestFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider,
    private val testSettings: TestSettings
) : CWAViewModel(dispatcherProvider) {

    val currentConfig = appConfigProvider.currentConfig.asLiveData()
    val errorEvent = SingleLiveEvent<Exception>()
    val isDeviceTimeFaked = testSettings.fakeCorrectDeviceTime.asLiveData2()

    fun download() = launch {
        try {
            appConfigProvider.getAppConfig()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get app config.")
            errorEvent.postValue(e)
        }
    }

    fun clearConfig() = launch {
        appConfigProvider.reset()
    }

    fun toggleFakeCorrectDeviceTime() = launch {
        testSettings.updateFakeCorrectDeviceTime { !it }
    }
}
