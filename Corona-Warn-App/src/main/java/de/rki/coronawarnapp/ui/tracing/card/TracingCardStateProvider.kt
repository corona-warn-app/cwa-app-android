package de.rki.coronawarnapp.ui.tracing.card

import dagger.Reusable
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.ui.tracing.common.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.util.BackgroundModeStatus
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TracingCardStateProvider @Inject constructor(
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage
) {

    val state: Flow<TracingCardState> = combine(
        tracingStatus.generalStatus.onEach {
            Timber.v("tracingStatus: $it")
        },
        tracingRepository.tracingProgress.onEach {
            Timber.v("tracingProgress: $it")
        },
        riskLevelStorage.riskLevelResults.onEach {
            Timber.v("riskLevelResults: $it")
        },
        tracingRepository.activeTracingDaysInRetentionPeriod.onEach {
            Timber.v("activeTracingDaysInRetentionPeriod: $it")
        },
        tracingRepository.lastTimeDiagnosisKeysFetched.onEach {
            Timber.v("lastTimeDiagnosisKeysFetched: $it")
        },
        backgroundModeStatus.isAutoModeEnabled.onEach {
            Timber.v("isAutoModeEnabled: $it")
        }
    ) { status,
        tracingProgress,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched,
        isBackgroundJobEnabled ->

        val (latestCalc, latestSuccessfulCalc) = riskLevelResults.tryLatestResultsWithDefaults(

        )

        TracingCardState(
            tracingStatus = status,
            riskState = latestCalc.riskState,
            tracingProgress = tracingProgress,
            lastSuccessfulRiskState = latestSuccessfulCalc.riskState,
            lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
            daysWithEncounters = latestCalc.daysWithEncounters,
            lastEncounterAt = latestCalc.lastRiskEncounterAt,
            activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
            isManualKeyRetrievalEnabled = !isBackgroundJobEnabled || latestCalc.riskState == RiskState.CALCULATION_FAILED
        )
    }
        .onStart { Timber.v("TracingCardState FLOW start") }
        .onEach { Timber.d("TracingCardState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingCardState FLOW completed.") }
}
