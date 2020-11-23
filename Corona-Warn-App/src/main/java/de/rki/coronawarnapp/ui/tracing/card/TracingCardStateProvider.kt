package de.rki.coronawarnapp.ui.tracing.card

import dagger.Reusable
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.SettingsRepository
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
    settingsRepository: SettingsRepository,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage
) {

    // TODO Refactor these singletons away
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
        },
        settingsRepository.isManualKeyRetrievalEnabledFlow.onEach {
            Timber.v("isManualKeyRetrievalEnabledFlow: $it")
        },
        settingsRepository.manualKeyRetrievalTimeFlow.onEach {
            Timber.v("manualKeyRetrievalTimeFlow: $it")
        }
    ) { status,
        tracingProgress,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched,
        isBackgroundJobEnabled,
        isManualKeyRetrievalEnabled,
        manualKeyRetrievalTime ->

        val (latestCalc, latestSuccessfulCalc) = riskLevelResults.tryLatestResultsWithDefaults()


        TracingCardState(
            tracingStatus = status,
            riskLevelScore = latestCalc.riskLevel.raw,
            tracingProgress = tracingProgress,
            lastRiskLevelScoreCalculated = latestSuccessfulCalc.riskLevel.raw,
            lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
            matchedKeyCount = latestCalc.matchedKeyCount,
            daysSinceLastExposure = latestCalc.daysSinceLastExposure,
            activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
            isBackgroundJobEnabled = isBackgroundJobEnabled,
            isManualKeyRetrievalEnabled = isManualKeyRetrievalEnabled,
            manualKeyRetrievalTime = manualKeyRetrievalTime
        )
    }
        .onStart { Timber.v("TracingCardState FLOW start") }
        .onEach { Timber.d("TracingCardState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingCardState FLOW completed.") }
}
