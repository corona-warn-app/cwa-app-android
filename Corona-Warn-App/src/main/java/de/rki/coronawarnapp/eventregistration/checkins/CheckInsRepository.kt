package de.rki.coronawarnapp.eventregistration.checkins

import kotlinx.coroutines.flow.Flow

interface CheckInsRepository {

    val allCheckIns: Flow<List<TraceLocationCheckIn>>

    suspend fun addCheckIn(checkIn: TraceLocationCheckIn)
}
