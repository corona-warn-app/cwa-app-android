package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.EventRegistrationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.TraceLocationDao
import de.rki.coronawarnapp.eventregistration.storage.entity.toTraceLocationEntity
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTraceLocationRepository @Inject constructor(
    eventRegistrationDatabaseFactory: EventRegistrationDatabase.Factory,
    @AppScope private val appScope: CoroutineScope
) : TraceLocationRepository {

    private val eventRegistrationDatabase: EventRegistrationDatabase by lazy {
        eventRegistrationDatabaseFactory.create()
    }

    private val traceLocationDao: TraceLocationDao by lazy {
        eventRegistrationDatabase.traceLocation()
    }

    override val allTraceLocations: Flow<List<TraceLocation>>
        get() = traceLocationDao.allEntries() // TODO: SORTING

    override fun addTraceLocation(event: TraceLocation) {
        appScope.launch {
            Timber.d("Add hosted event: $event")
            val eventEntity = event.toTraceLocationEntity()
            traceLocationDao.insert(eventEntity)
        }
    }

    override fun deleteTraceLocation(event: TraceLocation) {
        appScope.launch {
            Timber.d("Delete hosted event: $event")
            val eventEntity = event.toTraceLocationEntity()
            traceLocationDao.delete(eventEntity)
        }
    }

    override fun deleteAllTraceLocations() {
        appScope.launch {
            Timber.d("Delete all hosted events.")
            traceLocationDao.deleteAll()
        }
    }

    override fun clear() {
        appScope.launch {
            Timber.d("Deleting all tables of event registration database.")
            eventRegistrationDatabase.clearAllTables()
        }
    }
}
