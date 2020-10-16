package de.rki.coronawarnapp.ui.tracing.card

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

class TracingCardViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val tracingStatus: GeneralTracingStatus,
    private val backgroundModeStatus: BackgroundModeStatus,
    private val settingsRepository: SettingsRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    // TODO Refactore these singletons away
    val state: LiveData<TracingCardState> = combine(
        tracingStatus.generalStatus.onEach { Timber.v("tracingStatus: $it") },
        RiskLevelRepository.riskLevelScore.onEach { Timber.v("riskLevelScore: $it") },
        RiskLevelRepository.riskLevelScoreLastSuccessfulCalculated.onEach { Timber.v("riskLevelScoreLastSuccessfulCalculated: $it") },
        TracingRepository.isRefreshing.onEach { Timber.v("isRefreshing: $it") },
        ExposureSummaryRepository.matchedKeyCount.onEach { Timber.v("matchedKeyCount: $it") },
        ExposureSummaryRepository.daysSinceLastExposure.onEach { Timber.v("daysSinceLastExposure: $it") },
        TracingRepository.activeTracingDaysInRetentionPeriod.onEach { Timber.v("activeTracingDaysInRetentionPeriod: $it") },
        TracingRepository.lastTimeDiagnosisKeysFetched.onEach { Timber.v("lastTimeDiagnosisKeysFetched: $it") },
        backgroundModeStatus.isAutoModeEnabled.onEach { Timber.v("isAutoModeEnabled: $it") },
        settingsRepository.isManualKeyRetrievalEnabledFlow.onEach { Timber.v("isManualKeyRetrievalEnabledFlow: $it") },
        settingsRepository.manualKeyRetrievalTimeFlow.onEach { Timber.v("manualKeyRetrievalTimeFlow: $it") }

    ) { sources ->
        val tracingStatus = sources[0] as GeneralTracingStatus.Status
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

        TracingCardState(
            tracingStatus = tracingStatus,
            riskLevelScore = riskLevelScore,
            isRefreshing = isRefreshing,
            riskLevelLastSuccessfulCalculation = riskLevelScoreLastSuccessfulCalculated,
            matchedKeyCount = matchedKeyCount,
            daysSinceLastExposure = daysSinceLastExposure,
            activeTracingDaysInRetentionPeriod = activeTracingDaysInRetentionPeriod,
            lastTimeDiagnosisKeysFetched = lastTimeDiagnosisKeysFetched,
            isBackgroundJobEnabled = isBackgroundJobEnabled,
            isManualKeyRetrievalEnabled = isManualKeyRetrievalEnabled,
            manualKeyRetrievalTime = manualKeyRetrievalTime
        )
    }
        .onStart { Timber.v("TracingCardState FLOW start") }
        .onEach { Timber.d("TracingCardState FLOW emission: %s", it) }
        .onCompletion { Timber.v("TracingCardState FLOW completed.") }
        .asLiveData(dispatcherProvider.Default)
}
