package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.checkins.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.checkins.storage.dao.TraceLocationCheckInDao
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.TraceLocationCheckInEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TraceLocationCheckInRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory
) {

    private val traceLocationDatabase: TraceLocationDatabase by lazy {
        traceLocationDatabaseFactory.create()
    }

    private val traceLocationCheckInDao: TraceLocationCheckInDao by lazy {
        traceLocationDatabase.eventCheckInDao()
    }

    val allCheckIns: Flow<List<TraceLocationCheckIn>> =
        traceLocationCheckInDao
            .allEntries()

    suspend fun addCheckIn(checkIn: TraceLocationCheckIn) =
        traceLocationCheckInDao
            .insert(checkIn as TraceLocationCheckInEntity)
}
