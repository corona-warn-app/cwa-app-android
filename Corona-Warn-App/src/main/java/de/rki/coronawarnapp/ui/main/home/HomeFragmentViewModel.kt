package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowErrorResetDialog
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowInteropDeltaOnboarding
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowTracingExplanation
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardViewModel
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map

class HomeFragmentViewModel @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val errorResetTool: EncryptionErrorResetTool,
    tracingStatus: GeneralTracingStatus,
    tracingCardViewModel: TracingCardViewModel,
    submissionCardsViewModel: SubmissionCardsViewModel,
    val settingsViewModel: SettingsViewModel,
    private val tracingRepository: TracingRepository
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(
        tracingCardViewModel,
        submissionCardsViewModel, settingsViewModel
    )
) {

    val tracingHeaderState: LiveData<TracingHeaderState> = tracingStatus.generalStatus
        .map { it.toHeaderState() }
        .asLiveData(dispatcherProvider.Default)

    val tracingCardState: LiveData<TracingCardState> = tracingCardViewModel.state

    val submissionCardState: LiveData<SubmissionCardState> = submissionCardsViewModel.state
        .asLiveData(dispatcherProvider.Default)

    val popupEvents: SingleLiveEvent<HomeFragmentEvents> by lazy {
        SingleLiveEvent<HomeFragmentEvents>().apply {
            if (!LocalData.isInteroperabilityShownAtLeastOnce) {
                postValue(ShowInteropDeltaOnboarding)
            } else {
                launch {
                    if (!LocalData.tracingExplanationDialogWasShown()) {
                        postValue(
                            ShowTracingExplanation(
                                TimeVariables.getActiveTracingDaysInRetentionPeriod()
                            )
                        )
                    }
                }
                launch {
                    if (errorResetTool.isResetNoticeToBeShown) {
                        postValue(ShowErrorResetDialog)
                    }
                }
            }
        }
    }

    fun errorResetDialogDismissed() {
        errorResetTool.isResetNoticeToBeShown = false
    }

    fun refreshRequiredData() {
        SubmissionRepository.refreshDeviceUIState()
        // TODO the ordering here is weird, do we expect these to run in sequence?
        tracingRepository.refreshRiskLevel()
        tracingRepository.refreshExposureSummary()
        tracingRepository.refreshLastTimeDiagnosisKeysFetchedDate()
        tracingRepository.refreshActiveTracingDaysInRetentionPeriod()
        TimerHelper.checkManualKeyRetrievalTimer()
        tracingRepository.refreshLastSuccessfullyCalculatedScore()
    }

    fun tracingExplanationWasShown() {
        LocalData.tracingExplanationDialogWasShown(true)
    }

    fun refreshDiagnosisKeys() {
        tracingRepository.refreshDiagnosisKeys()
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<HomeFragmentViewModel> {
        fun create(handle: SavedStateHandle): HomeFragmentViewModel
    }
}
