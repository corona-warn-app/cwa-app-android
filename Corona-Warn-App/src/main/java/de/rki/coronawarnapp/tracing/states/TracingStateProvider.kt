package de.rki.coronawarnapp.tracing.states

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.latestSubmission
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestResultsWithDefaults
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.TracingProgress
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

class TracingStateProvider @AssistedInject constructor(
    @Assisted private val isDetailsMode: Boolean,
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage,
    exposureDetectionTracker: ExposureDetectionTracker
) {

    val state: Flow<TracingState> = combine(
        tracingStatus.generalStatus.onEach {
            Timber.v("tracingStatus: $it")
        },
        tracingRepository.tracingProgress.onEach {
            Timber.v("tracingProgress: $it")
        },
        riskLevelStorage.latestAndLastSuccessful.onEach {
            Timber.v("riskLevelResults: $it")
        },
        tracingRepository.activeTracingDaysInRetentionPeriod.onEach {
            Timber.v("activeTracingDaysInRetentionPeriod: $it")
        },
        exposureDetectionTracker.latestSubmission().onEach {
            Timber.v("latestSubmission: $it")
        },
        backgroundModeStatus.isAutoModeEnabled.onEach {
            Timber.v("isAutoModeEnabled: $it")
        }
    ) { tracingStatus,
        tracingProgress,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod,
        latestSubmission,
        isBackgroundJobEnabled ->

        val (
            latestCalc,
            latestSuccessfulCalc
        ) = riskLevelResults.tryLatestResultsWithDefaults()

        return@combine when {
            tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> TracingDisabled(
                isInDetailsMode = isDetailsMode,
                riskState = latestSuccessfulCalc.riskState,
                lastExposureDetectionTime = latestSubmission?.startedAt
            )
            tracingProgress != TracingProgress.Idle -> TracingInProgress(
                isInDetailsMode = isDetailsMode,
                riskState = latestCalc.riskState,
                tracingProgress = tracingProgress
            )
            latestCalc.riskState == RiskState.LOW_RISK -> LowRisk(
                isInDetailsMode = isDetailsMode,
                riskState = latestCalc.riskState,
                lastExposureDetectionTime = latestSubmission?.startedAt,
                daysWithEncounters = latestCalc.daysWithEncounters,
                activeTracingDays = activeTracingDaysInRetentionPeriod.toInt(),
                allowManualUpdate = !isBackgroundJobEnabled
            )
            latestCalc.riskState == RiskState.INCREASED_RISK -> IncreasedRisk(
                isInDetailsMode = isDetailsMode,
                riskState = latestCalc.riskState,
                lastExposureDetectionTime = latestSubmission?.startedAt,
                lastEncounterAt = latestCalc.lastRiskEncounterAt,
                daysWithEncounters = latestCalc.daysWithEncounters,
                activeTracingDays = activeTracingDaysInRetentionPeriod.toInt(),
                allowManualUpdate = !isBackgroundJobEnabled
            )
            else -> TracingFailed(
                isInDetailsMode = isDetailsMode,
                riskState = latestSuccessfulCalc.riskState,
                lastExposureDetectionTime = latestSubmission?.startedAt
            )
        }
    }
        .onStart { Timber.v("TracingStateProvider FLOW start") }
        .onEach { Timber.d("TracingStateProvider FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingStateProvider FLOW completed.") }

    @AssistedInject.Factory
    interface Factory {
        fun create(isDetailsMode: Boolean): TracingStateProvider
    }
}
