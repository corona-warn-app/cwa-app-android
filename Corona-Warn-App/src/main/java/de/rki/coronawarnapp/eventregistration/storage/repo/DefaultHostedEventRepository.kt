package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.HostedEvent
import de.rki.coronawarnapp.eventregistration.storage.EventRegistrationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.HostedEventDao
import de.rki.coronawarnapp.eventregistration.storage.entity.toHostedEventEntity
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultHostedEventRepository @Inject constructor(
    eventRegistrationDatabaseFactory: EventRegistrationDatabase.Factory,
    @AppScope private val appScope: CoroutineScope
) : HostedEventRepository {

    private val eventRegistrationDatabase: EventRegistrationDatabase by lazy {
        eventRegistrationDatabaseFactory.create()
    }

    private val hostedEventDao: HostedEventDao by lazy {
        eventRegistrationDatabase.hostedEventsDao()
    }

    override val allHostedEvents: Flow<List<HostedEvent>>
        get() = hostedEventDao.allEntries() // TODO: SORTING

    override suspend fun addHostedEvent(event: HostedEvent) {
        appScope.launch {
            Timber.d("Add hosted event: $event")
            val eventEntity = event.toHostedEventEntity()
            hostedEventDao.insert(eventEntity)
        }
    }

    override suspend fun deleteHostedEvent(event: HostedEvent) {
        appScope.launch {
            Timber.d("Delete hosted event: $event")
            val eventEntity = event.toHostedEventEntity()
            hostedEventDao.delete(eventEntity)
        }
    }

    override suspend fun deleteAllHostedEvents() {
        appScope.launch {
            Timber.d("Delete all hosted events.")
            hostedEventDao.deleteAll()
        }
    }
}
