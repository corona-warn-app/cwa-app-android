package de.rki.coronawarnapp.ui.main.home

import dagger.Reusable
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

@Reusable
class SubmissionCardsStateProvider @Inject constructor(
    submissionRepository: SubmissionRepository
) {

    val state: Flow<SubmissionCardState> = combine(
        submissionRepository.deviceUIStateFlow,
        submissionRepository.hasViewedTestResult
    ) { uiState, hasTestBeenSeen ->
        SubmissionCardState(
            deviceUiState = uiState,
            isDeviceRegistered = LocalData.registrationToken() != null,
            hasTestResultBeenSeen = hasTestBeenSeen
        )
    }
        .onStart { Timber.v("SubmissionCardState FLOW start") }
        .onEach { Timber.d("SubmissionCardState FLOW emission: %s", it) }
        .onCompletion { Timber.v("SubmissionCardState FLOW completed.") }
}
