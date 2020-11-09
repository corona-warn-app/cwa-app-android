package de.rki.coronawarnapp.ui.main.riskcards

import dagger.Reusable
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.TracingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject

@Reusable
class RiskCardNoInternetStateProvider @Inject constructor(
    tracingRepository: TracingRepository
) {

    val state: Flow<RiskCardNoInternet> = combine(
        RiskLevelRepository.riskLevelScoreLastSuccessfulCalculated.onEach {
            Timber.v("riskLevelScoreLastSuccessfulCalculated: $it")
        },
        tracingRepository.lastTimeDiagnosisKeysFetched.onEach {
            Timber.v("lastTimeDiagnosisKeysFetched: $it")
        }
    ) { lastCalculatedRiskLevelScore,
        lastActualisationOfRiskLevel ->

        RiskCardNoInternet(
            lastRiskLevelScoreCalculated = lastCalculatedRiskLevelScore,
            lastRiskActualisation = lastActualisationOfRiskLevel
        )
    }
        .onStart { Timber.v("RiskCardNoInternet FLOW start") }
        .onEach { Timber.d("RiskCardNoInternet FLOW emission: %s", it) }
        .onCompletion { Timber.v("RiskCardNoInternet FLOW completed.") }
}
