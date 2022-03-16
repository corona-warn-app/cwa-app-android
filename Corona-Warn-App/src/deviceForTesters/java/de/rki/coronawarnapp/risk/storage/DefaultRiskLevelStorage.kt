package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.risk.EwRiskLevelResult
import de.rki.coronawarnapp.risk.ExposureWindowsFilter
import de.rki.coronawarnapp.risk.storage.internal.RiskCombinator
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.windows.PersistedExposureWindowDao.PersistedScanInstance
import de.rki.coronawarnapp.risk.storage.internal.windows.toPersistedExposureWindow
import de.rki.coronawarnapp.risk.storage.internal.windows.toPersistedScanInstances
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRiskLevelStorage @Inject constructor(
    riskResultDatabaseFactory: RiskResultDatabase.Factory,
    presenceTracingRiskRepository: PresenceTracingRiskRepository,
    riskCombinator: RiskCombinator,
    ewFilter: ExposureWindowsFilter,
) : BaseRiskLevelStorage(
    riskResultDatabaseFactory,
    presenceTracingRiskRepository,
    riskCombinator,
    ewFilter,
) {

    // 14 days, 6 times per day
    // For testers keep all the results!
    override val storedResultLimit: Int = 14 * 6

    override suspend fun storeExposureWindows(storedResultId: String, resultEw: EwRiskLevelResult) {
        Timber.d("Storing exposure windows for storedResultId=%s", storedResultId)
        try {
            val startTime = System.currentTimeMillis()
            val exposureWindows = resultEw.exposureWindows ?: emptyList()
            val windowIds = exposureWindows
                .map { it.toPersistedExposureWindow(riskLevelResultId = storedResultId) }
                .let { exposureWindowsTables.insertWindows(it) }

            require(windowIds.size == exposureWindows.size) {
                Timber.e("Inserted ${windowIds.size}, but wanted ${exposureWindows.size}")
            }

            val persistedScanInstances: List<PersistedScanInstance> = windowIds.flatMapIndexed { index, id ->
                val scanInstances = exposureWindows[index].scanInstances
                scanInstances.toPersistedScanInstances(exposureWindowId = id)
            }
            exposureWindowsTables.insertScanInstances(persistedScanInstances)

            Timber.d("Storing ExposureWindows took %dms.", (System.currentTimeMillis() - startTime))
        } catch (e: Exception) {
            Timber.e(e, "Failed to save exposure windows")
        }
    }

    override suspend fun deletedOrphanedExposureWindows() {
        Timber.d("deletedOrphanedExposureWindows() running...")
        val currentRiskResultIds = ewRiskResultsTables.allEntries().firstOrNull()?.map { it.id } ?: emptyList()

        exposureWindowsTables.deleteByRiskResultId(currentRiskResultIds).also {
            Timber.d("$it orphaned exposure windows were deleted.")
        }
    }
}
