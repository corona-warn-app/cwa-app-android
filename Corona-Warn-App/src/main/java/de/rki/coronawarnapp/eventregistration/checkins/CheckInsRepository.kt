package de.rki.coronawarnapp.eventregistration.checkins

import kotlinx.coroutines.flow.Flow

interface CheckInsRepository {

    val allCheckIns: Flow<List<EventCheckIn>>

    suspend fun addCheckIn(checkIn: EventCheckIn)
}
