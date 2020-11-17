package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowErrorResetDialog
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowInteropDeltaOnboarding
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowTracingExplanation
import de.rki.coronawarnapp.ui.tracing.card.TracingCardState
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample

class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val errorResetTool: EncryptionErrorResetTool,
    tracingStatus: GeneralTracingStatus,
    tracingCardStateProvider: TracingCardStateProvider,
    private val submissionCardsStateProvider: SubmissionCardsStateProvider,
    val settingsViewModel: SettingsViewModel,
    private val tracingRepository: TracingRepository,
    private val testResultNotificationService: TestResultNotificationService
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(settingsViewModel)
) {

    val tracingHeaderState: LiveData<TracingHeaderState> = tracingStatus.generalStatus
        .map { it.toHeaderState() }
        .asLiveData(dispatcherProvider.Default)

    val tracingCardState: LiveData<TracingCardState> = tracingCardStateProvider.state
        .asLiveData(dispatcherProvider.Default)

    val submissionCardState: LiveData<SubmissionCardState> = submissionCardsStateProvider.state
        .sample(150L)
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

    private var isLoweredRiskLevelDialogBeingShown = false

    suspend fun observeTestResultToSchedulePositiveTestResultReminder() =
        submissionCardsStateProvider.state
            .first { it.isPositiveSubmissionCardVisible() }
            .also { testResultNotificationService.schedulePositiveTestResultReminder() }

    // TODO only lazy to keep tests going which would break because of LocalData access
    val showLoweredRiskLevelDialog: LiveData<Boolean> by lazy {
        LocalData
            .isUserToBeNotifiedOfLoweredRiskLevelFlow
            .map { shouldBeNotified ->
                val shouldBeShown = shouldBeNotified && !isLoweredRiskLevelDialogBeingShown
                if (shouldBeShown) {
                    isLoweredRiskLevelDialogBeingShown = true
                }
                shouldBeShown
            }
            .asLiveData(context = dispatcherProvider.Default)
    }

    fun errorResetDialogDismissed() {
        errorResetTool.isResetNoticeToBeShown = false
    }

    fun refreshRequiredData() {
        SubmissionRepository.refreshDeviceUIState()
        // TODO the ordering here is weird, do we expect these to run in sequence?
        tracingRepository.refreshRiskLevel()
        tracingRepository.refreshExposureSummary()
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

    fun removeTestPushed() {
        popupEvents.postValue(HomeFragmentEvents.ShowDeleteTestDialog)
    }

    fun deregisterWarningAccepted() {
        SubmissionService.deleteTestGUID()
        SubmissionService.deleteRegistrationToken()
        LocalData.isAllowedToSubmitDiagnosisKeys(false)
        LocalData.initialTestResultReceivedTimestamp(0L)
        SubmissionRepository.refreshDeviceUIState()
    }

    fun userHasAcknowledgedTheLoweredRiskLevel() {
        isLoweredRiskLevelDialogBeingShown = false
        LocalData.isUserToBeNotifiedOfLoweredRiskLevel = false
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
