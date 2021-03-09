package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.TraceLocationCheckInDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    val allCheckIns: Flow<List<CheckIn>> =
        traceLocationCheckInDao
            .allEntries()
            .map { list -> list.map { it.toCheckIn() } }

    fun addCheckIn(checkIn: CheckIn) {
        appScope.launch {
            traceLocationCheckInDao.insert(checkIn.toEntity())
        }
    }
}

private fun TraceLocationCheckInEntity.toCheckIn() = DefaultCheckIn(
    id,
    guid,
    version,
    type,
    description,
    address,
    traceLocationStart,
    traceLocationEnd,
    defaultCheckInLengthInMinutes,
    signature,
    checkInStart,
    checkInEnd,
    targetCheckInEnd,
    createJournalEntry
)

private fun CheckIn.toEntity() = TraceLocationCheckInEntity(
    id,
    guid,
    version,
    type,
    description,
    address,
    traceLocationStart,
    traceLocationEnd,
    defaultCheckInLengthInMinutes,
    signature,
    checkInStart,
    checkInEnd,
    targetCheckInEnd,
    createJournalEntry
)
