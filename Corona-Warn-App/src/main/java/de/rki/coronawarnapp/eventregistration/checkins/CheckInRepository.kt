package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.CheckInDao
import de.rki.coronawarnapp.eventregistration.storage.entity.toCheckIn
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CheckInRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory
) {

    private val traceLocationDatabase: TraceLocationDatabase by lazy {
        traceLocationDatabaseFactory.create()
    }

    private val checkInDao: CheckInDao by lazy {
        traceLocationDatabase.eventCheckInDao()
    }

    val allCheckIns: Flow<List<CheckIn>> =
        checkInDao
            .allEntries()
            .map { list -> list.map { it.toCheckIn() } }

    suspend fun addCheckIn(checkIn: CheckIn) = withContext(NonCancellable) {
        Timber.d("addCheckIn(checkIn=%s)", checkIn)
        if (checkIn.id == 0L) throw IllegalArgumentException("ID will be set by DB, ID should be 0!")

        checkInDao.insert(checkIn.toEntity())
    }

    suspend fun updateCheckIn(checkInId: Long, update: (CheckIn?) -> CheckIn?) = withContext(NonCancellable) {
        Timber.d("updateCheckIn(checkInId=%d, update=%s)", checkInId, update)
        checkInDao.updateEntityById(checkInId, update)
    }

    suspend fun deleteCheckIns(checkIns: Collection<CheckIn>) = withContext(NonCancellable) {
        Timber.d("deleteCheckIns(checkIns=%s)", checkIns)
        checkInDao.deleteByIds(checkIns.map { it.id })
    }

    suspend fun clear() = withContext(NonCancellable) {
        Timber.d("clear()")
        checkInDao.deleteAll()
    }
}
