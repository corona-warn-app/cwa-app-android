package de.rki.coronawarnapp.ui.tracing.details

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
class TracingDetailsStateProvider @Inject constructor(
    private val riskDetailPresenter: DefaultRiskDetailPresenter,
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    settingsRepository: SettingsRepository,
    tracingRepository: TracingRepository,
    riskLevelStorage: RiskLevelStorage
) {

    val state: Flow<TracingDetailsState> = combine(
        tracingStatus.generalStatus,
        tracingRepository.tracingProgress,
        riskLevelStorage.riskLevelResults,
        tracingRepository.activeTracingDaysInRetentionPeriod,
        tracingRepository.lastTimeDiagnosisKeysFetched,
        backgroundModeStatus.isAutoModeEnabled,
        settingsRepository.isManualKeyRetrievalEnabledFlow,
        settingsRepository.manualKeyRetrievalTimeFlow
    ) { status,
        tracingProgress,
        riskLevelResults,
        activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched,
        isBackgroundJobEnabled,
        isManualKeyRetrievalEnabled,
        manualKeyRetrievalTime ->

        val (latestCalc, latestSuccessfulCalc) = riskLevelResults.tryLatestResultsWithDefaults()

        val isAdditionalInformationVisible = riskDetailPresenter.isAdditionalInfoVisible(
            latestCalc.riskLevel.raw, latestCalc.matchedKeyCount
        )
        val isInformationBodyNoticeVisible = riskDetailPresenter.isInformationBodyNoticeVisible(
            latestCalc.riskLevel.raw
        )

        TracingDetailsState(
            tracingStatus = status,
            riskLevelScore = latestCalc.riskLevel.raw,
            tracingProgress = tracingProgress,
            lastRiskLevelScoreCalculated = latestSuccessfulCalc.riskLevel.raw,
            matchedKeyCount = latestCalc.matchedKeyCount,
            daysSinceLastExposure = latestCalc.daysSinceLastExposure,
            activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
            lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
            isBackgroundJobEnabled = isBackgroundJobEnabled,
            isManualKeyRetrievalEnabled = isManualKeyRetrievalEnabled,
            manualKeyRetrievalTime = manualKeyRetrievalTime,
            isAdditionalInformationVisible = isAdditionalInformationVisible,
            isInformationBodyNoticeVisible = isInformationBodyNoticeVisible
        )
    }
        .onStart { Timber.v("TracingDetailsState FLOW start") }
        .onEach { Timber.d("TracingDetailsState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingDetailsState FLOW completed.") }
}
