package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object RiskLevelRepository {

    private val internalRisklevelScore = MutableStateFlow(RiskLevelConstants.UNKNOWN_RISK_INITIAL)
    val riskLevelScore: Flow<Int> = internalRisklevelScore

    private val internalRiskLevelScoreLastSuccessfulCalculated =
        MutableStateFlow(LocalData.lastSuccessfullyCalculatedRiskLevel().raw)
    val riskLevelScoreLastSuccessfulCalculated: Flow<Int> =
        internalRiskLevelScoreLastSuccessfulCalculated

    /**
     * Set the new calculated [RiskLevel]
     * Calculation happens in the [de.rki.coronawarnapp.transaction.RiskLevelTransaction]
     *
     * @see de.rki.coronawarnapp.transaction.RiskLevelTransaction
     * @see de.rki.coronawarnapp.risk.RiskLevels
     *
     * @param riskLevel
     */
    fun setRiskLevelScore(riskLevel: RiskLevel) {
        val rawRiskLevel = riskLevel.raw
        internalRisklevelScore.value = rawRiskLevel

        setLastCalculatedScore(rawRiskLevel)
        setLastSuccessfullyCalculatedScore(riskLevel)
    }

    /**
     * Resets the data in the [RiskLevelRepository]
     *
     * @see de.rki.coronawarnapp.util.DataReset
     *
     */
    fun reset() {
        internalRisklevelScore.value = RiskLevelConstants.UNKNOWN_RISK_INITIAL
    }

    /**
     * Set the current risk level from the last calculated risk level.
     * This is necessary if the app has no connectivity and the risk level transaction
     * fails.
     *
     * @see de.rki.coronawarnapp.transaction.RiskLevelTransaction
     *
     */
    fun setLastCalculatedRiskLevelAsCurrent() {
        var lastRiskLevelScore = getLastCalculatedScore()
        if (lastRiskLevelScore == RiskLevel.UNDETERMINED) {
            lastRiskLevelScore = RiskLevel.UNKNOWN_RISK_INITIAL
        }
        internalRisklevelScore.value = lastRiskLevelScore.raw
    }

    /**
     * Get the last calculated RiskLevel
     *
     * @return
     */
    fun getLastCalculatedScore(): RiskLevel = LocalData.lastCalculatedRiskLevel()

    /**
     * Set the last calculated RiskLevel
     *
     * @param rawRiskLevel
     */
    private fun setLastCalculatedScore(rawRiskLevel: Int) =
        LocalData.lastCalculatedRiskLevel(rawRiskLevel)

    /**
     * Get the last successfully calculated [RiskLevel]
     *
     * @see RiskLevel
     *
     * @return
     */
    private fun getLastSuccessfullyCalculatedScore(): RiskLevel =
        LocalData.lastSuccessfullyCalculatedRiskLevel()

    /**
     * Refreshes repository variable with local data
     *
     */
    fun refreshLastSuccessfullyCalculatedScore() {
        internalRiskLevelScoreLastSuccessfulCalculated.value =
            getLastSuccessfullyCalculatedScore().raw
    }

    /**
     * Set the last successfully calculated [RiskLevel]
     *
     * @param riskLevel
     */
    private fun setLastSuccessfullyCalculatedScore(riskLevel: RiskLevel) {
        if (!RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(riskLevel)) {
            LocalData.lastSuccessfullyCalculatedRiskLevel(riskLevel.raw)
            internalRiskLevelScoreLastSuccessfulCalculated.value = riskLevel.raw
        }
    }
}
