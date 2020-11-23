package de.rki.coronawarnapp.risk.storage.legacy

import android.content.SharedPreferences
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import dagger.Lazy
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TODO Remove this in the future
 * Once a significant portion of the user base has already been running 1.8.x,
 * this class can be removed to reduce access to the EncryptedPreferences.
 */
@Singleton
class RiskLevelResultMigrator @Inject constructor(
    @EncryptedPreferences encryptedPreferences: Lazy<SharedPreferences>,
    private val timeStamper: TimeStamper
) {

    private val prefs by lazy { encryptedPreferences.get() }

    private fun lastTimeRiskLevelCalculation(): Instant? {
        val time = prefs.getLong("preference_timestamp_risk_level_calculation", 0L)
        return Instant.ofEpochMilli(time)
    }

    private fun lastCalculatedRiskLevel(): RiskLevel {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score", RiskLevel.UNDETERMINED.raw)
        return RiskLevel.forValue(rawRiskLevel)
    }

    private fun lastSuccessfullyCalculatedRiskLevel(): RiskLevel {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score_successful", RiskLevel.UNDETERMINED.raw)
        return RiskLevel.forValue(rawRiskLevel)
    }

    fun getLegacyResults(): List<RiskLevelResult> {
        val lastCalculatedRisk = object : RiskLevelResult {
            override val riskLevel: RiskLevel = lastCalculatedRiskLevel()
            override val calculatedAt: Instant = lastTimeRiskLevelCalculation() ?: timeStamper.nowUTC
            override val aggregatedRiskResult: AggregatedRiskResult? = null
            override val exposureWindows: List<ExposureWindow>? = null
            override val isIncreasedRisk: Boolean = riskLevel == RiskLevel.INCREASED_RISK
            override val matchedKeyCount: Int = 0
            override val daysSinceLastExposure: Int = 0
        }
        val lastSuccessfullyCalculatedRisk = object : RiskLevelResult {
            override val riskLevel: RiskLevel = lastSuccessfullyCalculatedRiskLevel()
            override val calculatedAt: Instant = timeStamper.nowUTC
            override val aggregatedRiskResult: AggregatedRiskResult? = null
            override val exposureWindows: List<ExposureWindow>? = null
            override val isIncreasedRisk: Boolean = riskLevel == RiskLevel.INCREASED_RISK
            override val matchedKeyCount: Int = 0
            override val daysSinceLastExposure: Int = 0
        }
        return listOf(lastCalculatedRisk, lastSuccessfullyCalculatedRisk)
    }
}
