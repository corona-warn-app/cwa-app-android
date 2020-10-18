package de.rki.coronawarnapp.ui.main.home

import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import timber.log.Timber
import javax.inject.Inject

class SubmissionCardsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val state: Flow<SubmissionCardState> = combine(
        SubmissionRepository.deviceUIStateFlow,
        SubmissionRepository.uiStateStateFlow
    ) { args ->
        SubmissionCardState(
            deviceUiState = args[0] as DeviceUIState,
            uiStateState = args[1] as ApiRequestState,
            isDeviceRegistered = LocalData.registrationToken() != null
        )
    }
        .onStart { Timber.v("SubmissionCardState FLOW start") }
        .onEach { Timber.d("SubmissionCardState FLOW PRE-SAMPLE emission: %s", it) }
        .onCompletion { Timber.v("SubmissionCardState FLOW completed.") }
        .sample(150L)
        .onEach { Timber.d("SubmissionCardState FLOW POST-SAMPLE emission: %s", it) }
}
