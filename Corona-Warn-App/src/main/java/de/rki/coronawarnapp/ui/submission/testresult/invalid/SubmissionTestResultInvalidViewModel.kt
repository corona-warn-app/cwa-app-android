package de.rki.coronawarnapp.ui.submission.testresult.invalid

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestProvider
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultInvalidViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val recycledTestProvider: RecycledCoronaTestsProvider,
    @Assisted private val testIdentifier: TestIdentifier,
    private val coronaTestProvider: CoronaTestProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() testIdentifier=%s", testIdentifier)
    }

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    val testResult: LiveData<TestResultUIState> = coronaTestProvider.getTestForIdentifier(testIdentifier)
        .filterNotNull()
        .map { test ->
            coronaTestProvider.setTestAsViewed(test)
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = dispatcherProvider.Default)

    fun moveTestToRecycleBinStorage() = launch {
        recycledTestProvider.recycleCoronaTest(testIdentifier)
        routeToScreen.postValue(null)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultInvalidViewModel> {
        fun create(testIdentifier: TestIdentifier): SubmissionTestResultInvalidViewModel
    }
}
