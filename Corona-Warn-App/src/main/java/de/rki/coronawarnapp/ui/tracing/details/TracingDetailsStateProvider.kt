package de.rki.coronawarnapp.ui.tracing.details

import dagger.Reusable
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
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
    tracingRepository: TracingRepository
) {

    // TODO Refactore these singletons away
    val state: Flow<TracingDetailsState> = combine(
        tracingStatus.generalStatus,
        RiskLevelRepository.riskLevelScore,
        RiskLevelRepository.riskLevelScoreLastSuccessfulCalculated,
        tracingRepository.isRefreshing,
        ExposureSummaryRepository.matchedKeyCount,
        ExposureSummaryRepository.daysSinceLastExposure,
        tracingRepository.activeTracingDaysInRetentionPeriod,
        tracingRepository.lastTimeDiagnosisKeysFetched,
        backgroundModeStatus.isAutoModeEnabled,
        settingsRepository.isManualKeyRetrievalEnabledFlow,
        settingsRepository.manualKeyRetrievalTimeFlow
    ) { status,
        riskLevelScore,
        riskLevelScoreLastSuccessfulCalculated,
        isRefreshing, matchedKeyCount,
        daysSinceLastExposure, activeTracingDaysInRetentionPeriod,
        lastTimeDiagnosisKeysFetched,
        isBackgroundJobEnabled,
        isManualKeyRetrievalEnabled,
        manualKeyRetrievalTime ->

        val isAdditionalInformationVisible = riskDetailPresenter.isAdditionalInfoVisible(
            riskLevelScore, matchedKeyCount
        )
        val isInformationBodyNoticeVisible =
            riskDetailPresenter.isInformationBodyNoticeVisible(
            riskLevelScore
        )

        TracingDetailsState(
            tracingStatus = status,
            riskLevelScore = riskLevelScore,
            isRefreshing = isRefreshing,
            lastRiskLevelScoreCalculated = riskLevelScoreLastSuccessfulCalculated,
            matchedKeyCount = matchedKeyCount,
            daysSinceLastExposure = daysSinceLastExposure,
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
