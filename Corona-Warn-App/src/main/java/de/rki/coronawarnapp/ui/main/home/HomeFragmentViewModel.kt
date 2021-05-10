package de.rki.coronawarnapp.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
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
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.vaccination.ui.homecards.CompleteVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecards.CreateVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecards.IncompleteVaccinationHomeCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@Suppress("LongParameterList")
class HomeFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val errorResetTool: EncryptionErrorResetTool,
    tracingStatus: GeneralTracingStatus,
    tracingStateProviderFactory: TracingStateProvider.Factory,
    private val coronaTestRepository: CoronaTestRepository,
    private val tracingRepository: TracingRepository,
    private val submissionRepository: SubmissionRepository,
    private val cwaSettings: CWASettings,
    private val appConfigProvider: AppConfigProvider,
    statisticsProvider: StatisticsProvider,
    private val appShortcutsHelper: AppShortcutsHelper,
    private val tracingSettings: TracingSettings,
    private val traceLocationOrganizerSettings: TraceLocationOrganizerSettings,
    private val timeStamper: TimeStamper,
    private val bluetoothSupport: BluetoothSupport,
    private val vaccinationRepository: VaccinationRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val tracingStateProvider by lazy { tracingStateProviderFactory.create(isDetailsMode = false) }

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val openFAQUrlEvent = SingleLiveEvent<Unit>()
    val openIncompatibleEvent = SingleLiveEvent<Unit>()
    val openTraceLocationOrganizerFlow = SingleLiveEvent<Unit>()
    val errorEvent = SingleLiveEvent<Throwable>()

    val tracingHeaderState: LiveData<TracingHeaderState> = tracingStatus.generalStatus
        .map { it.toHeaderState() }
        .asLiveData(dispatcherProvider.Default)

    val popupEvents = SingleLiveEvent<HomeFragmentEvents>()

    val coronaTestErrors = coronaTestRepository.testErrorsSingleEvent
        .asLiveData(context = dispatcherProvider.Default)

    fun showPopUps() {
        launch {
            if (errorResetTool.isResetNoticeToBeShown) {
                popupEvents.postValue(ShowErrorResetDialog)
            }
            if (!cwaSettings.wasTracingExplanationDialogShown) {
                popupEvents.postValue(ShowTracingExplanation)
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
                onUpdateClick = { refreshRiskResult() }
            )
            is IncreasedRisk -> IncreasedRiskCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                },
                onUpdateClick = { refreshRiskResult() }
            )
            is TracingFailed -> TracingFailedCard.Item(
                state = tracingState,
                onCardClick = {
                    routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment())
                },
                onRetryClick = { refreshRiskResult() }
            )
        }
    }.distinctUntilChanged()

    private fun PCRCoronaTest?.toTestCardItem() = when (val state = this.toSubmissionState()) {
        is SubmissionStatePCR.NoTest -> TestUnregisteredCard.Item(state) {
            routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToSubmissionDispatcher())
        }
        is SubmissionStatePCR.FetchingResult -> TestFetchingCard.Item(state)
        is SubmissionStatePCR.TestResultReady -> PcrTestReadyCard.Item(state) {
            routeToScreen.postValue(
                HomeFragmentDirections.actionMainFragmentToSubmissionTestResultAvailableFragment(CoronaTest.Type.PCR)
            )
        }
        is SubmissionStatePCR.TestPositive -> PcrTestPositiveCard.Item(state) {
            routeToScreen.postValue(
                HomeFragmentDirections
                    .actionMainFragmentToSubmissionResultPositiveOtherWarningNoConsentFragment(CoronaTest.Type.PCR)
            )
        }
        is SubmissionStatePCR.TestNegative -> PcrTestNegativeCard.Item(state) {
            routeToScreen.postValue(
                HomeFragmentDirections.actionMainFragmentToSubmissionTestResultNegativeFragment(CoronaTest.Type.PCR)
            )
        }
        is SubmissionStatePCR.TestInvalid -> PcrTestInvalidCard.Item(state) {
            popupEvents.postValue(HomeFragmentEvents.ShowDeleteTestDialog(CoronaTest.Type.PCR))
        }
        is SubmissionStatePCR.TestError -> PcrTestErrorCard.Item(state) {
            routeToScreen.postValue(
                HomeFragmentDirections
                    .actionMainFragmentToSubmissionTestResultPendingFragment(testType = CoronaTest.Type.PCR)
            )
        }
        is SubmissionStatePCR.TestPending -> PcrTestPendingCard.Item(state) {
            routeToScreen.postValue(
                HomeFragmentDirections
                    .actionMainFragmentToSubmissionTestResultPendingFragment(testType = CoronaTest.Type.PCR)
            )
        }
        is SubmissionStatePCR.SubmissionDone -> PcrTestSubmissionDoneCard.Item(state) {
            routeToScreen.postValue(
                HomeFragmentDirections
                    .actionMainFragmentToSubmissionTestResultKeysSharedFragment(CoronaTest.Type.PCR)
            )
        }
    }

    private fun RACoronaTest?.toTestCardItem(coronaTestConfig: CoronaTestConfig) =
        when (val state = this.toSubmissionState(timeStamper.nowUTC, coronaTestConfig)) {
            is SubmissionStateRAT.NoTest -> TestUnregisteredCard.Item(state) {
                routeToScreen.postValue(HomeFragmentDirections.actionMainFragmentToSubmissionDispatcher())
            }
            is SubmissionStateRAT.FetchingResult -> TestFetchingCard.Item(state)
            is SubmissionStateRAT.TestResultReady -> RapidTestReadyCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultAvailableFragment(CoronaTest.Type.RAPID_ANTIGEN)
                )
            }
            is SubmissionStateRAT.TestPositive -> RapidTestPositiveCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionResultPositiveOtherWarningNoConsentFragment(
                            CoronaTest.Type.RAPID_ANTIGEN
                        )
                )
            }
            is SubmissionStateRAT.TestNegative -> RapidTestNegativeCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionNegativeAntigenTestResultFragment()
                )
            }
            is SubmissionStateRAT.TestInvalid -> RapidTestInvalidCard.Item(state) {
                popupEvents.postValue(HomeFragmentEvents.ShowDeleteTestDialog(CoronaTest.Type.RAPID_ANTIGEN))
            }
            is SubmissionStateRAT.TestError -> RapidTestErrorCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultPendingFragment(
                            testType = CoronaTest.Type.RAPID_ANTIGEN
                        )
                )
            }
            is SubmissionStateRAT.TestPending -> RapidTestPendingCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultPendingFragment(
                            testType = CoronaTest.Type.RAPID_ANTIGEN
                        )
                )
            }
            is SubmissionStateRAT.TestOutdated -> RapidTestOutdatedCard.Item(state) {
                submissionRepository.removeTestFromDevice(type = CoronaTest.Type.RAPID_ANTIGEN)
            }
            is SubmissionStateRAT.SubmissionDone -> RapidTestSubmissionDoneCard.Item(state) {
                routeToScreen.postValue(
                    HomeFragmentDirections
                        .actionMainFragmentToSubmissionTestResultKeysSharedFragment(CoronaTest.Type.RAPID_ANTIGEN)
                )
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

            vaccinatedPersons.forEach {
                val card = when (it.vaccinationStatus) {
                    VaccinatedPerson.Status.COMPLETE -> CompleteVaccinationHomeCard.Item(
                        vaccinatedPerson = it,
                        onClickAction = {
                            // TODO
                        }
                    )
                    VaccinatedPerson.Status.INCOMPLETE -> IncompleteVaccinationHomeCard.Item(
                        vaccinatedPerson = it,
                        onClickAction = {
                            // TODO
                        }
                    )
                }
                add(card)
            }

            if (bluetoothSupport.isAdvertisingSupported == false) {
                add(
                    IncompatibleCard.Item(
                        onClickAction = { openIncompatibleEvent.postValue(Unit) },
                        bluetoothSupported = bluetoothSupport.isScanningSupported != false
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
                                routeToScreen.postValue(
                                    HomeFragmentDirections.actionMainFragmentToSubmissionDispatcher()
                                )
                            }
                        )
                    } else add(testRAT.toTestCardItem(coronaTestParameters))
                }
            }

            add(
                CreateVaccinationHomeCard.Item(
                    onClickAction = {
                        // TODO: implement in another PR
                        popupEvents.postValue(HomeFragmentEvents.VaccinationList)
                    }
                )
            )

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

            add(CreateTraceLocationCard.Item(onClickAction = { openTraceLocationOrganizerFlow.postValue(Unit) }))

            add(FAQCard.Item(onClickAction = { openFAQUrlEvent.postValue(Unit) }))
        }
    }
        .distinctUntilChanged()
        .asLiveData(dispatcherProvider.Default)

    private var isLoweredRiskLevelDialogBeingShown = false

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

    fun restoreAppShortcuts() {
        launch {
            appShortcutsHelper.restoreAppShortcut()
        }
    }

    private fun refreshRiskResult() {
        tracingRepository.refreshRiskResult()
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

    fun wasQRInfoWasAcknowledged() = traceLocationOrganizerSettings.qrInfoAcknowledged

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<HomeFragmentViewModel>
}
