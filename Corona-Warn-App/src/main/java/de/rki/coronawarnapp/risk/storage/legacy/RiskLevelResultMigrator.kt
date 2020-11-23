package de.rki.coronawarnapp.risk.storage.legacy

import androidx.core.content.edit
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.storage.LocalData
import org.joda.time.Instant
import java.util.Date
import javax.inject.Singleton

@Singleton
class RiskLevelResultMigrator {
    // FIXME
    /**
     * Gets the last time of successful risk level calculation as long
     * from the EncryptedSharedPrefs
     *
     * @return Long
     */
    fun lastTimeRiskLevelCalculation(): Long? {
        val time = LocalData.getSharedPreferenceInstance().getLong(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_timestamp_risk_level_calculation),
            0L
        )
        return Date(time).time
    }

    /**
     * Sets the last time of successful risk level calculation as long
     * from the EncryptedSharedPrefs
     *
     * @param value timestamp as Long
     */
    fun lastTimeRiskLevelCalculation(value: Long?) {
        LocalData.getSharedPreferenceInstance().edit(true) {
            putLong(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_timestamp_risk_level_calculation),
                value ?: 0L
            )
        }
    }

    /****************************************************
     * RISK LEVEL
     ****************************************************/

    /**
     * Gets the last calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @return
     */
    fun lastCalculatedRiskLevel(): RiskLevel {
        val rawRiskLevel = LocalData.getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_risk_level_score),
            RiskLevel.UNDETERMINED.raw
        )
        return RiskLevel.forValue(rawRiskLevel)
    }

    /**
     * Sets the last calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @param rawRiskLevel
     */
    fun lastCalculatedRiskLevel(rawRiskLevel: Int) =
        LocalData.getSharedPreferenceInstance().edit(true) {
            putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_risk_level_score),
                rawRiskLevel
            )
        }

    /**
     * Gets the last successfully calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @return
     */
    fun lastSuccessfullyCalculatedRiskLevel(): RiskLevel {
        val rawRiskLevel = LocalData.getSharedPreferenceInstance().getInt(
            CoronaWarnApplication.getAppContext()
                .getString(R.string.preference_risk_level_score_successful),
            RiskLevel.UNDETERMINED.raw
        )
        return RiskLevel.forValue(rawRiskLevel)
    }

    /**
     * Sets the last calculated risk level
     * from the EncryptedSharedPrefs
     *
     * @see RiskLevelRepository
     *
     * @param rawRiskLevel
     */
    fun lastSuccessfullyCalculatedRiskLevel(rawRiskLevel: Int) =
        LocalData.getSharedPreferenceInstance().edit(true) {
            putInt(
                CoronaWarnApplication.getAppContext()
                    .getString(R.string.preference_risk_level_score_successful),
                rawRiskLevel
            )
        }

    suspend fun getLegacyResult(): RiskLevelResult = object : RiskLevelResult {
        override val riskLevel: RiskLevel
            get() = TODO("Not yet implemented")
        override val calculatedAt: Instant
            get() = TODO("Not yet implemented")
        override val aggregatedRiskResult: AggregatedRiskResult?
            get() = TODO("Not yet implemented")
        override val exposureWindows: List<ExposureWindow>?
            get() = TODO("Not yet implemented")
        override val isIncreasedRisk: Boolean
            get() = TODO("Not yet implemented")
        override val matchedKeyCount: Int
            get() = TODO("Not yet implemented")
        override val daysSinceLastExposure: Int
            get() = TODO("Not yet implemented")
    }
}
