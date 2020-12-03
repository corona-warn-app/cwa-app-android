package de.rki.coronawarnapp.ui.submission.testresult

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SubmissionTestResultNoConsentViewModel @AssistedInject constructor(
    val submissionRepository: SubmissionRepository
) : CWAViewModel() {

    private val showRedeemedTokenWarning = SingleLiveEvent<Unit>()
    private var wasRedeemedTokenErrorShown = false
    private val tokenErrorMutex = Mutex()

    val uiState: LiveData<TestResultUIState> = combineTransform(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.testResultReceivedDateFlow
    ) { deviceUiState, resultDate ->

        tokenErrorMutex.withLock {
            if (!wasRedeemedTokenErrorShown) {
                deviceUiState.withSuccess {
                    if (it == DeviceUIState.PAIRED_REDEEMED) {
                        wasRedeemedTokenErrorShown = true
                        showRedeemedTokenWarning.postValue(Unit)
                    }
                }
            }
        }

        TestResultUIState(
            deviceUiState = deviceUiState,
            testResultReceivedDate = resultDate
        ).let { emit(it) }
    }.asLiveData(context = Dispatchers.Default)

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestResultNoConsentViewModel>
}
