package de.rki.coronawarnapp.ui.tracing.card

import dagger.Reusable
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.util.BackgroundModeStatus
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    enfClient: ENFClient
) {

    // TODO Refactor these singletons away
    val state: Flow<TracingCardState> = combine(
        tracingStatus.generalStatus.onEach {
            Timber.v("tracingStatus: $it")
        },
        RiskLevelRepository.riskLevelScore.onEach {
            Timber.v("riskLevelScore: $it")
        },
        RiskLevelRepository.riskLevelScoreLastSuccessfulCalculated.onEach {
            Timber.v("riskLevelScoreLastSuccessfulCalculated: $it")
        },
        tracingRepository.tracingProgress.onEach {
            Timber.v("tracingProgress: $it")
        },
        ExposureSummaryRepository.matchedKeyCount.onEach {
            Timber.v("matchedKeyCount: $it")
        },
        ExposureSummaryRepository.daysSinceLastExposure.onEach {
            Timber.v("daysSinceLastExposure: $it")
        },
        tracingRepository.activeTracingDaysInRetentionPeriod.onEach {
            Timber.v("activeTracingDaysInRetentionPeriod: $it")
        },
        enfClient.latestFinishedCalculation().onEach {
            Timber.v("latestFinishedCalculation: $it")
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
        riskLevelScore,
        riskLevelScoreLastSuccessfulCalculated,
        tracingProgress,
        matchedKeyCount,
        daysSinceLastExposure,
        activeTracingDaysInRetentionPeriod,
        lastENFCalculation,
        isBackgroundJobEnabled,
        isManualKeyRetrievalEnabled,
        manualKeyRetrievalTime ->

        // TODO Remove a later version (1.7+), when everyone likely has tracked calc data
        // When the update with this change hits, there will not yet be a last tracked calculation
        val lastUpdateDate = lastENFCalculation?.finishedAt?.toDate()
            ?: tracingRepository.lastTimeDiagnosisKeysFetched.first()

        TracingCardState(
            tracingStatus = status,
            riskLevelScore = riskLevelScore,
            tracingProgress = tracingProgress,
            lastRiskLevelScoreCalculated = riskLevelScoreLastSuccessfulCalculated,
            lastENFCalculation = lastUpdateDate,
            matchedKeyCount = matchedKeyCount,
            daysSinceLastExposure = daysSinceLastExposure,
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
