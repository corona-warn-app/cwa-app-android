package de.rki.coronawarnapp.presencetracing.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TraceLocationCleaner @Inject constructor(
    private val traceLocationRepository: TraceLocationRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        Timber.d("Starting to clean up stale trace locations.")

        val now = timeStamper.nowUTC
        traceLocationRepository.allTraceLocations.first()
            .filter { traceLocation ->
                traceLocation.isOutOfRetention(now)
            }.forEach {
                Timber.d("Cleaning up stale trace location: %s", it)
                traceLocationRepository.deleteTraceLocation(it)
            }

        Timber.d("Clean up of stale trace locations completed.")
    }
}
