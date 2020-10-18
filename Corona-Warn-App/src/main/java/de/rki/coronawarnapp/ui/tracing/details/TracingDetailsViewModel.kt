package de.rki.coronawarnapp.ui.tracing.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.storage.TracingRepository
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.util.BackgroundModeStatus
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class TracingDetailsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val riskDetailPresenter: DefaultRiskDetailPresenter,
    tracingStatus: GeneralTracingStatus,
    backgroundModeStatus: BackgroundModeStatus,
    settingsRepository: SettingsRepository,
    tracingRepository: TracingRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    // TODO Refactore these singletons away
    val state: LiveData<TracingDetailsState> = combine(
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
    ) { sources ->
        val status = sources[0] as GeneralTracingStatus.Status
        val riskLevelScore = sources[1] as Int
        val riskLevelScoreLastSuccessfulCalculated = sources[2] as Int
        val isRefreshing = sources[3] as Boolean
        val matchedKeyCount = sources[4] as Int
        val daysSinceLastExposure = sources[5] as Int
        val activeTracingDaysInRetentionPeriod = sources[6] as Long
        val lastTimeDiagnosisKeysFetched = sources[7] as Date?
        val isBackgroundJobEnabled = sources[8] as Boolean
        val isManualKeyRetrievalEnabled = sources[9] as Boolean
        val manualKeyRetrievalTime = sources[10] as Long

        val isAdditionalInformationVisible = riskDetailPresenter.isAdditionalInfoVisible(
            riskLevelScore, matchedKeyCount
        )
        val isInformationBodyNoticeVisible = riskDetailPresenter.isInformationBodyNoticeVisible(
            riskLevelScore, matchedKeyCount
        )

        TracingDetailsState(
            tracingStatus = status,
            riskLevelScore = riskLevelScore,
            isRefreshing = isRefreshing,
            riskLevelLastSuccessfulCalculation = riskLevelScoreLastSuccessfulCalculated,
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
        .asLiveData(dispatcherProvider.Default)
}
