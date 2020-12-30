package de.rki.coronawarnapp.tracing.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
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
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
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
    private val tracingRepository: TracingRepository
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
        riskLevelStorage.latestAndLastSuccessful,
        backgroundModeStatus.isAutoModeEnabled
    ) { status,
        riskLevelResults,
        isBackgroundJobEnabled ->

        val (latestCalc, _) = riskLevelResults.tryLatestResultsWithDefaults()

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

    fun refreshData() {
        launch {
            tracingRepository.refreshRiskLevel()
            tracingRepository.refreshActiveTracingDaysInRetentionPeriod()
        }
    }

    fun updateRiskDetails() {
        tracingRepository.refreshDiagnosisKeys()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TracingDetailsFragmentViewModel>
}
