package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.CheckInDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject

class CheckInRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory,
    @AppScope private val appScope: CoroutineScope
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

    fun addCheckIn(checkIn: CheckIn) {
        appScope.launch {
            checkInDao.insert(checkIn.toEntity())
        }
    }

    fun updateCheckIn(checkIn: CheckIn) {
        appScope.launch {
            checkInDao.update(checkIn.toEntity())
        }
    }

    fun clear() {
        appScope.launch {
            checkInDao.deleteAll()
        }
    }
}

private fun TraceLocationCheckInEntity.toCheckIn() = CheckIn(
    id = id,
    guid = guid,
    guidHash = guidHashBase64.decodeBase64()!!,
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    traceLocationBytes = traceLocationBytesBase64.decodeBase64()!!,
    signature = signatureBase64.decodeBase64()!!,
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    completed = completed,
    createJournalEntry = createJournalEntry
)

private fun CheckIn.toEntity() = TraceLocationCheckInEntity(
    id = id,
    guid = guid,
    guidHashBase64 = guidHash.base64(),
    version = version,
    type = type,
    description = description,
    address = address,
    traceLocationStart = traceLocationStart,
    traceLocationEnd = traceLocationEnd,
    defaultCheckInLengthInMinutes = defaultCheckInLengthInMinutes,
    traceLocationBytesBase64 = traceLocationBytes.base64(),
    signatureBase64 = signature.base64(),
    checkInStart = checkInStart,
    checkInEnd = checkInEnd,
    completed = completed,
    createJournalEntry = createJournalEntry
)
