package de.rki.coronawarnapp.eventregistration.storage.retention

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeConstants
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class TraceLocationCleaner @Inject constructor(
    private val traceLocationRepository: TraceLocationRepository,
    private val timeStamper: TimeStamper
) {

    suspend fun cleanUp() {
        traceLocationRepository.allTraceLocations.first()
            .filter {
                // filter out permanent trace locations without an endDate
                it.endDate != null
            }.filter {
                isOutOfRetention(it.endDate!!)
            }.forEach {
                traceLocationRepository.deleteTraceLocation(it)
            }
    }

    private fun isOutOfRetention(traceLocationEndDate: Instant): Boolean {
        val retentionThreshold = (timeStamper.nowUTC.seconds - (RETENTION_DAYS * DateTimeConstants.SECONDS_PER_DAY))
        return traceLocationEndDate.seconds < retentionThreshold
    }

    companion object {
        const val RETENTION_DAYS = 15
    }
}
