package de.rki.coronawarnapp.eventregistration.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class TraceLocationCleaner @Inject constructor(
    private val traceLocationRepository: TraceLocationRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        Timber.d("Starting to clean up stale trace locations.")
        val retentionThreshold = (timeStamper.nowUTC.seconds - RETENTION_SECONDS)
        traceLocationRepository.allTraceLocations.first()
            .filter {
                it.endDate != null && it.endDate.seconds < retentionThreshold
            }.forEach {
                Timber.d("Cleaning up stale trace location: %s", it)
                traceLocationRepository.deleteTraceLocation(it)
            }
        Timber.d("Clean up of stale trace locations completed.")
    }

    companion object {
        private const val RETENTION_DAYS = 15
        private val RETENTION_SECONDS = TimeUnit.DAYS.toSeconds(RETENTION_DAYS.toLong())
    }
}
