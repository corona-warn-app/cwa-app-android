package de.rki.coronawarnapp.eventregistration.checkins

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.DateTime
import org.joda.time.Instant
import javax.inject.Inject

interface CheckInsRepository {

    val allCheckIns: Flow<List<EventCheckIn>>

    suspend fun addCheckIn(checkIn: EventCheckIn)
}

class FakeCheckInsRepository @Inject constructor() : CheckInsRepository {
    override val allCheckIns: Flow<List<EventCheckIn>>
        get() = listOf(listOf(fakeEventCheckIn1)).asFlow()

    override suspend fun addCheckIn(checkIn: EventCheckIn) {
        // todo
    }
}

private val fakeEventCheckIn1: EventCheckIn = object : EventCheckIn {
    override val id = 1L
    override val guid = "eventOne"
    override val startTime = Instant.ofEpochMilli(
        DateTime(2021, 2, 20, 12, 0).millis
    )
    override val endTime = Instant.ofEpochMilli(
        DateTime(2021, 2, 20, 12, 30).millis
    )
}
