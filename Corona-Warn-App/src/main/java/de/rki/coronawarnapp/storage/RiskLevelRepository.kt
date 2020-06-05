package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelConstants

object RiskLevelRepository {

    /**
     * LiveData variable that can be consumed in a ViewModel to observe RiskLevel changes
     */
    val riskLevelScore = MutableLiveData(RiskLevelConstants.UNKNOWN_RISK_INITIAL)

    /**
     * Set the new calculated [RiskLevel]
     * Calculation happens in the [de.rki.coronawarnapp.transaction.RiskLevelTransaction]
     *
     * @see de.rki.coronawarnapp.transaction.RiskLevelTransaction
     * @see de.rki.coronawarnapp.risk.RiskLevelCalculation
     *
     * @param riskLevel
     */
    fun setRiskLevelScore(riskLevel: RiskLevel) {
        val rawRiskLevel = riskLevel.raw
        riskLevelScore.postValue(rawRiskLevel)

        setLastCalculatedScore(rawRiskLevel)
        setLastSuccessfullyCalculatedScore(riskLevel)
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
    fun getLastSuccessfullyCalculatedScore(): RiskLevel =
        LocalData.lastSuccessfullyCalculatedRiskLevel()

    /**
     * Set the last successfully calculated [RiskLevel]
     *
     * @param riskLevel
     */
    private fun setLastSuccessfullyCalculatedScore(riskLevel: RiskLevel) {
        if (!RiskLevel.UNSUCCESSFUL_RISK_LEVELS.contains(riskLevel)) {
            LocalData.lastSuccessfullyCalculatedRiskLevel(riskLevel.raw)
        }
    }
}
