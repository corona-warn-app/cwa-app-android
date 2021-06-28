package de.rki.coronawarnapp.statistics.local.source

import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsRetrievalScheduler @Inject constructor(
    foregroundState: ForegroundState,
    localStatisticsConfigStorage: LocalStatisticsConfigStorage,
) {
    private val lastActiveStates = mutableSetOf<FederalStateToPackageId>()

    val updateStatsTrigger = combine(
        foregroundState.isInForeground,
        localStatisticsConfigStorage.activeStates.flow
    ) { isInForeground, statistics ->
        val hasNewStats = statistics.any {
            val isNew = !lastActiveStates.contains(it)
            if (isNew) lastActiveStates.add(it)
            isNew
        }

        Timber.tag(TAG).v(
            "should stats update: isInForeground = %s || hasNewStats = %s",
            isInForeground,
            hasNewStats
        )
        isInForeground || hasNewStats
    }.distinctUntilChanged()

    companion object {
        private val TAG = LocalStatisticsRetrievalScheduler::class.simpleName!!
    }
}
