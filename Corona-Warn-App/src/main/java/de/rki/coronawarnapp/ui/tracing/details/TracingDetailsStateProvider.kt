package de.rki.coronawarnapp.ui.tracing.details

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
class TracingDetailsStateProvider @Inject constructor(
    private val riskDetailPresenter: DefaultRiskDetailPresenter,
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage
) {

    val state: Flow<TracingDetailsState> = combine(
        tracingStatus.generalStatus,
        tracingRepository.tracingProgress,
        riskLevelStorage.riskLevelResults,
        tracingRepository.activeTracingDaysInRetentionPeriod,
        backgroundModeStatus.isAutoModeEnabled
    ) { status,
        tracingProgress,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod,
        isBackgroundJobEnabled ->

        val (latestCalc, latestSuccessfulCalc) = riskLevelResults.tryLatestResultsWithDefaults()

        val isAdditionalInformationVisible = riskDetailPresenter.isAdditionalInfoVisible(
            latestCalc.riskState, latestCalc.matchedKeyCount
        )
        val isInformationBodyNoticeVisible = riskDetailPresenter.isInformationBodyNoticeVisible(
            latestCalc.riskState
        )

        val isRestartButtonEnabled = !isBackgroundJobEnabled || latestCalc.riskState == RiskState.CALCULATION_FAILED

        TracingDetailsState(
            tracingStatus = status,
            riskState = latestCalc.riskState,
            tracingProgress = tracingProgress,
            matchedKeyCount = latestCalc.matchedKeyCount,
            daysSinceLastExposure = latestCalc.daysWithEncounters,
            activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
            isManualKeyRetrievalEnabled = isRestartButtonEnabled,
            isAdditionalInformationVisible = isAdditionalInformationVisible,
            isInformationBodyNoticeVisible = isInformationBodyNoticeVisible
        )
    }
        .onStart { Timber.v("TracingDetailsState FLOW start") }
        .onEach { Timber.d("TracingDetailsState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingDetailsState FLOW completed.") }
}
