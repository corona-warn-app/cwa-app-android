package de.rki.coronawarnapp.statistics.local.source

import de.rki.coronawarnapp.statistics.local.FederalStateToPackageId
import de.rki.coronawarnapp.statistics.local.storage.LocalStatisticsConfigStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStatisticsRetrievalScheduler @Inject constructor(
    foregroundState: ForegroundState,
    localStatisticsConfigStorage: LocalStatisticsConfigStorage,
    @AppScope private val appScope: CoroutineScope,
    private val localStatisticsProvider: LocalStatisticsProvider
) {
    private val lastActiveStates = mutableSetOf<FederalStateToPackageId>()

    private val updateStatsTrigger = combine(
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

    fun setup() {
        Timber.tag(TAG).i("setup()")

        updateStatsTrigger
            .onEach {
                if (it) {
                    Timber.tag(TAG).d("Triggering local statistics update.")
                    localStatisticsProvider.triggerUpdate()
                }
            }
            .launchIn(appScope)
    }

    companion object {
        private val TAG = LocalStatisticsRetrievalScheduler::class.simpleName!!
    }
}
