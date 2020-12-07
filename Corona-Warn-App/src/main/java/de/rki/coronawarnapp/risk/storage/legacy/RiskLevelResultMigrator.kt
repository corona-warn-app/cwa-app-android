package de.rki.coronawarnapp.risk.storage.legacy

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import dagger.Lazy
import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.AggregatedRiskResult
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Duration
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
    private val timeStamper: TimeStamper,
    @AppContext
    private val context: Context
) {

    private val prefs by lazy { encryptedPreferences.get() }

    private fun lastTimeRiskLevelCalculation(): Instant? {
        prefs.getLong("preference_timestamp_risk_level_calculation", -1L).also {
            Timber.tag(TAG).d("preference_timestamp_risk_level_calculation=$it")
            return if (it < 0) null else Instant.ofEpochMilli(it)
        }
    }

    private fun lastCalculatedRiskLevel(): RiskState? {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score", -1)
        Timber.tag(TAG).d("preference_risk_level_score=$rawRiskLevel")
        return if (rawRiskLevel != -1) mapRiskLevelConstant(rawRiskLevel) else null
    }

    private fun lastSuccessfullyCalculatedRiskLevel(): RiskState? {
        val rawRiskLevel = prefs.getInt("preference_risk_level_score_successful", -1)
        Timber.tag(TAG).d("preference_risk_level_score_successful=$rawRiskLevel")
        return if (rawRiskLevel != -1) mapRiskLevelConstant(rawRiskLevel) else null
    }

    private suspend fun lastEncounterAt(): Instant? {
        return try {
            val daysSinceLastExposure =
                AppDatabase.getInstance(context)
                    .exposureSummaryDao()
                    .getLatestExposureSummary()?.daysSinceLastExposure
            if (daysSinceLastExposure == null) {
                null
            } else {
                timeStamper.nowUTC.minus(Duration.standardDays(daysSinceLastExposure.toLong()))
            }
        } catch (exception: Exception) {
            Timber.tag(TAG).w(exception, "failed to select exposure summary dao from enf v1")
            null
        }
    }

    suspend fun getLegacyResults(): List<RiskLevelResult> = try {
        val legacyResults = mutableListOf<RiskLevelResult>()
        lastCalculatedRiskLevel()?.let {
            legacyResults.add(
                LegacyResult(
                    riskState = it,
                    calculatedAt = lastTimeRiskLevelCalculation() ?: timeStamper.nowUTC,
                    lastEncounterAt = lastEncounterAt()
                )
            )
        }

        lastSuccessfullyCalculatedRiskLevel()?.let {
            legacyResults.add(
                LegacyResult(
                    riskState = it,
                    calculatedAt = timeStamper.nowUTC,
                    lastEncounterAt = lastEncounterAt()
                )
            )
        }

        Timber.tag(TAG).d("legacyResults=$legacyResults")
        legacyResults
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to parse legacy risklevel data.")
        emptyList()
    }

    data class LegacyResult(
        override val riskState: RiskState,
        override val calculatedAt: Instant,
        private val lastEncounterAt: Instant?
    ) : RiskLevelResult {
        override val failureReason: RiskLevelResult.FailureReason? = null
        override val aggregatedRiskResult: AggregatedRiskResult? = null
        override val exposureWindows: List<ExposureWindow>? = null
        override val matchedKeyCount: Int = 0
        override val daysWithEncounters: Int = 0
        override val lastRiskEncounterAt: Instant? = lastEncounterAt
    }

    companion object {
        private const val TAG = "RiskLevelResultMigrator"

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
