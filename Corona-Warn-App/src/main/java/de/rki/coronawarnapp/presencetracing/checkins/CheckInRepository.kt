package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.presencetracing.storage.TraceLocationDatabase
import de.rki.coronawarnapp.presencetracing.storage.dao.CheckInDao
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.presencetracing.storage.entity.toCheckIn
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory,
    private val timeStamper: TimeStamper
) : Resettable {

    private val traceLocationDatabase: TraceLocationDatabase by lazy {
        traceLocationDatabaseFactory.create()
    }

    private val checkInDao: CheckInDao by lazy {
        traceLocationDatabase.checkInDao()
    }

    /**
     * Returns all stored check-ins
     *
     * Attention: this could also include check-ins that are older than
     * the retention period. Therefore, you should probably use [checkInsWithinRetention]
     */
    val allCheckIns: Flow<List<CheckIn>> = checkInDao
        .allEntries()
        .map { list -> list.map { it.toCheckIn() } }

    /**
     * Returns check-ins that are within the retention period. Even though we have a worker that deletes all stale
     * check-ins it's still possible to have stale check-ins in the database because the worker only runs once a day.
     */
    val checkInsWithinRetention: Flow<List<CheckIn>> = allCheckIns.map { checkInList ->
        val now = timeStamper.nowUTC
        checkInList.filter { checkIn ->
            checkIn.isWithinRetention(now)
        }
    }

    suspend fun getCheckInById(checkInId: Long): CheckIn? {
        Timber.d("getCheckInById(checkInId=$checkInId)")
        return checkInDao.entryForId(checkInId)?.toCheckIn()
    }

    suspend fun addCheckIn(checkIn: CheckIn) = withContext(NonCancellable) {
        Timber.d("addCheckIn(checkIn=%s)", checkIn)
        if (checkIn.id != 0L) throw IllegalArgumentException("ID will be set by DB, ID should be 0!")

        checkInDao.insert(checkIn.toEntity())
    }

    suspend fun updateCheckIn(checkInId: Long, update: (CheckIn) -> CheckIn) = withContext(NonCancellable) {
        Timber.d("updateCheckIn(checkInId=%d, update=%s)", checkInId, update)
        checkInDao.updateEntityById(checkInId, update)
    }

    suspend fun updatePostSubmissionFlags(checkInId: Long) {
        Timber.d("markCheckInAsSubmitted(checkInId=$checkInId)")
        checkInDao.updateEntity(
            TraceLocationCheckInEntity.SubmissionUpdate(
                checkInId = checkInId,
                isSubmitted = true,
                hasSubmissionConsent = false,
            )
        )
    }

    suspend fun deleteCheckIns(checkIns: Collection<CheckIn>) = withContext(NonCancellable) {
        Timber.d("deleteCheckIns(checkIns=%s)", checkIns)
        checkInDao.deleteByIds(checkIns.map { it.id })
    }

    suspend fun checkInForId(checkInId: Long): CheckIn {
        val checkIn = checkInDao.entryForId(checkInId)
            ?: throw IllegalArgumentException("No checkIn found for ID=$checkInId")

        return checkIn.toCheckIn()
    }

    suspend fun updateSubmissionConsents(checkInIds: Collection<Long>, consent: Boolean) {
        Timber.d("updateSubmissionConsents(checkInIds=%s, consent=%b)", checkInIds, consent)
        val consentUpdates = checkInIds.map {
            TraceLocationCheckInEntity.SubmissionConsentUpdate(
                checkInId = it,
                hasSubmissionConsent = consent
            )
        }
        checkInDao.updateSubmissionConsents(consentUpdates)
    }

    override suspend fun reset() {
        Timber.d("reset()")
        checkInDao.deleteAll()
    }
}
