package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import kotlinx.coroutines.flow.Flow

interface TraceLocationRepository {

    val allTraceLocations: Flow<List<TraceLocation>>

    fun addTraceLocation(event: TraceLocation)

    fun deleteTraceLocation(event: TraceLocation)

    fun deleteAllTraceLocations()

    fun clear()
}
