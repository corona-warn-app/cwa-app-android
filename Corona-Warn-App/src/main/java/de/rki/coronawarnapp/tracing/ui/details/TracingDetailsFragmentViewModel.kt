package de.rki.coronawarnapp.tracing.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.datadonation.survey.Surveys.ConsentResult.AlreadyGiven
import de.rki.coronawarnapp.datadonation.survey.Surveys.ConsentResult.Needed
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.states.TracingFailed
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.states.TracingStateProvider
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.tracing.ui.details.items.risk.IncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.LowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingDisabledBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingFailedBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingProgressBox
import de.rki.coronawarnapp.tracing.ui.details.items.survey.UserSurveyBox
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class TracingDetailsFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    riskLevelStorage: RiskLevelStorage,
    tracingDetailsItemProvider: TracingDetailsItemProvider,
    tracingStateProviderFactory: TracingStateProvider.Factory,
    private val tracingRepository: TracingRepository,
    private val surveys: Surveys
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val tracingStateProvider by lazy { tracingStateProviderFactory.create(isDetailsMode = true) }

    private val tracingCardItems = tracingStateProvider.state.map { tracingState ->
        when (tracingState) {
            is TracingInProgress -> TracingProgressBox.Item(state = tracingState)
            is TracingDisabled -> TracingDisabledBox.Item(state = tracingState)
            is LowRisk -> LowRiskBox.Item(state = tracingState)
            is IncreasedRisk -> IncreasedRiskBox.Item(state = tracingState)
            is TracingFailed -> TracingFailedBox.Item(state = tracingState)
        }
    }

    val detailsItems: LiveData<List<DetailsItem>> = combine(
        tracingCardItems,
        tracingDetailsItemProvider.state
    ) { tracingItem, details ->
        mutableListOf<DetailsItem>().apply {
            add(tracingItem)
            addAll(details)
        }
    }
        .distinctUntilChanged()
        .asLiveData(dispatcherProvider.Default)

    val buttonStates: LiveData<TracingDetailsState> = combine(
        tracingStatus.generalStatus,
        riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult,
        backgroundModeStatus.isAutoModeEnabled
    ) { status,
        riskLevelResults,
        isBackgroundJobEnabled ->

        val latestCalc = riskLevelResults.lastCalculated

        val isRestartButtonEnabled = !isBackgroundJobEnabled || latestCalc.riskState == RiskState.CALCULATION_FAILED

        TracingDetailsState(
            tracingStatus = status,
            riskState = latestCalc.riskState,
            isManualKeyRetrievalEnabled = isRestartButtonEnabled
        )
    }
        .onStart { Timber.v("TracingDetailsState FLOW start") }
        .onEach { Timber.d("TracingDetailsState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingDetailsState FLOW completed.") }
        .asLiveData(dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<TracingDetailsNavigationEvents> = SingleLiveEvent()

    fun refreshData() {
        launch {
            tracingRepository.attemptDiagnosisKeyDownloadAndEwRiskCalculation()
        }
    }

    fun updateRiskDetails() {
        tracingRepository.triggerRiskCalculation()
    }

    fun onItemClicked(item: DetailsItem) {
        when (item) {
            is UserSurveyBox.Item ->
                launch {
                    when (val consentResult = surveys.isConsentNeeded(Surveys.Type.HIGH_RISK_ENCOUNTER)) {
                        is Needed -> routeToScreen.postValue(
                            TracingDetailsNavigationEvents.NavigateToSurveyConsentFragment(
                                item.type
                            )
                        )
                        is AlreadyGiven -> routeToScreen.postValue(
                            TracingDetailsNavigationEvents.NavigateToSurveyUrlInBrowser(
                                consentResult.surveyLink
                            )
                        )
                    }
                }
        }
    }

    fun onInfoItemClicked(item: InfoItem) {
        when (item) {
            InfoItem.HYGIENE_RULES -> routeToScreen.postValue(TracingDetailsNavigationEvents.NavigateToHygieneRules)
            InfoItem.HOME_RULES -> routeToScreen.postValue(TracingDetailsNavigationEvents.NavigateToHomeRules)
        }
    }

    enum class InfoItem {
        HYGIENE_RULES,
        HOME_RULES
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TracingDetailsFragmentViewModel>
}
