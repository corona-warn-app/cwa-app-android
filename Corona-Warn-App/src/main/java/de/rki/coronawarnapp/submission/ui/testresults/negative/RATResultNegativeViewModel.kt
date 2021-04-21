package de.rki.coronawarnapp.submission.ui.testresults.negative

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class RATResultNegativeViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider) {

    val timer = intervalFlow(1).map {
        timeSinceRegistration()
    }.asLiveData(context = dispatcherProvider.Default)

    private suspend fun timeSinceRegistration() {
        val hours = appConfigProvider.getAppConfig()
            .coronaTestParameters
            .coronaRapidAntigenTestParameters

        timeStamper.nowUTC
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATResultNegativeViewModel>
}
