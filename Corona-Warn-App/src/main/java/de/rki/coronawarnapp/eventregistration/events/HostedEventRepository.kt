package de.rki.coronawarnapp.eventregistration.events

import kotlinx.coroutines.flow.Flow

interface HostedEventRepository {

    val allHostedEvents: Flow<List<HostedEvent>>

    fun addHostedEvent(event: HostedEvent)

    fun deleteHostedEvent(event: HostedEvent)
}
