package de.rki.coronawarnapp.ui.tracing.details

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
class TracingDetailsStateProvider @Inject constructor(
    private val riskDetailPresenter: DefaultRiskDetailPresenter,
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    settingsRepository: SettingsRepository,
    tracingRepository: TracingRepository,
    enfClient: ENFClient
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
        enfClient.latestFinishedCalculation().onEach {
            Timber.v("latestFinishedCalculation: $it")
        },
        backgroundModeStatus.isAutoModeEnabled,
        settingsRepository.isManualKeyRetrievalEnabledFlow,
        settingsRepository.manualKeyRetrievalTimeFlow
    ) { status,
        riskLevelScore,
        riskLevelScoreLastSuccessfulCalculated,
        isRefreshing, matchedKeyCount,
        daysSinceLastExposure, activeTracingDaysInRetentionPeriod,
        lastENFCalculation,
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

        // TODO Remove a later version (1.7+), when everyone likely has tracked calc data
        // When the update with this change hits, there will not yet be a last tracked calculation
        val lastUpdateDate = lastENFCalculation?.finishedAt?.toDate()
            ?: tracingRepository.lastTimeDiagnosisKeysFetched.first()

        TracingDetailsState(
            tracingStatus = status,
            riskLevelScore = riskLevelScore,
            isRefreshing = isRefreshing,
            lastRiskLevelScoreCalculated = riskLevelScoreLastSuccessfulCalculated,
            matchedKeyCount = matchedKeyCount,
            daysSinceLastExposure = daysSinceLastExposure,
            activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
            lastENFCalculation = lastUpdateDate,
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
