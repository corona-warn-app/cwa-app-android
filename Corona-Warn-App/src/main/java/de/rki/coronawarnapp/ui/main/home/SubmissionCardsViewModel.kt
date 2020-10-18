package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

class SubmissionCardsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val state: LiveData<SubmissionCardState> = combine(
        SubmissionRepository.deviceUIStateFlow,
        flow { LocalData.registrationToken() != null },
        SubmissionRepository.testResultReceivedDateFlow,
        SubmissionRepository.uiStateStateFlow
    ) { args ->
        SubmissionCardState(
            deviceUiState = args[0] as DeviceUIState,
            isDeviceRegistered = args[1] as Boolean,
            uiStateState = args[3] as ApiRequestState
        )
    }
        .onStart { Timber.v("SubmissionCardState FLOW start") }
        .onEach { Timber.d("SubmissionCardState FLOW emission: %s", it) }
        .onCompletion { Timber.v("SubmissionCardState FLOW completed.") }
        .asLiveData(dispatcherProvider.Default)
}
