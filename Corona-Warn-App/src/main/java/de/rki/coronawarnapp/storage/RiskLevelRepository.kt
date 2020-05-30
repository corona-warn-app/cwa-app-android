package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevelConstants

object RiskLevelRepository {

    val riskLevelScore = MutableLiveData(RiskLevelConstants.UNKNOWN_RISK_INITIAL)

    fun setRiskLevelScore(score: RiskLevel) {
        val rawRiskLevel = score.raw
        riskLevelScore.postValue(rawRiskLevel)
        LocalData.getSharedPreferenceInstance()
            .edit()
            .putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_risk_level_score),
                rawRiskLevel
            ).apply()
    }

    fun getLastCalculatedScore(): RiskLevel {
        val riskLevelScoreRaw = LocalData.getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_risk_level_score), UNDETERMINED.raw
        )
        return RiskLevel.forValue(riskLevelScoreRaw)
    }
}
