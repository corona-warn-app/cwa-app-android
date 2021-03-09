package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.TraceLocationCheckInDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraceLocationCheckInRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory,
    @AppScope private val appScope: CoroutineScope

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

    fun addCheckIn(checkIn: TraceLocationCheckIn) {
        appScope.launch {
            traceLocationCheckInDao.insert(checkIn as TraceLocationCheckInEntity)
        }
    }
}
