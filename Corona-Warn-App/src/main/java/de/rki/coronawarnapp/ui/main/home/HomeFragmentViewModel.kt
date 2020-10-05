package de.rki.coronawarnapp.ui.main.home

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val errorResetTool: EncryptionErrorResetTool,
    val tracingViewModel: TracingViewModel,
    val settingsViewModel: SettingsViewModel,
    val submissionViewModel: SubmissionViewModel
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(tracingViewModel, settingsViewModel, submissionViewModel)
) {

    val showInteropDeltaOnboarding = SingleLiveEvent<Unit>()
    val showTracingExplanation = SingleLiveEvent<Long>()
    val showErrorResetDialog = SingleLiveEvent<Boolean>()

    init {
        if (!LocalData.isInteroperabilityShownAtLeastOnce) {
            showInteropDeltaOnboarding.postValue(Unit)
        } else {
            launch {
                if (!LocalData.tracingExplanationDialogWasShown()) {
                    showTracingExplanation.postValue(TimeVariables.getActiveTracingDaysInRetentionPeriod())
                }
            }
            launch {
                if (errorResetTool.isResetNoticeToBeShown) {
                    showErrorResetDialog.postValue(true)
                }
            }
        }
    }

    fun errorResetDialogDismissed() {
        errorResetTool.isResetNoticeToBeShown = false
    }

    fun refreshRequiredData() {
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        tracingViewModel.refreshIsTracingEnabled()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        TimerHelper.checkManualKeyRetrievalTimer()
        submissionViewModel.refreshDeviceUIState()
        tracingViewModel.refreshLastSuccessfullyCalculatedScore()
    }

    fun tracingExplanationWasShown() {
        LocalData.tracingExplanationDialogWasShown(true)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
