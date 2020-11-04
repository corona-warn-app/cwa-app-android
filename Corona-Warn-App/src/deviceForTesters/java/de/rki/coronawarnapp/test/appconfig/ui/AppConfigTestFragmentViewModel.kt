package de.rki.coronawarnapp.test.appconfig.ui

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class AppConfigTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val currentConfig = appConfigProvider.currentConfig.asLiveData()
    val errorEvent = SingleLiveEvent<Exception>()

    fun download() {
        launch {
            try {
                appConfigProvider.getAppConfig()
            } catch (e: Exception) {
                Timber.e(e, "Failed to get app config.")
                errorEvent.postValue(e)
            }
        }
    }

    fun clearConfig() {
        launch {
            appConfigProvider.clear()
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<AppConfigTestFragmentViewModel>
}
