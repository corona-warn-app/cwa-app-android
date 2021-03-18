package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import kotlinx.coroutines.flow.Flow

interface TraceLocationRepository {

    val allTraceLocations: Flow<List<TraceLocation>>

    fun addTraceLocation(event: TraceLocation)

    fun deleteTraceLocation(event: TraceLocation)

    fun deleteAllTraceLocations()
}
