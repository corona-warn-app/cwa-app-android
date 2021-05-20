package de.rki.coronawarnapp.risk.changedetection

import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/*
* Checks for changes in PT risk
* Collects data for analytics
*
* */
class PtRiskLevelChangeDetector @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
) {
    fun launch() {
        Timber.v("Monitoring PT risk level changes.")
        riskLevelStorage.allPtRiskLevelResults
            .map { results ->
                results
                    .sortedByDescending { it.calculatedAt }
                    .filter { it.wasSuccessfullyCalculated }
                    .take(2)
            }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for change in PT risk level.")
                it.checkForRiskLevelChanges()
            }
            .launchIn(appScope)
    }

    private fun List<PtRiskLevelResult>.checkForRiskLevelChanges() {
        if (isEmpty()) return

        val oldResult = minByOrNull { it.calculatedAt }
        val newResult = maxByOrNull { it.calculatedAt }

        if (oldResult == null || newResult == null) return

        if (oldResult.riskState.hasChangedFromLowToHigh(newResult.riskState)) {
            riskLevelSettings.ptLastChangeToHighRiskLevelTimestamp = newResult.calculatedAt
        }

        if (newResult.wasSuccessfullyCalculated)
            riskLevelSettings.ptMostRecentDateWithHighOrLowRiskLevel = when (newResult.riskState) {
                RiskState.INCREASED_RISK -> newResult.mostRecentDateWithHighRisk
                RiskState.LOW_RISK -> newResult.mostRecentDateWithLowRisk
                else -> riskLevelSettings.ptMostRecentDateWithHighOrLowRiskLevel
            }
    }
}

internal fun RiskState.hasChangedFromLowToHigh(other: RiskState) =
    this == RiskState.LOW_RISK && other == RiskState.INCREASED_RISK
