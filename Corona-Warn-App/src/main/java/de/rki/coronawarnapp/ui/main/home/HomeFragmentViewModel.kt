package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.testErrorsSingleEvent
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.coronatest.type.pcr.toSubmissionState
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.coronatest.type.rapidantigen.toSubmissionState
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestOutdatedCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.states.TracingFailed
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.states.TracingState
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
import de.rki.coronawarnapp.ui.main.home.items.CreateTraceLocationCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.items.IncompatibleCard
import de.rki.coronawarnapp.ui.presencetracing.organizer.TraceLocationOrganizerSettings
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.bluetooth.BluetoothSupport
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import de.rki.coronawarnapp.vaccination.core.VaccinationSettings
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@Suppress("LongParameterList")
class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    tracingStateProviderFactory: TracingStateProvider.Factory,
    coronaTestRepository: CoronaTestRepository,
    statisticsProvider: StatisticsProvider,
    vaccinationRepository: VaccinationRepository,
    private val errorResetTool: EncryptionErrorResetTool,
    private val tracingRepository: TracingRepository,
    private val submissionRepository: SubmissionRepository,
    private val cwaSettings: CWASettings,
    private val appConfigProvider: AppConfigProvider,
    private val appShortcutsHelper: AppShortcutsHelper,
    private val tracingSettings: TracingSettings,
    private val traceLocationOrganizerSettings: TraceLocationOrganizerSettings,
    private val timeStamper: TimeStamper,
    private val bluetoothSupport: BluetoothSupport,
    private val vaccinationSettings: VaccinationSettings,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private var isLoweredRiskLevelDialogBeingShown = false
    private val tracingStateProvider by lazy { tracingStateProviderFactory.create(isDetailsMode = false) }
    private val tracingCardItems = tracingStateProvider.state.map { tracingStateItem(it) }.distinctUntilChanged()

    val errorEvent = SingleLiveEvent<Throwable>()
    val events = SingleLiveEvent<HomeFragmentEvents>()

    val tracingHeaderState: LiveData<TracingHeaderState> = tracingStatus.generalStatus.map { it.toHeaderState() }
        .asLiveData(dispatcherProvider.Default)
    val coronaTestErrors = coronaTestRepository.testErrorsSingleEvent
        .asLiveData(dispatcherProvider.Default)

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

    val homeItems: LiveData<List<HomeItem>> = combine(
        tracingCardItems,
        coronaTestRepository.latestPCRT,
        coronaTestRepository.latestRAT,
        statisticsProvider.current.distinctUntilChanged(),
        appConfigProvider.currentConfig.map { it.coronaTestParameters }.distinctUntilChanged(),
        vaccinationRepository.vaccinationInfos
    ) { tracingItem, testPCR, testRAT, statsData, coronaTestParameters, vaccinatedPersons ->
        val statePCR = testPCR.toSubmissionState()
        val stateRAT = testRAT.toSubmissionState(timeStamper.nowUTC, coronaTestParameters)
        mutableListOf<HomeItem>().apply {
            when {
                statePCR is SubmissionStatePCR.TestPositive || statePCR is SubmissionStatePCR.SubmissionDone -> {
                    // Don't show risk card
                }
                stateRAT is SubmissionStateRAT.TestPositive || stateRAT is SubmissionStateRAT.SubmissionDone -> {
                    // Don't show risk card
                }
                else -> add(tracingItem)
            }

            if (bluetoothSupport.isAdvertisingSupported == false) {
                val scanningSupported = bluetoothSupport.isScanningSupported != false
                add(
                    IncompatibleCard.Item(
                        onClickAction = { events.postValue(HomeFragmentEvents.OpenIncompatibleUrl(scanningSupported)) },
                        bluetoothSupported = scanningSupported
                    )
                )
            }

            // TODO: Would be nice to have a more elegant solution of displaying the result cards in the right order
            when (statePCR) {
                SubmissionStatePCR.NoTest -> {
                    if (stateRAT == SubmissionStateRAT.NoTest) {
                        add(testPCR.toTestCardItem())
                    } else {
                        add(testRAT.toTestCardItem(coronaTestParameters))
                        add(testPCR.toTestCardItem())
                    }
                }
                else -> {
                    add(testPCR.toTestCardItem())
                    if (stateRAT != SubmissionStateRAT.NoTest) {
                        add(testRAT.toTestCardItem(coronaTestParameters))
                        add(
                            TestUnregisteredCard.Item(SubmissionStatePCR.NoTest) {
                                events.postValue(HomeFragmentEvents.GoToSubmissionDispatcher)
                            }
                        )
                    } else add(testRAT.toTestCardItem(coronaTestParameters))
                }
            }

            if (statsData.isDataAvailable) {
                add(
                    StatisticsHomeCard.Item(
                        data = statsData,
                        onHelpAction = {
                            events.postValue(HomeFragmentEvents.GoToStatisticsExplanation)
                        }
                    )
                )
            }

            add(
                CreateTraceLocationCard.Item(
                    onClickAction = {
                        events.postValue(
                            HomeFragmentEvents.OpenTraceLocationOrganizerGraph(
                                traceLocationOrganizerSettings.qrInfoAcknowledged
                            )
                        )
                    }
                )
            )

            add(FAQCard.Item(onClickAction = { events.postValue(HomeFragmentEvents.OpenFAQUrl) }))
        }
    }
        .distinctUntilChanged()
        .asLiveData(dispatcherProvider.Default)

    // TODO only lazy to keep tests going which would break because of LocalData access
    val showLoweredRiskLevelDialog: LiveData<Boolean> by lazy {
        tracingSettings
            .isUserToBeNotifiedOfLoweredRiskLevel
            .flow
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
            try {
                submissionRepository.refreshTest()
            } catch (e: CoronaTestNotFoundException) {
                Timber.e(e, "refreshTest failed")
                errorEvent.postValue(e)
            }
            tracingRepository.refreshRiskLevel()
        }
    }

    fun showPopUps() = launch {
        if (errorResetTool.isResetNoticeToBeShown) events.postValue(ShowErrorResetDialog)
        if (!cwaSettings.wasTracingExplanationDialogShown) events.postValue(ShowTracingExplanation)
    }

    fun restoreAppShortcuts() {
        launch {
            appShortcutsHelper.restoreAppShortcut()
        }
    }

    fun deregisterWarningAccepted(type: CoronaTest.Type) {
        submissionRepository.removeTestFromDevice(type)
    }

    fun userHasAcknowledgedTheLoweredRiskLevel() {
        isLoweredRiskLevelDialogBeingShown = false
        tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update { false }
    }

    fun userHasAcknowledgedIncorrectDeviceTime() {
        cwaSettings.wasDeviceTimeIncorrectAcknowledged = true
    }

    fun tracingExplanationWasShown() {
        cwaSettings.wasTracingExplanationDialogShown = true
    }

    private fun PCRCoronaTest?.toTestCardItem() = when (val state = this.toSubmissionState()) {
        is SubmissionStatePCR.NoTest -> TestUnregisteredCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToSubmissionDispatcher)
        }
        is SubmissionStatePCR.FetchingResult -> TestFetchingCard.Item(state)
        is SubmissionStatePCR.TestResultReady -> PcrTestReadyCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToTestResultAvailableFragment(PCR))
        }
        is SubmissionStatePCR.TestPositive -> PcrTestPositiveCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToTestResultPositiveFragment(PCR))
        }
        is SubmissionStatePCR.TestNegative -> PcrTestNegativeCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToPcrTestResultNegativeFragment(PCR))
        }
        is SubmissionStatePCR.TestInvalid -> PcrTestInvalidCard.Item(state) {
            events.postValue(HomeFragmentEvents.ShowDeleteTestDialog(PCR))
        }
        is SubmissionStatePCR.TestError -> PcrTestErrorCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToTestResultPendingFragment(PCR))
        }
        is SubmissionStatePCR.TestPending -> PcrTestPendingCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToTestResultPendingFragment(PCR, true))
        }
        is SubmissionStatePCR.SubmissionDone -> PcrTestSubmissionDoneCard.Item(state) {
            events.postValue(HomeFragmentEvents.GoToTestResultKeysSharedFragment(PCR))
        }
    }

    private fun RACoronaTest?.toTestCardItem(coronaTestConfig: CoronaTestConfig) =
        when (val state = this.toSubmissionState(timeStamper.nowUTC, coronaTestConfig)) {
            is SubmissionStateRAT.NoTest -> TestUnregisteredCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToSubmissionDispatcher)
            }
            is SubmissionStateRAT.FetchingResult -> TestFetchingCard.Item(state)
            is SubmissionStateRAT.TestResultReady -> RapidTestReadyCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToTestResultAvailableFragment(RAPID_ANTIGEN))
            }
            is SubmissionStateRAT.TestPositive -> RapidTestPositiveCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToTestResultPositiveFragment(RAPID_ANTIGEN))
            }
            is SubmissionStateRAT.TestNegative -> RapidTestNegativeCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToRapidTestResultNegativeFragment)
            }
            is SubmissionStateRAT.TestInvalid -> RapidTestInvalidCard.Item(state) {
                events.postValue(HomeFragmentEvents.ShowDeleteTestDialog(RAPID_ANTIGEN))
            }
            is SubmissionStateRAT.TestError -> RapidTestErrorCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToTestResultPendingFragment(RAPID_ANTIGEN))
            }
            is SubmissionStateRAT.TestPending -> RapidTestPendingCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToTestResultPendingFragment(RAPID_ANTIGEN, true))
            }
            is SubmissionStateRAT.TestOutdated -> RapidTestOutdatedCard.Item(state) {
                submissionRepository.removeTestFromDevice(RAPID_ANTIGEN)
            }
            is SubmissionStateRAT.SubmissionDone -> RapidTestSubmissionDoneCard.Item(state) {
                events.postValue(HomeFragmentEvents.GoToTestResultKeysSharedFragment(RAPID_ANTIGEN))
            }
        }

    private fun refreshRiskResult() {
        tracingRepository.refreshRiskResult()
    }

    private fun tracingStateItem(tracingState: TracingState) = when (tracingState) {
        is TracingInProgress -> TracingProgressCard.Item(
            state = tracingState,
            onCardClick = { events.postValue(HomeFragmentEvents.GoToRiskDetailsFragment) }
        )
        is TracingDisabled -> TracingDisabledCard.Item(
            state = tracingState,
            onCardClick = { events.postValue(HomeFragmentEvents.GoToRiskDetailsFragment) },
            onEnableTracingClick = { events.postValue(HomeFragmentEvents.GoToSettingsTracingFragment) }
        )
        is LowRisk -> LowRiskCard.Item(
            state = tracingState,
            onCardClick = { events.postValue(HomeFragmentEvents.GoToRiskDetailsFragment) },
            onUpdateClick = { refreshRiskResult() }
        )
        is IncreasedRisk -> IncreasedRiskCard.Item(
            state = tracingState,
            onCardClick = { events.postValue(HomeFragmentEvents.GoToRiskDetailsFragment) },
            onUpdateClick = { refreshRiskResult() }
        )
        is TracingFailed -> TracingFailedCard.Item(
            state = tracingState,
            onCardClick = { events.postValue(HomeFragmentEvents.GoToRiskDetailsFragment) },
            onRetryClick = { refreshRiskResult() }
        )
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
