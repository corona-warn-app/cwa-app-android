package de.rki.coronawarnapp.risk.storage.legacy

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import dagger.Lazy
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
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

    private fun lastCalculatedRiskLevel(): RiskState? {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score", -1)
        return if (rawRiskLevel != -1) mapRiskLevelConstant(rawRiskLevel) else null
    }

    private fun lastSuccessfullyCalculatedRiskLevel(): RiskState? {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score_successful", -1)
        return if (rawRiskLevel != -1) mapRiskLevelConstant(rawRiskLevel) else null
    }

    fun getLegacyResults(): List<RiskLevelResult> = try {
        val legacyResults = mutableListOf<RiskLevelResult>()
        lastCalculatedRiskLevel()?.let {
            legacyResults.add(
                LegacyResult(
                    riskState = it,
                    calculatedAt = lastTimeRiskLevelCalculation() ?: timeStamper.nowUTC
                )
            )
        }

        lastSuccessfullyCalculatedRiskLevel()?.let {
            legacyResults.add(LegacyResult(riskState = it, calculatedAt = timeStamper.nowUTC))
        }

        legacyResults
    } catch (e: Exception) {
        Timber.e(e, "Failed to parse legacy risklevel data.")
        emptyList()
    }

    data class LegacyResult(
        override val riskState: RiskState,
        override val calculatedAt: Instant
    ) : RiskLevelResult {
        override val failureReason: RiskLevelResult.FailureReason? = null
        override val aggregatedRiskResult: AggregatedRiskResult? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
    }

    companion object {

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun mapRiskLevelConstant(value: Int): RiskState = when (value) {
            MigrationRiskLevelConstants.LOW_LEVEL_RISK -> RiskState.LOW_RISK
            MigrationRiskLevelConstants.INCREASED_RISK -> RiskState.INCREASED_RISK
            else -> RiskState.CALCULATION_FAILED
        }
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal object MigrationRiskLevelConstants {
    const val NO_CALCULATION_POSSIBLE_TRACING_OFF = 1
    const val LOW_LEVEL_RISK = 2
    const val INCREASED_RISK = 3
    const val UNKNOWN_RISK_OUTDATED_RESULTS = 4
    const val UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL = 5
    const val UNKNOWN_RISK_NO_INTERNET = 6
    const val UNDETERMINED = 9001
}
