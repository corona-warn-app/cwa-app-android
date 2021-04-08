package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocations
import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.TraceLocationDao
import de.rki.coronawarnapp.eventregistration.storage.entity.toTraceLocationEntity
import de.rki.coronawarnapp.eventregistration.storage.retention.isWithinRetention
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTraceLocationRepository @Inject constructor(
    traceLocationDatabaseFactory: TraceLocationDatabase.Factory,
    @AppScope private val appScope: CoroutineScope,
    private val timeStamper: TimeStamper
) : TraceLocationRepository {

    private val traceLocationDatabase: TraceLocationDatabase by lazy {
        traceLocationDatabaseFactory.create()
    }

    private val traceLocationDao: TraceLocationDao by lazy {
        traceLocationDatabase.traceLocationDao()
    }

    override suspend fun traceLocationForId(id: Long): TraceLocation {
        val checkIn = traceLocationDao.entryForId(id)
            ?: throw IllegalArgumentException("No traceLocation found for ID=$id")

        return checkIn.toTraceLocation()
    }

    /**
     * Returns all stored trace locations
     *
     * Attention: this could also include trace locations that are older than
     * the retention period. Therefore, you should probably use [traceLocationsWithinRetention]
     */
    override val allTraceLocations: Flow<List<TraceLocation>>
        get() = traceLocationDao.allEntries().map { it.toTraceLocations() }

    /**
     * Returns trace locations that are within the retention period. Even though we have a worker that deletes all stale
     * trace locations it's still possible to have stale trace-locations in the database because the worker only runs
     * once a day.
     */
    override val traceLocationsWithinRetention: Flow<List<TraceLocation>>
        get() = allTraceLocations.map { traceLocationList ->
            val now = timeStamper.nowUTC
            traceLocationList.filter { traceLocation ->
                traceLocation.isWithinRetention(now)
            }
        }

    override suspend fun addTraceLocation(traceLocation: TraceLocation): TraceLocation {
        Timber.d("Add trace location: %s", traceLocation)
        val traceLocationEntity = traceLocation.toTraceLocationEntity()
        val generatedId = traceLocationDao.insert(traceLocationEntity)
        return traceLocationDao.entityForId(generatedId).toTraceLocation()
    }

    override fun deleteTraceLocation(traceLocation: TraceLocation) {
        appScope.launch {
            Timber.d("Delete hosted event: $traceLocation")
            val eventEntity = traceLocation.toTraceLocationEntity()
            traceLocationDao.delete(eventEntity)
        }
    }

    override fun deleteAllTraceLocations() {
        appScope.launch {
            Timber.d("Delete all hosted events.")
            traceLocationDao.deleteAll()
        }
    }
}
