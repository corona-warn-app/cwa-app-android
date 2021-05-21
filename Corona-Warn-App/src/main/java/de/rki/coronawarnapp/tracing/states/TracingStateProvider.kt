package de.rki.coronawarnapp.tracing.states

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.diagnosiskeys.download.RiskDetectionCanceller
import de.rki.coronawarnapp.diagnosiskeys.download.RiskDetectionCanceller.CancelResult.CANCEL_DUE_TO_RECENT_RISK_DETECTION
import de.rki.coronawarnapp.installTime.InstallTimeProvider
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.latestSubmission
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
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
    exposureDetectionTracker: ExposureDetectionTracker,
    installTimeProvider: InstallTimeProvider,
    riskDetectionCanceller: RiskDetectionCanceller
) {
    val state: Flow<TracingState> = combine(
        tracingStatus.generalStatus.onEach {
            Timber.tag(TAG).v("tracingStatus: $it")
        },
        tracingRepository.tracingProgress.onEach {
            Timber.tag(TAG).v("tracingProgress: $it")
        },
        riskLevelStorage.latestAndLastSuccessfulCombinedEwPtRiskLevelResult.onEach {
            Timber.tag(TAG).v("riskLevelResults: $it")
        },
        exposureDetectionTracker.latestSubmission().onEach {
            Timber.tag(TAG).v("latestSubmission: $it")
        },
        backgroundModeStatus.isAutoModeEnabled.onEach {
            Timber.tag(TAG).v("isAutoModeEnabled: $it")
        },
        riskDetectionCanceller.lastCancelResult.onEach {
            Timber.tag(TAG).v("lastCancelResult: $it")
        }
    ) { tracingStatus,
        tracingProgress,
        riskLevelResults,
        latestSubmission,
        isBackgroundJobEnabled,
        lastRiskDetectionCancelResult ->

        val latestCalc = riskLevelResults.lastCalculated
        val lastSuccessfullyCalc = riskLevelResults.lastSuccessfullyCalculated

        return@combine when {
            tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> TracingDisabled(
                isInDetailsMode = isDetailsMode,
                riskState = lastSuccessfullyCalc.riskState,
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
                lastEncounterAt = latestCalc.lastRiskEncounterAt,
                daysWithEncounters = latestCalc.daysWithEncounters,
                allowManualUpdate = !isBackgroundJobEnabled,
                daysSinceInstallation = installTimeProvider.daysSinceInstallation
            )
            latestCalc.riskState == RiskState.INCREASED_RISK -> IncreasedRisk(
                isInDetailsMode = isDetailsMode,
                riskState = latestCalc.riskState,
                lastExposureDetectionTime = latestSubmission?.startedAt,
                lastEncounterAt = latestCalc.lastRiskEncounterAt,
                daysWithEncounters = latestCalc.daysWithEncounters,
                allowManualUpdate = !isBackgroundJobEnabled
            )
            lastRiskDetectionCancelResult == CANCEL_DUE_TO_RECENT_RISK_DETECTION -> {
                when (lastSuccessfullyCalc.riskState) {
                    RiskState.LOW_RISK -> {
                        LowRisk(
                            isInDetailsMode = isDetailsMode,
                            riskState = lastSuccessfullyCalc.riskState,
                            lastExposureDetectionTime = latestSubmission?.startedAt,
                            lastEncounterAt = lastSuccessfullyCalc.lastRiskEncounterAt,
                            daysWithEncounters = lastSuccessfullyCalc.daysWithEncounters,
                            allowManualUpdate = !isBackgroundJobEnabled,
                            daysSinceInstallation = installTimeProvider.daysSinceInstallation
                        )
                    }
                    RiskState.INCREASED_RISK -> {
                        IncreasedRisk(
                            isInDetailsMode = isDetailsMode,
                            riskState = latestCalc.riskState,
                            lastExposureDetectionTime = latestSubmission?.startedAt,
                            lastEncounterAt = latestCalc.lastRiskEncounterAt,
                            daysWithEncounters = latestCalc.daysWithEncounters,
                            allowManualUpdate = !isBackgroundJobEnabled
                        )
                    }
                    else -> throw IllegalStateException(
                        "Last Successfully calculated Risk State should only either" +
                            "be LOW_RISK or INCREASED_RISK"
                    )
                }
            }
            else -> TracingFailed(
                isInDetailsMode = isDetailsMode,
                riskState = lastSuccessfullyCalc.riskState,
                lastExposureDetectionTime = latestSubmission?.startedAt
            )
        }
    }
        .onStart { Timber.tag(TAG).v("TracingStateProvider FLOW start") }
        .onEach { Timber.tag(TAG).d("TracingStateProvider FLOW emission: %s", it) }
        .onCompletion { Timber.tag(TAG).v("TracingStateProvider FLOW completed.") }

    @AssistedFactory
    interface Factory {
        fun create(isDetailsMode: Boolean): TracingStateProvider
    }

    companion object {
        const val TAG = "TracingStateProvider"
    }
}
