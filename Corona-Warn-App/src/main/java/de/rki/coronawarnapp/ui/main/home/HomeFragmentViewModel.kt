package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.ui.homecards.FetchingResult
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionDone
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.submission.ui.homecards.TestError
import de.rki.coronawarnapp.submission.ui.homecards.TestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalid
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.TestNegative
import de.rki.coronawarnapp.submission.ui.homecards.TestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPending
import de.rki.coronawarnapp.submission.ui.homecards.TestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPositive
import de.rki.coronawarnapp.submission.ui.homecards.TestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.TestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.TestResultReady
import de.rki.coronawarnapp.submission.ui.homecards.TestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.states.TracingFailed
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.homecards.IncreasedRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingDisabledCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingFailedCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingProgressCard
import de.rki.coronawarnapp.tracing.ui.statusbar.TracingHeaderState
import de.rki.coronawarnapp.tracing.ui.statusbar.toHeaderState
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowErrorResetDialog
import de.rki.coronawarnapp.ui.main.home.HomeFragmentEvents.ShowTracingExplanation
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.items.ReenableRiskCard
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@Suppress("LongParameterList")
class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val errorResetTool: EncryptionErrorResetTool,
    tracingStatus: GeneralTracingStatus,
    tracingStateProviderFactory: TracingStateProvider.Factory,
    submissionStateProvider: SubmissionStateProvider,
    private val tracingRepository: TracingRepository,
    private val shareTestResultNotificationService: ShareTestResultNotificationService,
    private val submissionRepository: SubmissionRepository,
    private val cwaSettings: CWASettings,
    appConfigProvider: AppConfigProvider,
    statisticsProvider: StatisticsProvider,
    private val deadmanNotificationScheduler: DeadmanNotificationScheduler,
    private val appShortcutsHelper: AppShortcutsHelper
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val tracingStateProvider by lazy { tracingStateProviderFactory.create(isDetailsMode = false) }

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val openFAQUrlEvent = SingleLiveEvent<Unit>()

    val tracingHeaderState: LiveData<TracingHeaderState> = tracingStatus.generalStatus
        .map { it.toHeaderState() }
        .asLiveData(dispatcherProvider.Default)

    val popupEvents = SingleLiveEvent<HomeFragmentEvents>()

    fun showPopUps() {
        launch {
            if (!LocalData.tracingExplanationDialogWasShown()) {
                popupEvents.postValue(
                    ShowTracingExplanation(
                        TimeVariables.getActiveTracingDaysInRetentionPeriod()
                    )
                )
            }
        }
        launch {
            if (errorResetTool.isResetNoticeToBeShown) {
                popupEvents.postValue(ShowErrorResetDialog)
            }
        }
    }

    val showIncorrectDeviceTimeDialog by lazy {
        var wasDeviceTimeDialogShown = false
        SingleLiveEvent<Boolean>().also { singleLiveEvent ->
            appConfigProvider.currentConfig.map { it.isDeviceTimeCorrect }.onEach { isDeviceTimeCorrect ->
                if (isDeviceTimeCorrect) {
                    singleLiveEvent.postValue(false)
                    wasDeviceTimeDialogShown = false
                } else if (!wasDeviceTimeDialogShown && !cwaSettings.wasDeviceTimeIncorrectAcknowledged) {
                    singleLiveEvent.postValue(true)
                    wasDeviceTimeDialogShown = true
                }
            }.launchInViewModel()
        }
    }
    private val tracingCardItems = tracingStateProvider.state.map { tracingState ->
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
    }.distinctUntilChanged()

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
    }.distinctUntilChanged()

    val homeItems: LiveData<List<HomeItem>> = combine(
        tracingCardItems,
        submissionCardItems,
        submissionStateProvider.state.distinctUntilChanged(),
        statisticsProvider.current.distinctUntilChanged()
    ) { tracingItem, submissionItem, submissionState, statsData ->
        mutableListOf<HomeItem>().apply {
            when (submissionState) {
                TestPositive, is SubmissionDone -> {
                    // Don't show risk card
                }
                else -> add(tracingItem)
            }

            add(submissionItem)

            if (submissionState is SubmissionDone) {
                add(
                    ReenableRiskCard.Item(
                        state = submissionState,
                        onClickAction = { popupEvents.postValue(HomeFragmentEvents.ShowReactivateRiskCheckDialog) }
                    )
                )
            }

            if (statsData.isDataAvailable) {
                add(
                    StatisticsHomeCard.Item(
                        data = statsData,
                        onHelpAction = {
                            popupEvents.postValue(HomeFragmentEvents.GoToStatisticsExplanation)
                        }
                    )
                )
            }

            add(FAQCard.Item(onClickAction = { openFAQUrlEvent.postValue(Unit) }))
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
            .also { shareTestResultNotificationService.scheduleSharePositiveTestResultReminder() }
    }

    fun reenableRiskCalculation() {
        deregisterWarningAccepted()
        deadmanNotificationScheduler.schedulePeriodic()
        refreshDiagnosisKeys()
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
        launch {
            submissionRepository.refreshDeviceUIState()
            tracingRepository.refreshRiskLevel()
            tracingRepository.refreshActiveTracingDaysInRetentionPeriod()
        }
    }

    fun restoreAppShortcuts() {
        launch {
            appShortcutsHelper.restoreAppShortcut()
        }
    }

    fun tracingExplanationWasShown() {
        LocalData.tracingExplanationDialogWasShown(true)
    }

    private fun refreshDiagnosisKeys() {
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

    fun userHasAcknowledgedIncorrectDeviceTime() {
        cwaSettings.wasDeviceTimeIncorrectAcknowledged = true
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
