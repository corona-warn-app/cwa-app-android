package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.HostedEvent
import kotlinx.coroutines.flow.Flow

interface HostedEventRepository {

    val allHostedEvents: Flow<List<HostedEvent>>

    suspend fun addHostedEvent(event: HostedEvent)

    suspend fun deleteHostedEvent(event: HostedEvent)

    suspend fun deleteAllHostedEvents()
}
