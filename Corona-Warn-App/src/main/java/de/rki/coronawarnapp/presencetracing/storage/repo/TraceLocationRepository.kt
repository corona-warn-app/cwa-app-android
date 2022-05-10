package de.rki.coronawarnapp.presencetracing.storage.repo

import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow

interface TraceLocationRepository : Resettable {

    /**
     * Returns all stored trace locations
     *
     * Attention: this could also include trace locations that are older than
     * the retention period. Therefore, you should probably use [traceLocationsWithinRetention]
     */
    val allTraceLocations: Flow<List<TraceLocation>>

    /**
     * Returns trace locations that are within the retention period. Even though we have a worker that deletes all stale
     * trace locations it's still possible to have stale trace-locations in the database because the worker only runs
     * once a day.
     */
    val traceLocationsWithinRetention: Flow<List<TraceLocation>>

    suspend fun traceLocationForId(id: Long): TraceLocation

    suspend fun addTraceLocation(traceLocation: TraceLocation): TraceLocation

    fun deleteTraceLocation(traceLocation: TraceLocation)
}
