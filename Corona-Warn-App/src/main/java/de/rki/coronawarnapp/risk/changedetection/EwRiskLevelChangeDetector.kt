package de.rki.coronawarnapp.risk.changedetection

import de.rki.coronawarnapp.datadonation.analytics.storage.TestResultDonorSettings
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.RiskLevelSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.storage.TracingSettings
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
* Collects data for analytics
*
* */
@Suppress("LongParameterList")
class EwRiskLevelChangeDetector @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val riskLevelStorage: RiskLevelStorage,
    private val riskLevelSettings: RiskLevelSettings,
    private val surveys: Surveys,
    private val tracingSettings: TracingSettings,
    private val testResultDonorSettings: TestResultDonorSettings
) {
    fun launch() {
        Timber.v("Monitoring risk level changes.")
        riskLevelStorage.latestEwRiskLevelResults
            .map { results ->
                results.sortedBy { it.calculatedAt }.takeLast(2)
            }
            .filter { it.size == 2 }
            .onEach {
                Timber.v("Checking for ew risklevel change.")
                checkEwRiskForStateChanges(it)
            }
            .catch { Timber.e(it, "App config change checks failed.") }
            .launchIn(appScope)
    }

    private fun checkEwRiskForStateChanges(results: List<EwRiskLevelResult>) {
        val oldResult = results.first()
        val newResult = results.last()

        val lastCheckedResult = riskLevelSettings.ewLastChangeCheckedRiskLevelTimestamp
        if (lastCheckedResult == newResult.calculatedAt) {
            Timber.d("We already checked this risk level change, skipping further checks.")
            return
        }
        riskLevelSettings.ewLastChangeCheckedRiskLevelTimestamp = newResult.calculatedAt

        val oldRiskState = oldResult.riskState
        val newRiskState = newResult.riskState
        Timber.d("Last state was $oldRiskState and current state is $newRiskState")

        if (oldRiskState.hasChangedFromHighToLow(newRiskState)) {
            tracingSettings.isUserToBeNotifiedOfLoweredRiskLevel.update { true }
            Timber.d("Risk level changed LocalData is updated. Current Risk level is $newRiskState")

            surveys.resetSurvey(Surveys.Type.HIGH_RISK_ENCOUNTER)
        }

        if (oldRiskState.hasChangedFromLowToHigh(newRiskState)) {
            riskLevelSettings.ewLastChangeToHighRiskLevelTimestamp = newResult.calculatedAt
        }

        // Save riskLevelTurnedRedTime if not already set before for high risk detection
        Timber.i("riskLevelTurnedRedTime=%s", testResultDonorSettings.riskLevelTurnedRedTime.value)
        if (testResultDonorSettings.riskLevelTurnedRedTime.value == null && newResult.isIncreasedRisk) {
            testResultDonorSettings.riskLevelTurnedRedTime.update { newResult.calculatedAt }
            Timber.i(
                "riskLevelTurnedRedTime: newRiskState=%s, riskLevelTurnedRedTime=%s",
                newResult.riskState,
                newResult.calculatedAt
            )
        }

        // Save most recent date of high or low risks
        if (newResult.riskState in listOf(RiskState.INCREASED_RISK, RiskState.LOW_RISK)) {
            Timber.d("newRiskState=$newResult")
            val lastRiskEncounterAt = newResult.lastRiskEncounterAt
            Timber.i(
                "mostRecentDateWithHighOrLowRiskLevel: newRiskState=%s, lastRiskEncounterAt=%s",
                newResult.riskState,
                lastRiskEncounterAt
            )

            testResultDonorSettings.mostRecentDateWithHighOrLowRiskLevel.update { lastRiskEncounterAt }
        }
    }
}

private fun RiskState.hasChangedFromHighToLow(other: RiskState) =
    this == RiskState.INCREASED_RISK && other == RiskState.LOW_RISK
