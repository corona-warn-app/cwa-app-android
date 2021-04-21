package de.rki.coronawarnapp.submission.ui.testresults.negative

import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.lang.Exception

class RATResultNegativeViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val appConfigProvider: AppConfigProvider,
    private val timeStamper: TimeStamper,
    private val submissionRepository: SubmissionRepository,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<RATResultNegativeNavigation>()

    val timer = intervalFlow(1).map {
        timeSinceRegistration()
    }.asLiveData(context = dispatcherProvider.Default)

    private suspend fun timeSinceRegistration() {
        val hours = appConfigProvider.getAppConfig()
            .coronaTestParameters
            .coronaRapidAntigenTestParameters

        timeStamper.nowUTC
    }

    fun deleteTest() {
        try {
            Timber.d("deleteTest")
            submissionRepository.removeTestFromDevice(CoronaTest.Type.RAPID_ANTIGEN)
            events.postValue(RATResultNegativeNavigation.Back)
        } catch (e: Exception) {
            Timber.d(e, "Failed to delete rapid antigen test")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RATResultNegativeViewModel>
}
