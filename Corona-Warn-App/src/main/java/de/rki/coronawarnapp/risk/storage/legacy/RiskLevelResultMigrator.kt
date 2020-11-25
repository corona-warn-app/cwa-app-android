package de.rki.coronawarnapp.risk.storage.legacy

import android.content.SharedPreferences
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import dagger.Lazy
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Instant
import timber.log.Timber
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
        prefs.getLong("preference_timestamp_risk_level_calculation", -1L).also {
            return if (it < 0) null else Instant.ofEpochMilli(it)
        }
    }

    private fun lastCalculatedRiskLevel(): RiskLevel? {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score", -1)
        return if (rawRiskLevel != -1) mapRiskLevelConstant(rawRiskLevel) else null
    }

    private fun lastSuccessfullyCalculatedRiskLevel(): RiskLevel? {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score_successful", -1)
        return if (rawRiskLevel != -1) mapRiskLevelConstant(rawRiskLevel) else null
    }

    fun getLegacyResults(): List<RiskLevelResult> = try {
        val legacyResults = mutableListOf<RiskLevelResult>()
        lastCalculatedRiskLevel()?.let {
            legacyResults.add(
                LegacyResult(
                    riskLevel = it,
                    calculatedAt = lastTimeRiskLevelCalculation() ?: timeStamper.nowUTC
                )
            )
        }

        lastSuccessfullyCalculatedRiskLevel()?.let {
            legacyResults.add(LegacyResult(riskLevel = it, calculatedAt = timeStamper.nowUTC))
        }

        legacyResults
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse legacy risklevel data.")
        emptyList()
    }

    data class LegacyResult(
        override val riskLevel: RiskLevel,
        override val calculatedAt: Instant
    ) : RiskLevelResult {
        override val failureReason: RiskLevelResult.FailureReason? = null
        override val aggregatedRiskResult: AggregatedRiskResult? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    companion object {
        private fun mapRiskLevelConstant(value: Int): RiskLevel {
            return when (value) {
                RiskLevelConstants.NO_CALCULATION_POSSIBLE_TRACING_OFF -> RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
                RiskLevelConstants.LOW_LEVEL_RISK -> RiskLevel.LOW_LEVEL_RISK
                RiskLevelConstants.INCREASED_RISK -> RiskLevel.INCREASED_RISK
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS -> RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
                RiskLevelConstants.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL -> RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
                RiskLevelConstants.UNKNOWN_RISK_NO_INTERNET -> RiskLevel.UNKNOWN_RISK_NO_INTERNET
                else -> RiskLevel.UNDETERMINED
            }
        }
    }
}
