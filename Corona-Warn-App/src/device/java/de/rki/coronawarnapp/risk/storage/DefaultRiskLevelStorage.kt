package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRiskLevelStorage @Inject constructor(
    riskResultDatabaseFactory: RiskResultDatabase.Factory,
    riskLevelResultMigrator: RiskLevelResultMigrator,
    @AppScope scope: CoroutineScope
) : BaseRiskLevelStorage(riskResultDatabaseFactory, riskLevelResultMigrator, scope) {

    // 2 days, 6 times per day, data is considered stale after 48 hours with risk calculation
    // Taken from TimeVariables.MAX_STALE_EXPOSURE_RISK_RANGE
    override val storedResultLimit: Int = 2 * 6

    override suspend fun storeExposureWindows(storedResultId: String, result: RiskLevelResult) {
        Timber.d("storeExposureWindows(): NOOP")
        // NOOP
    }

    override suspend fun deletedOrphanedExposureWindows() {
        Timber.d("deletedOrphanedExposureWindows(): NOOP")
        // NOOP
    }
}
