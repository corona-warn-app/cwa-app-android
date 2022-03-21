package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultSubmissionUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionTestResultKeysSharedViewModel @AssistedInject constructor(
    private val submissionRepository: SubmissionRepository,
    private val recycledTestProvider: RecycledCoronaTestsProvider,
    @Assisted val testType: Type,
    @Assisted private val testIdentifier: TestIdentifier,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val submissionUiState: LiveData<TestResultSubmissionUIState> = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .map { test ->
            TestResultSubmissionUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    val showDeleteTestDialog = SingleLiveEvent<Unit>()

    val routeToScreen = SingleLiveEvent<NavDirections?>()

    fun onTestOpened() = launch {
        submissionRepository.setViewedTestResult(type = testType)
    }

    fun onShowDeleteTestDialog() {
        showDeleteTestDialog.postValue(Unit)
    }

    fun moveTestToRecycleBinStorage() = launch {
        recycledTestProvider.recycleCoronaTest(testIdentifier)
        routeToScreen.postValue(null)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultKeysSharedViewModel> {
        fun create(testType: Type, testIdentifier: TestIdentifier): SubmissionTestResultKeysSharedViewModel
    }
}
