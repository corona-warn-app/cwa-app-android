package de.rki.coronawarnapp.ui.submission.testresult.positive

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.TestResultUIState
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
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
    @Assisted val testType: Type,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    init {
        Timber.v("init() coronaTestType=%s", testType)
    }

    val uiState: LiveData<TestResultUIState> = submissionRepository.testForType(type = testType)
        .filterNotNull()
        .map { test ->
            TestResultUIState(coronaTest = test)
        }.asLiveData(context = Dispatchers.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    val showDeleteTestDialog = SingleLiveEvent<Unit>()

    fun onTestOpened() = launch {
        submissionRepository.setViewedTestResult(type = testType)
    }

    fun onShowDeleteTestDialog() {
        showDeleteTestDialog.postValue(Unit)
    }

    fun onDeleteTestConfirmed() {
        submissionRepository.removeTestFromDevice(type = testType)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionTestResultKeysSharedViewModel> {
        fun create(testType: Type): SubmissionTestResultKeysSharedViewModel
    }
}
