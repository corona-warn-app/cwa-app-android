package de.rki.coronawarnapp.eventregistration.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import javax.inject.Inject

@Reusable
class TraceLocationCleaner @Inject constructor(
    private val traceLocationRepository: TraceLocationRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        val retentionThreshold = (timeStamper.nowUTC.seconds - RETENTION_SECONDS)
        traceLocationRepository.allTraceLocations.first()
            .filter {
                // filter out permanent trace locations without an endDate
                it.endDate != null
            }.filter {
                it.endDate!!.seconds < retentionThreshold
            }.forEach {
                traceLocationRepository.deleteTraceLocation(it)
            }
    }

    companion object {
        private const val RETENTION_DAYS = 15
        private const val RETENTION_SECONDS = RETENTION_DAYS * DateTimeConstants.SECONDS_PER_DAY
    }
}
