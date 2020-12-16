package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.ui.states.LowRisk
import de.rki.coronawarnapp.tracing.ui.states.TracingDisabled
import de.rki.coronawarnapp.tracing.ui.states.TracingFailed
import de.rki.coronawarnapp.tracing.ui.states.TracingInProgress
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowErrorResetDialog
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowInteropDeltaOnboarding
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowTracingExplanation
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.items.faq.FAQCardVH
import de.rki.coronawarnapp.ui.main.home.items.testresult.FetchingResult
import de.rki.coronawarnapp.ui.main.home.items.testresult.NoTest
import de.rki.coronawarnapp.ui.main.home.items.testresult.SubmissionDone
import de.rki.coronawarnapp.ui.main.home.items.testresult.SubmissionStateProvider
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestError
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestErrorCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestFetchingCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestInvalid
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestInvalidCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestNegative
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestNegativeCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestPending
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestPendingCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestPositive
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestPositiveCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestReadyCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestResultReady
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestSubmissionDoneCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestUnregisteredCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.IncreasedRiskCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.LowRiskCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingDisabledCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingFailedCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingProgressCard
import de.rki.coronawarnapp.ui.tracing.card.TracingCardStateProvider
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val errorResetTool: EncryptionErrorResetTool,
    tracingStatus: GeneralTracingStatus,
    tracingCardStateProviderFactory: TracingCardStateProvider.Factory,
    submissionStateProvider: SubmissionStateProvider,
    val settingsViewModel: SettingsViewModel,
    private val tracingRepository: TracingRepository,
    private val testResultNotificationService: TestResultNotificationService,
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(
    dispatcherProvider = dispatcherProvider,
    childViewModels = listOf(settingsViewModel)
) {

    private val tracingCardStateProvider by lazy { tracingCardStateProviderFactory.create(isDetailsMode = false) }

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val openFAQUrlEvent = SingleLiveEvent<Unit>()

    val tracingHeaderState: LiveData<TracingHeaderState> = tracingStatus.generalStatus
        .map { it.toHeaderState() }
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

    private val tracingCardItems = tracingCardStateProvider.state.map { tracingState ->
        when (tracingState) {
            is TracingInProgress -> TracingProgressCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                }
            )
            is TracingDisabled -> TracingDisabledCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                },
                onEnableTracingClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
                }
            )
            is LowRisk -> LowRiskCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                },
                onUpdateClick = { refreshDiagnosisKeys() }
            )
            is IncreasedRisk -> IncreasedRiskCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                },
                onUpdateClick = { refreshDiagnosisKeys() }
            )
            is TracingFailed -> TracingFailedCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                },
                onRetryClick = { refreshDiagnosisKeys() }
            )
        }
    }

    private val submissionCardItems = submissionStateProvider.state.map { state ->
        when (state) {
            is NoTest -> TestUnregisteredCard.Item(state) {
                routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToSubmissionDispatcher())
            }
            is FetchingResult -> TestFetchingCard.Item(state)
            is TestResultReady -> TestReadyCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections.actionMainFragmentToSubmissionTestResultAvailableFragment()
                )
            }
            is TestPositive -> TestPositiveCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionResultPositiveOtherWarningNoConsentFragment()
                )
            }
            is TestNegative -> TestNegativeCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultNegativeFragment()
                )
            }
            is TestInvalid -> TestInvalidCard.Item(state) {
                popupEvents.postValue(HomeFragmentEvents.ShowDeleteTestDialog)
            }
            is TestError -> TestErrorCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultPendingFragment()
                )
            }
            is TestPending -> TestPendingCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultPendingFragment()
                )
            }
            is SubmissionDone -> TestSubmissionDoneCard.Item(state)
        }
    }

    val homeItems: LiveData<List<HomeItem>> = combine(
        tracingCardItems,
        submissionCardItems,
        submissionStateProvider.state
    ) { tracingItem, submissionItem, submissionState ->
        mutableListOf<HomeItem>().apply {
            if (submissionState !is SubmissionDone) {
                add(tracingItem)
            }

            add(submissionItem)

            add(FAQCardVH.Item(onClickAction = { openFAQUrlEvent.postValue(Unit) }))
        }
    }
        .distinctUntilChanged()
        .asLiveData(dispatcherProvider.Default)

    private var isLoweredRiskLevelDialogBeingShown = false
    fun observeTestResultToSchedulePositiveTestResultReminder() = launch {
        submissionRepository.deviceUIStateFlow
            .first { state ->
                state.withSuccess(false) {
                    when (it) {
                        DeviceUIState.PAIRED_POSITIVE, DeviceUIState.PAIRED_POSITIVE_TELETAN -> true
                        else -> false
                    }
                }
            }
            .also { testResultNotificationService.schedulePositiveTestResultReminder() }
    }

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
        submissionRepository.refreshDeviceUIState()
        // TODO the ordering here is weird, do we expect these to run in sequence?
        tracingRepository.refreshRiskLevel()
        tracingRepository.refreshActiveTracingDaysInRetentionPeriod()
    }

    fun tracingExplanationWasShown() {
        LocalData.tracingExplanationDialogWasShown(true)
    }

    fun refreshDiagnosisKeys() {
        tracingRepository.refreshDiagnosisKeys()
    }

    fun deregisterWarningAccepted() {
        submissionRepository.removeTestFromDevice()
        submissionRepository.refreshDeviceUIState()
    }

    fun userHasAcknowledgedTheLoweredRiskLevel() {
        isLoweredRiskLevelDialogBeingShown = false
        LocalData.isUserToBeNotifiedOfLoweredRiskLevel = false
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
