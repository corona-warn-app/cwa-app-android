package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultKeysSharedViewModel @AssistedInject constructor(
    private val recycledTestProvider: RecycledCoronaTestsProvider,
    @Assisted private val testIdentifier: TestIdentifier,
    private val coronaTestProvider: CoronaTestProvider,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestIdentifier=%s", testIdentifier)
    }

    private val coronaTestFlow = coronaTestProvider.getTestForIdentifier(testIdentifier).filterNotNull()
    val uiState: LiveData<TestResultUIState> = coronaTestFlow
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    val showDeleteTestDialog = SingleLiveEvent<Unit>()

    val routeToScreen = SingleLiveEvent<Unit>()

    fun onTestOpened() = launch {
        coronaTestProvider.setTestAsViewed(coronaTestFlow.first())
    }

    fun onShowDeleteTestDialog() {
        showDeleteTestDialog.postValue(Unit)
    }

    fun moveTestToRecycleBinStorage() = launch {
        recycledTestProvider.recycleCoronaTest(testIdentifier)
        routeToScreen.postValue(Unit)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultKeysSharedViewModel> {
        fun create(testIdentifier: TestIdentifier): SubmissionTestResultKeysSharedViewModel
    }
}
