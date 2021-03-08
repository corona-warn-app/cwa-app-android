package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.checkins.storage.EventDatabase
import de.rki.coronawarnapp.eventregistration.checkins.storage.dao.EventCheckInDao
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.EventCheckInEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckInsRepository @Inject constructor(
    eventDatabaseFactory: EventDatabase.Factory
) {

    private val eventDatabase: EventDatabase by lazy {
        eventDatabaseFactory.create()
    }

    private val eventCheckInDao: EventCheckInDao by lazy {
        eventDatabase.eventCheckInDao()
    }

    val allCheckIns: Flow<List<EventCheckIn>> =
        eventCheckInDao
            .allEntries()

    suspend fun addCheckIn(checkIn: EventCheckIn) =
        eventCheckInDao
            .insert(checkIn as EventCheckInEntity)
}
