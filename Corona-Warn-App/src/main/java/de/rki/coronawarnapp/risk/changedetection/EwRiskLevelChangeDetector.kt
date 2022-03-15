package de.rki.coronawarnapp.risk.changedetection

import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/*
* Checks for changes in EW risk
* Resets survey
*
* */
@Suppress("LongParameterList")
class EwRiskLevelChangeDetector @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val surveys: Surveys,

) {
    fun launch() {
        Timber.v("Monitoring risk level changes.")
        riskLevelStorage.allEwRiskLevelResults
            .map { results ->
                results.sortedBy { it.calculatedAt }.takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for changes in EW risk level.")
                it.checkForRiskLevelChanges()
            }
            .catch { Timber.e(it, "Exposure window risk level checks failed.") }
            .launchIn(appScope)
    }

    private fun List<EwRiskLevelResult>.checkForRiskLevelChanges() {
        if (isEmpty()) return

        val oldResult = minByOrNull { it.calculatedAt }
        val newResult = maxByOrNull { it.calculatedAt }

        if (oldResult == null || newResult == null) return

        val lastCheckedResult = riskLevelSettings.ewLastChangeCheckedRiskLevelTimestamp
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.ewLastChangeCheckedRiskLevelTimestamp = newResult.calculatedAt

        Timber.d("Last state was ${oldResult.riskState} and current state is ${newResult.riskState}")

        if (oldResult.riskState.hasChangedFromHighToLow(newResult.riskState)) {
            surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER)
        }
    }
}
