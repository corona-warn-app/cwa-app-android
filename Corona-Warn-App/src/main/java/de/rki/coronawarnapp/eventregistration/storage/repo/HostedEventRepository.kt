package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.HostedEvent
import kotlinx.coroutines.flow.Flow

interface HostedEventRepository {

    val allHostedEvents: Flow<List<HostedEvent>>

    fun addHostedEvent(event: HostedEvent)

    fun deleteHostedEvent(event: HostedEvent)

    fun deleteAllHostedEvents()
}
