package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.EventCheckIn
import org.joda.time.DateTime
import org.joda.time.Instant

interface CheckInsPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractCheckIns(): List<CheckIn>
}

object FakeCheckInsPackage : CheckInsPackage {
    override suspend fun extractCheckIns(): List<EventCheckIn> {
        return listOf(fakeEventCheckIn1)
    }
}

private val fakeEventCheckIn1: EventCheckIn = object : EventCheckIn {
    override val id = 1L
    override val guid = "eventOne"
    override val startTime = Instant.ofEpochMilli(
        DateTime(2021, 2, 20, 11, 45).millis
    )
    override val endTime = Instant.ofEpochMilli(
        DateTime(2021, 2, 20, 12, 15).millis
    )
}
