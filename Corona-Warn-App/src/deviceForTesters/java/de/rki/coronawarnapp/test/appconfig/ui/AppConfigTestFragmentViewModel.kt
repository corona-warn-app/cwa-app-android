package de.rki.coronawarnapp.test.appconfig.ui

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.sample

class AppConfigTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val currentConfig = appConfigProvider.appConfig
        .sample(250L)
        .asLiveData()

    fun forceDownload() {
        launch { appConfigProvider.forceUpdate() }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<AppConfigTestFragmentViewModel>
}
