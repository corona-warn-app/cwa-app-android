package de.rki.coronawarnapp.eventregistration.storage.repo

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import kotlinx.coroutines.flow.Flow

interface HostedEventRepository {

    val allHostedEvents: Flow<List<TraceLocation>>

    fun addHostedEvent(event: TraceLocation)

    fun deleteHostedEvent(event: TraceLocation)

    fun deleteAllHostedEvents()
}
